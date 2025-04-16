import geojson

def load_geojson(filename):
    with open(filename, 'r') as file:
        return geojson.load(file)

def save_geojson(data, filename):
    with open(filename, 'w') as file:
        geojson.dump(data, file)

# Load the merged feature collection
merged_fc = load_geojson('DublinCycleNetwork.json')

# Initialize OBJECTID counter starting from 5852
next_objectid = 5852

# List of properties to add with default values
additional_properties = ["buslane", "shareduse", "segregated", "trafficfre"]

# Process each feature in the merged collection
for feature in merged_fc['features']:
    properties = feature["properties"]
    
    # Check if feature follows the second format (using "Name" instead of "classifica")
    if "Name" in properties:
        
        # Remove the "description" field
        properties.pop("description", None)
        
        # Add "OBJECTID" and increment counter
        properties["OBJECTID"] = next_objectid
        next_objectid += 1
        
        # Set "classifica" to lowercase version of "Name"
        name_lower = properties["Name"].lower()
        properties["classifica"] = name_lower
        properties["classifi_1"] = name_lower
        
        # Add missing properties and set to 1 if the name contains the property
        for prop in additional_properties:
            properties[prop] = 1 if prop in name_lower else 0
        
        # Set cyclelane to 1 if name contains "cyclelane" and "segregated" is not already 1
        if "cyclelane" in name_lower and properties.get("segregated", 0) != 1:
            properties["cyclelane"] = 1
        else:
            properties["cyclelane"] = 0
        
        if "surfacech" in name_lower:
            properties["surface_ch"] = 1
        else:
            properties["surface_ch"] = 1
        
        properties.pop("tessellate", None)
        properties.pop("extrude", None)
        properties.pop("visibility", None)
        properties.pop("cdo", None)
        properties.pop("Name", None)

# Save the updated feature collection
save_geojson(merged_fc, 'CombinedDublinCycleNetwork.geojson')
print("Properties standardized and saved in 'updated_cycle_network.geojson'.")
