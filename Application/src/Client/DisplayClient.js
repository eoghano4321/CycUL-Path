import axios from 'axios';
import { toast } from 'react-toastify';

export async function getMapData() {
    let toastId;
    try {
        toastId = toast.loading("Loading...");
        const response = await axios.get(`${process.env.REACT_APP_SERVER}/api/display/map`, {
            headers: {
                'ngrok-skip-browser-warning': 'true'
            }
        });
        console.log(response);
        toast.dismiss(toastId);
        return JSON.stringify(response.data);
    } catch (error) {
        if (toastId) toast.dismiss(toastId);
        console.error('Error fetching map data: ', error);
        throw error;
    }
}

export async function getIncidents() {
    let toastId;
    try {
        toastId = toast.loading("Loading...");
        const response = await axios.get(`${process.env.REACT_APP_SERVER}/api/display/incidents`, {
            headers: {
                'ngrok-skip-browser-warning': 'true'
            }
        });
        console.log(response);
        toast.dismiss(toastId);
        return JSON.stringify(response.data);
    } catch (error) {
        if (toastId) toast.dismiss(toastId);
        console.error('Error fetching map data: ', error);
        throw error;
    }
}

export async function getShortestPath(startLat, startLon, endLat, endLon, withIncidents) {
    let toastId;
    try {
        toastId = toast.loading("Loading...");
        const response = await axios.get(`${process.env.REACT_APP_SERVER}/api/shortest-path?startLat=${startLat}&startLon=${startLon}&endLat=${endLat}&endLon=${endLon}&includeIncidents=${withIncidents}`, {
            headers: {
                'ngrok-skip-browser-warning': 'true'
            }
        });
        console.log(response);
        toast.dismiss(toastId);
        return JSON.stringify(response.data);
    } catch (error) {
        if (toastId) toast.dismiss(toastId);
        console.error("Error fetching map data: ", error);
        throw error;
    }
}
