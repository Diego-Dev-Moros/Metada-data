/**
 * Configuración de Auth0 para MetaMapa
 * 
 * Este archivo centraliza la configuración de Auth0
 * para facilitar cambios entre entornos (dev, prod)
 */

export const auth0Config = {
  // Dominio de Auth0 (desde dashboard)
  domain: "dev-x8zpgn3i6vnkjg4m.us.auth0.com",
  
  // Client ID de la aplicación en Auth0
  clientId: "4tEUVJvJcQ8VYD9gSlNzBSIJSQTdnF2E",
  
  // URL de redirección después del login
  redirect_uri: window.location.origin + '/callback',
  
  // Audience de la API (para incluir roles en el token)
  audience: "https://metamapa-api",
  
  // Scopes solicitados (información que queremos del usuario)
  scope: "openid profile email",
  
  // Almacenar tokens en localStorage
  cacheLocation: 'localstorage',
};

export default auth0Config;
