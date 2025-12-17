import { useAuth0 } from '@auth0/auth0-react';

/**
 * Botón de Logout
 * 
 * Muestra un botón para cerrar sesión.
 * Se oculta automáticamente si el usuario no está autenticado.
 * 
 * @param {Object} props - Props del componente
 * @param {string} [props.className] - Clases CSS adicionales
 * @returns {JSX.Element|null} - Botón de logout o null si no está autenticado
 */
const LogoutButton = ({ className = '' }) => {
  const { logout, isAuthenticated } = useAuth0();

  // No mostrar si no está autenticado
  if (!isAuthenticated) return null;

  return (
    <button 
      onClick={() => logout({ 
        logoutParams: { 
          returnTo: window.location.origin 
        }
      })}
      className={`btn btn-secondary ${className}`}
    >
      Cerrar Sesión
    </button>
  );
};

export default LogoutButton;
