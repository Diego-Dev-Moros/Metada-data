import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./ColeccionPage.css";

const ColeccionPage = () => {
    const navigate = useNavigate();
    const rol = localStorage.getItem("rol");

    // 🔹 Estado del modal de edición
    const [showModal, setShowModal] = useState(false);
    const [editAlgoritmo, setEditAlgoritmo] = useState("");
    const [editFuentes, setEditFuentes] = useState({
        estatica: false,
        dinamica: false,
        proxy: false,
    });

    // 🔹 hechos de ejemplo (esto después lo traés del back por id de colección)
    const [hechos] = useState([
        {
            id: 1,
            titulo: "Título del hecho 1",
            fechaSuceso: "2025-02-20",
            fechaCarga: "2025-02-21",
            ubicacion: "Buenos Aires",
            descripcion: "Descripción del hecho 1",
            imagen: "/uploads/multimedia/imagen1.jpg",
        },
        {
            id: 2,
            titulo: "Título del hecho 2",
            fechaSuceso: "2025-02-21",
            fechaCarga: "2025-02-22",
            ubicacion: "Córdoba",
            descripcion: "Descripción del hecho 2",
            imagen: "", // este no tiene multimedia
        },
        {
            id: 3,
            titulo: "Título del hecho 3",
            fechaSuceso: "2025-02-23",
            fechaCarga: "2025-02-24",
            ubicacion: "Rosario",
            descripcion: "Descripción del hecho 3",
            imagen: "/uploads/multimedia/imagen3.jpg",
        },
    ]);

    // 🔹 handler para ir a la página de solicitud de eliminación
    const irASolicitud = (hechoId) => {
        navigate("/solicitudes/nueva", {
            state: { hechoId }, // se lo mandamos a la otra página
        });
    };

    // 🔹 handlers para el modal de edición
    const abrirModal = () => {
        setShowModal(true);
    };

    const cerrarModal = () => {
        setShowModal(false);
    };

    const handleFuenteChange = (name) => {
        setEditFuentes((prev) => ({
            ...prev,
            [name]: !prev[name],
        }));
    };

    const guardarCambios = async () => {
        // Construir array de fuentes seleccionadas
        const fuentesSeleccionadas = Object.entries(editFuentes)
            .filter(([, v]) => v)
            .map(([k]) => k.toUpperCase()); // ["ESTATICA", "DINAMICA", "PROXY"]

        const payload = {
            algoritmoConsenso: editAlgoritmo,
            fuenteIds: fuentesSeleccionadas,
        };
        
        console.log("Guardar cambios:", payload);
        // TODO: PUT a /api/admin/colecciones/{id}
        // await fetch(`/api/admin/colecciones/{id}`, {
        //     method: 'PUT',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(payload)
        // });
        alert("Cambios guardados (simulado)");
        setShowModal(false);
    };

    return (
        <div className="coleccion-page">
            {/* PANEL IZQUIERDO */}
            <aside className="coleccion-sidebar">
                <div className="coleccion-titulo">
                    {rol === "administrador" && (
                        <button className="btn-editar-coleccion" onClick={abrirModal}>
                            ✏️ Editar
                        </button>
                    )}
                    <h1>Nombre coleccion</h1>
                    <p>Descripcion de la coleccion</p>
                </div>

                {/* Buscador */}
                <div className="search-container-sidebar">
                    <input
                        type="text"
                        placeholder="Buscar"
                        className="search-input-sidebar"
                    />
                    <button className="search-button-sidebar">🔍</button>
                </div>

                <div className="coleccion-filtros">
                    <h3>Filtros</h3>

                <label className="label">Categoría</label>
                <select>
                    <option value="">Todas</option>
                    <option value="categoria1">Categoría 1</option>
                    <option value="categoria2">Categoría 2</option>
                </select>

                <label className="label">Rango Fecha</label>
                <div className="fecha-rango">
                    <div className="fecha-field">
                        <span className="mini-label">Desde</span>
                        <input type="date" />
                    </div>
                    <div className="fecha-field">
                        <span className="mini-label">Hasta</span>
                        <input type="date" />
                    </div>
                </div>

                <label className="label">Ubicación</label>
                <input type="text" placeholder="Ej: Buenos Aires" />

                <div className="modo-nav">
                    <label>
                        <input type="radio" name="modo" /> Irrestricta
                    </label>
                    <label>
                        <input type="radio" name="modo" /> Curada
                    </label>
                </div>

                    <button className="btn-aplicar">Aplicar filtros</button>
                </div>
            </aside>

            {/* CONTENIDO DERECHO */}
            <main className="coleccion-contenido">
                <div className="hechos-container">
                    {hechos.map((h) => (
                        <article 
                            key={h.id} 
                            className="hecho-card"
                            onClick={() => navigate(`/hechos/${h.id}`)}
                            style={{ cursor: 'pointer' }}
                        >
                            <div className="hecho-texto">
                                <h2>Titulo del hecho</h2>
                                <p className="hecho-descripcion">Lorem ipsum dolor sit amet et delectus</p>
                                
                                <div className="hecho-meta-grid">
                                    <div><strong>Categoria:</strong> 560</div>
                                    <div><strong>Fecha suceso:</strong> 560</div>
                                    <div><strong>Fecha ingreso:</strong> 3/12/25</div>
                                    <div><strong>Ubicacion:</strong> Buenos Aires</div>
                                </div>
                            </div>

                            {/* bloque multimedia */}
                            <div className="hecho-media">
                                {h.imagen ? (
                                    <img src={h.imagen} alt={h.titulo} className="hecho-img" />
                                ) : (
                                    <div className="img-placeholder"></div>
                                )}
                            </div>
                        </article>
                    ))}

                    {hechos.length === 0 && (
                        <p>No hay hechos para esta colección.</p>
                    )}
                </div>
            </main>

            {/* MODAL DE EDICIÓN */}
            {showModal && (
                <div className="modal-overlay" onClick={cerrarModal}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h2>Editar Colección</h2>
                        
                        <div className="modal-form">
                            <div className="form-group">
                                <label>Algoritmo de Consenso</label>
                                <select
                                    value={editAlgoritmo}
                                    onChange={(e) => setEditAlgoritmo(e.target.value)}
                                >
                                    <option value="">Sin algoritmo (Por defecto)</option>
                                    <option value="ABSOLUTA">Absoluta</option>
                                    <option value="MAYORIA_SIMPLE">Mayoría simple</option>
                                    <option value="MULTIPLES_MENCIONES">Múltiples menciones</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Fuentes</label>
                                <div className="fuentes-group">
                                    <label>
                                        <input
                                            type="checkbox"
                                            checked={editFuentes.estatica}
                                            onChange={() => handleFuenteChange("estatica")}
                                        />
                                        ESTÁTICA
                                    </label>
                                    <label>
                                        <input
                                            type="checkbox"
                                            checked={editFuentes.dinamica}
                                            onChange={() => handleFuenteChange("dinamica")}
                                        />
                                        DINÁMICA
                                    </label>
                                    <label>
                                        <input
                                            type="checkbox"
                                            checked={editFuentes.proxy}
                                            onChange={() => handleFuenteChange("proxy")}
                                        />
                                        PROXY
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div className="modal-actions">
                            <button className="btn-cancelar" onClick={cerrarModal}>
                                Cancelar
                            </button>
                            <button className="btn-guardar" onClick={guardarCambios}>
                                Guardar cambios
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ColeccionPage;
