import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import RobustScaler
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
import matplotlib.pyplot as plt
from torch.amp import autocast, GradScaler
import time
from tqdm import tqdm
import os
from datetime import datetime
from torch.utils.data.distributed import DistributedSampler
import torch.multiprocessing as mp
import torch.distributed as dist
from torch.nn.parallel import DistributedDataParallel as DDP
from torch.cuda.amp import autocast
import torch.backends.cudnn as cudnn

# Enable cuDNN autotuner
cudnn.benchmark = True

# Set random seeds for reproducibility
def set_seed(seed=42):
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    np.random.seed(seed)
    torch.backends.cudnn.deterministic = True

set_seed()

# Check if CUDA is available
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(f"Using device: {device}")

# Custom dataset class
class PeptideDataset(Dataset):
    def __init__(self, features, targets=None, weights=None, peptides=None):
        # Convert pandas Series/DataFrame to numpy arrays if needed
        if isinstance(features, (pd.Series, pd.DataFrame)):
            features = features.to_numpy()
        if isinstance(targets, pd.Series):
            targets = targets.to_numpy()
        if isinstance(weights, pd.Series):
            weights = weights.to_numpy()
            
        # Always keep tensors on CPU - let DataLoader handle GPU transfer
        self.features = torch.FloatTensor(features)
        self.targets = torch.FloatTensor(targets) if targets is not None else None
        self.weights = torch.FloatTensor(weights) if weights is not None else None
        self.peptides = peptides  # Store peptide sequences for reference
        
    def __len__(self):
        return len(self.features)
    
    def __getitem__(self, idx):
        if self.targets is not None and self.weights is not None:
            return self.features[idx], self.targets[idx], self.weights[idx]
        return self.features[idx]

# Remove the @torch.jit.script decorator from the class definition
class ResidualBlock(nn.Module):
    """Residual block with pre-activation (normalization)"""
    def __init__(self, in_features, out_features, dropout_rate=0.2):
        super().__init__()
        self.norm1 = nn.LayerNorm(in_features)
        self.linear1 = nn.Linear(in_features, out_features)
        self.relu = nn.ReLU(inplace=True)
        self.norm2 = nn.LayerNorm(out_features)
        self.linear2 = nn.Linear(out_features, out_features)
        self.dropout = nn.Dropout(dropout_rate)
        
        # Skip connection (with projection if dimensions don't match)
        self.skip = nn.Linear(in_features, out_features) if in_features != out_features else nn.Identity()
        
    def forward(self, x):
        residual = x
        
        # First block
        out = self.norm1(x)
        out = self.relu(out)
        out = self.linear1(out)
        
        # Second block
        out = self.norm2(out)
        out = self.relu(out)
        out = self.dropout(out)
        out = self.linear2(out)
        
        # Skip connection
        residual = self.skip(residual)
        
        return out + residual

# Model definition with performance optimizations
class PeptideIntensityPredictor(nn.Module):
    def __init__(self, input_size, seq_len=8, lstm_hidden_1=128, lstm_hidden_2=64, 
                 num_heads=4, dropout=0.3):
        super().__init__()
        
        # Cache dimensions for faster access
        self.seq_len = seq_len
        self.feature_dim = (input_size + seq_len - 1) // seq_len  # Ceiling division
        
        # Use fused operations where possible
        self.input_projection = nn.Sequential(
            nn.BatchNorm1d(input_size),
            nn.Linear(input_size, seq_len * self.feature_dim),
            nn.ReLU(inplace=True)  # Inplace ReLU for memory efficiency
        )
        
        # Optimized LSTM layers
        self.lstm1 = nn.LSTM(self.feature_dim, lstm_hidden_1, bidirectional=True, 
                            batch_first=True, num_layers=1)  # Explicit num_layers
        self.lstm_norm1 = nn.LayerNorm(lstm_hidden_1 * 2)
        self.residual_proj1 = nn.Linear(self.feature_dim, lstm_hidden_1 * 2)
        
        self.lstm2 = nn.LSTM(lstm_hidden_1 * 2, lstm_hidden_2, bidirectional=True, 
                            batch_first=True, num_layers=1)
        self.lstm_norm2 = nn.LayerNorm(lstm_hidden_2 * 2)
        self.residual_proj2 = nn.Linear(lstm_hidden_1 * 2, lstm_hidden_2 * 2)
        
        # Optimized attention
        self.attention = nn.MultiheadAttention(lstm_hidden_2 * 2, num_heads, dropout=dropout, batch_first=True)
        self.attention_norm = nn.LayerNorm(lstm_hidden_2 * 2)
        
        # Feature extraction with fused operations
        self.feature_extraction = nn.Sequential(
            nn.Linear((lstm_hidden_2 * 2) * 2, 256),
            nn.LayerNorm(256),
            nn.ReLU(inplace=True),
            nn.Dropout(dropout)
        )
        
        # Create residual blocks (without JIT for now)
        self.res_block1 = ResidualBlock(256, 256, dropout)
        self.res_block2 = ResidualBlock(256, 128, dropout)
        
        # Output layer with fused operations
        self.output = nn.Sequential(
            nn.Linear(128, 64),
            nn.LayerNorm(64),
            nn.ReLU(inplace=True),
            nn.Dropout(0.1),
            nn.Linear(64, 1),
            nn.Softplus()
        )
        
        self._init_weights()
        
    def _init_weights(self):
        for m in self.modules():
            if isinstance(m, nn.Linear):
                nn.init.kaiming_normal_(m.weight, mode='fan_in', nonlinearity='relu')
                if m.bias is not None:
                    nn.init.zeros_(m.bias)
                    
    def forward(self, x):
        batch_size = x.size(0)
        
        # Fused operations for input processing
        x = self.input_projection(x)
        x = x.view(batch_size, self.seq_len, self.feature_dim)
        original_x = x
        
        # LSTM processing with optimized memory usage
        lstm_out1, _ = self.lstm1(x)
        lstm_out1 = self.lstm_norm1(lstm_out1)
        projected_x = self.residual_proj1(original_x)
        x_residual1 = lstm_out1 + projected_x
        
        lstm_out2, _ = self.lstm2(lstm_out1)
        lstm_out2 = self.lstm_norm2(lstm_out2)
        projected_lstm1 = self.residual_proj2(lstm_out1)
        x_residual2 = lstm_out2 + projected_lstm1
        
        # Optimized attention mechanism (now batch_first=True)
        attn_output, _ = self.attention(x_residual2, x_residual2, x_residual2)
        attn_output = self.attention_norm(attn_output)
        x = attn_output + x_residual2
        
        # Efficient pooling operations
        avg_pool = torch.mean(x, dim=1)
        max_pool, _ = torch.max(x, dim=1)
        x = torch.cat([avg_pool, max_pool], dim=1)
        
        # Final processing
        x = self.feature_extraction(x)
        x = self.res_block1(x)
        x = self.res_block2(x)
        x = self.output(x)
        
        return x

# Remove the @torch.jit.script decorator from the compute_loss function
def compute_loss(outputs, targets, weights):
    return (outputs - targets.view(-1, 1)).pow(2) * weights.view(-1, 1)

def train_epoch(model, train_loader, criterion, optimizer, scaler, device, gradient_accumulation_steps=1):
    model.train()
    total_loss = 0
    steps = 0
    
    optimizer.zero_grad(set_to_none=True)  # More efficient than zero_grad()
    
    # Use tqdm for progress tracking
    pbar = tqdm(train_loader, desc="Training", leave=False)
    for i, (features, targets, weights) in enumerate(pbar):
        # Transfer data to device
        features = features.to(device)
        targets = targets.to(device)
        weights = weights.to(device)
        
        with autocast():
            outputs = model(features)
            loss = criterion(outputs, targets.view(-1, 1)) * weights.view(-1, 1)
            loss = loss.mean() / gradient_accumulation_steps
        
        scaler.scale(loss).backward()
        
        if (i + 1) % gradient_accumulation_steps == 0:
            scaler.step(optimizer)
            scaler.update()
            optimizer.zero_grad(set_to_none=True)
        
        total_loss += loss.item() * gradient_accumulation_steps
        steps += 1
        
        # Update progress bar
        pbar.set_postfix({'loss': f"{total_loss / steps:.4f}"})
    
    return total_loss / steps

# Optimized validation function
@torch.no_grad()
def validate(model, val_loader, criterion, device):
    model.eval()
    total_loss = 0
    all_targets = []
    all_predictions = []
    
    # Use tqdm for progress tracking
    pbar = tqdm(val_loader, desc="Validating", leave=False)
    for features, targets, weights in pbar:
        # Transfer data to device
        features = features.to(device)
        targets = targets.to(device)
        weights = weights.to(device)
        
        outputs = model(features)
        loss = criterion(outputs, targets.view(-1, 1)) * weights.view(-1, 1)
        loss = loss.mean()
        
        total_loss += loss.item()
        all_targets.append(targets.cpu())
        all_predictions.append(outputs.cpu())
        
        # Update progress bar
        pbar.set_postfix({'loss': f"{total_loss / (pbar.n + 1):.4f}"})
    
    all_targets = torch.cat(all_targets).numpy()
    all_predictions = torch.cat(all_predictions).numpy().ravel()
    
    return total_loss / len(val_loader), all_targets, all_predictions

# Optimized prediction function
@torch.no_grad()
def predict_batches(model, data_loader, device):
    model.eval()
    predictions = []
    
    # Use tqdm for progress tracking
    pbar = tqdm(data_loader, desc="Predicting", leave=False)
    for batch in pbar:
        if isinstance(batch, torch.Tensor):
            features = batch.to(device)
        else:
            features = batch[0].to(device)
        
        outputs = model(features)
        predictions.append(outputs.cpu())
    
    return torch.cat(predictions).numpy().ravel()

def main():
    # Create output directories
    os.makedirs('models', exist_ok=True)
    os.makedirs('results', exist_ok=True)
    
    # Current timestamp for model versioning
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    model_name = f"peptide_model_{timestamp}"
    
    # Data loading and preprocessing
    print("Loading data...")
    data = pd.read_csv("trainData.tsv", sep="\t")
    
    # Store peptide sequences for reference
    peptides = data['peptide'].values
    X = data.drop(columns=['peptide', 'intensity'])
    y = data['intensity']
    
    print(f"Dataset shape: {data.shape}")
    print(f"Features: {list(X.columns)}")
    print(f"Number of samples: {len(data)}")
    
    # Scale features
    scaler = RobustScaler()
    X_scaled = scaler.fit_transform(X)
    
    # Split data with stratification based on intensity quantiles
    # This ensures balanced training and validation sets
    intensity_quantiles = pd.qcut(y, q=10, labels=False, duplicates='drop')
    X_train, X_test, y_train, y_test, peptides_train, peptides_test = train_test_split(
        X_scaled, y, peptides, test_size=0.2, random_state=42, stratify=intensity_quantiles
    )
    
    # Calculate sample weights (apply log1p to reduce dominance of very high values)
    intensity_weights = np.log1p(y) / np.log1p(y.max())
    sample_weights_train = intensity_weights[y_train.index]
    sample_weights_test = intensity_weights[y_test.index]
    
    # Create datasets and dataloaders
    BATCH_SIZE = 128
    NUM_WORKERS = 4
    
    train_dataset = PeptideDataset(X_train, y_train, sample_weights_train, peptides_train)
    test_dataset = PeptideDataset(X_test, y_test, sample_weights_test, peptides_test)
    
    # Configure DataLoader with appropriate settings
    train_loader = DataLoader(
        train_dataset, 
        batch_size=BATCH_SIZE,
        shuffle=True, 
        num_workers=NUM_WORKERS, 
        pin_memory=torch.cuda.is_available(),  # Only pin if CUDA is available
        persistent_workers=True if NUM_WORKERS > 0 else False  # Keep workers alive between epochs
    )
    
    test_loader = DataLoader(
        test_dataset, 
        batch_size=BATCH_SIZE,
        shuffle=False, 
        num_workers=NUM_WORKERS, 
        pin_memory=torch.cuda.is_available(),
        persistent_workers=True if NUM_WORKERS > 0 else False
    )
    
    # Model initialization
    input_size = X_train.shape[1]
    seq_len = 8  # Adjust based on domain knowledge if needed
    
    model = PeptideIntensityPredictor(
        input_size=input_size, 
        seq_len=seq_len,
        lstm_hidden_1=128, 
        lstm_hidden_2=64,
        num_heads=4, 
        dropout=0.3
    ).to(device)
    
    print(f"Model initialized with {sum(p.numel() for p in model.parameters())} parameters")
    
    # Loss function: Huber loss is robust to outliers
    criterion = nn.HuberLoss()
    
    # Optimizer with weight decay
    optimizer = optim.AdamW(model.parameters(), lr=5e-4, weight_decay=1e-4)
    
    # Learning rate scheduler - cosine annealing with warm restarts
    scheduler = optim.lr_scheduler.CosineAnnealingWarmRestarts(
        optimizer, T_0=10, T_mult=2, eta_min=1e-6
    )
    
    # Mixed precision training
    grad_scaler = GradScaler()
    
    # Training loop
    n_epochs = 150
    best_val_loss = float('inf')
    patience = 20
    patience_counter = 0
    train_losses = []
    val_losses = []
    learning_rates = []
    
    # Model checkpoint paths
    best_model_path = f'models/best_model_pytorch_{model_name}.pt'
    final_model_path = f'models/final_model_pytorch_{model_name}.pt'
    
    print("Starting training...")
    for epoch in range(n_epochs):
        # Track learning rate
        current_lr = optimizer.param_groups[0]['lr']
        learning_rates.append(current_lr)
        
        # Train and validate
        train_loss = train_epoch(model, train_loader, criterion, optimizer, 
                               grad_scaler, device, gradient_accumulation_steps=2)
        val_loss, targets, predictions = validate(model, test_loader, criterion, 
                                               device)
        
        # Update scheduler
        scheduler.step()
        
        # Store losses
        train_losses.append(train_loss)
        val_losses.append(val_loss)
        
        # Calculate metrics
        r2 = r2_score(targets, predictions)
        mse = mean_squared_error(targets, predictions)
        rmse = np.sqrt(mse)
        mae = mean_absolute_error(targets, predictions)
        corr = np.corrcoef(targets, predictions)[0, 1]
        
        # Print epoch statistics
        print(f"Epoch {epoch+1}/{n_epochs} - LR: {current_lr:.6f}")
        print(f"Train Loss: {train_loss:.4f}, Val Loss: {val_loss:.4f}")
        print(f"R²: {r2:.4f}, RMSE: {rmse:.4f}, MAE: {mae:.4f}, Corr: {corr:.4f}")
        
        # Save checkpoint if validation loss improves
        if val_loss < best_val_loss:
            best_val_loss = val_loss
            patience_counter = 0
            torch.save(model.state_dict(), best_model_path)
            print(f"New best model saved! (Val Loss: {val_loss:.4f})")
        else:
            patience_counter += 1
            if patience_counter >= patience:
                print(f"Early stopping triggered after {epoch+1} epochs")
                break
        
        # Save intermediate model every 10 epochs
        if (epoch + 1) % 10 == 0:
            torch.save(model.state_dict(), f'models/checkpoint_epoch_{epoch+1}_{model_name}.pt')
            
    # Save final model
    torch.save(model.state_dict(), final_model_path)
    
    # Load best model and evaluate
    model.load_state_dict(torch.load(best_model_path))
    _, targets, predictions = validate(model, test_loader, criterion, device)
    
    # Calculate final metrics
    metrics = {
        'R2 Score': r2_score(targets, predictions),
        'MSE': mean_squared_error(targets, predictions),
        'RMSE': np.sqrt(mean_squared_error(targets, predictions)),
        'MAE': mean_absolute_error(targets, predictions),
        'Correlation': np.corrcoef(targets, predictions)[0, 1]
    }
    
    # Save results
    results_file = f'results/pytorch_model_results_{model_name}.txt'
    with open(results_file, 'w') as f:
        # Model information
        f.write(f"Model: {model_name}\n")
        f.write(f"Timestamp: {timestamp}\n")
        f.write("-" * 40 + "\n")
        
        # Dataset information
        f.write("Dataset Information:\n")
        f.write(f"Training set size: {len(X_train)} samples\n")
        f.write(f"Validation set size: {len(X_test)} samples\n")
        f.write(f"Number of features: {X_train.shape[1]}\n")
        f.write(f"Prediction dataset size: {len(pred_data) if 'pred_data' in locals() else 'Not predicted yet'} samples\n")
        f.write("-" * 40 + "\n")
        
        # Model configuration
        f.write("Model Configuration:\n")
        f.write(f"Batch size: {BATCH_SIZE}\n")
        f.write(f"Learning rate: {optimizer.param_groups[0]['lr']}\n")
        f.write(f"Weight decay: {optimizer.param_groups[0]['weight_decay']}\n")
        f.write(f"Sequence length: {seq_len}\n")
        f.write(f"LSTM hidden sizes: {128}, {64}\n")
        f.write(f"Number of attention heads: {4}\n")
        f.write(f"Dropout rate: {0.3}\n")
        f.write(f"Total parameters: {sum(p.numel() for p in model.parameters()):,}\n")
        f.write("-" * 40 + "\n")
        
        # Training information
        f.write("Training Information:\n")
        f.write(f"Number of epochs: {epoch + 1}\n")
        f.write(f"Best validation loss: {best_val_loss:.4f}\n")
        f.write(f"Early stopping patience: {patience}\n")
        f.write(f"Device used: {device}\n")
        f.write("-" * 40 + "\n")
        
        # Performance metrics
        f.write("Performance Metrics:\n")
        for metric, value in metrics.items():
            f.write(f"{metric}: {value:.4f}\n")
        
        # Add prediction statistics if available
        if 'predictions' in locals():
            f.write("-" * 40 + "\n")
            f.write("Prediction Statistics:\n")
            f.write(f"Min predicted value: {predictions.min():.4f}\n")
            f.write(f"Max predicted value: {predictions.max():.4f}\n")
            f.write(f"Mean predicted value: {predictions.mean():.4f}\n")
            f.write(f"Std predicted value: {predictions.std():.4f}\n")
    
    # Copy to standard location for compatibility (with basic metrics only)
    with open('pytorch_model_results.txt', 'w') as f:
        for metric, value in metrics.items():
            f.write(f"{metric}: {value:.4f}\n")
        # Add dataset sizes to the basic results file as well
        f.write(f"\nDataset Sizes:\n")
        f.write(f"Training samples: {len(X_train)}\n")
        f.write(f"Validation samples: {len(X_test)}\n")
        if 'pred_data' in locals():
            f.write(f"Prediction samples: {len(pred_data)}\n")
    
    # Plot training history and results
    plt.figure(figsize=(15, 10))
    
    # Loss plot
    plt.subplot(2, 2, 1)
    plt.plot(train_losses, label='Training Loss')
    plt.plot(val_losses, label='Validation Loss')
    plt.title('Model Loss')
    plt.xlabel('Epoch')
    plt.ylabel('Loss')
    plt.legend()
    
    # Learning rate plot
    plt.subplot(2, 2, 2)
    plt.plot(learning_rates)
    plt.title('Learning Rate')
    plt.xlabel('Epoch')
    plt.ylabel('Learning Rate')
    
    # Prediction scatter plot
    plt.subplot(2, 2, 3)
    plt.scatter(targets, predictions, alpha=0.5)
    plt.plot([targets.min(), targets.max()], 
             [targets.min(), targets.max()], 'r--')
    plt.xlabel('True Values')
    plt.ylabel('Predictions')
    plt.title(f'Prediction Scatter Plot (R² = {metrics["R2 Score"]:.4f})')
    
    # Residual plot
    plt.subplot(2, 2, 4)
    residuals = predictions - targets
    plt.scatter(predictions, residuals, alpha=0.5)
    plt.axhline(y=0, color='r', linestyle='--')
    plt.xlabel('Predicted Values')
    plt.ylabel('Residuals')
    plt.title('Residual Plot')
    
    plt.tight_layout()
    plt.savefig(f'results/pytorch_model_performance_{model_name}.png')
    plt.savefig('pytorch_model_performance.png')  # For compatibility
    plt.close()
    
    # Predict on new data in batches
    print("\nPredicting on new data...")
    try:
        pred_data = pd.read_csv("predictionData.tsv", sep="\t")
        pred_peptides = pred_data['peptide'].values
        
        print(f"Prediction dataset shape: {pred_data.shape}")
        print(f"Processing {len(pred_data)} samples for prediction...")
        
        pred_features = pred_data.drop(columns=['peptide'])
        pred_features_scaled = scaler.transform(pred_features)
        
        # Create prediction DataLoader with appropriate settings
        pred_dataset = PeptideDataset(pred_features_scaled, peptides=pred_peptides)
        pred_loader = DataLoader(
            pred_dataset, 
            batch_size=BATCH_SIZE,
            shuffle=False, 
            num_workers=NUM_WORKERS, 
            pin_memory=torch.cuda.is_available(),
            persistent_workers=True if NUM_WORKERS > 0 else False
        )
        
        # Predict in batches
        predictions = predict_batches(model, pred_loader, device)
        
        # Save predictions
        result_df = pd.DataFrame({
            'peptide': pred_data['peptide'],
            'predicted_intensity': predictions
        })
        result_df.to_csv('pytorch_predictions.tsv', sep='\t', index=False)
        
        print(f"Predictions saved to 'pytorch_predictions.tsv'")
        print(f"Prediction statistics: Min={predictions.min():.4f}, Max={predictions.max():.4f}, Mean={predictions.mean():.4f}")
        
    except Exception as e:
        print(f"Error during prediction: {e}")
        print("Saving model checkpoint anyway.")
    
    print("\nProcess completed! Check the output files for results.")

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"Error in main execution: {e}")
        import traceback
        traceback.print_exc()