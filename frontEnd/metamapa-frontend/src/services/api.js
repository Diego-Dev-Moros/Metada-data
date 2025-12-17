// Centralizacion de la configuracion de axios
import axios from 'axios';

// Asume que tu backend corre en el puerto 8080
const API_URL = 'http://localhost:8080/api'; // O la URL base de tu API

const apiClient = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export default apiClient;