import { Link } from 'react-router-dom';
import './AccesoDenegadoPage.css';

/**
 * Página de acceso denegado
 * 
 * Se muestra cuando un usuario autenticado intenta acceder a un recurso
 * para el cual no tiene los permisos necesarios.
 * 
 * @returns {JSX.Element} - Página de acceso denegado
 */
const AccesoDenegadoPage = () => {
  return (
    <div className="acceso-denegado-page">
      <div className="acceso-denegado-container">
        <div className="icon-container">
          <svg 
            className="icon-forbidden" 
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor"
          >
            <path 
              strokeLinecap="round" 
              strokeLinejoin="round" 
              strokeWidth={2} 
              d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" 
            />
          </svg>
        </div>
        
        <h1>Acceso Denegado</h1>
        <p className="message">
          No tienes permisos para acceder a este recurso.
        </p>
        <p className="submessage">
          Si crees que deberías tener acceso, contacta al administrador del sistema.
        </p>
        
        <div className="actions">
          <Link to="/mapa" className="btn btn-primary">
            Volver al Mapa
          </Link>
          <Link to="/" className="btn btn-secondary">
            Ir al Inicio
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AccesoDenegadoPage;
