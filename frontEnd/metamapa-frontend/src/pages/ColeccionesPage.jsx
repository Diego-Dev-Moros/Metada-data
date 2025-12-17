// src/pages/ColeccionesPage.jsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./ColeccionesPage.css";

export default function ColeccionesPage() {
    const navigate = useNavigate();

    const [colecciones, setColecciones] = useState([]);
    const [busqueda, setBusqueda] = useState("");
    const [orden, setOrden] = useState("visitas"); // visitas | fecha | hechos

    // TODO: cuando tengas el back real, cambiás esta URL
    // const API_URL = "http://localhost:8081/api/colecciones";

    useEffect(() => {
        const fetchData = async () => {
            try {
                // const res = await fetch(API_URL);
                // const data = await res.json();
                // setColecciones(data);

                // mock de prueba
                const fake = [
                    {
                        id: 1,
                        nombre: "Nombre colección 1",
                        descripcion: "Descripción colección 1",
                        visitas: 321,
                        fecha: "2025-01-10",
                        hechos: 45,
                    },
                    {
                        id: 2,
                        nombre: "Nombre colección 2",
                        descripcion: "Descripción colección 2",
                        visitas: 120,
                        fecha: "2025-02-01",
                        hechos: 12,
                    },
                    {
                        id: 3,
                        nombre: "Nombre colección 3",
                        descripcion: "Descripción colección 3",
                        visitas: 560,
                        fecha: "2025-01-22",
                        hechos: 75,
                    },
                ];
                setColecciones(fake);
            } catch (e) {
                console.error("Error obteniendo colecciones", e);
            }
        };

        fetchData();
    }, []);

    // filtrado + ordenamiento
    const coleccionesFiltradas = useMemo(() => {
        let lista = [...colecciones];

        if (busqueda.trim() !== "") {
            const q = busqueda.toLowerCase();
            lista = lista.filter(
                (c) =>
                    c.nombre.toLowerCase().includes(q) ||
                    (c.descripcion && c.descripcion.toLowerCase().includes(q))
            );
        }

        if (orden === "visitas") {
            lista.sort((a, b) => (b.visitas || 0) - (a.visitas || 0));
        } else if (orden === "fecha") {
            lista.sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
        } else if (orden === "hechos") {
            lista.sort((a, b) => (b.hechos || 0) - (a.hechos || 0));
        }

        return lista;
    }, [colecciones, busqueda, orden]);

    const rol = localStorage.getItem("rol");

    return (
        <div className="colecciones-page">
            {/* header */}
            <div className="colecciones-header">
                <h1>Colecciones</h1>
                {rol === "administrador" && (
                    <button
                        className="btn-nueva"
                        onClick={() => navigate("/colecciones/nueva")}
                    >
                        + Nueva colección
                    </button>
                )}
            </div>

            {/* buscador */}
            <div className="colecciones-search">
                <input
                    type="text"
                    placeholder="Buscar colecciones"
                    value={busqueda}
                    onChange={(e) => setBusqueda(e.target.value)}
                />
                <button className="search-btn">🔍</button>
            </div>

            {/* ordenar */}
            <div className="colecciones-order">
                <label>Ordenar por</label>
                <select value={orden} onChange={(e) => setOrden(e.target.value)}>
                    <option value="visitas">Cantidad de visitas</option>
                    <option value="fecha">Fecha</option>
                    <option value="hechos">Cantidad de hechos</option>
                </select>
            </div>

            {/* lista */}
            <div className="colecciones-lista">
                {coleccionesFiltradas.map((c) => (
                    <div
                        key={c.id}
                        className="coleccion-card"
                        onClick={() => navigate(`/colecciones/${c.id}`)}
                        style={{ cursor: "pointer" }}
                    >
                        <div className="coleccion-body">
                            <h2>{c.nombre}</h2>
                            <p>Lorem ipsum dolor sit amet et delectus</p>
                        </div>
                        <div className="coleccion-meta">
                            <div>Cantidad de vistas: {c.visitas ?? 560}</div>
                            <div>Fecha: {c.fecha ?? "3/12/25"}</div>
                            <div>Cantidad hechos: {c.hechos ?? 80}</div>
                        </div>
                    </div>
                ))}

                {coleccionesFiltradas.length === 0 && (
                    <p className="empty-state">No se encontraron colecciones.</p>
                )}
            </div>
        </div>
    );
}
