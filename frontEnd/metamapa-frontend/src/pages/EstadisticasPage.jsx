import React, { useState, useEffect } from "react";
import "./EstadisticasPage.css";

const EstadisticasPage = () => {
    const [estadisticas, setEstadisticas] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // TODO: Cargar estadísticas desde el backend
        const estadisticasMock = {
            totalHechos: 1250,
            totalColecciones: 45,
            totalUsuarios: 320,
            hechosPendientes: 28,
            solicitudesPendientes: 12,
            visitasUltimoMes: 15420,
            hechosPorCategoria: [
                { categoria: "Histórico", cantidad: 450 },
                { categoria: "Social", cantidad: 380 },
                { categoria: "Cultural", cantidad: 250 },
                { categoria: "Político", cantidad: 170 },
            ],
            hechosPorMes: [
                { mes: "Enero", cantidad: 95 },
                { mes: "Febrero", cantidad: 120 },
                { mes: "Marzo", cantidad: 145 },
            ],
        };
        
        setTimeout(() => {
            setEstadisticas(estadisticasMock);
            setLoading(false);
        }, 500);
    }, []);

    if (loading) {
        return <div className="estadisticas-page">Cargando estadísticas...</div>;
    }

    return (
        <div className="estadisticas-page">
            <div className="estadisticas-container">
                <h1>Estadísticas de MetaMapa</h1>
                <p className="subtitulo">Datos y métricas generales de la plataforma</p>

                {/* Tarjetas de resumen */}
                <div className="stats-grid-main">
                    <div className="stat-card-main">
                        <div className="stat-icon">📍</div>
                        <div className="stat-content">
                            <div className="stat-number">{estadisticas.totalHechos}</div>
                            <div className="stat-label">Total de Hechos</div>
                        </div>
                    </div>

                    <div className="stat-card-main">
                        <div className="stat-icon">📚</div>
                        <div className="stat-content">
                            <div className="stat-number">{estadisticas.totalColecciones}</div>
                            <div className="stat-label">Colecciones</div>
                        </div>
                    </div>

                    <div className="stat-card-main">
                        <div className="stat-icon">👥</div>
                        <div className="stat-content">
                            <div className="stat-number">{estadisticas.totalUsuarios}</div>
                            <div className="stat-label">Usuarios Registrados</div>
                        </div>
                    </div>

                    <div className="stat-card-main">
                        <div className="stat-icon">👁️</div>
                        <div className="stat-content">
                            <div className="stat-number">{estadisticas.visitasUltimoMes.toLocaleString()}</div>
                            <div className="stat-label">Visitas este Mes</div>
                        </div>
                    </div>
                </div>

                {/* Hechos por categoría */}
                <div className="stats-section">
                    <h2>Hechos por Categoría</h2>
                    <div className="chart-container">
                        {estadisticas.hechosPorCategoria.map((item, index) => (
                            <div key={index} className="bar-item">
                                <div className="bar-label">{item.categoria}</div>
                                <div className="bar-wrapper">
                                    <div 
                                        className="bar-fill" 
                                        style={{ 
                                            width: `${(item.cantidad / estadisticas.totalHechos) * 100}%` 
                                        }}
                                    >
                                        <span className="bar-value">{item.cantidad}</span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Hechos por mes */}
                <div className="stats-section">
                    <h2>Hechos Creados por Mes (2025)</h2>
                    <div className="chart-container">
                        {estadisticas.hechosPorMes.map((item, index) => (
                            <div key={index} className="bar-item">
                                <div className="bar-label">{item.mes}</div>
                                <div className="bar-wrapper">
                                    <div 
                                        className="bar-fill" 
                                        style={{ width: `${(item.cantidad / 200) * 100}%` }}
                                    >
                                        <span className="bar-value">{item.cantidad}</span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Estadísticas de administración (solo para admins) */}
                {localStorage.getItem("rol") === "administrador" && (
                    <div className="stats-section admin-section">
                        <h2>Estadísticas de Administración</h2>
                        <div className="admin-stats-grid">
                            <div className="admin-stat-card pending">
                                <div className="admin-stat-number">{estadisticas.hechosPendientes}</div>
                                <div className="admin-stat-label">Hechos Pendientes</div>
                            </div>
                            <div className="admin-stat-card pending">
                                <div className="admin-stat-number">{estadisticas.solicitudesPendientes}</div>
                                <div className="admin-stat-label">Solicitudes Pendientes</div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default EstadisticasPage;
