package com.example.panaderiaorellana;

// Modelo para guardar las notas y observaciones de la bitácora en memoria
public class Nota {
    private String titulo;
    private String detalle;
    private String fechaHora;
    private String autor;

    public Nota(String titulo, String detalle, String fechaHora, String autor) {
        this.titulo = titulo;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
        this.autor = autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}