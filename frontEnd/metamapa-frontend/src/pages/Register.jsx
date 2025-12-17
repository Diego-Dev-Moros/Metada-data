// src/pages/Register.jsx
import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth0 } from "@auth0/auth0-react";
import "./Register.css";

const Register = () => {
    const { loginWithRedirect, isAuthenticated } = useAuth0();
    const navigate = useNavigate();

    // Si ya está autenticado, redirigir a home
    useEffect(() => {
        if (isAuthenticated) {
            navigate("/");
        }
    }, [isAuthenticated, navigate]);

    // Redirigir a Auth0 con pantalla de registro
    const handleRegister = () => {
        loginWithRedirect({ screen_hint: 'signup' });
    };

    // Función para redirigir a Login
    const irALogin = () => {
        navigate("/login");
    };

    return (
        <div className="register-page">
            <div className="register-card">
                <h1>Registrarse como Contribuyente</h1>

                <p className="mb-4 text-center">
                    Crea tu cuenta de forma segura con Auth0
                </p>

                <button 
                    onClick={handleRegister} 
                    className="reg-btn"
                    type="button"
                >
                    📝 Registrarse con Auth0
                </button>

                {/* Botón que redirige al login */}
                <button
                    type="button"
                    onClick={irALogin}
                    className="register-secondary-link"
                >
                    ¿Ya tenés cuenta? Iniciar sesión.
                </button>
            </div>
        </div>
    );
};

export default Register;
