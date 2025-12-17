package com.metamapa.entities.rol;
/**
 * Enumerado que representa todos los permisos granulares del sistema MetaMapa,
 * utilizados para la autorización de endpoints a través de Spring Security.
 */
public enum Permiso {

    // --- GESTIÓN ADMINISTRATIVA Y COLECCIONES

    COLECCION_CREAR,
    COLECCION_GESTIONAR, // Modificar o eliminar colecciones
    CONSENSO_ALGORITMO_GESTIONAR,
    FUENTES_CONFIGURAR,
    HECHO_IMPORTAR_CSV,
    SEGURIDAD_DATOS_GESTIONAR, // Gestión de denuncias y datos personales

    // --- MODERACIÓN Y SOLICITUDES DE ELIMINACIÓN

    HECHO_APROBAR_RECHAZAR, // Moderación de hechos aportados
    HECHO_SOLICITUD_ELIMINACION_GESTIONAR, // Aprobar o denegar solicitudes
    HECHO_SOLICITUD_ELIMINACION_GENERAR, // Crear una solicitud de eliminación

    // --- CONTRIBUCIÓN

    HECHO_CREAR, // Crear nuevos hechos (reportar)
    HECHO_EDITAR_PROPIO,
    HECHO_REPORTAR, // Reportar un hecho por inapropiado

    // --- CONSULTA Y VISUALIZACIÓN

    HECHO_NAVEGAR_FILTRAR,
    HECHO_MODO_NAVEGACION_SELECCIONAR,
    HECHO_OBTENER_FUENTES_DINAMICAS,
    HECHO_VISUALIZAR_MAPA
}