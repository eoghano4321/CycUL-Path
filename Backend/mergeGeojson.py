import geojson
from shapely.geometry import shape

def load_geojson(filename):
    with open(filename, 'r') as file:
        return geojson.load(file)

def save_geojson(data, filename):
    with open(filename, 'w') as file:
        geojson.dump(data, file)

# Load the two feature collections
fc_2019 = load_geojson('CycleLanesDublinReprojected.geojson')
fc_2023 = load_geojson('segregated_cycle_network.json')

# Convert features to Shapely geometries for easier spatial comparison
def to_shapely(feature):
    return shape(feature['geometry'])

# Get geometries for quick comparison

# Get geometries for quick comparison
geometries_2019 = [to_shapely(f) for f in fc_2019['features']]
features_added = 0

# Define a maximum distance for approximate matching (in degrees, assuming small area)
max_distance = 0.00005

# Loop through each feature in the 2023 collection
for feature_2023 in fc_2023['features']:
    geometry_2023 = to_shapely(feature_2023)
    
    # Check if this geometry is approximately close to any in the 2019 collection
    is_duplicate = any(geometry_2023.distance(geometry_2019) < max_distance for geometry_2019 in geometries_2019)
    
    # If no close match, add the feature to the 2019 feature collection
    if not is_duplicate:
        fc_2019['features'].append(feature_2023)
        features_added += 1

print(f"Added {features_added} new features to the 2019 collection.")

# Save the updated 2019 collection
save_geojson(fc_2019, 'updated_cycle_network3_2019.geojson')
