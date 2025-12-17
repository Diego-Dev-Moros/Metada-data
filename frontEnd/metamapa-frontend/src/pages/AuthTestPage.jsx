import { useAuth0 } from '@auth0/auth0-react';
import { useState, useEffect } from 'react';

function AuthTestPage() {
  const { 
    isAuthenticated, 
    isLoading, 
    user, 
    loginWithRedirect, 
    logout,
    getAccessTokenSilently 
  } = useAuth0();
  
  const [accessToken, setAccessToken] = useState(null);
  const [apiResponse, setApiResponse] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isAuthenticated) {
      getAccessTokenSilently()
        .then(token => {
          setAccessToken(token);
          console.log('Access Token:', token);
        })
        .catch(err => console.error('Error getting token:', err));
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  const testPublicEndpoint = async () => {
    try {
      const response = await fetch('http://localhost:8086/api/publica/test');
      const data = await response.json();
      setApiResponse({ endpoint: 'Público', data });
      setError(null);
    } catch (err) {
      setError('Error llamando endpoint público: ' + err.message);
    }
  };

  const testProtectedEndpoint = async () => {
    if (!accessToken) {
      setError('No hay token de acceso');
      return;
    }
    
    try {
      const response = await fetch('http://localhost:8086/api/auth/me', {
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      });
      const data = await response.json();
      setApiResponse({ endpoint: 'Protegido (/api/auth/me)', data });
      setError(null);
    } catch (err) {
      setError('Error llamando endpoint protegido: ' + err.message);
    }
  };

  const testAdminEndpoint = async () => {
    if (!accessToken) {
      setError('No hay token de acceso');
      return;
    }
    
    try {
      const response = await fetch('http://localhost:8086/api/admin/test', {
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      });
      const data = await response.json();
      setApiResponse({ endpoint: 'Admin', data });
      setError(null);
    } catch (err) {
      setError('Error llamando endpoint admin: ' + err.message);
    }
  };

  if (isLoading) {
    return (
      <div className="container mt-5">
        <div className="text-center">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-md-12">
          <h1 className="mb-4">🔐 Prueba de Autenticación Auth0</h1>
          
          {!isAuthenticated ? (
            <div className="card">
              <div className="card-body text-center">
                <h3>No estás autenticado</h3>
                <p>Haz clic para iniciar sesión con Auth0</p>
                <button 
                  className="btn btn-primary btn-lg" 
                  onClick={() => loginWithRedirect()}
                >
                  🔑 Iniciar Sesión
                </button>
              </div>
            </div>
          ) : (
            <>
              <div className="card mb-4">
                <div className="card-header bg-success text-white">
                  <h4>✅ Autenticado correctamente</h4>
                </div>
                <div className="card-body">
                  <h5>Información del Usuario:</h5>
                  <ul className="list-group mb-3">
                    <li className="list-group-item"><strong>Email:</strong> {user.email}</li>
                    <li className="list-group-item"><strong>Nombre:</strong> {user.name}</li>
                    <li className="list-group-item"><strong>Sub:</strong> {user.sub}</li>
                  </ul>
                  
                  {accessToken && (
                    <div className="alert alert-info">
                      <strong>Access Token obtenido:</strong>
                      <pre className="mt-2" style={{fontSize: '10px', maxHeight: '100px', overflow: 'auto'}}>
                        {accessToken}
                      </pre>
                    </div>
                  )}

                  <button 
                    className="btn btn-danger" 
                    onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
                  >
                    🚪 Cerrar Sesión
                  </button>
                </div>
              </div>

              <div className="card mb-4">
                <div className="card-header">
                  <h4>🧪 Probar Endpoints del Backend</h4>
                </div>
                <div className="card-body">
                  <div className="d-grid gap-2">
                    <button className="btn btn-secondary" onClick={testPublicEndpoint}>
                      📖 Endpoint Público (sin auth)
                    </button>
                    <button className="btn btn-primary" onClick={testProtectedEndpoint}>
                      🔒 Endpoint Protegido (/api/auth/me)
                    </button>
                    <button className="btn btn-warning" onClick={testAdminEndpoint}>
                      👑 Endpoint Admin (requiere rol ADMIN)
                    </button>
                  </div>
                </div>
              </div>

              {error && (
                <div className="alert alert-danger">
                  <strong>Error:</strong> {error}
                </div>
              )}

              {apiResponse && (
                <div className="card">
                  <div className="card-header bg-info text-white">
                    <h5>Respuesta de: {apiResponse.endpoint}</h5>
                  </div>
                  <div className="card-body">
                    <pre>{JSON.stringify(apiResponse.data, null, 2)}</pre>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default AuthTestPage;
