import { useState } from "react";
import { useNavigate } from "react-router-dom";
import FiltrosRapidos from "../components/FiltrosRapidos";
import MapaHechos from "../components/MapaHechos";
import "./HechosPage.css";

export default function HechosPage() {
    const navigate = useNavigate();
    const [filtros, setFiltros] = useState({
        categoria: "",
        desde: "",
        hasta: "",
        ubicacion: "",
        modo: "IRRESTRICTO",
    });

    const aplicar = (vals) => setFiltros(vals);

    return (
        <div className="main-content">
            <div className="hechos-layout">
                <aside className="aside-filtros">
                    {/* Buscador */}
                    <div className="search-container-sidebar">
                        <input
                            type="text"
                            placeholder="Buscar Hechos"
                            className="search-input-sidebar"
                        />
                        <button className="search-button-sidebar">🔍</button>
                    </div>
                    
                    <FiltrosRapidos filtros={filtros} onApply={aplicar} />
                    
                    {/* CTA dentro del sidebar */}
                    <section className="cta-subir-sidebar">
                        <div className="cta-preg">¿Presenciaste algo importante?</div>
                        <div className="cta-sub">Subí tu hecho y ayudá a visibilizarlo</div>
                        <button
                            className="cta-btn"
                            onClick={() => {
                                const rol = localStorage.getItem("rol");
                                if (rol === "contribuyente" || rol === "administrador") {
                                    navigate("/crear-hecho");
                                } else {
                                    navigate("/login");
                                }
                            }}
                        >
                            Subir Hecho
                        </button>
                    </section>
                </aside>

                <section className="section-mapa">
                    <MapaHechos filtros={filtros} />
                </section>
            </div>
        </div>
    );
}
