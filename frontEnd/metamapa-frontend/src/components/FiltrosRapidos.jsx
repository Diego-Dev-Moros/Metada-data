import { useState, useEffect } from "react";
import "./FiltrosRapidos.css";

export default function FiltrosRapidos({ filtros, onApply }) {
    const [form, setForm] = useState({
        categoria: filtros.categoria || "",
        desde: filtros.desde || "",
        hasta: filtros.hasta || "",
        ubicacion: filtros.ubicacion || ""
    });

    // 2. Este useEffect sirve para resetear el formulario si se limpian los filtros desde fuera
    useEffect(() => {
        setForm({
            categoria: filtros.categoria || "",
            desde: filtros.desde || "",
            hasta: filtros.hasta || "",
            ubicacion: filtros.ubicacion || ""
        });
    }, [filtros]);

    // Función genérica para actualizar cualquier campo
    const update = (campo, valor) => {
        setForm((prev) => ({ ...prev, [campo]: valor }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onApply(form);
    };
    // 3. Nueva función para limpiar filtros rápidamente
    const limpiar = () => {
        const filtrosLimpios = { categoria: "", desde: "", hasta: "", ubicacion: "" };
        setForm(filtrosLimpios);
        onApply(filtrosLimpios);// Aplicamos inmediatamente la limpieza
    };

    return (
        <form className="panel-filtros" onSubmit={handleSubmit}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px'}}>
            <h3>Filtros Rápidos</h3>
            {/* Botón pequeño para limpiar */}
            <button
                type="button"
                onClick={limpiar}
                style={{
                    background:'none', border:'none', cursor:'pointer',
                    color:'#666', textDecoration:'underline', fontSize:'0.85em'
                }}
            >
                Limpiar
            </button>
        </div>
            <label className="lbl">Categoría</label>
            <select
                className="in"
                value={form.categoria}
                onChange={(e) => update("categoria", e.target.value)}
            >
                <option value="">Todas</option>
                {/* Usamos mayúsculas para evitar problemas, aunque tu backend acepta ambos */}
                <option value="INCENDIO">Incendio</option>
                <option value="INUNDACION">Inundación</option>
                <option value="PROTESTA">Protesta</option>
                <option value="ACCIDENTE">Accidente</option>
            </select>

            <label className="lbl">Rango Fecha</label>
            <div className="range">
                <div className="range-field">
                    <span className="mini-label">Desde</span>
                    <input
                        type="date"
                        className="in"
                        value={form.desde}
                        onChange={(e) => update("desde", e.target.value)}
                    />
                </div>

                <div className="range-field">
                    <span className="mini-label">Hasta</span>
                    <input
                        type="date"
                        className="in"
                        value={form.hasta}
                        onChange={(e) => update("hasta", e.target.value)}
                    />
                </div>
            </div>

            <label className="lbl">Ubicación / Título</label>
            <input
                type="text"
                className="in"
                placeholder="Buscar ciudad o título..."
                value={form.ubicacion}
                onChange={(e) => update("ubicacion", e.target.value)}
            />
            <button type="submit" className="btn-apply">Aplicar filtros</button>
        </form>
    );
}
