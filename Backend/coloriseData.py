import geojson

def load_geojson(filename):
    with open(filename, 'r') as file:
        return geojson.load(file)

def save_geojson(data, filename):
    with open(filename, 'w') as file:
        geojson.dump(data, file)

# Load the merged feature collection
merged_fc = load_geojson('CombinedDublinCycleNetwork.geojson')

# Define stroke colors based on classifica values
stroke_colors = {
    "segregatedcyclelane": "#ff0000",
    "cyclelane": "#0000ff",
    "surfacechange": "#80ff80",
    "trafficfree": "#004000",
    "shareduse": "#ff8040",
    "buslane": "#888888",
    "unset": "#00ffff"  # Color for features with a null classifica
}

# Process each feature in the collection
for feature in merged_fc['features']:
    properties = feature["properties"]
    
    # Set default styling properties
    properties["stroke-width"] = 2
    properties["stroke-opacity"] = 1
    
    # Determine classifica and set stroke color based on its value
    classifica = properties.get("classifica")
    if classifica is None:  # If classifica is null, set it to "unset"
        properties["classifica"] = "unset"
        properties["stroke"] = stroke_colors["unset"]
    else:
        # Set stroke color based on the classifica value
        properties["stroke"] = stroke_colors.get(classifica, "#000000")  # Fallback to black if classifica not matched

# Save the updated feature collection with styling properties
save_geojson(merged_fc, 'CombinedDublinCycleNetworkColorised.geojson')
print("Styling properties added and saved in 'styled_cycle_network.geojson'.")
