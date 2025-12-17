import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import './PerfilPage.css';

/**
 * Página de Perfil del Usuario (Datos de Auth0)
 * 
 * Muestra la información del usuario obtenida directamente de Auth0:
 * - Email
 * - Email verificado
 * - Roles asignados
 * - Foto de perfil
 * - Fecha de última actualización
 */
const PerfilAuth0Page = () => {
  const { user, isAuthenticated, isLoading, logout } = useAuth0();
  const navigate = useNavigate();

  // Función para obtener roles del usuario
  const getUserRoles = () => {
    if (!user) return [];
    
    // Buscar roles en diferentes ubicaciones del token
    const rolesFromNamespace = user['https://metamapa.com/roles'] || [];
    const rolesFromClaim = user['roles'] || [];
    const rolesFromPermissions = user['permissions'] || [];
    
    // Combinar todos y eliminar duplicados
    const allRoles = [...rolesFromNamespace, ...rolesFromClaim, ...rolesFromPermissions];
    const uniqueRoles = [...new Set(allRoles)];
    
    return uniqueRoles.length > 0 ? uniqueRoles : ['Sin roles asignados'];
  };

  // Obtener el color del badge según el rol
  const getRoleBadgeColor = (role) => {
    const roleUpper = role.toUpperCase();
    if (roleUpper === 'ADMIN') return '#dc3545';
    if (roleUpper === 'CONTRIBUTOR') return '#ffc107';
    if (roleUpper === 'USER') return '#28a745';
    return '#6c757d';
  };

  // Obtener el icono según el rol
  const getRoleIcon = (role) => {
    const roleUpper = role.toUpperCase();
    if (roleUpper === 'ADMIN') return '👑';
    if (roleUpper === 'CONTRIBUTOR') return '✏️';
    if (roleUpper === 'USER') return '👤';
    return '🔒';
  };

  if (isLoading) {
    return (
      <div className="perfil-loading">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Cargando...</span>
        </div>
        <p>Cargando información del perfil...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="perfil-container">
        <div className="perfil-card">
          <h2>⚠️ No autenticado</h2>
          <p>Debes iniciar sesión para ver tu perfil.</p>
          <button 
            className="btn btn-primary"
            onClick={() => navigate('/')}
          >
            Ir al Inicio
          </button>
        </div>
      </div>
    );
  }

  const roles = getUserRoles();

  return (
    <div className="perfil-container">
      <div className="perfil-card">
        {/* Header con foto */}
        <div className="perfil-header">
          <img 
            src={user.picture} 
            alt={user.name} 
            className="perfil-avatar"
          />
          <h1 className="perfil-title">Mi Perfil</h1>
          <p className="perfil-subtitle">Información de Auth0</p>
        </div>

        {/* Información del usuario */}
        <div className="perfil-info">
          {/* Email */}
          <div className="perfil-field">
            <div className="field-icon">📧</div>
            <div className="field-content">
              <label className="field-label">Email</label>
              <div className="field-value">{user.email}</div>
            </div>
          </div>

          {/* Email Verificado */}
          <div className="perfil-field">
            <div className="field-icon">
              {user.email_verified ? '✅' : '❌'}
            </div>
            <div className="field-content">
              <label className="field-label">Email Verificado</label>
              <div className="field-value">
                <span className={`badge ${user.email_verified ? 'badge-success' : 'badge-warning'}`}>
                  {user.email_verified ? 'Verificado' : 'No Verificado'}
                </span>
                {!user.email_verified && (
                  <small className="text-muted d-block mt-1">
                    Revisa tu bandeja de entrada para verificar tu email
                  </small>
                )}
              </div>
            </div>
          </div>

          {/* Roles */}
          <div className="perfil-field">
            <div className="field-icon">🎭</div>
            <div className="field-content">
              <label className="field-label">Roles Asignados</label>
              <div className="field-value">
                <div className="roles-container">
                  {roles.map((role, index) => (
                    <span 
                      key={index}
                      className="role-badge"
                      style={{ backgroundColor: getRoleBadgeColor(role) }}
                    >
                      {getRoleIcon(role)} {role}
                    </span>
                  ))}
                </div>
                {roles.includes('Sin roles asignados') && (
                  <small className="text-danger d-block mt-2">
                    ⚠️ No tienes roles asignados. Contacta al administrador.
                  </small>
                )}
              </div>
            </div>
          </div>

          {/* Nickname */}
          <div className="perfil-field">
            <div className="field-icon">👤</div>
            <div className="field-content">
              <label className="field-label">Nombre de Usuario</label>
              <div className="field-value">{user.nickname || user.name}</div>
            </div>
          </div>

          {/* User ID */}
          <div className="perfil-field">
            <div className="field-icon">🔑</div>
            <div className="field-content">
              <label className="field-label">ID de Usuario</label>
              <div className="field-value">
                <code>{user.sub}</code>
              </div>
            </div>
          </div>

          {/* Última actualización */}
          <div className="perfil-field">
            <div className="field-icon">🕐</div>
            <div className="field-content">
              <label className="field-label">Última Actualización</label>
              <div className="field-value">
                {new Date(user.updated_at).toLocaleString('es-AR', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </div>
            </div>
          </div>
        </div>

        {/* Información de Debug (Solo si no hay roles) */}
        {roles.includes('Sin roles asignados') && (
          <div className="perfil-debug">
            <h3>🔍 Información de Debug</h3>
            <p className="text-muted">
              Esta información es útil para diagnosticar problemas con los roles:
            </p>
            <div className="debug-info">
              <pre>{JSON.stringify(user, null, 2)}</pre>
            </div>
            <div className="alert alert-warning mt-3">
              <strong>⚠️ No se detectaron roles en el token</strong>
              <p className="mb-0">Posibles causas:</p>
              <ul className="mb-0">
                <li>La Action de Auth0 no está configurada</li>
                <li>La Action no está en el flow de Login</li>
                <li>El usuario no tiene roles asignados en Auth0</li>
                <li>El token necesita ser renovado (cierra sesión y vuelve a entrar)</li>
              </ul>
            </div>
          </div>
        )}

        {/* Acciones */}
        <div className="perfil-actions">
          <button 
            className="btn btn-secondary"
            onClick={() => navigate('/')}
          >
            Volver al Mapa
          </button>
          <button 
            className="btn btn-danger"
            onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
          >
            Cerrar Sesión
          </button>
        </div>
      </div>

      {/* Instrucciones para asignar roles */}
      {roles.includes('Sin roles asignados') && (
        <div className="perfil-help">
          <h3>📚 ¿Cómo asignar roles?</h3>
          <ol>
            <li>Ve al dashboard de Auth0: <a href="https://manage.auth0.com" target="_blank" rel="noopener noreferrer">manage.auth0.com</a></li>
            <li>Navega a <strong>User Management</strong> → <strong>Users</strong></li>
            <li>Busca tu usuario: <code>{user.email}</code></li>
            <li>En la pestaña <strong>Roles</strong>, click en <strong>Assign Roles</strong></li>
            <li>Selecciona uno de estos roles: <strong>ADMIN</strong>, <strong>CONTRIBUTOR</strong>, o <strong>USER</strong></li>
            <li>Guarda los cambios</li>
            <li>Cierra sesión y vuelve a iniciar sesión para obtener un nuevo token</li>
          </ol>
        </div>
      )}
    </div>
  );
};

export default PerfilAuth0Page;
