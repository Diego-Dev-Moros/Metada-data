import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./UsuariosPage.css";

const UsuariosPage = () => {
    const navigate = useNavigate();
    const [usuarios, setUsuarios] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filtroRol, setFiltroRol] = useState("todos");

    useEffect(() => {
        // TODO: Cargar usuarios desde el backend
        const usuariosMock = [
            {
                id: 1,
                nombre: "Juan Pérez",
                email: "juan.perez@email.com",
                rol: "contribuyente",
                fechaRegistro: "2024-01-15",
                hechosCreados: 12,
            },
            {
                id: 2,
                nombre: "María González",
                email: "maria.gonzalez@email.com",
                rol: "contribuyente",
                fechaRegistro: "2024-02-20",
                hechosCreados: 8,
            },
            {
                id: 3,
                nombre: "Admin Principal",
                email: "admin@metamapa.com",
                rol: "administrador",
                fechaRegistro: "2023-12-01",
                hechosCreados: 45,
            },
        ];
        
        setUsuarios(usuariosMock);
        setLoading(false);
    }, []);

    const usuariosFiltrados = filtroRol === "todos" 
        ? usuarios 
        : usuarios.filter(u => u.rol === filtroRol);

    const handleVerPerfil = (id) => {
        navigate(`/usuarios/${id}`);
    };

    if (loading) {
        return <div className="usuarios-page">Cargando...</div>;
    }

    return (
        <div className="usuarios-page">
            <div className="usuarios-container">
                <h1>Gestión de Usuarios</h1>

                <div className="filtros-usuarios">
                    <label>Filtrar por rol:</label>
                    <select value={filtroRol} onChange={(e) => setFiltroRol(e.target.value)}>
                        <option value="todos">Todos</option>
                        <option value="contribuyente">Contribuyentes</option>
                        <option value="administrador">Administradores</option>
                    </select>
                </div>

                {usuariosFiltrados.length === 0 ? (
                    <p className="sin-usuarios">No hay usuarios para mostrar</p>
                ) : (
                    <div className="usuarios-lista">
                        <table className="usuarios-table">
                            <thead>
                                <tr>
                                    <th>Nombre</th>
                                    <th>Email</th>
                                    <th>Rol</th>
                                    <th>Fecha Registro</th>
                                    <th>Hechos Creados</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {usuariosFiltrados.map((usuario) => (
                                    <tr key={usuario.id}>
                                        <td>{usuario.nombre}</td>
                                        <td>{usuario.email}</td>
                                        <td>
                                            <span className={`rol-badge ${usuario.rol}`}>
                                                {usuario.rol}
                                            </span>
                                        </td>
                                        <td>{usuario.fechaRegistro}</td>
                                        <td>{usuario.hechosCreados}</td>
                                        <td>
                                            <button 
                                                onClick={() => handleVerPerfil(usuario.id)}
                                                className="btn-ver-perfil"
                                            >
                                                Ver Perfil
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default UsuariosPage;
