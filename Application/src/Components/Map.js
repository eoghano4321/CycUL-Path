import React from 'react';
import Radar from 'radar-sdk-js';

import 'radar-sdk-js/dist/radar.css';

class RadarMap extends React.Component {
  componentDidMount() {
    Radar.initialize('prj_test_pk_a38604f904532778b141e186b60184e3502a5e68');

    // create a map
    const map = Radar.ui.map({
      container: 'map',
      style: 'radar-default-v1',
      center: [-6.2603, 53.3498],
      zoom: 14,
    });

    // Radar.ui
  }

  render() {
    return (
      <div id="map-container" style={{ width:' 100%', height: '100vh', position: 'absolute' }}>
        <div id="map" style={{ height: '100%', position: 'absolute', width: '100%' }} />
      </div>
    );
  }
};

export default RadarMap;