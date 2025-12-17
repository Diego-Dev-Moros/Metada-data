import React from "react";
import { Navigate, Link } from "react-router-dom";
import { useAuth0 } from "@auth0/auth0-react";

/**
 * Componente para proteger rutas según el rol del usuario
 * Ahora integrado con Auth0 para verificar roles desde el JWT
 * 
 * @param {Array} allowedRoles - Array de roles permitidos (usar mayúsculas: ["ADMIN", "CONTRIBUTOR", "USER"])
 * @param {React.Component} children - Componente hijo a renderizar si tiene permisos
 */
const ProtectedRoute = ({ allowedRoles, children }) => {
    const { isAuthenticated, isLoading, user, loginWithRedirect } = useAuth0();

    // Mostrar loading mientras Auth0 verifica la sesión
    if (isLoading) {
        return (
            <div style={{ 
                padding: "2rem", 
                textAlign: "center", 
                minHeight: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center"
            }}>
                <div>
                    <div className="spinner-border" role="status">
                        <span className="visually-hidden">Cargando...</span>
                    </div>
                    <p style={{ marginTop: "1rem" }}>Verificando autenticación...</p>
                </div>
            </div>
        );
    }

    // Si no está autenticado, redirigir al login de Auth0
    if (!isAuthenticated) {
        loginWithRedirect({
            appState: { returnTo: window.location.pathname }
        });
        return null;
    }

    // Obtener roles del usuario desde Auth0
    const getUserRoles = () => {
        if (!user) return [];
        
        // Buscar roles en diferentes ubicaciones del token
        const rolesFromNamespace = user['https://metamapa.com/roles'] || [];
        const rolesFromClaim = user['roles'] || [];
        
        // Combinar y normalizar a mayúsculas
        const allRoles = [...rolesFromNamespace, ...rolesFromClaim];
        return allRoles.map(role => role.toUpperCase());
    };

    const userRoles = getUserRoles();

    // Si se especificaron roles permitidos, verificar
    if (allowedRoles && allowedRoles.length > 0) {
        // Normalizar allowedRoles a mayúsculas
        const normalizedAllowedRoles = allowedRoles.map(role => role.toUpperCase());
        
        // Verificar si el usuario tiene al menos uno de los roles permitidos
        const hasPermission = normalizedAllowedRoles.some(role => userRoles.includes(role));
        
        if (!hasPermission) {
            return (
                <div style={{ 
                    padding: "2rem", 
                    textAlign: "center", 
                    minHeight: "100vh",
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "center",
                    alignItems: "center"
                }}>
                    <h1 style={{ color: "#dc3545", marginBottom: "1rem" }}>🚫 Acceso Denegado</h1>
                    <p style={{ color: "#666", marginBottom: "0.5rem" }}>
                        No tienes permisos para acceder a esta página.
                    </p>
                    <p style={{ color: "#999", fontSize: "0.9rem", marginBottom: "2rem" }}>
                        Roles requeridos: <strong>{normalizedAllowedRoles.join(', ')}</strong><br/>
                        Tus roles: <strong>{userRoles.length > 0 ? userRoles.join(', ') : 'Ninguno'}</strong>
                    </p>
                    <div style={{ display: "flex", gap: "1rem" }}>
                        <Link 
                            to="/"
                            style={{
                                padding: "0.75rem 1.5rem",
                                backgroundColor: "#007bff",
                                color: "white",
                                border: "none",
                                borderRadius: "4px",
                                textDecoration: "none",
                                cursor: "pointer"
                            }}
                        >
                            Ir al Inicio
                        </Link>
                        <button 
                            onClick={() => window.history.back()}
                            style={{
                                padding: "0.75rem 1.5rem",
                                backgroundColor: "#6c757d",
                                color: "white",
                                border: "none",
                                borderRadius: "4px",
                                cursor: "pointer"
                            }}
                        >
                            Volver
                        </button>
                    </div>
                </div>
            );
        }
    }

    // Si tiene permisos, renderiza el componente hijo
    return children;
};

export default ProtectedRoute;
