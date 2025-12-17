import { Link, useNavigate } from "react-router-dom";
import { useState, useEffect, useRef } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import "./Navbar.css";

export default function Navbar() {
    const navigate = useNavigate();
    const { isAuthenticated, user, logout, loginWithRedirect } = useAuth0();
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const dropdownRef = useRef(null);

    // Extraer roles de Auth0 user object
    const getRoles = () => {
        if (!user) return [];
        const roles = user['https://metamapa.com/roles'] || [];
        return roles;
    };

    const roles = getRoles();
    const isAdmin = roles.includes('ADMIN');
    const isContributor = roles.includes('CONTRIBUTOR');
    const isUser = roles.includes('USER');
    
    // Debug: Log de roles para verificar
    useEffect(() => {
        if (isAuthenticated && user) {
            console.log('=== DEBUG AUTH0 ROLES ===');
            console.log('Usuario:', user.email);
            console.log('Objeto user completo:', user);
            console.log('Roles extraídos:', roles);
            console.log('isAdmin:', isAdmin);
            console.log('isContributor:', isContributor);
            console.log('isUser:', isUser);
            console.log('========================');
        }
    }, [isAuthenticated, user, roles]);

    // Cerrar dropdown al hacer clic fuera
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setDropdownOpen(false);
            }
        };

        if (dropdownOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [dropdownOpen]);

    const handleLogout = () => {
        logout({ logoutParams: { returnTo: window.location.origin } });
    };

    const handleLogin = () => {
        loginWithRedirect();
    };

    return (
        <nav className="navbar-metamapa">
            {/* Logo izquierda */}
            <div className="navbar-left">
                <Link to="/" className="navbar-logo">
                    <img src="/logo-metamapa.png" alt="MetaMapa" />
                </Link>
            </div>

            {/* Enlaces de navegación */}
            <div className="navbar-right">
                <Link to="/colecciones" className="nav-link">Colecciones</Link>
                <Link to="/estadisticas" className="nav-link">Estadísticas</Link>
                
                {/* Crear Hecho - Para Contributors y Admins */}
                {(isContributor || isAdmin) && (
                    <Link to="/crear-hecho" className="nav-link">Crear Hecho</Link>
                )}
                
                {/* Panel de Administrador - Solo admin */}
                {isAdmin && (
                    <div 
                        className="dropdown-admin" 
                        ref={dropdownRef}
                        style={{ position: 'relative', display: 'inline-block' }}
                    >
                        <button
                            type="button"
                            className="nav-link"
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                const newState = !dropdownOpen;
                                console.log("Click detectado, cambiando a:", newState);
                                setDropdownOpen(newState);
                            }}
                            style={{ 
                                cursor: 'pointer',
                                background: 'none',
                                border: 'none',
                                color: 'white',
                                fontWeight: 500,
                                padding: 0,
                                fontSize: 'inherit',
                                fontFamily: 'inherit'
                            }}
                        >
                            Panel Admin {dropdownOpen ? '▲' : '▼'}
                        </button>
                        
                        <div 
                            style={{
                                display: dropdownOpen ? 'block' : 'none',
                                position: 'absolute',
                                top: '100%',
                                right: 0,
                                backgroundColor: 'white',
                                borderRadius: '8px',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                                minWidth: '220px',
                                marginTop: '0.5rem',
                                overflow: 'hidden',
                                zIndex: 9999
                            }}
                        >
                            <Link 
                                to="/reportes-hechos" 
                                onClick={() => setDropdownOpen(false)}
                                style={{
                                    display: 'block',
                                    padding: '0.75rem 1rem',
                                    color: '#333',
                                    textDecoration: 'none'
                                }}
                            >
                                📋 Reportes de Hechos
                            </Link>
                            <Link 
                                to="/solicitudes" 
                                onClick={() => setDropdownOpen(false)}
                                style={{
                                    display: 'block',
                                    padding: '0.75rem 1rem',
                                    color: '#333',
                                    textDecoration: 'none',
                                    borderTop: '1px solid #e0e0e0'
                                }}
                            >
                                🗑️ Solicitudes de Eliminación
                            </Link>
                            <Link 
                                to="/usuarios" 
                                onClick={() => setDropdownOpen(false)}
                                style={{
                                    display: 'block',
                                    padding: '0.75rem 1rem',
                                    color: '#333',
                                    textDecoration: 'none',
                                    borderTop: '1px solid #e0e0e0'
                                }}
                            >
                                👥 Usuarios
                            </Link>
                            <Link 
                                to="/importar-dataset" 
                                onClick={() => setDropdownOpen(false)}
                                style={{
                                    display: 'block',
                                    padding: '0.75rem 1rem',
                                    color: '#333',
                                    textDecoration: 'none',
                                    borderTop: '1px solid #e0e0e0'
                                }}
                            >
                                📥 Importar Dataset
                            </Link>
                        </div>
                    </div>
                )}
                
                <Link to="/nosotros" className="nav-link">Nosotros</Link>
                <Link to="/contacto" className="nav-link">Contactanos</Link>

                {/* Sesión y perfil - alineados a la derecha */}
                <div className="session-btns">
                    {isAuthenticated ? (
                        <>
                            {/* Usuarios autenticados ven Mi Perfil */}
                            {isAuthenticated && (
                                <Link to="/perfil" className="nav-link">Mi Perfil</Link>
                            )}
                            <button onClick={handleLogout} className="nav-btn logout">
                                Cerrar sesión
                            </button>
                        </>
                    ) : (
                        <>
                            <button onClick={handleLogin} className="nav-btn login">Iniciar sesión</button>
                            <Link to="/registro" className="nav-btn register">Registrarse</Link>
                        </>
                    )}

                    {/* Indicador de rol actual (Auth0) */}
                    {isAuthenticated && (
                        <div 
                            className="nav-btn test-rol"
                            style={{
                                backgroundColor: isAdmin ? "#dc3545" : isContributor ? "#ffc107" : "#28a745",
                                color: "#fff",
                                fontSize: "0.85rem",
                                padding: "0.4rem 0.8rem",
                                cursor: "default"
                            }}
                            title={`Usuario: ${user?.email} | Roles: ${roles.join(', ')}`}
                        >
                            {isAdmin ? "👑 Logueado → Admin" : isContributor ? "✏️ Logueado → Contributor" : "👤 Logueado → User"}
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
}
