import pandas as pd
import gc
from datetime import datetime
from transformers import pipeline
import tensorflow as tf
import csv
import os
os.environ['TF_GPU_ALLOCATOR'] = 'cuda_malloc_async'


# Load zero-shot classification pipeline
classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")

# Define factors as candidate labels
factors = [
    "Dim Lighting",
    "Broken Streetlight",
    "Dark",
    "Bad Junction",
    "No roadsigns",
    "No crossing",
    "Pothole",
    "Slippery road surface",
    "Busy junction",
    "Too fast",
    "Obstruction",
    "Often",
    "Weather",
    "Broke light",
    "Did not yield",
    "Broke rules",
    "Unlikely to repeat"
]

# Function to detect factors using zero-shot classification
def detect_factors(description):
    # Perform zero-shot classification
    result = classifier(description, factors, multi_label=True)
    
    # Map scores back to their original factor order
    label_to_score = dict(zip(result['labels'], result['scores']))
    original_order_scores = [label_to_score[label] for label in factors]
    
    # Filter factors based on the threshold in the original order
    detected_factors = [factors[i] for i, score in enumerate(original_order_scores) if score > 0.9]  # Adjust threshold as needed
    
    return detected_factors


# Function to calculate severity score
weights = {  # Factor weights as before
    "Dim Lighting": 0.7,
    "Bad Junction": 0.45,
    "No roadsigns": 0.5,
    "No crossing": 0.5,
    "Pothole": 0.5,
    "Slippery road surface": 0.6,
    "Busy junction": 0.45,
    "Too fast": 0.5,
    "Collision and Fatality": 0.95,
    "Collision and Serious Injury": 0.65,
    "Collision and Minor Injury": 0.5,
    "Collision and No Injury": 0.35,
    "Near Miss": 0.3,
    "Hazard": 0.55,
    "Obstruction": 0.4,
    "Often": 0.55,
    "Weather": -0.5,
    "Broke light": -0.15,
    "Did not yield": -0.05,
    "Broke rules": -0.05,
    "Unlikely to repeat": -0.1
}

# Generator for loading data in chunks
def data_generator(file_path, chunk_size=100):
    for chunk in pd.read_csv(file_path, chunksize=chunk_size):
        for _, row in chunk.iterrows():
            yield row

# Function to calculate severity score for each row
def calculate_severity(row):
    print(row['Description'])
    factors = detect_factors(row['Description'])
    if row['IncidentType'] == "Collision":
        if row['Outcome'] == "No injuries":
            factors.append("Collision and No Injury")
        elif row['Outcome'] == "Minor injuries":
            factors.append("Collision and Minor Injury")
        elif row['Outcome'] == "Serious injuries":
            factors.append("Collision and Serious Injury")
        elif row['Outcome'] == "Fatality":
            factors.append("Collision and Fatality")
    elif row['IncidentType'] == "Near Miss":
        factors.append("Near Miss")
    else:
        factors.append("Hazard")
    print(factors)
    # Calculate weight sum and number of factors
    total_weight = sum(weights.get(factor, 0) for factor in factors)
    num_factors = len(factors) if factors else 1  # Avoid division by zero
    base_score = total_weight / num_factors

    # Time decay factor
    occurrence_date = datetime.strptime(row['OccurredAt'], '%Y-%m-%dT%H:%M')
    months_since = (datetime.now() - occurrence_date).days // 30
    time_decay = 0.01 * months_since

    # Final severity score
    severity_score = base_score - time_decay
    print(severity_score)
    print(max(severity_score, 0.05))
    return max(severity_score, 0.05)  # Ensure score is at least 0.05

# Incremental processing and saving
def process_data_incrementally(input_file, output_file):
    with open(output_file, mode='w', newline='', encoding='utf-8') as out_file:
        i = 0
        # Initialize CSV writer
        csv_writer = None
        for chunk in pd.read_csv(input_file, chunksize=1):  # Process one row at a time
            print("[" + str(i) + " / 1148]")
            for _, row in chunk.iterrows():
                i += 1
                # Calculate severity score
                row['CalculatedSeverity'] = calculate_severity(row).round(3)

                # Initialize writer with headers
                if csv_writer is None:
                    csv_writer = csv.DictWriter(out_file, fieldnames=row.keys())
                    csv_writer.writeheader()

                # Write the row to the file
                csv_writer.writerow(row.to_dict())

                # Explicitly call garbage collection
                gc.collect()

# Specify the file paths
input_file = 'TimedIncidentsDublin.csv'
output_file = 'IncidentsWithSeverityDublin.csv'

# Process the data
process_data_incrementally(input_file, output_file)
