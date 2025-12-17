import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./SolicitudEliminacionPage.css";

const SolicitudEliminacionPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [solicitud, setSolicitud] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // TODO: Cargar la solicitud desde el backend usando el id
        // Ejemplo de datos mock
        const solicitudMock = {
            id: id,
            hechoTitulo: "Título del hecho a eliminar",
            hechoId: 123,
            motivo: "Información incorrecta",
            descripcionDetallada: "El hecho contiene información que ha sido verificada como incorrecta por múltiples fuentes.",
            estado: "Pendiente",
            fechaSolicitud: "2025-03-01",
            usuarioSolicitante: "usuario@email.com",
        };
        
        setSolicitud(solicitudMock);
        setLoading(false);
    }, [id]);

    if (loading) {
        return <div className="solicitud-detalle-page">Cargando...</div>;
    }

    if (!solicitud) {
        return <div className="solicitud-detalle-page">Solicitud no encontrada</div>;
    }

    return (
        <div className="solicitud-detalle-page">
            <div className="solicitud-detalle-container">
                <button onClick={() => navigate(-1)} className="btn-volver">
                    ← Volver
                </button>
                
                <h1>Detalle de Solicitud de Eliminación</h1>
                
                <div className="solicitud-info">
                    <div className="info-row">
                        <span className="label">Estado:</span>
                        <span className={`estado ${solicitud.estado.toLowerCase().replace(' ', '-')}`}>
                            {solicitud.estado}
                        </span>
                    </div>
                    
                    <div className="info-row">
                        <span className="label">Fecha de solicitud:</span>
                        <span>{solicitud.fechaSolicitud}</span>
                    </div>
                    
                    <div className="info-row">
                        <span className="label">Usuario solicitante:</span>
                        <span>{solicitud.usuarioSolicitante}</span>
                    </div>
                    
                    <div className="info-row">
                        <span className="label">Hecho relacionado:</span>
                        <a href={`/hechos/${solicitud.hechoId}`} className="link-hecho">
                            {solicitud.hechoTitulo}
                        </a>
                    </div>
                </div>
                
                <div className="solicitud-motivo">
                    <h2>Motivo</h2>
                    <p>{solicitud.motivo}</p>
                </div>
                
                <div className="solicitud-descripcion">
                    <h2>Descripción Detallada</h2>
                    <p>{solicitud.descripcionDetallada}</p>
                </div>
                
                {/* TODO: Agregar botones de acción para admin (aprobar/rechazar) */}
            </div>
        </div>
    );
};

export default SolicitudEliminacionPage;
