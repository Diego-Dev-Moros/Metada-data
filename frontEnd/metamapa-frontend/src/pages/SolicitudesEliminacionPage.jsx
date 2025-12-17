import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./SolicitudesEliminacionPage.css";

const SolicitudesEliminacionPage = () => {
    const navigate = useNavigate();
    const [solicitudes, setSolicitudes] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // TODO: Cargar solicitudes desde el backend
        // Ejemplo de datos mock
        const solicitudesMock = [
            {
                id: 1,
                hechoTitulo: "Título del hecho a eliminar",
                hechoId: 123,
                motivo: "Información incorrecta",
                estado: "Pendiente",
                fechaSolicitud: "2025-03-01",
            },
            {
                id: 2,
                hechoTitulo: "Otro hecho solicitado para eliminar",
                hechoId: 456,
                motivo: "Duplicado",
                estado: "En revisión",
                fechaSolicitud: "2025-03-05",
            },
        ];
        
        setSolicitudes(solicitudesMock);
        setLoading(false);
    }, []);

    const handleVerDetalle = (id) => {
        navigate(`/solicitudes/${id}`);
    };

    const handleAprobarSolicitud = async (id) => {
        if (window.confirm("¿Estás seguro de que deseas aprobar esta solicitud de eliminación?")) {
            // TODO: Llamar al backend para aprobar la solicitud
            console.log("Aprobar solicitud:", id);
            alert("Solicitud aprobada. El hecho será eliminado.");
            // Recargar solicitudes
        }
    };

    const handleRechazarSolicitud = async (id) => {
        if (window.confirm("¿Estás seguro de que deseas rechazar esta solicitud de eliminación?")) {
            // TODO: Llamar al backend para rechazar la solicitud
            console.log("Rechazar solicitud:", id);
            alert("Solicitud rechazada.");
            // Recargar solicitudes
        }
    };

    if (loading) {
        return <div className="solicitudes-page">Cargando...</div>;
    }

    return (
        <div className="solicitudes-page">
            <div className="solicitudes-container">
                <h1>Solicitudes de Eliminación</h1>
                
                <button 
                    onClick={() => navigate("/solicitudes/nueva")} 
                    className="btn-nueva-solicitud"
                >
                    + Nueva Solicitud
                </button>

                {solicitudes.length === 0 ? (
                    <p className="sin-solicitudes">No hay solicitudes de eliminación</p>
                ) : (
                    <div className="solicitudes-lista">
                        {solicitudes.map((solicitud) => (
                            <div key={solicitud.id} className="solicitud-card">
                                <div className="solicitud-header">
                                    <h3>{solicitud.hechoTitulo}</h3>
                                    <span className={`estado ${solicitud.estado.toLowerCase().replace(' ', '-')}`}>
                                        {solicitud.estado}
                                    </span>
                                </div>
                                <p><strong>Motivo:</strong> {solicitud.motivo}</p>
                                <p><strong>Fecha de solicitud:</strong> {solicitud.fechaSolicitud}</p>
                                <div className="solicitud-acciones">
                                    <button 
                                        onClick={() => handleVerDetalle(solicitud.id)}
                                        className="btn-ver-detalle"
                                    >
                                        Ver Detalle
                                    </button>
                                    <button 
                                        onClick={() => handleAprobarSolicitud(solicitud.id)}
                                        className="btn-aprobar-solicitud"
                                    >
                                        Aprobar
                                    </button>
                                    <button 
                                        onClick={() => handleRechazarSolicitud(solicitud.id)}
                                        className="btn-rechazar-solicitud"
                                    >
                                        Rechazar
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default SolicitudesEliminacionPage;
