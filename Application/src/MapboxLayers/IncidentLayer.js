export default function incidentLayer() {
    const incidentLayer = {
        id: 'incident-layer',
        type: 'circle',
        source: 'incident-source',
        minzoom: 11,
        paint: {
            "circle-radius": 6, // Adjust as needed
            "circle-color": [
            "interpolate",
            ["linear"],
            ["get", "Severity"], // Get the severity property
            0,
            'rgb(255, 247, 136)',
            1,
            'rgb(178,24,43)'
            ],
            "circle-opacity": 0.85
        },
        layout: { visibility: 'visible'},
    };

    return incidentLayer;
}