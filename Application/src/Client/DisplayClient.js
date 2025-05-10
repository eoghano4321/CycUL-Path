import axios from 'axios';
import { toast } from 'react-toastify';
import emailjs from '@emailjs/browser';

function sendMaintenanceEmail() {
    const serviceId = process.env.REACT_APP_EMAILJS_SERVICE_ID;
    const templateId = process.env.REACT_APP_EMAILJS_TEMPLATE_ID;
    const publicKey = process.env.REACT_APP_EMAILJS_PUBLIC_KEY;

    emailjs.send(serviceId, templateId, {}, { publicKey: publicKey }).then
    (function(response) {
        console.log('Email sent successfully:', response);
    }, function(error) {
        console.error('Failed to send email:', error);
    });
}

export async function getMapData() {
    let toastId;
    const primaryUrl = `${process.env.REACT_APP_SERVER}/api/display/map`;
    const fallbackUrl = process.env.REACT_APP_FALLBACK_SERVER ? `${process.env.REACT_APP_FALLBACK_SERVER}/api/display/map` : null;
    const authToken = process.env.REACT_APP_API_AUTH_TOKEN;
    const headers = {
        'ngrok-skip-browser-warning': 'true',
        'Authorization': authToken
    };

    try {
        toastId = toast.loading("Loading...");
        try {
            const response = await axios.get(primaryUrl, { headers });
            console.log('Response from primary URL:', response);
            toast.dismiss(toastId);
            return JSON.stringify(response.data);
        } catch (error) {
            console.warn('Primary URL request failed:', error);
            sendMaintenanceEmail();
            if (!fallbackUrl) {
                throw error; // No fallback defined, rethrow
            }
            console.log("Attempting fallback URL...");
            const response = await axios.get(fallbackUrl, { headers });
            console.log('Response from fallback URL:', response);
            toast.dismiss(toastId);
            return JSON.stringify(response.data);
        }
    } catch (error) {
        if (toastId) toast.dismiss(toastId);
        console.error('Error fetching map data (all attempts failed): ', error);
        toast.error("Error fetching map data. Please try again later.");
        throw error;
    }
}

export async function getIncidents() {
    let toastId;
    const primaryUrl = `${process.env.REACT_APP_SERVER}/api/display/incidents`;
    const fallbackUrl = process.env.REACT_APP_FALLBACK_SERVER ? `${process.env.REACT_APP_FALLBACK_SERVER}/api/display/incidents` : null;
    const authToken = process.env.REACT_APP_API_AUTH_TOKEN;
    const headers = {
        'ngrok-skip-browser-warning': 'true',
        'Authorization': authToken
    };

    try {
        toastId = toast.loading("Loading...");
        try {
            const response = await axios.get(primaryUrl, { headers });
            console.log('Response from primary URL:', response);
            toast.dismiss(toastId);
            return JSON.stringify(response.data);
        } catch (error) {
            console.warn('Primary URL request failed:', error);
            sendMaintenanceEmail();
            if (!fallbackUrl) {
                throw error; // No fallback defined, rethrow
            }
            console.log("Attempting fallback URL...");
            const response = await axios.get(fallbackUrl, { headers });
            console.log('Response from fallback URL:', response);
            toast.dismiss(toastId);
            return JSON.stringify(response.data);
        }
    } catch (error) {
        if (toastId) toast.dismiss(toastId);
        console.error('Error fetching incidents data (all attempts failed): ', error);
        toast.error("Error fetching map data. Please try again later.");
        throw error;
    }
}

export async function getShortestPath(startLat, startLon, endLat, endLon, withIncidents) {
    let toastId;
    const primaryPath = `/api/shortest-path?startLat=${startLat}&startLon=${startLon}&endLat=${endLat}&endLon=${endLon}&includeIncidents=${withIncidents}`;
    const primaryUrl = `${process.env.REACT_APP_SERVER}${primaryPath}`;
    const fallbackUrl = process.env.REACT_APP_FALLBACK_SERVER ? `${process.env.REACT_APP_FALLBACK_SERVER}${primaryPath}` : null;
    const authToken = process.env.REACT_APP_API_AUTH_TOKEN;
    const headers = {
        'ngrok-skip-browser-warning': 'true',
        'Authorization': authToken
    };

    try {
        toastId = toast.loading("Loading...");
        try {
            const response = await axios.get(primaryUrl, { headers });
            console.log('Response from primary URL:', response);
            toast.dismiss(toastId);
            return JSON.stringify(response.data);
        } catch (error) {
            console.warn('Primary URL request failed:', error);
            if (!fallbackUrl) {
                throw error; // No fallback defined, rethrow
            }
            console.log("Attempting fallback URL...");
            const response = await axios.get(fallbackUrl, { headers });
            console.log('Response from fallback URL:', response);
            toast.dismiss(toastId);
            return JSON.stringify(response.data);
        }
    } catch (error) {
        if (toastId) toast.dismiss(toastId);
        console.error("Error fetching shortest path data (all attempts failed): ", error);
        toast.error("Error fetching map data. Please try again later.");
        throw error;
    }
}
