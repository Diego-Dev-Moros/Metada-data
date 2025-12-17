package com.metamapa.dto;

import java.util.ArrayList;
import java.util.List;

public class CargaCsvResultado {
    private boolean exitoso;
    private String mensaje;
    private int procesadas;
    private Long archivoId;
    
    // Campos adicionales para compatibilidad
    private int insertadas;
    private int reemplazadas;
    private int salteadas;
    private List<String> errores = new ArrayList<>();

    public CargaCsvResultado() {}

    // Constructor para nuevo flujo (file system)
    public CargaCsvResultado(boolean exitoso, String mensaje, int procesadas, Long archivoId) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.procesadas = procesadas;
        this.archivoId = archivoId;
    }

    // Constructor original para compatibilidad
    public CargaCsvResultado(int procesadas, int insertadas, int reemplazadas, int salteadas, List<String> errores) {
        this.exitoso = true;
        this.procesadas = procesadas;
        this.insertadas = insertadas;
        this.reemplazadas = reemplazadas;
        this.salteadas = salteadas;
        this.errores = errores;
    }
    
    public CargaCsvResultado(int procesadas, int insertadas, int reemplazadas, int salteadas, List<String> errores, String mensaje) {
        this.exitoso = true;
        this.procesadas = procesadas;
        this.insertadas = insertadas;
        this.reemplazadas = reemplazadas;
        this.salteadas = salteadas;
        this.errores = errores;
        this.mensaje = mensaje;
    }

    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public int getProcesadas() { return procesadas; }
    public void setProcesadas(int procesadas) { this.procesadas = procesadas; }

    public Long getArchivoId() { return archivoId; }
    public void setArchivoId(Long archivoId) { this.archivoId = archivoId; }

    public int getInsertadas() { return insertadas; }
    public void setInsertadas(int insertadas) { this.insertadas = insertadas; }

    public int getReemplazadas() { return reemplazadas; }
    public void setReemplazadas(int reemplazadas) { this.reemplazadas = reemplazadas; }

    public int getSalteadas() { return salteadas; }
    public void setSalteadas(int salteadas) { this.salteadas = salteadas; }

    public List<String> getErrores() { return errores; }
    public void setErrores(List<String> errores) { this.errores = errores; }
}
