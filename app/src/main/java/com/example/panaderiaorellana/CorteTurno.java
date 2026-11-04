package com.example.panaderiaorellana;

import java.util.ArrayList;
import java.util.List;

// Modelo que representa el corte de un turno específico para una sucursal y encuesta
public class CorteTurno {
    private String nombreSucursal;
    private String turno; // "Mañana" o "Tarde"
    private int totalRespuestas;
    private List<PreguntaVaciada> preguntas;
    private List<String> notasIncidencias;

    public CorteTurno(String nombreSucursal, String turno, int totalRespuestas) {
        this.nombreSucursal = nombreSucursal;
        this.turno = turno;
        this.totalRespuestas = totalRespuestas;
        this.preguntas = new ArrayList<>();
        this.notasIncidencias = new ArrayList<>();
    }

    public String getNombreSucursal() {
        return nombreSucursal;
    }

    public void setNombreSucursal(String nombreSucursal) {
        this.nombreSucursal = nombreSucursal;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public int getTotalRespuestas() {
        return totalRespuestas;
    }

    public void setTotalRespuestas(int totalRespuestas) {
        this.totalRespuestas = totalRespuestas;
    }

    public List<PreguntaVaciada> getPreguntas() {
        return preguntas;
    }

    public List<String> getNotasIncidencias() {
        return notasIncidencias;
    }

    // Calcula el promedio general normalizado en escala estándar de 1 a 10 (100%)
    public double getPromedioGeneral() {
        double suma = 0;
        int cantidad = 0;

        for (PreguntaVaciada p : preguntas) {
            if (p.getNotaEscala() > 0) {
                suma += p.getNotaNormalizada10();
                cantidad++;
            }
        }

        if (cantidad == 0) return 0.0;
        return suma / cantidad;
    }
}