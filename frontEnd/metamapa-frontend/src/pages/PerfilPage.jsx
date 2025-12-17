import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./PerfilPage.css";

const PerfilPage = () => {
    const navigate = useNavigate();
    const [usuario, setUsuario] = useState(null);
    const [loading, setLoading] = useState(true);
    const [editMode, setEditMode] = useState(false);

    useEffect(() => {
        // TODO: Cargar datos del usuario logueado desde el backend
        const usuarioMock = {
            id: 1,
            nombre: "Juan Pérez",
            email: "juan.perez@email.com",
            fechaNacimiento: "1990-05-20",
            rol: localStorage.getItem("rol") || "contribuyente",
            fechaRegistro: "2024-01-15",
            hechos: [
                {
                    id: 1,
                    titulo: "Revolución Francesa",
                    descripcion: "La Revolución Francesa fue un conflicto social y político, con diversos periodos de violencia, que convulsionó Francia y, por extensión de sus implicaciones, a otras naciones de Europa.",
                    fechaSuceso: "1789-07-14",
                    categoria: "Histórico",
                    fechaCreacion: "2025-01-10",
                    estado: "ACEPTADO",
                    imagen: "/uploads/multimedia/imagen1.jpg",
                },
                {
                    id: 2,
                    titulo: "Primera Guerra Mundial",
                    descripcion: "La Primera Guerra Mundial, también conocida como Gran Guerra, fue una confrontación bélica centrada en Europa que empezó el 28 de julio de 1914.",
                    fechaSuceso: "1914-07-28",
                    categoria: "Bélico",
                    fechaCreacion: "2025-01-15",
                    estado: "PENDIENTE",
                    imagen: null,
                },
                {
                    id: 3,
                    titulo: "Caída del Muro de Berlín",
                    descripcion: "La caída del Muro de Berlín fue un acontecimiento que tuvo lugar el 9 de noviembre de 1989, cuando los habitantes de la República Democrática Alemana pudieron cruzar libremente.",
                    fechaSuceso: "1989-11-09",
                    categoria: "Político",
                    fechaCreacion: "2025-02-01",
                    estado: "ACEPTADO_CON_SUGERENCIAS",
                    imagen: "/uploads/multimedia/muro.jpg",
                },
                {
                    id: 4,
                    titulo: "Llegada del hombre a la Luna",
                    descripcion: "El 20 de julio de 1969, los astronautas estadounidenses Neil Armstrong y Buzz Aldrin se convirtieron en los primeros humanos en pisar la superficie lunar.",
                    fechaSuceso: "1969-07-20",
                    categoria: "Científico",
                    fechaCreacion: "2025-02-10",
                    estado: "RECHAZADO",
                    imagen: "/uploads/multimedia/luna.jpg",
                },
                {
                    id: 5,
                    titulo: "Descubrimiento de América",
                    descripcion: "El 12 de octubre de 1492, Cristóbal Colón llegó a lo que hoy conocemos como América.",
                    fechaSuceso: "1492-10-12",
                    categoria: "Histórico",
                    fechaCreacion: "2025-02-15",
                    estado: "ACEPTADO",
                    imagen: null,
                },
            ],
        };
        
        setUsuario(usuarioMock);
        setLoading(false);
    }, []);

    const handleGuardar = () => {
        // TODO: Guardar cambios del usuario
        console.log("Guardar cambios");
        setEditMode(false);
    };

    if (loading) {
        return <div className="perfil-page">Cargando...</div>;
    }

    if (!usuario) {
        return <div className="perfil-page">Usuario no encontrado</div>;
    }

    return (
        <div className="perfil-page">
            <div className="perfil-container">
                <div className="perfil-header">
                    <h1>Mi Perfil</h1>
                    {!editMode ? (
                        <button onClick={() => setEditMode(true)} className="btn-editar">
                            Editar Perfil
                        </button>
                    ) : (
                        <div className="btn-group">
                            <button onClick={handleGuardar} className="btn-guardar">
                                Guardar
                            </button>
                            <button onClick={() => setEditMode(false)} className="btn-cancelar">
                                Cancelar
                            </button>
                        </div>
                    )}
                </div>

                <div className="perfil-info">
                    <div className="info-section">
                        <h2>Información Personal</h2>
                        
                        <div className="info-field">
                            <label>Nombre:</label>
                            {editMode ? (
                                <input type="text" defaultValue={usuario.nombre} />
                            ) : (
                                <span>{usuario.nombre}</span>
                            )}
                        </div>

                        <div className="info-field">
                            <label>Email:</label>
                            {editMode ? (
                                <input type="email" defaultValue={usuario.email} />
                            ) : (
                                <span>{usuario.email}</span>
                            )}
                        </div>

                        <div className="info-field">
                            <label>Fecha de nacimiento:</label>
                            {editMode ? (
                                <input type="date" defaultValue={usuario.fechaNacimiento} />
                            ) : (
                                <span>{usuario.fechaNacimiento}</span>
                            )}
                        </div>

                        <div className="info-field">
                            <label>Rol:</label>
                            <span className={`rol-badge ${usuario.rol}`}>{usuario.rol}</span>
                        </div>

                        <div className="info-field">
                            <label>Fecha de registro:</label>
                            <span>{usuario.fechaRegistro}</span>
                        </div>
                    </div>

                    <div className="mis-hechos-section">
                        <div className="hechos-header">
                            <h2>Mis Hechos ({usuario.hechos.length})</h2>
                        </div>
                        
                        <div className="hechos-grid">
                            {usuario.hechos.slice(0, 4).map((hecho) => (
                                <div key={hecho.id} className="hecho-card" onClick={() => navigate(`/hecho/${hecho.id}`)}>
                                    <div className="hecho-card-content">
                                        <div className="hecho-card-header">
                                            <h3>{hecho.titulo}</h3>
                                            <span className={`estado-badge ${hecho.estado.toLowerCase().replace('_', '-')}`}>
                                                {hecho.estado.replace('_', ' ')}
                                            </span>
                                        </div>
                                        
                                        <p className="hecho-descripcion">
                                            {hecho.descripcion.length > 50 
                                                ? `${hecho.descripcion.substring(0, 50)}...` 
                                                : hecho.descripcion}
                                        </p>
                                        
                                        <div className="hecho-info">
                                            <p><strong>Fecha de hecho:</strong> {hecho.fechaSuceso}</p>
                                            <p><strong>Categoría:</strong> {hecho.categoria}</p>
                                            <p><strong>Fecha de creación:</strong> {hecho.fechaCreacion}</p>
                                        </div>
                                    </div>
                                    
                                    <div className="hecho-imagen">
                                        {hecho.imagen ? (
                                            <img src={hecho.imagen} alt={hecho.titulo} />
                                        ) : (
                                            <div style={{ 
                                                width: '100%', 
                                                height: '100%', 
                                                display: 'flex', 
                                                alignItems: 'center', 
                                                justifyContent: 'center',
                                                backgroundColor: '#f0f0f0',
                                                color: '#999',
                                                fontSize: '0.75rem'
                                            }}>
                                                IMG
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                        
                        {usuario.hechos.length > 4 && (
                            <button onClick={() => navigate('/mis-hechos')} className="btn-ver-todos">
                                Ver todos mis hechos
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PerfilPage;
