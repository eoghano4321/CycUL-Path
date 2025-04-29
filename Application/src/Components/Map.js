import React, { useEffect, useRef, useState, useCallback } from 'react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import { getMapData, getShortestPath, getIncidents } from '../Client/DisplayClient';
import ToggleButtonControl from './ToggleButtonControl';
import MapboxGeocoder from '@mapbox/mapbox-gl-geocoder';
import '@mapbox/mapbox-gl-geocoder/dist/mapbox-gl-geocoder.css';
import SearchButton from './SearchButton';
import LogoBlue from '../Assets/LogoBlue.svg';
import Incident from '../Assets/Incident.svg';
import '../App.css';
import { ToastContainer, toast } from 'react-toastify';
import CancelButton from './CancelButton';
import incidentLayer from '../MapboxLayers/IncidentLayer';
import cycleLaneLayer from '../MapboxLayers/CycleLaneLayer';

mapboxgl.accessToken = process.env.REACT_APP_MAPBOX_TOKEN;

const MapboxMap = () => {
  const mapRef = useRef(null);
  const searchButton = useRef(null);
  const [incidentsVisible, setIncidentsVisible] = useState(true);
  const [startLocation, setStartLocation] = useState(null);
  const [destinationLocation, setDestinationLocation] = useState(null);
  const [routeDetails, setRouteDetails] = useState(null);
  const [incidentDescription, setIncidentDescription] = useState(null);
  const [incidentDate, setIncidentDate] = useState(null);
  const [pathData, setPathData] = useState(null);
  const startSearch = new MapboxGeocoder({
    accessToken: mapboxgl.accessToken,
    mapboxgl: mapboxgl,
    marker: false,
    placeholder: 'Start Location',
    countries: 'IE',
    marker: {
      color: 'red' 
    },
    proximity: {
      longitude: -6.2603,
      latitude: 53.3498,
    }
  })
  const destinationSearch = new MapboxGeocoder({
    accessToken: mapboxgl.accessToken,
    mapboxgl: mapboxgl,
    marker: {
    color: 'green'
    },
    placeholder: 'Destination',
    countries: 'IE',
    proximity: {
    longitude: -6.2603,
    latitude: 53.3498,
    }
  })

  const handleIncidentToggle = () => {
    setIncidentsVisible((prev) => {
      const newVisibility = !prev;
      const buttonElement = document.querySelector('.toggle-button');
      if (buttonElement) {
        buttonElement.style.backgroundColor = newVisibility ? 'rgba(19, 54, 110, 0.8)' : 'rgba(46, 51, 59, 0.8)'; 
      }
      return newVisibility;
    });
  };

  const handleSearch = async () => {
    if (startLocation && destinationLocation) {
      const path = await getShortestPath(startLocation[1], startLocation[0], destinationLocation[1], destinationLocation[0], incidentsVisible);
      const parsedPath = JSON.parse(path); // Parse the JSON
      console.log(parsedPath); // Log the parsed object
      // Check if the parsed path has the expected structure and set routeDetails to the properties
      if (parsedPath && parsedPath.features && parsedPath.features.length > 0 && parsedPath.features[0].properties) {
        setRouteDetails(parsedPath.features[0].properties);
      } else {
        console.error("Parsed path does not contain expected features structure:", parsedPath);
        toast.error("Received invalid route data from the server.");
        setRouteDetails(null); // Clear potentially stale details
      }
      setPathData(path); // Keep the original JSON string for the map layer
    } else {
      toast.error("Please select both start and destination locations.");
    }
  };

  const handleCancel = () => {
    setRouteDetails(null);
    setPathData(null);
    if (mapRef.current.getLayer('path-layer')) {
      mapRef.current.removeLayer('path-layer');
      mapRef.current.removeSource('path-source');
    }
    const markers = document.getElementsByClassName('mapboxgl-marker');
    while (markers.length > 0) {
      markers[0].remove();
    }
  };

  useEffect(() => {
    if (searchButton.current) {
      searchButton.current.updateOnClick(handleSearch);
    }
  }, [startLocation, destinationLocation, incidentsVisible]);

  useEffect(() => {
    const map = new mapboxgl.Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [-6.2603, 53.3498],
      zoom: 14,
    });
  
    mapRef.current = map;
    const controlContainer = document.createElement("div");
    controlContainer.style = "display: flex; flex-direction: row; gap: 5px; padding: 10px 10px 5px 0px;";

    const incidentButton = new ToggleButtonControl("Avoid Incidents", Incident, handleIncidentToggle);

    controlContainer.appendChild(incidentButton.onAdd(mapRef.current));

    const toggleWrapper = {
      onAdd() {
        return controlContainer;
      },
      onRemove() {
        controlContainer.parentNode.removeChild(controlContainer);
      },
    };

    mapRef.current.addControl(toggleWrapper, 'top-right');
    mapRef.current.addControl(new mapboxgl.NavigationControl(), 'top-right');
  
    const startGeocoderContainer = document.createElement('div');
    startGeocoderContainer.id = 'start-geocoder';
    startGeocoderContainer.className = 'start-geocoder';
    if(document.getElementById('start-geocoder')) {
      document.getElementById('start-geocoder').remove();
    }
    document.body.appendChild(startGeocoderContainer);

    // Add the second geocoder to the custom container
    startGeocoderContainer.appendChild(startSearch.onAdd(mapRef.current));

    startSearch.on('result', (e) => {
      const coordinates = e.result.geometry.coordinates;
      setStartLocation(coordinates);
      console.log(`Start: Longitude: ${coordinates[0]}, Latitude: ${coordinates[1]}`);
    });

    const destGeocoderContainer = document.createElement('div');
    destGeocoderContainer.id = 'destination-geocoder';
    destGeocoderContainer.className = 'destination-geocoder';
    if(document.getElementById('destination-geocoder')) {
      document.getElementById('destination-geocoder').remove();
    }
    document.body.appendChild(destGeocoderContainer);

    // Add the second geocoder to the custom container
    destGeocoderContainer.appendChild(destinationSearch.onAdd(mapRef.current));

    destinationSearch.on('result', (e) => {
      const coordinates = e.result.geometry.coordinates;
      setDestinationLocation(coordinates);
      console.log(`Destination: Longitude: ${coordinates[0]}, Latitude: ${coordinates[1]}`);
    });

    searchButton.current = new SearchButton("Directions");

    const cancelButton = CancelButton(handleCancel);

    const searchButtonContainer = document.createElement("div");
    searchButtonContainer.id = "search-button-container";
    searchButtonContainer.className = "search-button";
    searchButtonContainer.style = "align-content: center; display: flex;";

    if(document.getElementById('search-button-container')) {
      document.getElementById('search-button-container').remove();
    }
    document.body.appendChild(searchButtonContainer);
    document.getElementById('search-button-container').appendChild(searchButton.current.onAdd(mapRef.current));
    document.getElementById('search-button-container').appendChild(cancelButton);


    const logo = document.createElement("img");
    logo.id = "bottom-logo";
    logo.className = "bottom-logo";
    logo.src = LogoBlue;
    if(document.getElementById('bottom-logo')) {
      document.getElementById('bottom-logo').remove();
    }
    document.body.appendChild(logo);

    mapRef.current.on('load', async () => {
      try {
        const mapData = await getMapData();
        const incidentData = await getIncidents();

        const mapFeatureCollection = JSON.parse(mapData);
        const incidentFeatureCollection = JSON.parse(incidentData);       
  
        // Add the features source
        if (!mapRef.current.getSource('features')) {
          mapRef.current.addSource('features', {
            type: 'geojson',
            data: mapFeatureCollection,
          });
        }
  
        // Add cycle lanes layer
        if (!mapRef.current.getLayer('cyclelanes-layer')) {
          mapRef.current.addLayer(cycleLaneLayer());
        }

        if (!mapRef.current.getSource('incident-source')) {
          mapRef.current.addSource('incident-source', {
            type: 'geojson',
            data: incidentFeatureCollection,
          })
        }

        if (!mapRef.current.getLayer('incident-layer')){
          mapRef.current.addLayer(incidentLayer())
        }
  
        // Add the shortest path source
      } catch (error) {
        console.error('Error fetching map data:', error);
      }
    });
  
    return () => mapRef.current.remove();
  }, []);

  useEffect(() => {
    if (mapRef.current && mapRef.current.getLayer('incident-layer')) {
      mapRef.current.setLayoutProperty('incident-layer', 'visibility', incidentsVisible ? 'visible' : 'none');
    }
  }, [incidentsVisible]);
  
  useEffect(() => {
    console.log("Path data updated:", pathData);
    if (!pathData || !mapRef.current) return;
    
    if (mapRef.current.getSource('path-source')) {
      mapRef.current.getSource('path-source').setData(JSON.parse(pathData));
    } else {
      mapRef.current.addSource('path-source', {
        type: 'geojson',
        data: JSON.parse(pathData),
      });
    }
    if (mapRef.current.getLayer('path-layer')) {
      mapRef.current.setLayoutProperty('path-layer', 'visibility', 'visible');
    } else {
      mapRef.current.addLayer({
        id: 'path-layer',
        type: 'line',
        source: 'path-source',
        paint: {
          'line-color': 'rgb(19, 54, 110)',
          'line-width': 8,
          'line-join': 'round',
          'line-blur': 0.2,
          'line-cap': 'round',
          'line-opacity': 0.8,
        },
        layout: { visibility: 'visible' },
      });
    }
  }, [pathData]);

  useEffect(() => {
    if (!mapRef.current) return;
  
    // Add a click event listener for the incident-layer
    mapRef.current.on('click', 'incident-layer', (e) => {
      if (e.features && e.features.length > 0) {
        const incidentDescription = e.features[0].properties.Description;
        const rawIncidentDate = e.features[0].properties.OccurredAt;

        // Format the date
        const date = new Date(rawIncidentDate);
        const formattedDate = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1)
          .toString()
          .padStart(2, '0')}/${date.getFullYear()}`;

        setIncidentDescription(incidentDescription);
        setIncidentDate(formattedDate);
      }
    });
  
    // Change the cursor to a pointer when hovering over the incident-layer
    mapRef.current.on('mouseenter', 'incident-layer', () => {
      mapRef.current.getCanvas().style.cursor = 'pointer';
    });
  
    // Reset the cursor when leaving the incident-layer
    mapRef.current.on('mouseleave', 'incident-layer', () => {
      mapRef.current.getCanvas().style.cursor = '';
    });
  
    return () => {
      // Clean up event listeners when the component unmounts
      mapRef.current.off('click', 'incident-layer');
      mapRef.current.off('mouseenter', 'incident-layer');
      mapRef.current.off('mouseleave', 'incident-layer');
    };
  }, []);

  return (
    <div id="map-container" style={{ width: '100vw', height: '100vh', position: 'absolute' }}>
      <div id="map" style={{ height: '100%', width: '100vw', position: 'absolute' }} />
      {incidentDescription && (
        <div
          style={{
            position: 'absolute',
            bottom: '25px',
            right: '10px',
            backgroundColor: 'rgba(19, 54, 110, 0.8)',
            padding: '10px',
            borderRadius: '5px',
            boxShadow: '0 2px 5px rgba(0, 0, 0, 0.2)',
            zIndex: 1000,
            maxWidth: '300px', // Limit the width to 300px
            wordWrap: 'break-word',
          }}
          onClick={() => {
            setIncidentDescription(null)
            setIncidentDate(null)
          }}
        >
          <p style={{ margin: 0, fontSize: '14px', color: '#fff' }}>{incidentDescription}</p>
          <hr style={{ border: '1px solid #fff' }} />
          <p style={{ margin: 0, fontSize: '14px', color: '#fff' }}>{`Reported ${incidentDate}`}</p>
        </div>
      )}
      {routeDetails && (
        <div
          style={{
            position: 'absolute',
            bottom: '25px',
            right: '40%',
            left: '40%',
            backgroundColor: 'rgba(19, 54, 110, 0.8)',
            padding: '10px',
            borderRadius: '5px',
            boxShadow: '0 2px 5px rgba(0, 0, 0, 0.2)',
            zIndex: 1000,
            maxWidth: '300px', // Limit the width to 300px
            wordWrap: 'break-word',
          }}
          onClick={() => {
            setRouteDetails(null)
          }}
        >
          {(() => {
            console.log("Route details:", routeDetails);
            const travelTimeMinutes = parseFloat(routeDetails.travelTime, 10);
            const hours = Math.floor(travelTimeMinutes / 60);
            const minutes = Math.round(travelTimeMinutes % 60);
            return (
              <>
                <p style={{ margin: 0, fontSize: '14px', color: '#fff' }}>
                  {`Travel Time: ${hours > 0 ? `${hours} hr ` : ''}${minutes} min`}
                </p>
                <hr style={{ border: '1px solid #fff' }} />
                <p style={{ margin: 0, fontSize: '14px', color: '#fff' }}>
                  {`Historic Risk Score: ~${routeDetails.riskScore}%`}
                </p>
              </>
            );
          })()}
        </div>
      )}
      <ToastContainer 
        position="bottom-center" 
        autoClose={500} 
        closeButton={false} 
        theme='light'
      />
    </div>
  );
};

export default MapboxMap;