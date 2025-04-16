import pandas as pd
from datetime import datetime

# Load the CSV file
input_file = 'Incidents-Exportable_ForResearchers_Nov2024.csv'  # Replace with your actual file name
output_file = 'TimedIncidents.csv'

# Read the CSV file into a DataFrame
df = pd.read_csv(input_file)

# Define a function to process the dates and incorporate time from "Created" column
def standardize_date(row):
    try:
        date_str = row['OccurredAt']
        created_time = row['Created']
        
        # Extract the time from the "Created" column
        if isinstance(created_time, str) and ' ' in created_time:
            time_part = created_time.split(' ')[1]  # Get the time portion
        else:
            time_part = '12:00'  # Default fallback time
        
        if 'T' in date_str:
            # Return the original date if it's already in the correct format
            return date_str
        else:
            # Process the date
            # Convert "25/04/2024" format to "2024-04-25Ttime_part"
            parsed_date = datetime.strptime(date_str, "%Y-%m-%d")
            return parsed_date.strftime(f'%Y-%m-%dT{time_part}')
    except Exception as e:
        print(f"Error processing row: {row}, Error: {e}")
        return date_str  # Return the original date if parsing fails

# Apply the function row-wise to the DataFrame
df['OccurredAt'] = df.apply(standardize_date, axis=1)

# Drop the "Created" and "AuthorRole" columns
columns_to_drop = ['Created', 'AuthorRole']
df = df.drop(columns=columns_to_drop, errors='ignore')

# Save the updated DataFrame to a new CSV file
df.to_csv(output_file, index=False)

print(f"Updated file saved as {output_file}")
