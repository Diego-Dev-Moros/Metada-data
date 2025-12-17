import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./UsuarioPage.css";

const UsuarioPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [usuario, setUsuario] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // TODO: Cargar usuario específico desde el backend usando el id
        const usuarioMock = {
            id: id,
            nombre: "Juan Pérez",
            email: "juan.perez@email.com",
            rol: "contribuyente",
            fechaRegistro: "2024-01-15",
            hechosCreados: 12,
            coleccionesCreadas: 3,
            solicitudesRealizadas: 5,
        };
        
        setUsuario(usuarioMock);
        setLoading(false);
    }, [id]);

    const handleCambiarRol = (nuevoRol) => {
        // TODO: Implementar cambio de rol en el backend
        console.log(`Cambiar rol de usuario ${id} a ${nuevoRol}`);
        setUsuario({ ...usuario, rol: nuevoRol });
    };

    const handleDesactivarUsuario = () => {
        // TODO: Implementar desactivación de usuario
        console.log(`Desactivar usuario ${id}`);
        alert("Usuario desactivado");
        navigate("/usuarios");
    };

    if (loading) {
        return <div className="usuario-page">Cargando...</div>;
    }

    if (!usuario) {
        return <div className="usuario-page">Usuario no encontrado</div>;
    }

    return (
        <div className="usuario-page">
            <div className="usuario-container">
                <button onClick={() => navigate(-1)} className="btn-volver">
                    ← Volver
                </button>

                <div className="usuario-header">
                    <h1>Perfil de Usuario</h1>
                    <span className={`rol-badge ${usuario.rol}`}>{usuario.rol}</span>
                </div>

                <div className="usuario-info">
                    <div className="info-section">
                        <h2>Información Personal</h2>
                        
                        <div className="info-field">
                            <label>Nombre:</label>
                            <span>{usuario.nombre}</span>
                        </div>

                        <div className="info-field">
                            <label>Email:</label>
                            <span>{usuario.email}</span>
                        </div>

                        <div className="info-field">
                            <label>Rol:</label>
                            <span>{usuario.rol}</span>
                        </div>

                        <div className="info-field">
                            <label>Fecha de registro:</label>
                            <span>{usuario.fechaRegistro}</span>
                        </div>
                    </div>

                    <div className="info-section">
                        <h2>Estadísticas</h2>
                        
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-number">{usuario.hechosCreados}</div>
                                <div className="stat-label">Hechos Creados</div>
                            </div>
                            
                            <div className="stat-card">
                                <div className="stat-number">{usuario.coleccionesCreadas}</div>
                                <div className="stat-label">Colecciones</div>
                            </div>
                            
                            <div className="stat-card">
                                <div className="stat-number">{usuario.solicitudesRealizadas}</div>
                                <div className="stat-label">Solicitudes</div>
                            </div>
                        </div>
                    </div>

                    <div className="info-section admin-actions">
                        <h2>Acciones de Administrador</h2>
                        
                        <div className="action-group">
                            <label>Cambiar rol:</label>
                            <select 
                                value={usuario.rol} 
                                onChange={(e) => handleCambiarRol(e.target.value)}
                            >
                                <option value="contribuyente">Contribuyente</option>
                                <option value="administrador">Administrador</option>
                            </select>
                        </div>

                        <button 
                            onClick={handleDesactivarUsuario}
                            className="btn-desactivar"
                        >
                            Desactivar Usuario
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UsuarioPage;
