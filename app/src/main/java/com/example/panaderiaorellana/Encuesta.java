package com.example.panaderiaorellana;

// Modelo para guardar las encuestas de satisfacción en memoria
public class Encuesta {
    private int calificacionAtencion;
    private int calificacionCalidad;
    private int calificacionLimpieza;
    private String comentarios;
    private String sucursal;

    public Encuesta(int calificacionAtencion, int calificacionCalidad, int calificacionLimpieza, String comentarios, String sucursal) {
        this.calificacionAtencion = calificacionAtencion;
        this.calificacionCalidad = calificacionCalidad;
        this.calificacionLimpieza = calificacionLimpieza;
        this.comentarios = comentarios;
        this.sucursal = sucursal;
    }

    public int getCalificacionAtencion() {
        return calificacionAtencion;
    }

    public void setCalificacionAtencion(int calificacionAtencion) {
        this.calificacionAtencion = calificacionAtencion;
    }

    public int getCalificacionCalidad() {
        return calificacionCalidad;
    }

    public void setCalificacionCalidad(int calificacionCalidad) {
        this.calificacionCalidad = calificacionCalidad;
    }

    public int getCalificacionLimpieza() {
        return calificacionLimpieza;
    }

    public void setCalificacionLimpieza(int calificacionLimpieza) {
        this.calificacionLimpieza = calificacionLimpieza;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    // Calcula el promedio general de la encuesta
    public double getPromedio() {
        return (calificacionAtencion + calificacionCalidad + calificacionLimpieza) / 3.0;
    }
}