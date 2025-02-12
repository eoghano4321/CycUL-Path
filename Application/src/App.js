import logo from './logo.svg';
import './App.css';
import RadarMap from './Components/Map';
import AddressToGeoJSON from './Components/LocationSearch';

function App() {
  return (
    <div className="App">
      <AddressToGeoJSON></AddressToGeoJSON>
      <RadarMap></RadarMap>
    </div>
  );
}

export default App;
