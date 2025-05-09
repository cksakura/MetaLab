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

# Set random seeds for reproducibility
torch.manual_seed(42)
np.random.seed(42)

# Check if CUDA is available
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(f"Using device: {device}")

# Custom dataset class
class PeptideDataset(Dataset):
    def __init__(self, features, targets=None, weights=None):
        # Convert pandas Series/DataFrame to numpy arrays if needed
        if isinstance(features, (pd.Series, pd.DataFrame)):
            features = features.to_numpy()
        if isinstance(targets, pd.Series):
            targets = targets.to_numpy()
        if isinstance(weights, pd.Series):
            weights = weights.to_numpy()
            
        self.features = torch.FloatTensor(features)
        self.targets = torch.FloatTensor(targets) if targets is not None else None
        self.weights = torch.FloatTensor(weights) if weights is not None else None
        
    def __len__(self):
        return len(self.features)
    
    def __getitem__(self, idx):
        if self.targets is not None and self.weights is not None:
            return self.features[idx], self.targets[idx], self.weights[idx]
        return self.features[idx]

# Model definition
class PeptideIntensityPredictor(nn.Module):
    def __init__(self, input_size, lstm_hidden_1=128, lstm_hidden_2=64, 
                 num_heads=4, dropout=0.3):
        super().__init__()
        
        self.batch_norm_input = nn.BatchNorm1d(input_size)
        
        # Bidirectional LSTM layers
        self.lstm1 = nn.LSTM(input_size, lstm_hidden_1, bidirectional=True, 
                            batch_first=True)
        self.lstm2 = nn.LSTM(lstm_hidden_1*2, lstm_hidden_2, bidirectional=True, 
                            batch_first=True)
        
        # Multi-head attention
        self.attention = nn.MultiheadAttention(lstm_hidden_2*2, num_heads)
        
        # Dense layers
        self.fc1 = nn.Linear(lstm_hidden_2*2, 256)
        self.fc2 = nn.Linear(256, 128)
        self.fc3 = nn.Linear(128, 1)
        
        # Normalization and regularization
        self.batch_norm1 = nn.BatchNorm1d(256)
        self.batch_norm2 = nn.BatchNorm1d(128)
        self.dropout = nn.Dropout(dropout)
        
    def forward(self, x):
        # Input normalization
        x = self.batch_norm_input(x)
        
        # Reshape for LSTM (batch_size, seq_len=1, features)
        x = x.unsqueeze(1)
        
        # LSTM layers
        lstm_out1, _ = self.lstm1(x)
        lstm_out2, _ = self.lstm2(lstm_out1)
        
        # Multi-head attention
        x = lstm_out2.transpose(0, 1)  # Change to attention expected shape
        x, _ = self.attention(x, x, x)
        x = x.transpose(0, 1)  # Change back
        
        # Global average pooling
        x = torch.mean(x, dim=1)
        
        # Dense layers with residual connections
        x = self.fc1(x)
        x = self.batch_norm1(x)
        x = torch.relu(x)
        x = self.dropout(x)
        
        x = self.fc2(x)
        x = self.batch_norm2(x)
        x = torch.relu(x)
        x = self.dropout(x)
        
        x = self.fc3(x)
        
        return x

# Training function
def train_epoch(model, train_loader, criterion, optimizer, scaler, device):
    model.train()
    total_loss = 0
    
    for features, targets, weights in tqdm(train_loader, desc='Training'):
        features, targets, weights = (features.to(device), targets.to(device), 
                                    weights.to(device))
        
        optimizer.zero_grad()
        
        with autocast(device_type='cuda' if torch.cuda.is_available() else 'cpu'):
            outputs = model(features)
            loss = criterion(outputs, targets.view(-1, 1)) * weights.view(-1, 1)
            loss = loss.mean()
        
        scaler.scale(loss).backward()
        scaler.step(optimizer)
        scaler.update()
        
        total_loss += loss.item()
    
    return total_loss / len(train_loader)

# Validation function
def validate(model, val_loader, criterion, device):
    model.eval()
    total_loss = 0
    all_targets = []
    all_predictions = []
    
    with torch.no_grad():
        for features, targets, weights in tqdm(val_loader, desc='Validation'):
            features, targets, weights = (features.to(device), targets.to(device), 
                                        weights.to(device))
            
            outputs = model(features)
            loss = criterion(outputs, targets.view(-1, 1)) * weights.view(-1, 1)
            loss = loss.mean()
            
            total_loss += loss.item()
            all_targets.extend(targets.cpu().numpy())
            all_predictions.extend(outputs.cpu().numpy().ravel())
    
    return (total_loss / len(val_loader), 
            np.array(all_targets), 
            np.array(all_predictions))

# Main training loop
def main():
    # Data loading and preprocessing
    print("Loading data...")
    data = pd.read_csv("trainData.tsv", sep="\t")
    X = data.drop(columns=['peptide', 'intensity'])
    y = data['intensity']
    
    # Scale features
    scaler = RobustScaler()
    X_scaled = scaler.fit_transform(X)
    
    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X_scaled, y, test_size=0.2, random_state=42
    )
    
    # Calculate sample weights
    intensity_weights = y / y.max()
    sample_weights_train = intensity_weights[y_train.index]
    sample_weights_test = intensity_weights[y_test.index]
    
    # Create datasets and dataloaders
    BATCH_SIZE = 128
    train_dataset = PeptideDataset(X_train, y_train, sample_weights_train)
    test_dataset = PeptideDataset(X_test, y_test, sample_weights_test)
    
    train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, 
                            shuffle=True, num_workers=4)
    test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE, 
                           shuffle=False, num_workers=4)
    
    # Initialize model
    model = PeptideIntensityPredictor(input_size=X_train.shape[1]).to(device)
    
    # Loss and optimizer
    criterion = nn.HuberLoss()
    optimizer = optim.AdamW(model.parameters(), lr=1e-3, weight_decay=1e-4)
    scheduler = optim.lr_scheduler.ReduceLROnPlateau(optimizer, mode='min', 
                                                    factor=0.5, patience=5, 
                                                    min_lr=1e-6)
    
    # Mixed precision training
    grad_scaler = GradScaler()
    
    # Training loop
    n_epochs = 100
    best_val_loss = float('inf')
    patience = 15
    patience_counter = 0
    train_losses = []
    val_losses = []
    
    print("Starting training...")
    for epoch in range(n_epochs):
        train_loss = train_epoch(model, train_loader, criterion, optimizer, 
                               grad_scaler, device)
        val_loss, targets, predictions = validate(model, test_loader, criterion, 
                                               device)
        
        train_losses.append(train_loss)
        val_losses.append(val_loss)
        
        # Calculate metrics
        r2 = r2_score(targets, predictions)
        mse = mean_squared_error(targets, predictions)
        mae = mean_absolute_error(targets, predictions)
        corr = np.corrcoef(targets, predictions)[0, 1]
        
        print(f"Epoch {epoch+1}/{n_epochs}")
        print(f"Train Loss: {train_loss:.4f}, Val Loss: {val_loss:.4f}")
        print(f"R2: {r2:.4f}, MSE: {mse:.4f}, MAE: {mae:.4f}, Corr: {corr:.4f}")
        
        # Learning rate scheduling
        scheduler.step(val_loss)
        
        # Early stopping
        if val_loss < best_val_loss:
            best_val_loss = val_loss
            patience_counter = 0
            torch.save(model.state_dict(), 'best_model_pytorch.pt')
        else:
            patience_counter += 1
            if patience_counter >= patience:
                print("Early stopping triggered")
                break
    
    # Load best model and evaluate
    model.load_state_dict(torch.load('best_model_pytorch.pt'))
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
    with open('pytorch_model_results.txt', 'w') as f:
        for metric, value in metrics.items():
            f.write(f"{metric}: {value:.4f}\n")
    
    # Plot results
    plt.figure(figsize=(12, 4))
    
    plt.subplot(1, 2, 1)
    plt.plot(train_losses, label='Training Loss')
    plt.plot(val_losses, label='Validation Loss')
    plt.title('Model Loss')
    plt.xlabel('Epoch')
    plt.ylabel('Loss')
    plt.legend()
    
    plt.subplot(1, 2, 2)
    plt.scatter(targets, predictions, alpha=0.5)
    plt.plot([targets.min(), targets.max()], 
             [targets.min(), targets.max()], 'r--')
    plt.xlabel('True Values')
    plt.ylabel('Predictions')
    plt.title(f'Prediction Scatter Plot (R² = {metrics["R2 Score"]:.4f})')
    
    plt.tight_layout()
    plt.savefig('pytorch_model_performance.png')
    
    # Predict on new data
    print("\nPredicting on new data...")
    pred_data = pd.read_csv("predictionData.tsv", sep="\t")
    pred_features = pred_data.drop(columns=['peptide'])
    pred_features_scaled = scaler.transform(pred_features)
    
    pred_dataset = PeptideDataset(pred_features_scaled)
    pred_loader = DataLoader(pred_dataset, batch_size=BATCH_SIZE, 
                           shuffle=False, num_workers=4)
    
    model.eval()
    predictions = []
    with torch.no_grad():
        for features in tqdm(pred_loader, desc='Predicting'):
            features = features.to(device)
            outputs = model(features)
            predictions.extend(outputs.cpu().numpy().ravel())
    
    # Save predictions
    result_df = pd.DataFrame({
        'peptide': pred_data['peptide'],
        'predicted_intensity': predictions
    })
    result_df.to_csv('pytorch_predictions.tsv', sep='\t', index=False)
    
    print("\nProcess completed! Check the output files for results.")

if __name__ == "__main__":
    main()