import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./ReporteHechoPage.css";

const ReporteHechoPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [reporte, setReporte] = useState(null);
    const [loading, setLoading] = useState(true);
    
    // Modales
    const [modalAprobar, setModalAprobar] = useState({ open: false, etiquetas: "" });
    const [modalSugerencias, setModalSugerencias] = useState({ open: false, sugerencias: "" });
    const [modalRechazar, setModalRechazar] = useState({ open: false, motivo: "" });

    useEffect(() => {
        // TODO: Cargar el reporte desde el backend usando el id
        // Ejemplo de datos mock
        const reporteMock = {
            id: id,
            titulo: "Nuevo hecho reportado por usuario",
            fechaSuceso: "2025-03-10",
            fechaCarga: "2025-03-11",
            ubicacion: "Buenos Aires",
            descripcion: "Descripción detallada del hecho reportado que está pendiente de aprobación.",
            imagen: "/uploads/multimedia/imagen1.jpg",
            usuarioCreador: "usuario1@email.com",
            estado: "Pendiente",
            categoria: "Histórico",
        };
        
        setReporte(reporteMock);
        setLoading(false);
    }, [id]);

    const handleAprobar = () => {
        setModalAprobar({ open: true, etiquetas: "" });
    };

    const handleAprobarConSugerencias = () => {
        setModalSugerencias({ open: true, sugerencias: "" });
    };

    const handleRechazar = () => {
        setModalRechazar({ open: true, motivo: "" });
    };

    const confirmarAprobar = async () => {
        // TODO: Llamar al backend para aprobar el hecho con etiquetas
        console.log("Aprobar hecho", id, "con etiquetas:", modalAprobar.etiquetas);
        alert("Hecho aprobado exitosamente");
        setModalAprobar({ open: false, etiquetas: "" });
        navigate("/reportes-hechos");
    };

    const confirmarSugerencias = async () => {
        // TODO: Llamar al backend para aprobar con sugerencias
        console.log("Aprobar con sugerencias hecho", id, ":", modalSugerencias.sugerencias);
        alert("Hecho devuelto con sugerencias al contribuyente");
        setModalSugerencias({ open: false, sugerencias: "" });
        navigate("/reportes-hechos");
    };

    const confirmarRechazar = async () => {
        // TODO: Llamar al backend para rechazar el hecho
        console.log("Rechazar hecho", id, "con motivo:", modalRechazar.motivo);
        alert("Hecho rechazado");
        setModalRechazar({ open: false, motivo: "" });
        navigate("/reportes-hechos");
    };

    if (loading) {
        return <div className="reporte-detalle-page">Cargando...</div>;
    }

    if (!reporte) {
        return <div className="reporte-detalle-page">Reporte no encontrado</div>;
    }

    return (
        <div className="reporte-detalle-page">
            <div className="reporte-detalle-container">
                <button onClick={() => navigate(-1)} className="btn-volver">
                    ← Volver
                </button>
                
                <div className="reporte-header">
                    <h1>{reporte.titulo}</h1>
                    <span className={`estado ${reporte.estado.toLowerCase().replace(' ', '-')}`}>
                        {reporte.estado}
                    </span>
                </div>
                
                {reporte.imagen && (
                    <div className="reporte-imagen">
                        <img src={reporte.imagen} alt={reporte.titulo} />
                    </div>
                )}
                
                <div className="reporte-info">
                    <div className="info-row">
                        <span className="label">Fecha del suceso:</span>
                        <span>{reporte.fechaSuceso}</span>
                    </div>
                    <div className="info-row">
                        <span className="label">Fecha de carga:</span>
                        <span>{reporte.fechaCarga}</span>
                    </div>
                    <div className="info-row">
                        <span className="label">Ubicación:</span>
                        <span>{reporte.ubicacion}</span>
                    </div>
                    <div className="info-row">
                        <span className="label">Categoría:</span>
                        <span>{reporte.categoria}</span>
                    </div>
                    <div className="info-row">
                        <span className="label">Creado por:</span>
                        <span>{reporte.usuarioCreador}</span>
                    </div>
                </div>
                
                <div className="reporte-descripcion">
                    <h2>Descripción</h2>
                    <p>{reporte.descripcion}</p>
                </div>
                
                <div className="reporte-acciones">
                    <button onClick={handleAprobar} className="btn-aprobar">
                        ✓ Aceptar
                    </button>
                    <button onClick={handleAprobarConSugerencias} className="btn-sugerencias">
                        ✏ Aceptar con sugerencias
                    </button>
                    <button onClick={handleRechazar} className="btn-rechazar">
                        ✗ Rechazar
                    </button>
                </div>

                {/* Modal Aceptar con Etiquetas */}
                {modalAprobar.open && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <h2>Aceptar Hecho</h2>
                            <p>Agrega etiquetas para categorizar este hecho (ej: #robo #violencia)</p>
                            <input
                                type="text"
                                placeholder="#etiqueta1 #etiqueta2"
                                value={modalAprobar.etiquetas}
                                onChange={(e) => setModalAprobar({ ...modalAprobar, etiquetas: e.target.value })}
                                className="modal-input"
                            />
                            <div className="modal-acciones">
                                <button onClick={confirmarAprobar} className="btn-confirmar">
                                    Aceptar Hecho
                                </button>
                                <button 
                                    onClick={() => setModalAprobar({ open: false, etiquetas: "" })}
                                    className="btn-cancelar"
                                >
                                    Cancelar
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Modal Aceptar con Sugerencias */}
                {modalSugerencias.open && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <h2>Aceptar con Sugerencias</h2>
                            <p>El hecho volverá al contribuyente con tus sugerencias para que lo corrija</p>
                            <textarea
                                placeholder="Escribe tus sugerencias para el contribuyente..."
                                value={modalSugerencias.sugerencias}
                                onChange={(e) => setModalSugerencias({ ...modalSugerencias, sugerencias: e.target.value })}
                                className="modal-textarea"
                                rows="4"
                            />
                            <div className="modal-acciones">
                                <button onClick={confirmarSugerencias} className="btn-confirmar">
                                    Enviar Sugerencias
                                </button>
                                <button 
                                    onClick={() => setModalSugerencias({ open: false, sugerencias: "" })}
                                    className="btn-cancelar"
                                >
                                    Cancelar
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Modal Rechazar */}
                {modalRechazar.open && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <h2>Rechazar Hecho</h2>
                            <p>Indica el motivo por el cual se rechaza este hecho</p>
                            <textarea
                                placeholder="Motivo del rechazo..."
                                value={modalRechazar.motivo}
                                onChange={(e) => setModalRechazar({ ...modalRechazar, motivo: e.target.value })}
                                className="modal-textarea"
                                rows="4"
                            />
                            <div className="modal-acciones">
                                <button onClick={confirmarRechazar} className="btn-confirmar btn-confirmar-rechazar">
                                    Rechazar Hecho
                                </button>
                                <button 
                                    onClick={() => setModalRechazar({ open: false, motivo: "" })}
                                    className="btn-cancelar"
                                >
                                    Cancelar
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ReporteHechoPage;
