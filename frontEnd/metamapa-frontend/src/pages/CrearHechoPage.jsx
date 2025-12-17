import { useState } from "react";
import axios from "axios";
import { MapContainer, TileLayer, Marker, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import "./CrearHechoPage.css";
import { useNavigate } from "react-router-dom";

// Icono del marcador para el selector
const selectorIcon = new L.Icon({
    iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

// Componente para detectar clics en el mapa
function LocationPicker({ setLatitud, setLongitud }) {
    useMapEvents({
        click(e) {
            setLatitud(e.latlng.lat);
            setLongitud(e.latlng.lng);
        },
    });
    return null;
}

export default function CrearHechoPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false); // Estado para el Popup
    const [tagInput, setTagInput] = useState("");      // Estado temporal para el input de etiquetas

    const [formData, setFormData] = useState({
        titulo: "",
        descripcion: "",
        categoria: "",
        etiquetas: [], // <--- Nuevo array de etiquetas
        fecha: "",
        hora: "",
        latitud: null,
        longitud: null,
    });

    const [archivosMultimedia, setArchivosMultimedia] = useState([]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // --- LÓGICA DE ETIQUETAS ---
    const handleTagKeyDown = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault(); // Evita que se envíe el formulario
            agregarEtiqueta();
        }
    };

    const agregarEtiqueta = () => {
        const tag = tagInput.trim();
        if (tag && !formData.etiquetas.includes(tag)) {
            setFormData(prev => ({
                ...prev,
                etiquetas: [...prev.etiquetas, tag]
            }));
            setTagInput(""); // Limpiar input
        }
    };

    const removerEtiqueta = (tagToRemove) => {
        setFormData(prev => ({
            ...prev,
            etiquetas: prev.etiquetas.filter(t => t !== tagToRemove)
        }));
    };
    // ---------------------------

    const handleFileChange = (e) => {
        const files = Array.from(e.target.files);
        const validFiles = files.filter(file =>
            file.type.startsWith('image/') || file.type.startsWith('video/')
        );
        setArchivosMultimedia(prev => [...prev, ...validFiles]);
    };

    const removeFile = (index) => {
        setArchivosMultimedia(prev => prev.filter((_, i) => i !== index));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.latitud || !formData.longitud) {
            alert("⚠️ Por favor, hacé clic en el mapa para ubicar el hecho.");
            return;
        }

        setLoading(true);

        try {
            const dataToSend = new FormData();

            // Armamos el JSON con las etiquetas incluidas
            const hechoDTO = {
                titulo: formData.titulo,
                descripcion: formData.descripcion,
                categoria: formData.categoria,
                etiquetas: formData.etiquetas, // <--- Enviamos el array
                fechaHecho: `${formData.fecha}T${formData.hora || "00:00"}:00`,
                latitud: formData.latitud,
                longitud: formData.longitud
            };

            dataToSend.append("hecho", JSON.stringify(hechoDTO));

            archivosMultimedia.forEach((file) => {
                dataToSend.append("archivos", file);
            });

            const contribuyenteId = localStorage.getItem("usuarioId") || "1";

            await axios.post("http://localhost:8080/api/interna/hechos", dataToSend, {
                headers: {
                    "Content-Type": "multipart/form-data",
                    "X-Contribuyente-Id": contribuyenteId
                },
            });

            // EN LUGAR DE ALERT, MOSTRAMOS EL MODAL
            setShowModal(true);

        } catch (err) {
            console.error("Error al publicar:", err);
            alert("❌ Error al crear el hecho. Revisá la consola.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="crear-hecho-container">
            <h2>Reportar Nuevo Hecho</h2>
            <form className="crear-hecho-form" onSubmit={handleSubmit}>

                {/* Título y Descripción (Igual que antes) */}
                <label>Título</label>
                <input
                    type="text"
                    name="titulo"
                    placeholder="Ej: Árbol caído en Av. Siempreviva"
                    value={formData.titulo}
                    onChange={handleChange}
                    required
                />

                <label>Descripción</label>
                <textarea
                    name="descripcion"
                    placeholder="Describí qué pasó..."
                    value={formData.descripcion}
                    onChange={handleChange}
                    required
                />

                {/* Fila de Categoría y Fechas */}
                <div className="form-row">
                    <div>
                        <label>Categoría</label>
                        <input
                            type="text"
                            name="categoria"
                            list="lista-categorias"
                            placeholder="Seleccioná..."
                            value={formData.categoria}
                            onChange={handleChange}
                            required
                            autoComplete="off"
                        />
                        <datalist id="lista-categorias">
                            <option value="Incendio" />
                            <option value="Inundación" />
                            <option value="Accidente" />
                            <option value="Protesta" />
                            <option value="Inseguridad" />
                        </datalist>
                    </div>
                    <div>
                        <label>Fecha</label>
                        <input type="date" name="fecha" value={formData.fecha} onChange={handleChange} required />
                    </div>
                    <div>
                        <label>Hora</label>
                        <input type="time" name="hora" value={formData.hora} onChange={handleChange} required />
                    </div>
                </div>

                {/* --- SECCIÓN NUEVA: ETIQUETAS --- */}
                <label>Etiquetas (Opcional)</label>
                <div className="tags-input-container">
                    <input
                        type="text"
                        placeholder="Escribí una etiqueta y apretá Enter (ej: #urgente)"
                        value={tagInput}
                        onChange={(e) => setTagInput(e.target.value)}
                        onKeyDown={handleTagKeyDown}
                    />
                    <button type="button" onClick={agregarEtiqueta} className="btn-add-tag">+</button>
                </div>

                {/* Visualización de Chips */}
                <div className="tags-list">
                    {formData.etiquetas.map((tag, index) => (
                        <span key={index} className="tag-chip">
                            {tag}
                            <button type="button" onClick={() => removerEtiqueta(tag)}>×</button>
                        </span>
                    ))}
                </div>
                {/* ------------------------------- */}

                <label>Ubicación</label>
                <div className="map-container" style={{border: "2px solid #ddd", borderRadius: "8px", overflow: "hidden"}}>
                    <MapContainer
                        center={[-34.6037, -58.3816]}
                        zoom={12}
                        style={{ height: "300px", width: "100%" }}
                    >
                        <TileLayer
                            attribution='&copy; OpenStreetMap contributors'
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />
                        <LocationPicker
                            setLatitud={(lat) => setFormData(prev => ({ ...prev, latitud: lat }))}
                            setLongitud={(lng) => setFormData(prev => ({ ...prev, longitud: lng }))}
                        />
                        {formData.latitud && formData.longitud && (
                            <Marker position={[formData.latitud, formData.longitud]} icon={selectorIcon} />
                        )}
                    </MapContainer>
                </div>
                <div style={{ marginTop: "10px", fontSize: "0.9rem", color: "#555" }}>
                    {formData.latitud && formData.longitud ? (
                        <p>
                            <strong>📍 Coordenadas seleccionadas:</strong> <br/>
                            Lat: {formData.latitud.toFixed(6)}, Long: {formData.longitud.toFixed(6)}
                        </p>
                    ) : (
                        <p>👆 <em>Hacé clic en el mapa para marcar la ubicación exacta.</em></p>
                    )}
                </div>

                <label style={{marginTop: "15px"}}>Fotos o Videos</label>
                <input
                    type="file"
                    accept="image/*,video/*"
                    multiple
                    onChange={handleFileChange}
                    className="file-input"
                />

                {archivosMultimedia.length > 0 && (
                    <div className="archivos-preview">
                        {archivosMultimedia.map((file, index) => (
                            <div key={index} className="archivo-item">
                                <span className="archivo-nombre">{file.name}</span>
                                <button type="button" className="remove-file-btn" onClick={() => removeFile(index)}>✕</button>
                            </div>
                        ))}
                    </div>
                )}

                <div className="actions" style={{marginTop: "20px", display: "flex", gap: "10px"}}>
                    <button type="button" className="reset-btn" onClick={() => navigate(-1)}>Cancelar</button>
                    <button type="submit" className="publicar-btn" disabled={loading}>
                        {loading ? "Enviando..." : "Publicar Hecho"}
                    </button>
                </div>
            </form>

            {/* --- MODAL (POPUP) --- */}
            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="modal-icon">✅</div>
                        <h3>¡Hecho Enviado!</h3>
                        <p>Tu reporte ha sido registrado con éxito y está pendiente de validación.</p>
                        <button className="modal-btn" onClick={() => navigate("/")}>
                            Volver al Mapa
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}