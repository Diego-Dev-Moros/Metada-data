// AuthPage.jsx (Componente Padre)
import React, { useState } from 'react';
import Login from './Login.jsx';
import Register from './Register.jsx';

const AuthPage = () => {
    // Estado para alternar entre login y registro
    const [isLoginView, setIsLoginView] = useState(true);

    const switchToRegister = () => setIsLoginView(false);
    const switchToLogin = () => setIsLoginView(true);

    return (
        <div>
            {isLoginView ? (
                <Login switchToRegister={switchToRegister} />
            ) : (
                <Register switchToLogin={switchToLogin} />
            )}
        </div>
    );
};

export default AuthPage;