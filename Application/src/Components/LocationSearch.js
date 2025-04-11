import { useState, useEffect } from "react";
import { toast } from "react-toastify";

export default function AddressToGeoJSON() {
  const [address, setAddress] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [error, setError] = useState(null);
  const [debouncedQuery, setDebouncedQuery] = useState("");

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(address);
    }, 500); // Delay API calls

    return () => clearTimeout(handler);
  }, [address]);

  useEffect(() => {
    const fetchSuggestions = async () => {
      if (!debouncedQuery) {
        setSuggestions([]);
        return;
      }
      try {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(debouncedQuery)}&addressdetails=1&limit=3`
        );
        const data = await response.json();
        setSuggestions(data);
      } catch (err) {
        setSuggestions([]);
        toast.error("Failed to fetch suggestions");
      }
    };

    fetchSuggestions();
  }, [debouncedQuery]);

  const fetchCoordinates = async () => {
    if (!address) return;
    setError(null);
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=1`
      );
      const data = await response.json();
      if (data.length === 0) {
        setError("Address not found");
        return;
      }
      const { lat, lon } = data[0];
      const geoJSON = {
        type: "Feature",
        geometry: {
          type: "Point",
          coordinates: [parseFloat(lon), parseFloat(lat)],
        },
        properties: {
          address,
        },
      };
      console.log(geoJSON);
    } catch (err) {
      setError("Failed to fetch coordinates");
    }
  };

  return (
    <div className="flex flex-col gap-4 p-4 max-w-md mx-auto">
      <div className="flex items-center gap-2">
        <div className="relative w-1/5">
          <input
            type="text"
            placeholder="Enter an address"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            className="border p-2 rounded-lg w-full bg-white h-10 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
          {suggestions.length > 0 && (
            <ul className="absolute w-full border border-gray-300 rounded-lg bg-white mt-1 max-h-40 overflow-auto shadow-lg">
              {suggestions.map((suggestion, index) => (
                <li
                  key={index}
                  onClick={() => {
                    setAddress(suggestion.display_name);
                    setSuggestions([]);
                  }}
                  className="p-2 hover:bg-gray-200 cursor-pointer h-10 flex items-center"
                >
                  {suggestion.display_name}
                </li>
              ))}
            </ul>
          )}
        </div>
        <button onClick={fetchCoordinates} className="bg-green-500 text-white px-4 py-2 rounded-lg h-10 ml-2 shadow-md hover:bg-green-600">
          Get Coordinates
        </button>
      </div>
      {error && <p className="text-red-500">{error}</p>}
    </div>
  );
}