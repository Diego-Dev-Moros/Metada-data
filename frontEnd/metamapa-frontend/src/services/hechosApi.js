import axios from "axios";

const API_URL = "http://localhost:8082/api/fuente-dinamica/hechosTODOS"; // apunta a tu fuente dinámica o agregador

export const getHechos = async () => {
    const res = await axios.get(API_URL);
    return res.data;
};
