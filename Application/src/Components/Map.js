import React, { useEffect } from 'react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import { getMapData } from '../Client/DisplayClient';

mapboxgl.accessToken = 'pk.eyJ1IjoiZW9naGFub20iLCJhIjoiY204MGMxZTl0MHR1dTJsc2ZhYm01dW9pZyJ9.WJcOQwwDoWZiej24U6o3vA';

const MapboxMap = () => {
  useEffect(() => {
    const map = new mapboxgl.Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [-6.2603, 53.3498],
      zoom: 14,
    });

    getMapData()
      .then((data) => {
        const featureCollection = JSON.parse(data);
        map.on('load', () => {
          map.addSource('features', {
            type: 'geojson',
            data: featureCollection,
          });

          map.addLayer({
            id: 'features-layer',
            type: 'line',
            source: 'features',
            paint: {
              "fill-color": "F7D5CD"
            }
          });
        });
      })
      .catch((error) => {
        console.error('Error fetching map data:', error);
      });

    return () => map.remove();
  }, []);

  return (
    <div id="map-container" style={{ width: '100%', height: '100vh', position: 'absolute' }}>
      <div id="map" style={{ height: '100%', position: 'absolute', width: '100%' }} />
    </div>
  );
};

export default MapboxMap;