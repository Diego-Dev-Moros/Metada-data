import React, { useState, useEffect } from "react";
import "./ImportarDatasetPage.css";

const ImportarDatasetPage = () => {
    const [archivo, setArchivo] = useState(null);
    const [tipoDataset, setTipoDataset] = useState("csv");
    const [estado, setEstado] = useState("");
    const [progreso, setProgreso] = useState(0);
    const [importaciones, setImportaciones] = useState([]);
    const [cargandoHistorial, setCargandoHistorial] = useState(false);

    // Cargar historial de importaciones al montar el componente
    useEffect(() => {
        cargarHistorial();
    }, []);

    const cargarHistorial = async () => {
        setCargandoHistorial(true);
        try {
            const response = await fetch("http://localhost:8080/api/admin/importaciones");
            
            if (response.ok) {
                const archivos = await response.json();
                
                // Mapear ArchivoDatasetMongo a formato esperado por la tabla
                const importacionesMapeadas = archivos.map(archivo => ({
                    id: archivo.id,
                    fecha: new Date(archivo.fechaCarga).toLocaleString('es-AR'),
                    nombreArchivo: archivo.nombreArchivo,
                    urlArchivo: `/uploads/datasets/${archivo.nombreArchivo}`, // TODO: Obtener URL real
                    hechosImportados: archivo.hechoInsertados || 0,
                    estado: archivo.estado === "PROCESADO" ? "exitoso" : "fallido",
                    filasProcesadas: archivo.filasProcesadas,
                    filasReemplazadas: archivo.hechosReemplazados,
                    filasSalteadas: archivo.filasSalteadas,
                    errores: archivo.errores
                }));
                
                setImportaciones(importacionesMapeadas);
            } else {
                console.error("Error al cargar historial de importaciones");
            }
        } catch (error) {
            console.error("Error al cargar historial:", error);
        } finally {
            setCargandoHistorial(false);
        }
    };

    const handleArchivoChange = (e) => {
        const file = e.target.files[0];
        setArchivo(file);
        setEstado("");
    };

    const handleImportar = async () => {
        if (!archivo) {
            alert("Por favor selecciona un archivo");
            return;
        }

        setEstado("importando");
        setProgreso(0);

        try {
            const formData = new FormData();
            formData.append("file", archivo);

            // Simulación de progreso mientras se sube
            const progressInterval = setInterval(() => {
                setProgreso(prev => Math.min(prev + 20, 90));
            }, 300);

            const response = await fetch("http://localhost:8080/api/admin/importar-dataset", {
                method: "POST",
                headers: {
                    "X-Admin-Id": "1" // TODO: Obtener del contexto de usuario logueado
                },
                body: formData
            });

            clearInterval(progressInterval);

            if (response.ok) {
                const resultado = await response.json();
                setProgreso(100);
                setEstado("completado");
                
                // Recargar historial después de importar
                await cargarHistorial();
                
                setTimeout(() => {
                    setArchivo(null);
                    setEstado("");
                    setProgreso(0);
                }, 3000);
            } else {
                const error = await response.json();
                setEstado("error");
                alert(`Error al importar: ${error.error || "Error desconocido"}`);
            }
        } catch (error) {
            console.error("Error al importar dataset:", error);
            setEstado("error");
            alert("Error de conexión al importar el dataset");
        }
    };

    return (
        <div className="importar-dataset-page">
            <div className="importar-container">
                <h1>Importar Dataset</h1>
                <p className="subtitulo">Carga masiva de hechos históricos desde archivos externos</p>

                <div className="form-importar">
                    <div className="form-group">
                        <label>Tipo de archivo:</label>
                        <select 
                            value={tipoDataset} 
                            onChange={(e) => setTipoDataset(e.target.value)}
                        >
                            <option value="json">JSON</option>
                            <option value="csv">CSV</option>
                            <option value="xml">XML</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Seleccionar archivo:</label>
                        <input 
                            type="file" 
                            accept={`.${tipoDataset}`}
                            onChange={handleArchivoChange}
                        />
                        {archivo && (
                            <div className="archivo-seleccionado">
                                <span>📄 {archivo.name}</span>
                                <span className="archivo-size">
                                    ({(archivo.size / 1024).toFixed(2)} KB)
                                </span>
                            </div>
                        )}
                    </div>

                    {estado === "importando" && (
                        <div className="progreso-container">
                            <div className="progreso-bar">
                                <div 
                                    className="progreso-fill" 
                                    style={{ width: `${progreso}%` }}
                                ></div>
                            </div>
                            <p>Importando... {progreso}%</p>
                        </div>
                    )}

                    {estado === "completado" && (
                        <div className="mensaje-exito">
                            ✓ Dataset importado exitosamente
                        </div>
                    )}

                    <button 
                        onClick={handleImportar}
                        disabled={!archivo || estado === "importando"}
                        className="btn-importar"
                    >
                        {estado === "importando" ? "Importando..." : "Importar Dataset"}
                    </button>
                </div>

                <div className="historial-importaciones">
                    <h2>Últimas Importaciones</h2>
                    {cargandoHistorial ? (
                        <p className="cargando">Cargando historial...</p>
                    ) : importaciones.length === 0 ? (
                        <p className="sin-importaciones">No hay importaciones registradas</p>
                    ) : (
                        <table className="tabla-importaciones">
                            <thead>
                                <tr>
                                    <th>Fecha</th>
                                    <th>Archivo</th>
                                    <th>Hechos Importados</th>
                                    <th>Estado</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                {importaciones.map((imp) => (
                                    <tr key={imp.id}>
                                        <td>{imp.fecha}</td>
                                        <td>{imp.nombreArchivo}</td>
                                        <td>{imp.hechosImportados}</td>
                                        <td>
                                            <span className={`estado-badge estado-${imp.estado}`}>
                                                {imp.estado === "exitoso" ? "Exitoso" : "Fallido"}
                                            </span>
                                        </td>
                                        <td>
                                            <a 
                                                href={imp.urlArchivo} 
                                                download
                                                className="btn-descargar"
                                                title="Descargar archivo"
                                            >
                                                ⬇ Descargar
                                            </a>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ImportarDatasetPage;
