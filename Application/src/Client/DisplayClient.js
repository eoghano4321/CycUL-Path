import axios from 'axios';

export async function getMapData() {
    try {
        const response = await axios.get(`${process.env.REACT_APP_SERVER}/api/display/map`, {
            headers: {
                'ngrok-skip-browser-warning': 'true'
            }
        });
        console.log(response);
        return JSON.stringify(response.data);
    } catch (error) {
        console.error('Error fetching map data: ', error);
        throw error;
    }
}

export async function getIncidents() {
    try {
        const response = await axios.get(`${process.env.REACT_APP_SERVER}/api/display/incidents`, {
            headers: {
                'ngrok-skip-browser-warning': 'true'
            }
        });
        console.log(response);
        return JSON.stringify(response.data);
    } catch (error) {
        console.error('Error fetching map data: ', error);
        throw error;
    }
}

export async function getShortestPath(startLat, startLon, endLat, endLon, withIncidents) {
    try {
        const response = await axios.get(`${process.env.REACT_APP_SERVER}/api/shortest-path?startLat=${startLat}&startLon=${startLon}&endLat=${endLat}&endLon=${endLon}&includeIncidents=${withIncidents}`, {
            headers: {
                'ngrok-skip-browser-warning': 'true'
            }
        });
        console.log(response);
        return JSON.stringify(response.data);
    } catch (error) {
        console.error("Error fetching map data: ", error);
        throw error;
    }
}
