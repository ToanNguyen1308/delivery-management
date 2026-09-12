import { useEffect } from 'react';
import { MapContainer, Marker, Polyline, Popup, TileLayer, useMap } from 'react-leaflet';
import L, { LatLngExpression } from 'leaflet';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// Vite khong tu xu ly duong dan anh mac dinh cua Leaflet nen phai khai bao lai
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const createColoredIcon = (color: string) =>
  L.divIcon({
    className: 'custom-marker',
    html: `<div style="background:${color};width:18px;height:18px;border-radius:50%;border:3px solid #fff;box-shadow:0 0 6px rgba(0,0,0,.4)"></div>`,
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  });

export interface MapPoint {
  latitude: number;
  longitude: number;
  label: string;
  description?: string;
  color?: string;
}

interface DeliveryMapProps {
  points: MapPoint[];
  route?: { latitude: number; longitude: number }[];
  height?: number;
  /** Tu dong di chuyen ban do khi vi tri shipper thay doi. */
  followFirstPoint?: boolean;
}

const HANOI_CENTER: LatLngExpression = [21.0285, 105.8542];

const MapViewUpdater = ({ center, enabled }: { center?: LatLngExpression; enabled: boolean }) => {
  const map = useMap();
  useEffect(() => {
    if (enabled && center) {
      map.setView(center, map.getZoom());
    }
  }, [center, enabled, map]);
  return null;
};

const DeliveryMap = ({ points, route = [], height = 400, followFirstPoint = false }: DeliveryMapProps) => {
  const validPoints = points.filter((p) => p.latitude != null && p.longitude != null);
  const center: LatLngExpression = validPoints.length
    ? [validPoints[0].latitude, validPoints[0].longitude]
    : HANOI_CENTER;

  const routePositions: LatLngExpression[] = route
    .filter((p) => p.latitude != null && p.longitude != null)
    .map((p) => [p.latitude, p.longitude]);

  return (
    <div style={{ height }}>
      <MapContainer center={center} zoom={13} scrollWheelZoom style={{ height: '100%' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapViewUpdater center={center} enabled={followFirstPoint} />
        {routePositions.length > 1 && (
          <Polyline positions={routePositions} pathOptions={{ color: '#1677ff', weight: 4, opacity: 0.7 }} />
        )}
        {validPoints.map((point) => (
          <Marker
            key={`${point.label}-${point.latitude}-${point.longitude}`}
            position={[point.latitude, point.longitude]}
            icon={createColoredIcon(point.color ?? '#d32f2f')}
          >
            <Popup>
              <strong>{point.label}</strong>
              {point.description && (
                <>
                  <br />
                  {point.description}
                </>
              )}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default DeliveryMap;
