import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Auth0Provider } from '@auth0/auth0-react'
import { AuthProvider } from './auth/AuthContext'
import "bootstrap/dist/css/bootstrap.min.css";
import './index.css'
import App from './App.jsx'
import "leaflet/dist/leaflet.css";
import { auth0Config } from './auth/auth0Config'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <Auth0Provider
            domain={auth0Config.domain}
            clientId={auth0Config.clientId}
            authorizationParams={{
                redirect_uri: auth0Config.redirect_uri,
                audience: auth0Config.audience,
                scope: auth0Config.scope
            }}
            cacheLocation={auth0Config.cacheLocation}
        >
            <AuthProvider>
                <BrowserRouter>
                    <App />
                </BrowserRouter>
            </AuthProvider>
        </Auth0Provider>
    </StrictMode>,
)
