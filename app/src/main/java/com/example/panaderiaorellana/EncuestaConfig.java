package com.example.panaderiaorellana;

// Modelo para gestionar los formularios y enlaces de Google Forms en memoria
public class EncuestaConfig {
    private String nombreEncuesta;
    private String sucursal;
    private String enlaceGoogleForms;
    private boolean activa;

    public EncuestaConfig(String nombreEncuesta, String sucursal, String enlaceGoogleForms, boolean activa) {
        this.nombreEncuesta = nombreEncuesta;
        this.sucursal = sucursal;
        this.enlaceGoogleForms = enlaceGoogleForms;
        this.activa = activa;
    }

    public String getNombreEncuesta() {
        return nombreEncuesta;
    }

    public void setNombreEncuesta(String nombreEncuesta) {
        this.nombreEncuesta = nombreEncuesta;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getEnlaceGoogleForms() {
        return enlaceGoogleForms;
    }

    public void setEnlaceGoogleForms(String enlaceGoogleForms) {
        this.enlaceGoogleForms = enlaceGoogleForms;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}