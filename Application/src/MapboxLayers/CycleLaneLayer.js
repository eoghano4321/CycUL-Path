export default function cycleLaneLayer() {
    const cycleLaneLayer = {
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
            'line-opacity': 0.4,
        },
        layout: { visibility: 'visible' },
    };

    return cycleLaneLayer;
}