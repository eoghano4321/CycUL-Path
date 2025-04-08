import json
import geojson

# Load GeoJSON from file
with open("./OSM_Dublin_AllRoads.geojson", "r", encoding="utf-8") as f:
    data = geojson.load(f)

# Define disallowed bicycle tags
exclude_values = {"no", "dismount", "destination"}

# Filter features
filtered_features = [
    feature for feature in data["features"]
    if feature.get("properties", {}).get("bicycle") not in exclude_values
]

# Create new FeatureCollection
filtered_collection = geojson.FeatureCollection(filtered_features)

# Save to new file
with open("./filtered.geojson", "w", encoding="utf-8") as f:
    geojson.dump(filtered_collection, f, indent=2)

print(f"Filtered {len(data['features']) - len(filtered_features)} features.")
print("Saved cleaned data to filtered.geojson")
