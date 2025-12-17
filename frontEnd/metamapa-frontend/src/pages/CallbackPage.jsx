import { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';

/**
 * Página de callback después del login de Auth0
 * 
 * Auth0 redirige a esta página después de un login exitoso.
 * Este componente procesa la respuesta y redirige al usuario
 * a la página principal de la aplicación.
 * 
 * @returns {JSX.Element} - Página de loading mientras se procesa el callback
 */
const CallbackPage = () => {
  const { isAuthenticated, isLoading, error } = useAuth0();
  const navigate = useNavigate();

  useEffect(() => {
    // Si ya está autenticado, redirigir a la página principal
    if (isAuthenticated && !isLoading) {
      navigate('/');
    }
    
    // Si hay un error, redirigir a inicio
    if (error) {
      console.error('Error en callback de Auth0:', error);
      navigate('/');
    }
  }, [isAuthenticated, isLoading, error, navigate]);

  return (
    <div className="callback-page">
      <div className="callback-container">
        <div className="spinner"></div>
        <h2>Iniciando sesión...</h2>
        <p>Por favor espera mientras procesamos tu autenticación.</p>
      </div>
    </div>
  );
};

export default CallbackPage;
