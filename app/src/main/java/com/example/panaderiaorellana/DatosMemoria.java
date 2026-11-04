package com.example.panaderiaorellana;

import java.util.ArrayList;
import java.util.List;

// Clase global para manejar nuestros arreglos y base de datos en memoria
public class DatosMemoria {

    // Lista de usuarios registrados
    public static List<Usuario> listaUsuarios = new ArrayList<>();

    // Catálogo de encuestas de Google Forms por sucursal
    public static List<EncuestaConfig> listaEncuestasConfig = new ArrayList<>();

    // Lista de cortes organizados por sucursal y turno
    public static List<CorteTurno> listaCortesTurno = new ArrayList<>();

    // Turno actualmente seleccionado ("Mañana" o "Tarde")
    public static String turnoActual = "Mañana";

    // Usuario que tiene la sesión activa
    public static Usuario usuarioSesionActiva = null;

    // Inicializamos datos por defecto estructurados por sucursal
    static {
        // Usuarios iniciales
        listaUsuarios.add(new Usuario("Carlos Orellana", "admin", "1234", "Panadería Orellana Casa Matriz", true));
        listaUsuarios.add(new Usuario("María López", "mlopez", "12345", "Panadería Orellana Ilobasco", true));

        // 1. Casa Matriz
        listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Casa Matriz", "Panadería Orellana Casa Matriz", "https://forms.google.com/sample_matriz", true));
        CorteTurno corteMatrizManana = new CorteTurno("Panadería Orellana Casa Matriz", "Mañana", 18);
        corteMatrizManana.getPreguntas().add(new PreguntaVaciada("Amabilidad del Personal", "Escala 1-10", 0, 0, 9.0));
        corteMatrizManana.getPreguntas().add(new PreguntaVaciada("Calidad del Pan Dulce", "Escala 1-10", 0, 0, 9.4));
        corteMatrizManana.getPreguntas().add(new PreguntaVaciada("Rapidez en Caja", "Escala 1-10", 0, 0, 8.2));
        corteMatrizManana.getPreguntas().add(new PreguntaVaciada("¿Fue atendido con amabilidad?", "Sí/No", 17, 1, 0.0));
        corteMatrizManana.getNotasIncidencias().add("Fila moderada de 8:30 AM a 9:15 AM por hora pico de desayuno.");
        listaCortesTurno.add(corteMatrizManana);

        // 2. Barrio Santa Bárbara
        listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Santa Bárbara", "Panadería Orellana Barrio Santa Bárbara", "https://forms.google.com/sample_santabarbara", false));
        CorteTurno corteSantaBarbara = new CorteTurno("Panadería Orellana Barrio Santa Bárbara", "Mañana", 12);
        corteSantaBarbara.getPreguntas().add(new PreguntaVaciada("Amabilidad del Personal", "Escala 1-10", 0, 0, 8.5));
        corteSantaBarbara.getPreguntas().add(new PreguntaVaciada("Calidad del Pan Dulce", "Escala 1-10", 0, 0, 8.8));
        corteSantaBarbara.getPreguntas().add(new PreguntaVaciada("Limpieza del Establecimiento", "Escala 1-10", 0, 0, 9.1));
        corteSantaBarbara.getNotasIncidencias().add("Operación normal sin retrasos en caja.");
        listaCortesTurno.add(corteSantaBarbara);

        // 3. Centro
        listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Centro", "Panadería Orellana Centro", "https://forms.google.com/sample_centro", false));

        // 4. Ilobasco
        listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Ilobasco", "Panadería Orellana Ilobasco", "https://forms.google.com/sample_ilobasco", false));
        CorteTurno corteIlobasco = new CorteTurno("Panadería Orellana Ilobasco", "Mañana", 10);
        corteIlobasco.getPreguntas().add(new PreguntaVaciada("Amabilidad del Personal", "Escala 1-10", 0, 0, 6.5));
        corteIlobasco.getPreguntas().add(new PreguntaVaciada("Rapidez en Caja", "Escala 1-10", 0, 0, 5.8));
        corteIlobasco.getNotasIncidencias().add("Falla temporal en terminal POS de cobro de 9:00 AM a 9:45 AM.");
        listaCortesTurno.add(corteIlobasco);

        // 5. Ciudad Dolores
        listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Ciudad Dolores", "Panadería Orellana Ciudad Dolores", "https://forms.google.com/sample_dolores", false));

        // 6. San Isidro
        listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - San Isidro", "Panadería Orellana San Isidro", "https://forms.google.com/sample_sanisidro", false));
    }

    // Retorna o crea el corte específico de un turno y sucursal
    public static CorteTurno getCorte(String sucursal, String turno) {
        for (CorteTurno c : listaCortesTurno) {
            if (c.getNombreSucursal().equalsIgnoreCase(sucursal) && c.getTurno().equalsIgnoreCase(turno)) {
                return c;
            }
        }
        // Si no existe, creamos uno nuevo para esa sucursal y turno
        CorteTurno nuevoCorte = new CorteTurno(sucursal, turno, 0);
        listaCortesTurno.add(nuevoCorte);
        return nuevoCorte;
    }

    // Valida credenciales de login distinguiendo si la cuenta está activa o inactiva
    public static int verificarCredenciales(String usuario, String password) {
        for (Usuario u : listaUsuarios) {
            if (u.getUsuario().equalsIgnoreCase(usuario) && u.getPassword().equals(password)) {
                if (u.isActivo()) {
                    usuarioSesionActiva = u;
                    return 1; // Acceso Correcto
                } else {
                    return 2; // Cuenta Inactiva
                }
            }
        }
        return 0; // Credenciales Incorrectas
    }

    // Retorna la encuesta marcada como activa
    public static EncuestaConfig getEncuestaActiva() {
        for (EncuestaConfig ec : listaEncuestasConfig) {
            if (ec.isActiva()) {
                return ec;
            }
        }
        return listaEncuestasConfig.isEmpty() ? null : listaEncuestasConfig.get(0);
    }
}