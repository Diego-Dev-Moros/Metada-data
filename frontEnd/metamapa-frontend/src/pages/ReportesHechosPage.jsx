import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./ReportesHechosPage.css";

const ReportesHechosPage = () => {
    const navigate = useNavigate();
    const [reportes, setReportes] = useState([]);
    const [loading, setLoading] = useState(true);
    
    // Modales
    const [modalAprobar, setModalAprobar] = useState({ open: false, hechoId: null, etiquetas: "" });
    const [modalSugerencias, setModalSugerencias] = useState({ open: false, hechoId: null, sugerencias: "" });
    const [modalRechazar, setModalRechazar] = useState({ open: false, hechoId: null, motivo: "" });

    useEffect(() => {
        // TODO: Cargar reportes de hechos pendientes desde el backend
        // Ejemplo de datos mock
        const reportesMock = [
            {
                id: 1,
                titulo: "Nuevo hecho reportado por usuario",
                fechaSuceso: "2025-03-10",
                ubicacion: "Buenos Aires",
                usuarioCreador: "usuario1@email.com",
                fechaCreacion: "2025-03-11",
                estado: "Pendiente",
            },
            {
                id: 2,
                titulo: "Otro hecho pendiente de revisión",
                fechaSuceso: "2025-03-12",
                ubicacion: "Córdoba",
                usuarioCreador: "usuario2@email.com",
                fechaCreacion: "2025-03-13",
                estado: "En revisión",
            },
            {
                id: 3,
                titulo: "Hecho histórico aportado",
                fechaSuceso: "2024-12-05",
                ubicacion: "Rosario",
                usuarioCreador: "usuario3@email.com",
                fechaCreacion: "2025-03-14",
                estado: "Pendiente",
            },
        ];
        
        setReportes(reportesMock);
        setLoading(false);
    }, []);

    const handleVerDetalle = (id) => {
        navigate(`/reportes-hechos/${id}`);
    };

    const handleAprobar = (id) => {
        setModalAprobar({ open: true, hechoId: id, etiquetas: "" });
    };

    const handleAprobarConSugerencias = (id) => {
        setModalSugerencias({ open: true, hechoId: id, sugerencias: "" });
    };

    const handleRechazar = (id) => {
        setModalRechazar({ open: true, hechoId: id, motivo: "" });
    };

    const confirmarAprobar = async () => {
        // TODO: Llamar al backend para aprobar el hecho con etiquetas
        console.log("Aprobar hecho", modalAprobar.hechoId, "con etiquetas:", modalAprobar.etiquetas);
        // await axios.post(`/api/hechos/${modalAprobar.hechoId}/aprobar`, { etiquetas: modalAprobar.etiquetas });
        alert("Hecho aprobado exitosamente");
        setModalAprobar({ open: false, hechoId: null, etiquetas: "" });
        // Recargar reportes
    };

    const confirmarSugerencias = async () => {
        // TODO: Llamar al backend para aprobar con sugerencias
        console.log("Aprobar con sugerencias hecho", modalSugerencias.hechoId, ":", modalSugerencias.sugerencias);
        // await axios.post(`/api/hechos/${modalSugerencias.hechoId}/sugerencias`, { sugerencias: modalSugerencias.sugerencias });
        alert("Hecho devuelto con sugerencias al contribuyente");
        setModalSugerencias({ open: false, hechoId: null, sugerencias: "" });
        // Recargar reportes
    };

    const confirmarRechazar = async () => {
        // TODO: Llamar al backend para rechazar el hecho
        console.log("Rechazar hecho", modalRechazar.hechoId, "con motivo:", modalRechazar.motivo);
        // await axios.post(`/api/hechos/${modalRechazar.hechoId}/rechazar`, { motivo: modalRechazar.motivo });
        alert("Hecho rechazado");
        setModalRechazar({ open: false, hechoId: null, motivo: "" });
        // Recargar reportes
    };

    if (loading) {
        return <div className="reportes-page">Cargando...</div>;
    }

    return (
        <div className="reportes-page">
            <div className="reportes-container">
                <h1>Reportes de Hechos Pendientes</h1>
                <p className="subtitulo">Hechos creados por usuarios que esperan aprobación</p>

                {reportes.length === 0 ? (
                    <p className="sin-reportes">No hay hechos pendientes de revisión</p>
                ) : (
                    <div className="reportes-lista">
                        {reportes.map((reporte) => (
                            <div key={reporte.id} className="reporte-card">
                                <div className="reporte-header">
                                    <h3>{reporte.titulo}</h3>
                                    <span className={`estado ${reporte.estado.toLowerCase().replace(' ', '-')}`}>
                                        {reporte.estado}
                                    </span>
                                </div>
                                
                                <div className="reporte-info">
                                    <p><strong>Fecha del suceso:</strong> {reporte.fechaSuceso}</p>
                                    <p><strong>Ubicación:</strong> {reporte.ubicacion}</p>
                                    <p><strong>Creado por:</strong> {reporte.usuarioCreador}</p>
                                    <p><strong>Fecha de creación:</strong> {reporte.fechaCreacion}</p>
                                </div>
                                
                                <div className="reporte-acciones">
                                    <button 
                                        onClick={() => handleVerDetalle(reporte.id)}
                                        className="btn-ver"
                                    >
                                        Ver Detalle
                                    </button>
                                    <button 
                                        onClick={() => handleAprobar(reporte.id)}
                                        className="btn-aprobar"
                                    >
                                        Aceptar
                                    </button>
                                    <button 
                                        onClick={() => handleAprobarConSugerencias(reporte.id)}
                                        className="btn-sugerencias"
                                    >
                                        Aceptar con sugerencias
                                    </button>
                                    <button 
                                        onClick={() => handleRechazar(reporte.id)}
                                        className="btn-rechazar"
                                    >
                                        Rechazar
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

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
                                    onClick={() => setModalAprobar({ open: false, hechoId: null, etiquetas: "" })}
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
                                    onClick={() => setModalSugerencias({ open: false, hechoId: null, sugerencias: "" })}
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
                                    onClick={() => setModalRechazar({ open: false, hechoId: null, motivo: "" })}
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

export default ReportesHechosPage;
