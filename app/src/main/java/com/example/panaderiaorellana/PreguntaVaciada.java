package com.example.panaderiaorellana;

// Modelo para guardar cada pregunta vaciada del resumen de Google Forms
public class PreguntaVaciada {
    private String pregunta;
    private String tipoPregunta; // "Escala 1-5", "Escala 1-10", "Sí/No", "Opciones"
    private int conteoSi;
    private int conteoNo;
    private double notaEscala;

    public PreguntaVaciada(String pregunta, String tipoPregunta, int conteoSi, int conteoNo, double notaEscala) {
        this.pregunta = pregunta;
        this.tipoPregunta = tipoPregunta;
        this.conteoSi = conteoSi;
        this.conteoNo = conteoNo;
        this.notaEscala = notaEscala;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getTipoPregunta() {
        return tipoPregunta;
    }

    public void setTipoPregunta(String tipoPregunta) {
        this.tipoPregunta = tipoPregunta;
    }

    public int getConteoSi() {
        return conteoSi;
    }

    public void setConteoSi(int conteoSi) {
        this.conteoSi = conteoSi;
    }

    public int getConteoNo() {
        return conteoNo;
    }

    public void setConteoNo(int conteoNo) {
        this.conteoNo = conteoNo;
    }

    public double getNotaEscala() {
        return notaEscala;
    }

    public void setNotaEscala(double notaEscala) {
        this.notaEscala = notaEscala;
    }

    // Normaliza cualquier nota a una escala estándar de 1 a 10 para promediar justamente
    public double getNotaNormalizada10() {
        if (tipoPregunta.contains("1-5")) {
            return notaEscala * 2.0; // Multiplica por 2 (ej. 5/5 -> 10/10, 3/5 -> 6/10)
        }
        return notaEscala;
    }

    // Retorna el porcentaje de aprobación para preguntas Sí/No
    public double getPorcentajeSi() {
        int total = conteoSi + conteoNo;
        if (total == 0) return 0.0;
        return (conteoSi * 100.0) / total;
    }
}