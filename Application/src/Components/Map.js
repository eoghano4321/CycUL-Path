import React, { useEffect, useRef, useState, useCallback } from 'react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import { getMapData, getShortestPath, getIncidents } from '../Client/DisplayClient';
import ToggleButtonControl from './ToggleButtonControl';
import MapboxGeocoder from '@mapbox/mapbox-gl-geocoder';
import '@mapbox/mapbox-gl-geocoder/dist/mapbox-gl-geocoder.css';
import SearchButton from './SearchButton';
import LogoBlue from '../Assets/LogoBlue.svg';
import '../App.css';
import { ToastContainer, toast } from 'react-toastify';

mapboxgl.accessToken = 'pk.eyJ1IjoiZW9naGFub20iLCJhIjoiY204MGMxZTl0MHR1dTJsc2ZhYm01dW9pZyJ9.WJcOQwwDoWZiej24U6o3vA';

const MapboxMap = () => {
  const mapRef = useRef(null);
  const searchButton = useRef(null);
  const [cycleLaneVisible, setCycleLaneVisible] = useState(true);
  const [incidentsVisible, setIncidentsVisible] = useState(true);
  const [startLocation, setStartLocation] = useState(null);
  const [destinationLocation, setDestinationLocation] = useState(null);
  const [pathData, setPathData] = useState(null);

  const handleCycleLaneToggle = () => {
    setCycleLaneVisible((prev) => !prev);
  };

  const handleIncidentToggle = () => {
    setIncidentsVisible((prev) => !prev);
  };

  const handleSearch = async () => {
    if (startLocation && destinationLocation) {
      const path = await getShortestPath(startLocation[1], startLocation[0], destinationLocation[1], destinationLocation[0]);
      setPathData(path);
    } else {
      toast.error("Please select both start and destination locations.");
    }
  };

  useEffect(() => {
    if (searchButton.current) {
      searchButton.current.updateOnClick(handleSearch);
    }
  }, [startLocation, destinationLocation]);

  useEffect(() => {
    const map = new mapboxgl.Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [-6.2603, 53.3498],
      zoom: 14,
    });
  
    mapRef.current = map;
    const controlContainer = document.createElement("div");
    
    controlContainer.style.display = "flex";
    controlContainer.style.flexDirection = "row";
    controlContainer.style.gap = "5px";
    controlContainer.style.padding = "10px 10px 5px 0px ";

    const cycleLaneButton = new ToggleButtonControl("Toggle Cycle Lanes", handleCycleLaneToggle);
    const incidentButton = new ToggleButtonControl("Toggle Incidents", handleIncidentToggle);

    controlContainer.appendChild(cycleLaneButton.onAdd(mapRef.current));
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
  
    const startSearch = new MapboxGeocoder({
      accessToken: mapboxgl.accessToken,
      mapboxgl: mapboxgl,
      marker: false,
      placeholder: 'Start Location',
      countries: 'IE',
      marker: {
        color: 'red' 
        },
      flyTo: false,
      proximity: {
        longitude: -6.2603,
        latitude: 53.3498,}
    })
    mapRef.current.addControl(
      startSearch, 'top-left'
    );

    startSearch.on('result', (e) => {
      const coordinates = e.result.geometry.coordinates;
      setStartLocation(coordinates);
      console.log(`Start: Longitude: ${coordinates[0]}, Latitude: ${coordinates[1]}`);
    });

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

    const geocoderContainer = document.createElement('div');
    geocoderContainer.id = 'destination-geocoder';
    geocoderContainer.className = 'geocoder';
    if(document.getElementById('destination-geocoder')) {
      document.getElementById('destination-geocoder').remove();
    }
    document.body.appendChild(geocoderContainer);

    // Add the second geocoder to the custom container
    document.getElementById('destination-geocoder').appendChild(destinationSearch.onAdd(mapRef.current));

    destinationSearch.on('result', (e) => {
      const coordinates = e.result.geometry.coordinates;
      setDestinationLocation(coordinates);
      console.log(`Destination: Longitude: ${coordinates[0]}, Latitude: ${coordinates[1]}`);
    });

    searchButton.current = new SearchButton("Search");

    const searchButtonContainer = document.createElement("div");
    searchButtonContainer.id = "search-button-container";
    searchButtonContainer.className = "search-button";
    if(document.getElementById('search-button-container')) {
      document.getElementById('search-button-container').remove();
    }
    document.body.appendChild(searchButtonContainer);
    document.getElementById('search-button-container').appendChild(searchButton.current.onAdd(mapRef.current));


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
          mapRef.current.addLayer({
            id: 'cyclelanes-layer',
            type: 'line',
            minzoom: 9,
            source: 'features',
            paint: {
              'line-color': [
                'match',
                ['get', 'surface'],
                'asphalt', '#000000', 
                'concrete', '#808080',
                'wood', '#A52A2A',
                'compacted', '#552525',
                'paved', '#0000DD',
                'unpaved', '#FF0000',
                'paving_stones', '#303030',
                'grass', '#00EE00',
                /* other */ '#AAAAAA'
              ],
              'line-width': 2,
              'line-opacity': 0.7,
            },
            layout: { visibility: cycleLaneVisible ? 'visible' : 'none' },
          });
        }

        if (!mapRef.current.getSource('incident-source')) {
          mapRef.current.addSource('incident-source', {
            type: 'geojson',
            data: incidentFeatureCollection,
          })
        }

        if (!mapRef.current.getLayer('incident-layer')){
          mapRef.current.addLayer({
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
              "circle-opacity": 0.8
            },
            layout: { visibility: incidentsVisible ? 'visible' : 'none' },
          })
        }
  
        // Add the shortest path source
      } catch (error) {
        console.error('Error fetching map data:', error);
      }
    });
  
    return () => mapRef.current.remove();
  }, []);

  useEffect(() => {
    if (mapRef.current && mapRef.current.getLayer('cyclelanes-layer')) {
      mapRef.current.setLayoutProperty('cyclelanes-layer', 'visibility', cycleLaneVisible ? 'visible' : 'none');
    }
  }, [cycleLaneVisible]);

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
          'line-color': '#FF0000',
          'line-width': 8,
          'line-join': 'round',
          'line-blur': 0.2,
          'line-cap': 'round'
        },
        layout: { visibility: 'visible' },
      });
    }
  }, [pathData]);
  

  return (
    <div id="map-container" style={{ width: '100%', height: '100vh', position: 'absolute' }}>
      <div id="map" style={{ height: '100%', position: 'absolute', width: '100%' }} />
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