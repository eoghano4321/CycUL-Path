import axios from 'axios'

export async function getMapData() {
    try {
        const response = await axios.get(`http://127.0.0.1:3131/api/display/map`);
        console.log(response);
        return JSON.stringify(response.data);
    } catch (error) {
        console.error('Error fetching map data: ', error);
        throw error;
    }
}

export async function getIncidents() {
    try {
        const response = await axios.get(`http://127.0.0.1:3131/api/display/incidents`);
        console.log(response);
        return JSON.stringify(response.data);
    } catch (error) {
        console.error('Error fetching map data: ', error);
        throw error;
    }
}

export async function getShortestPath(startLat, startLon, endLat, endLon){
    try{
        const response = await axios.get(`http://127.0.0.1:3131/api/shortest-path?startLat=${startLat}&startLon=${startLon}&endLat=${endLat}&endLon=${endLon}`);
        console.log(response);
        return JSON.stringify(response.data)
    } catch (error) {
        console.error("Error fetching map data: ", error);
        throw error;
    }
}
