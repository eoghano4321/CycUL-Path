import axios from 'axios'

export async function getMapData() {
    try {
        const response = await axios.get(`http://127.0.0.1:3131/api/display/map`);
        console.log(response);
        return JSON.stringify(response.data);
    } catch (error) {
        console.error('Error fetching map data:', error);
        throw error;
    }
}
