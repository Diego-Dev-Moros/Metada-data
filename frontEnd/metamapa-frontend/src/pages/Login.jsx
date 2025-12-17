// src/pages/Login.jsx
import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth0 } from "@auth0/auth0-react";
import "./Login.css";

const Login = () => {
    const { loginWithRedirect, isAuthenticated } = useAuth0();
    const navigate = useNavigate();

    // Si ya está autenticado, redirigir a home
    useEffect(() => {
        if (isAuthenticated) {
            navigate("/");
        }
    }, [isAuthenticated, navigate]);

    // Redirigir a Auth0 para login
    const handleLogin = () => {
        loginWithRedirect();
    };

    // Redirección a registro (también usa Auth0)
    const irARegistro = () => {
        loginWithRedirect({ screen_hint: 'signup' });
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                <h2 className="auth-title">Iniciar Sesión en MetaMapa</h2>

                <p className="mb-4 text-center">
                    Usa tu cuenta de Auth0 para iniciar sesión de forma segura
                </p>

                <button 
                    onClick={handleLogin} 
                    className="submit-button"
                    type="button"
                >
                    🔑 Iniciar Sesión con Auth0
                </button>

                {/* Botón que redirige a Registro */}
                <button
                    type="button"
                    onClick={irARegistro}
                    className="auth-secondary-link"
                >
                    ¿Aún no sos Contribuyente? Regístrate aquí.
                </button>
            </div>
        </div>
    );
};

export default Login;
