package com.example.panaderiaorellana;

// Modelo para guardar los datos de los usuarios en memoria
public class Usuario {
    private String nombre;
    private String usuario;
    private String password;
    private String sucursal;
    private boolean activo;

    public Usuario(String nombre, String usuario, String password, String sucursal, boolean activo) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.password = password;
        this.sucursal = sucursal;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}