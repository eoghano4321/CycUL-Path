import pandas as pd
from datetime import datetime
from transformers import pipeline

classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")

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

weights = {
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

def process_data_incrementally(input_file, output_file, chunk_size=100):
    try:
        header_df = pd.read_csv(input_file, nrows=0)
        header_df['CalculatedSeverity'] = None
        header_df.to_csv(output_file, index=False, mode='w', encoding='utf-8')
    except Exception as e:
        print(f"Error initializing output file: {e}")
        return

    total_rows_processed = 0
    # Estimate total rows for progress (optional, could read whole file first if needed)
    # total_rows = sum(1 for row in open(input_file, 'r', encoding='utf-8')) - 1 # Subtract header
    # print(f"Estimated total rows: {total_rows}")

    for chunk in pd.read_csv(input_file, chunksize=chunk_size):
        print(f"Processing chunk of size {len(chunk)}...")
        descriptions = chunk['Description'].tolist()
        
        # Handle potential None or non-string values in descriptions
        descriptions = [str(d) if pd.notna(d) else "" for d in descriptions]

        # Perform batch inference
        batch_results = classifier(descriptions, factors, multi_label=True)

        calculated_severities = []
        for index, row in chunk.iterrows():
            print(f"Processing row {index}...")
            classifier_result = batch_results[index % chunk_size] 

            # Extract detected factors based on threshold
            label_to_score = dict(zip(classifier_result['labels'], classifier_result['scores']))
            original_order_scores = [label_to_score.get(label, 0) for label in factors] # Use .get for safety
            detected_factors = [factors[i] for i, score in enumerate(original_order_scores) if score > 0.9]

            # Add IncidentType/Outcome factors
            if row['IncidentType'] == "Collision":
                if row['Outcome'] == "No injuries":
                    detected_factors.append("Collision and No Injury")
                elif row['Outcome'] == "Minor injuries":
                    detected_factors.append("Collision and Minor Injury")
                elif row['Outcome'] == "Serious injuries":
                    detected_factors.append("Collision and Serious Injury")
                elif row['Outcome'] == "Fatality":
                    detected_factors.append("Collision and Fatality")
            elif row['IncidentType'] == "Near Miss":
                detected_factors.append("Near Miss")
            else: # Assuming Hazard if not Collision or Near Miss
                detected_factors.append("Hazard")

            # Calculate base score
            total_weight = sum(weights.get(factor, 0) for factor in detected_factors)
            num_factors = len(detected_factors) if detected_factors else 1
            base_score = total_weight / num_factors

            # Calculate time decay
            try:
                occurrence_date = datetime.strptime(row['OccurredAt'], '%Y-%m-%dT%H:%M')
                months_since = (datetime.now() - occurrence_date).days // 30
                time_decay = 0.01 * months_since
            except (ValueError, TypeError):
                time_decay = 0

            severity_score = base_score - time_decay
            final_severity = max(severity_score, 0.05)

            calculated_severities.append(final_severity)

        chunk['CalculatedSeverity'] = calculated_severities
        
        chunk.to_csv(output_file, mode='a', header=False, index=False, encoding='utf-8')

        total_rows_processed += len(chunk)
        print(f"Processed {total_rows_processed} rows...")

input_file = 'TimedIncidentsDublin.csv'
output_file = 'IncidentsWithSeverityDublin270425.csv' 

process_data_incrementally(input_file, output_file, chunk_size=100) 

print("Processing complete.")