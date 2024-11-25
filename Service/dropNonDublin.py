import pandas as pd
from datetime import datetime

# Load the CSV file
input_file = 'TimedIncidents.csv'  # Replace with your actual file name
output_file = 'TimedIncidentsDublin.csv'

# Read the CSV file into a DataFrame
df = pd.read_csv(input_file)

def removeOutsideDublin(row):
    try:
        latRow = row['Lat']
        longRow = row['Long']

        if float(latRow) < 53.00 or float(latRow) > 53.70:
           return False
        elif float(longRow) < -6.70 or float(longRow) > -6.10:
            return False
        else:
            return True
    except Exception as e:
        print(f"Error processing row: {row}, Error: {e}")
        return True
    
dataFrame = {
    'OccurredAt': [],
    'Description': [],
    'IncidentType': [],
    'Outcome': [],
    'Lat': [],
    'Long': [],
    'MultiParty': [],
    'CauseCategory': []
}
df1 = pd.DataFrame(dataFrame)

for i, row in df.iterrows():
    if removeOutsideDublin(row):
        df1.loc[len(df1)] = row

df1.to_csv(output_file, index=False)

print(f"Updated file saved as {output_file}")