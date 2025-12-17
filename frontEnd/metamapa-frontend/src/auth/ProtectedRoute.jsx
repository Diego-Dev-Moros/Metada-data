import { useAuth0 } from '@auth0/auth0-react';
import { Navigate } from 'react-router-dom';
import PropTypes from 'prop-types';

/**
 * Componente de ruta protegida
 * 
 * Protege rutas que requieren autenticación y/o roles específicos.
 * Si el usuario no está autenticado, redirige a la página de inicio.
 * Si el usuario no tiene el rol requerido, redirige a página de acceso denegado.
 * 
 * @param {Object} props - Props del componente
 * @param {ReactNode} props.children - Componente hijo a renderizar si está autorizado
 * @param {string} [props.requiredRole] - Rol requerido para acceder (opcional)
 * @returns {JSX.Element} - Componente hijo o redirección
 * 
 * @example
 * // Ruta que requiere autenticación
 * <Route path="/mapa" element={
 *   <ProtectedRoute>
 *     <Mapa />
 *   </ProtectedRoute>
 * } />
 * 
 * @example
 * // Ruta que requiere rol ADMIN
 * <Route path="/admin" element={
 *   <ProtectedRoute requiredRole="ADMIN">
 *     <AdminPanel />
 *   </ProtectedRoute>
 * } />
 */
const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, isLoading, user } = useAuth0();

  // Mostrar loading mientras se verifica la autenticación
  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Verificando autenticación...</p>
      </div>
    );
  }

  // Si no está autenticado, redirigir a inicio
  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  // Si se requiere un rol específico
  if (requiredRole) {
    const roles = user['https://metamapa.com/roles'] || [];
    
    // Si no tiene el rol requerido, redirigir a página de acceso denegado
    if (!roles.includes(requiredRole)) {
      return <Navigate to="/acceso-denegado" replace />;
    }
  }

  // Usuario autenticado y con rol correcto, renderizar componente hijo
  return children;
};

ProtectedRoute.propTypes = {
  children: PropTypes.node.isRequired,
  requiredRole: PropTypes.string
};

export default ProtectedRoute;
