import os
import sqlite3
import pandas as pd

def parse_lineage(lineage):
    """
    Parse the lineage string into its hierarchical components.
    """
    hierarchy = {"d": None, "p": None, "c": None, "o": None, "f": None, "g": None, "s": None}
    levels = lineage.split(";")
    for level in levels:
        if "__" in level:
            prefix, name = level.split("__", 1)
            hierarchy[prefix] = name
    return hierarchy

def process_tsv_to_sqlite(tsv_file, sqlite_db):
    """
    Read TSV, parse lineage, and store data in SQLite.
    """
    # Read the TSV file
    df = pd.read_csv(tsv_file, sep="\t")

    seen_genomes = set()
    parsed_data = []
    for _, row in df.iterrows():
        genome_name = row['Species_rep']
        lineage = row['Lineage']
        if genome_name in seen_genomes:
            continue
        
        seen_genomes.add(genome_name)
        hierarchy = parse_lineage(lineage)
        parsed_data.append([genome_name, hierarchy["d"], hierarchy["p"], hierarchy["c"],
                            hierarchy["o"], hierarchy["f"], hierarchy["g"], hierarchy["s"]])

    # Create a DataFrame for structured data
    columns = ["genome_name", "superkingdom", "phylum", "class", "order_name", "family", "genus", "species"]
    structured_df = pd.DataFrame(parsed_data, columns=columns)

    # Connect to SQLite and insert data
    with sqlite3.connect(sqlite_db) as conn:
        # Create table
        create_table_query = """
        CREATE TABLE IF NOT EXISTS taxonomy (
            genome_name TEXT PRIMARY KEY,
            superkingdom TEXT,
            phylum TEXT,
            class TEXT,
            order_name TEXT,
            family TEXT,
            genus TEXT,
            species TEXT
        );
        """
        conn.execute(create_table_query)

        # Insert data
        structured_df.to_sql('taxonomy', conn, if_exists='append', index=False)

def process_tsv_files(folder_path, db_path, func_name):
    """
    Process TSV files in the specified folder and insert data into a SQLite database.

    Parameters:
    - folder_path (str): Path to the folder containing TSV files.
    - db_path (str): Path to the SQLite database file.
    - func_name (str): Name of the function to process and database table/column names.
    """
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Safeguard: Use parameterized table creation
    cursor.execute(f'''CREATE TABLE IF NOT EXISTS {func_name} (
                            protein_name TEXT,
                            func_name TEXT
                        )''')

    for filename in os.listdir(folder_path):
        if filename.endswith(".tsv"):
            file_path = os.path.join(folder_path, filename)
            try:
                with open(file_path, 'r') as file:
                    header = next(file).strip().split("\t")  # Read header row
                    try:
                        query_index = header.index("#query")  # Get index of "#query" column
                        func_index = header.index(func_name)
                    except ValueError:
                        print(f"Missing expected columns in file: {filename}")
                        continue

                    rows_to_insert = []
                    for line in file:
                        try:
                            row = line.strip().split("\t")
                            protein_name = row[query_index]
                            data = row[func_index]
                            if data and data != "-":  # Skip empty entries
                                funcs = data.split(",")
                                for fun in funcs:
                                    rows_to_insert.append((protein_name, fun))
                        except (ValueError, IndexError):
                            print(f"Error processing line in {filename}: {line}")

                    # Batch insert to minimize database writes
                    if rows_to_insert:
                        cursor.executemany(
                            f"INSERT INTO {func_name} (protein_name, func_name) VALUES (?, ?)", 
                            rows_to_insert
                        )
                print(f"Processed: {filename}")
            except Exception as e:
                print(f"Error reading file {filename}: {e}")

    # Create index to speed up queries
    cursor.execute(f"CREATE INDEX IF NOT EXISTS idx_{func_name} ON {func_name} (func_name)")
    conn.commit()
    conn.close()