import { useState } from "react";
import "./NuevaColeccionPage.css";

export default function NuevaColeccionPage() {
    const [titulo, setTitulo] = useState("");
    const [descripcion, setDescripcion] = useState("");
    const [algoritmo, setAlgoritmo] = useState("");

    // fuentes
    const [fuentes, setFuentes] = useState({
        estatica: false,
        dinamica: false,
        proxy: false,
    });

    // criterios activados
    const [critCategoria, setCritCategoria] = useState(false);
    const [critUbicacion, setCritUbicacion] = useState(false);
    const [critDescripcion, setCritDescripcion] = useState(false);
    const [critTitulo, setCritTitulo] = useState(false);
    const [critFechaAcont, setCritFechaAcont] = useState(false);
    const [critFechaCarga, setCritFechaCarga] = useState(false);
    const [critOrigen, setCritOrigen] = useState(false);

    // valores de los criterios
    const [categoriaValor, setCategoriaValor] = useState("");
    const [ubicacionValor, setUbicacionValor] = useState("");
    const [descripcionValor, setDescripcionValor] = useState("");
    const [tituloContiene, setTituloContiene] = useState("");
    const [fechaAcontDesde, setFechaAcontDesde] = useState("");
    const [fechaAcontHasta, setFechaAcontHasta] = useState("");
    const [fechaCargaDesde, setFechaCargaDesde] = useState("");
    const [fechaCargaHasta, setFechaCargaHasta] = useState("");
    const [origenValor, setOrigenValor] = useState("");

    const handleFuenteChange = (name) => {
        setFuentes((prev) => ({
            ...prev,
            [name]: !prev[name],
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // 1. armo array de criterios en el formato del dominio
        const criterios = [];

        if (critCategoria && categoriaValor.trim() !== "") {
            criterios.push({
                tipo: "CATEGORIA",             // mapea a CriterioCategoria
                categoria: categoriaValor,
            });
        }

        if (critUbicacion && ubicacionValor.trim() !== "") {
            criterios.push({
                tipo: "UBICACION",             // mapea a CriterioUbicacion
                lugar: ubicacionValor,
            });
        }

        if (critDescripcion && descripcionValor.trim() !== "") {
            criterios.push({
                tipo: "DESCRIPCION",           // mapea a CriterioDescripcion
                descripcion: descripcionValor,
            });
        }

        if (critTitulo && tituloContiene.trim() !== "") {
            criterios.push({
                tipo: "TITULO",                // mapea a CriterioTitulo
                tituloContiene,
            });
        }

        if (critFechaAcont && (fechaAcontDesde || fechaAcontHasta)) {
            criterios.push({
                tipo: "FECHA_ACONTECIMIENTO",  // CriterioFechaAcontecimiento
                fechaInicial: fechaAcontDesde || null,
                fechaFinal: fechaAcontHasta || null,
            });
        }

        if (critFechaCarga && (fechaCargaDesde || fechaCargaHasta)) {
            criterios.push({
                tipo: "FECHA_CARGA",           // CriterioFechaCarga
                fechaCargaDesde: fechaCargaDesde || null,
                fechaCargaHasta: fechaCargaHasta || null,
            });
        }

        if (critOrigen && origenValor) {
            criterios.push({
                tipo: "ORIGEN_HECHO",          // CriterioOrigenHecho
                tipoOrigen: origenValor,       // ESTATICA | DINAMICA | PROXY
            });
        }

        // 2. fuentes seleccionadas
        const fuentesSeleccionadas = Object.entries(fuentes)
            .filter(([, v]) => v)
            .map(([k]) => k.toUpperCase()); // ["ESTATICA", "DINAMICA", ...]

        // 3. payload final
        const payload = {
            titulo,
            descripcion,
            algoritmoDeConsenso: algoritmo,
            criterios,
            fuentes: fuentesSeleccionadas,
        };

        console.log("Payload de nueva colección:", payload);

        // TODO: POST al agregador
        // fetch("http://localhost:8081/colecciones", {
        //   method: "POST",
        //   headers: { "Content-Type": "application/json" },
        //   body: JSON.stringify(payload),
        // });

        alert("Colección creada (simulado). Revisá el console.log()");
    };

    return (
        <div className="nueva-coleccion-page">
            <h1>Nueva Colección</h1>

            <form className="nueva-coleccion-form" onSubmit={handleSubmit}>
                {/* título */}
                <div className="form-group">
                    <label>Título de la colección</label>
                    <input
                        type="text"
                        value={titulo}
                        onChange={(e) => setTitulo(e.target.value)}
                        required
                    />
                </div>

                {/* descripción */}
                <div className="form-group">
                    <label>Descripción de la colección</label>
                    <textarea
                        rows={3}
                        value={descripcion}
                        onChange={(e) => setDescripcion(e.target.value)}
                    />
                </div>

                {/* fila: algoritmo + fuentes */}
                <div className="form-row">
                    <div className="form-group half">
                        <label>Algoritmo de Consenso</label>
                        <select
                            value={algoritmo}
                            onChange={(e) => setAlgoritmo(e.target.value)}
                        >
                            <option value="">Sin algoritmo (Por defecto)</option>
                            <option value="ABSOLUTA">Absoluta</option>
                            <option value="MAYORIA_SIMPLE">Mayoría simple</option>
                            <option value="MULTIPLES_MENCIONES">Múltiples menciones</option>
                        </select>
                    </div>

                    <div className="form-group half">
                        <label>Fuentes</label>
                        <div className="fuentes-group">
                            <label>
                                <input
                                    type="checkbox"
                                    checked={fuentes.estatica}
                                    onChange={() => handleFuenteChange("estatica")}
                                />
                                ESTÁTICA
                            </label>
                            <label>
                                <input
                                    type="checkbox"
                                    checked={fuentes.dinamica}
                                    onChange={() => handleFuenteChange("dinamica")}
                                />
                                DINÁMICA
                            </label>
                            <label>
                                <input
                                    type="checkbox"
                                    checked={fuentes.proxy}
                                    onChange={() => handleFuenteChange("proxy")}
                                />
                                PROXY
                            </label>
                        </div>
                    </div>
                </div>

                {/* CRITERIOS DE PERTENENCIA */}
                <div className="criterios-box">
                    <h2>Criterios de pertenencia</h2>

                    {/* Criterio categoría */}
                    <div className="criterio-line">
                        <label className="check-inline">
                            <input
                                type="checkbox"
                                checked={critCategoria}
                                onChange={() => setCritCategoria((v) => !v)}
                            />
                            Por categoría
                        </label>
                        {critCategoria && (
                            <input
                                type="text"
                                className="criterio-input"
                                placeholder="Ej: Ambiente, Incendio..."
                                value={categoriaValor}
                                onChange={(e) => setCategoriaValor(e.target.value)}
                            />
                        )}
                    </div>

                    {/* Criterio ubicación */}
                    <div className="criterio-line">
                        <label className="check-inline">
                            <input
                                type="checkbox"
                                checked={critUbicacion}
                                onChange={() => setCritUbicacion((v) => !v)}
                            />
                            Por ubicación
                        </label>
                        {critUbicacion && (
                            <input
                                type="text"
                                className="criterio-input"
                                placeholder="Ej: CABA, Rosario, Córdoba..."
                                value={ubicacionValor}
                                onChange={(e) => setUbicacionValor(e.target.value)}
                            />
                        )}
                    </div>

                    {/* Criterio descripción */}
                    <div className="criterio-line">
                        <label className="check-inline">
                            <input
                                type="checkbox"
                                checked={critDescripcion}
                                onChange={() => setCritDescripcion((v) => !v)}
                            />
                            Que la descripción contenga...
                        </label>
                        {critDescripcion && (
                            <input
                                type="text"
                                className="criterio-input"
                                placeholder="Texto a buscar en la descripción"
                                value={descripcionValor}
                                onChange={(e) => setDescripcionValor(e.target.value)}
                            />
                        )}
                    </div>

                    {/* Criterio título */}
                    <div className="criterio-line">
                        <label className="check-inline">
                            <input
                                type="checkbox"
                                checked={critTitulo}
                                onChange={() => setCritTitulo((v) => !v)}
                            />
                            Que el título contenga...
                        </label>
                        {critTitulo && (
                            <input
                                type="text"
                                className="criterio-input"
                                placeholder="Ej: incendio, derrame..."
                                value={tituloContiene}
                                onChange={(e) => setTituloContiene(e.target.value)}
                            />
                        )}
                    </div>

                    {/* Criterio fecha acontecimiento */}
                    <div className="criterio-line">
                        <label className="check-inline">
                            <input
                                type="checkbox"
                                checked={critFechaAcont}
                                onChange={() => setCritFechaAcont((v) => !v)}
                            />
                            Fecha de acontecimiento
                        </label>
                        {critFechaAcont && (
                            <div className="criterio-rango">
                                <input
                                    type="date"
                                    value={fechaAcontDesde}
                                    onChange={(e) => setFechaAcontDesde(e.target.value)}
                                />
                                <span>⇄</span>
                                <input
                                    type="date"
                                    value={fechaAcontHasta}
                                    onChange={(e) => setFechaAcontHasta(e.target.value)}
                                />
                            </div>
                        )}
                    </div>

                    {/* Criterio fecha carga */}
                    <div className="criterio-line">
                        <label className="check-inline">
                            <input
                                type="checkbox"
                                checked={critFechaCarga}
                                onChange={() => setCritFechaCarga((v) => !v)}
                            />
                            Fecha de carga
                        </label>
                        {critFechaCarga && (
                            <div className="criterio-rango">
                                <input
                                    type="date"
                                    value={fechaCargaDesde}
                                    onChange={(e) => setFechaCargaDesde(e.target.value)}
                                />
                                <span>⇄</span>
                                <input
                                    type="date"
                                    value={fechaCargaHasta}
                                    onChange={(e) => setFechaCargaHasta(e.target.value)}
                                />
                            </div>
                        )}
                    </div>

                    {/* Criterio origen */}
                    <div className="criterio-line">
                        <label className="check-inline">
                            <input
                                type="checkbox"
                                checked={critOrigen}
                                onChange={() => setCritOrigen((v) => !v)}
                            />
                            Origen del hecho
                        </label>
                        {critOrigen && (
                            <select
                                className="criterio-input"
                                value={origenValor}
                                onChange={(e) => setOrigenValor(e.target.value)}
                            >
                                <option value="">Seleccionar origen</option>
                                <option value="CARGA MANUAL">Carga manual</option>
                                <option value="DATASET">Dataset</option>
                                <option value="CONTRIBUYENTE">Contribuyente</option>
                                <option value="CONTRIBUCION ANONIMA">Contribución anónima</option>

                            </select>
                        )}
                    </div>
                </div>

                <div className="form-actions">
                    <button type="submit" className="btn-publicar">
                        Publicar colección
                    </button>
                </div>
            </form>
        </div>
    );
}
