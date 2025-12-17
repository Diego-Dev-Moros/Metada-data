import { useAuth0 } from '@auth0/auth0-react';
import './UserProfile.css';

/**
 * Componente de perfil de usuario
 * 
 * Muestra la información del usuario autenticado:
 * - Avatar
 * - Nombre
 * - Email
 * - Roles
 * 
 * @returns {JSX.Element|null} - Perfil de usuario o null si no está autenticado
 */
const UserProfile = () => {
  const { user, isAuthenticated } = useAuth0();

  if (!isAuthenticated || !user) return null;

  // Obtener roles del usuario
  const roles = user['https://metamapa.com/roles'] || [];

  return (
    <div className="user-profile">
      <img 
        src={user.picture} 
        alt={user.name}
        className="user-profile__avatar"
      />
      <div className="user-profile__info">
        <h4 className="user-profile__name">{user.name}</h4>
        <p className="user-profile__email">{user.email}</p>
        {roles.length > 0 && (
          <div className="user-profile__roles">
            {roles.map(role => (
              <span key={role} className="badge badge-role">
                {role}
              </span>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default UserProfile;
