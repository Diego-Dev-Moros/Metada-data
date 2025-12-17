import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./HechoPage.css";

const HechoPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [hecho, setHecho] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // TODO: Cargar el hecho desde el backend usando el id
        // Ejemplo de datos mock
        const hechoMock = {
            id: id,
            titulo: "Título del hecho",
            fechaSuceso: "2025-02-20",
            fechaCarga: "2025-02-21",
            ubicacion: "Buenos Aires",
            descripcion: "Descripción detallada del hecho",
            imagen: "/uploads/multimedia/imagen1.jpg",
            fuente: "Usuario registrado",
        };
        
        setHecho(hechoMock);
        setLoading(false);
    }, [id]);

    if (loading) {
        return <div className="hecho-page">Cargando...</div>;
    }

    if (!hecho) {
        return <div className="hecho-page">Hecho no encontrado</div>;
    }

    return (
        <div className="hecho-page">
            <div className="hecho-container">
                <div className="hecho-header">
                    <button onClick={() => navigate(-1)} className="btn-volver">
                        ← Volver
                    </button>
                    
                    <button 
                        onClick={() => navigate('/solicitudes/nueva', { state: { hechoId: id } })}
                        className="btn-eliminar"
                        title="Solicitar eliminación"
                    >
                        🗑️ Solicitar eliminación
                    </button>
                </div>
                
                <h1>{hecho.titulo}</h1>
                
                {hecho.imagen && (
                    <div className="hecho-imagen">
                        <img src={hecho.imagen} alt={hecho.titulo} />
                    </div>
                )}
                
                <div className="hecho-info">
                    <p><strong>Fecha del suceso:</strong> {hecho.fechaSuceso}</p>
                    <p><strong>Fecha de carga:</strong> {hecho.fechaCarga}</p>
                    <p><strong>Ubicación:</strong> {hecho.ubicacion}</p>
                    <p><strong>Fuente:</strong> {hecho.fuente}</p>
                </div>
                
                <div className="hecho-descripcion">
                    <h2>Descripción</h2>
                    <p>{hecho.descripcion}</p>
                </div>
            </div>
        </div>
    );
};

export default HechoPage;
