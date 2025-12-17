import { useAuth0 } from '@auth0/auth0-react';

/**
 * Botón de Login
 * 
 * Muestra un botón para iniciar sesión con Auth0.
 * Se oculta automáticamente si el usuario ya está autenticado.
 * 
 * @param {Object} props - Props del componente
 * @param {string} [props.className] - Clases CSS adicionales
 * @returns {JSX.Element|null} - Botón de login o null si ya está autenticado
 */
const LoginButton = ({ className = '' }) => {
  const { loginWithRedirect, isAuthenticated } = useAuth0();

  // No mostrar si ya está autenticado
  if (isAuthenticated) return null;

  return (
    <button 
      onClick={() => loginWithRedirect()}
      className={`btn btn-primary ${className}`}
    >
      Iniciar Sesión
    </button>
  );
};

export default LoginButton;
