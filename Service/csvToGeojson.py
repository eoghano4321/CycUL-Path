import pandas as pd
import json

# Load the CSV file
csv_file_path = 'TimedIncidentsDublin.csv'  # Replace with your actual file path
df = pd.read_csv(csv_file_path)

# Function to generate GeoJSON feature for each row
def generate_feature(row):
    return {
        "type": "Feature",
        "geometry": {
            "type": "Point",
            "coordinates": [row['Long'], row['Lat']]
        },
        "properties": {
            "IncidentType": row['IncidentType'],
            "Outcome": row['Outcome'],
            "MultiParty": str(row['MultiParty']),  # Ensure it's a string ("True"/"False")
            "CauseCategory": row['CauseCategory'],
            "SeverityScore": row['SeverityScore']
        }
    }

# Convert the entire DataFrame to GeoJSON features
features = [generate_feature(row) for _, row in df.iterrows()]

# Create the final GeoJSON structure
geojson = {
    "type": "FeatureCollection",
    "features": features
}

# Save to a GeoJSON file
output_file_path = 'dublin_incidents.geojson'
with open(output_file_path, 'w') as f:
    json.dump(geojson, f, indent=2)

print(f"GeoJSON file has been saved to {output_file_path}")
