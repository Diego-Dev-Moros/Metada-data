import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import "leaflet/dist/leaflet.css";
import "./MapaHechos.css";
import { useNavigate } from "react-router-dom";
// import MarkerClusterGroup from 'react-leaflet-cluster'; // <--- 1. LO QUITAMOS (no querés los números)

// íconos de colores
const iconFactory = (color) =>
    L.divIcon({
        className: "pin",
        html: `<span class="dot ${color}"></span>`,
        iconSize: [20, 20],
        iconAnchor: [10, 20],
    });

const ICONS = {
    verde: iconFactory("verde"),
    naranja: iconFactory("naranja"),
    rojo: iconFactory("rojo"),
};

export default function MapaHechos({ filtros }) {
    const [hechos, setHechos] = useState([]);
    const [ultimaActualizacion, setUltimaActualizacion] = useState(null);
    const navigate = useNavigate();

    const API_URL = "http://localhost:8080/api/interna/hechos";

    const capitalizar = (texto) => {
        if (!texto) return "";
        return texto.charAt(0).toUpperCase() + texto.slice(1);
    };

    useEffect(() => {
        const fetchHechos = async () => {
            try {
                const res = await axios.get(API_URL, {
                    params: {
                        categoria: filtros.categoria || undefined,
                        desde: filtros.desde || undefined,
                        hasta: filtros.hasta || undefined,
                        ubicacion: filtros.ubicacion || undefined,
                        modo: filtros.modo || undefined,
                    },
                });
                setHechos(res.data || []);
                setUltimaActualizacion(new Date());
            } catch (e) {
                console.error("Error obteniendo hechos", e);
            }
        };

        fetchHechos();
        const id = setInterval(fetchHechos, 60_000);
        return () => clearInterval(id);
    }, [API_URL, filtros]);

    // Helper para normalizar coordenadas (porque a veces vienen sueltas o en objeto)
    const getCoords = (h) => {
        const rawLat = h.ubicacion?.latitud || h.latitud || h.lat || -34.6;
        const rawLng = h.ubicacion?.longitud || h.longitud || h.lng || -58.4;

        // Convertimos a número (el backend podría mandar strings)
        const lat = parseFloat(rawLat);
        const lng = parseFloat(rawLng);

        // Si no son números válidos, usamos el default PERO avisamos en consola
        if (isNaN(lat) || isNaN(lng)) {
            console.warn(`⚠️ El hecho "${h.titulo}" (ID: ${h.id}) no tiene coordenadas válidas. Usando default.`);
            return { lat: -34.6, lng: -58.4 };
        }

        return { lat, lng };
    };

    const hechosConIcono = useMemo(() => {
        return (hechos || []).map((h) => ({
            ...h,
            color: h.color || "verde",
        }));
    }, [hechos]);

    // 2. NUEVA LÓGICA: Agrupar hechos por coordenada exacta
    const gruposDeHechos = useMemo(() => {
        const grupos = {};

        hechosConIcono.forEach((h) => {
            const { lat, lng } = getCoords(h);
            // Creamos una clave única "lat,lng"
            const key = `${lat},${lng}`;

            if (!grupos[key]) {
                grupos[key] = [];
            }
            grupos[key].push(h);
        });

        // Convertimos el objeto en un array de arrays para poder mapearlo
        return Object.values(grupos);
    }, [hechosConIcono]);


    const formatHace = (date) => {
        if (!date) return "-";
        const diffMs = Date.now() - date.getTime();
        const diffMin = Math.floor(diffMs / 60000);
        if (diffMin < 1) return "hace unos segundos";
        if (diffMin === 1) return "hace 1 min";
        if (diffMin < 60) return `hace ${diffMin} min`;
        const diffHs = Math.floor(diffMin / 60);
        if (diffHs === 1) return "hace 1 hora";
        return `hace ${diffHs} horas`;
    };

    return (
        <div className="map-wrapper">
            <MapContainer
                center={[-34.6, -58.4]}
                zoom={5}
                style={{ height: "62.4vh", width: "100%" }}
            >
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; OpenStreetMap contributors'
                />

                {/* 4. Mapeamos los GRUPOS, no los hechos individuales */}
                {gruposDeHechos.map((grupo, index) => {
                    const hechoPrincipal = grupo[0]; // Usamos el primero para sacar pos y color
                    const { lat, lng } = getCoords(hechoPrincipal);

                    return (
                        <Marker
                            key={index} // La key es el índice del grupo
                            position={[lat, lng]}
                            icon={ICONS[hechoPrincipal.color] || ICONS.verde}
                        >
                            <Popup>
                                {/* 5. Dentro del Popup, recorremos TODOS los hechos de este punto */}
                                <div style={{ maxHeight: "250px", overflowY: "auto", paddingRight: "5px" }}>

                                    {grupo.map((h, i) => (
                                        <div key={h.id || i} style={{
                                            borderBottom: i < grupo.length - 1 ? "1px solid #ccc" : "none",
                                            marginBottom: "10px",
                                            paddingBottom: "10px"
                                        }}>
                                            <b>{capitalizar(h.titulo) || "Hecho"}</b>

                                            <div style={{ marginTop: 4 }}>
                                                {h.descripcion && <p style={{margin: "4px 0"}}>{h.descripcion}</p>}
                                                <p style={{fontSize: "0.9em", color: "#666"}}>
                                                    Categoría: {capitalizar(h.categoria)}
                                                </p>

                                                <div style={{display: "flex", gap: "5px", marginTop: "5px"}}>
                                                    <button className="btn-popup"
                                                            onClick={() => navigate(`/hechos/${h.id}`)}
                                                    >Detalle
                                                    </button>
                                                    <button className="btn-popup warn"
                                                            onClick={() => navigate('/solicitudes/nueva', {
                                                                state: {
                                                                    hechoId: h.id,
                                                                    tituloHecho: h.titulo
                                                                }
                                                            })}
                                                    >Reportar
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}

                                </div>
                            </Popup>
                        </Marker>
                    );
                })}

            </MapContainer>

            <div className="map-meta">
                <div>{hechos.length} hechos visibles</div>
                <div className="update">
                    Última actualización: {formatHace(ultimaActualizacion)}
                </div>
            </div>
        </div>
    );
}