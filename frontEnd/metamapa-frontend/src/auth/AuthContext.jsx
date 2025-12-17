import { createContext, useContext } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

/**
 * Context de autenticación extendido para MetaMapa
 * 
 * Extiende useAuth0 con funciones helper específicas del proyecto,
 * como verificación de roles.
 */
const AuthContext = createContext();

/**
 * Provider del contexto de autenticación
 * 
 * @param {Object} props - Props del componente
 * @param {ReactNode} props.children - Componentes hijos
 */
export const AuthProvider = ({ children }) => {
  const auth0 = useAuth0();
  
  // Namespace usado en Auth0 para los roles
  const ROLES_CLAIM = 'https://metamapa.com/roles';
  
  /**
   * Verifica si el usuario tiene un rol específico
   * 
   * @param {string} role - Nombre del rol a verificar (ej: 'ADMIN', 'USER')
   * @returns {boolean} - True si el usuario tiene el rol
   */
  const hasRole = (role) => {
    const { user } = auth0;
    if (!user) return false;
    
    const roles = user[ROLES_CLAIM] || [];
    return roles.includes(role);
  };

  /**
   * Verifica si el usuario es administrador
   * 
   * @returns {boolean} - True si el usuario tiene rol ADMIN
   */
  const isAdmin = () => hasRole('ADMIN');
  
  /**
   * Verifica si el usuario es usuario regular
   * 
   * @returns {boolean} - True si el usuario tiene rol USER
   */
  const isUser = () => hasRole('USER');
  
  /**
   * Obtiene todos los roles del usuario actual
   * 
   * @returns {string[]} - Array de roles
   */
  const getRoles = () => {
    const { user } = auth0;
    if (!user) return [];
    return user[ROLES_CLAIM] || [];
  };

  // Valor del contexto con todas las funciones de Auth0 + helpers
  const value = {
    ...auth0,
    hasRole,
    isAdmin,
    isUser,
    getRoles
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Hook para usar el contexto de autenticación
 * 
 * @returns {Object} - Objeto con funciones de autenticación y helpers
 * 
 * @example
 * const { isAuthenticated, user, isAdmin, logout } = useAuth();
 * 
 * if (isAdmin()) {
 *   // Mostrar panel de administrador
 * }
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser usado dentro de AuthProvider');
  }
  return context;
}; 

export default AuthContext;
