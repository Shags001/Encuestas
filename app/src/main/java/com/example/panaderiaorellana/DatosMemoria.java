package com.example.panaderiaorellana;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

// Gestor de almacenamiento en memoria y persistencia local mediante SharedPreferences y Gson
public class DatosMemoria {

    // Arreglo inicial con las 6 sucursales oficiales de Panadería Orellana
    public static final String[] SUCURSALES_OFICIALES = {
            "Panadería Orellana Casa Matriz",
            "Panadería Orellana Barrio Santa Bárbara",
            "Panadería Orellana Centro",
            "Panadería Orellana Ilobasco",
            "Panadería Orellana Ciudad Dolores",
            "Panadería Orellana San Isidro"
    };

    // Claves de almacenamiento SharedPreferences
    private static final String PREFS_NAME = "panaderia_orellana_prefs";
    private static final String KEY_USUARIOS = "key_usuarios";
    private static final String KEY_ENCUESTAS = "key_encuestas";
    private static final String KEY_CORTES = "key_cortes";
    private static final String KEY_SUCURSALES = "key_sucursales";

    // Listas globales estáticas en memoria
    public static ArrayList<String> listaSucursales = new ArrayList<>();

    public static ArrayList<EncuestaConfig> listaEncuestasConfig = new ArrayList<>();
    public static ArrayList<EncuestaConfig> listaEncuestas = listaEncuestasConfig;

    public static ArrayList<Usuario> listaUsuarios = new ArrayList<>();

    public static ArrayList<CorteTurno> listaCortesTurno = new ArrayList<>();
    public static ArrayList<CorteTurno> listaCortes = listaCortesTurno;

    public static ArrayList<PreguntaVaciada> listaPreguntasVaciadas = new ArrayList<>();
    public static ArrayList<String> listaNotasTurno = new ArrayList<>();

    // Variables estáticas para la sesión actual
    public static Usuario usuarioSesionActiva = null;
    public static Usuario usuarioLogueado = null;
    public static String sucursalSeleccionada = SUCURSALES_OFICIALES[0];
    public static String turnoActual = "Mañana";
    public static boolean datosCargados = false;

    // Carga los datos guardados en SharedPreferences o genera los iniciales por primera vez
    public static void inicializarDatos(Context context) {
        if (context == null) return;

        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String jsonSucursales = prefs.getString(KEY_SUCURSALES, null);
        if (jsonSucursales != null) {
            Type typeSuc = new TypeToken<ArrayList<String>>() {}.getType();
            listaSucursales = gson.fromJson(jsonSucursales, typeSuc);
        }

        if (listaSucursales == null || listaSucursales.isEmpty()) {
            listaSucursales = new ArrayList<>();
            for (String s : SUCURSALES_OFICIALES) {
                listaSucursales.add(s);
            }
        }

        String jsonUsuarios = prefs.getString(KEY_USUARIOS, null);
        if (jsonUsuarios != null) {
            Type typeUsuarios = new TypeToken<ArrayList<Usuario>>() {}.getType();
            listaUsuarios = gson.fromJson(jsonUsuarios, typeUsuarios);
        }

        if (listaUsuarios == null || listaUsuarios.isEmpty()) {
            listaUsuarios = new ArrayList<>();
            listaUsuarios.add(new Usuario("Carlos Orellana", "admin", "1234", SUCURSALES_OFICIALES[0], true));
            listaUsuarios.add(new Usuario("María López", "mlopez", "12345", SUCURSALES_OFICIALES[3], true));
        }

        String jsonEncuestas = prefs.getString(KEY_ENCUESTAS, null);
        if (jsonEncuestas != null) {
            Type typeEncuestas = new TypeToken<ArrayList<EncuestaConfig>>() {}.getType();
            listaEncuestasConfig = gson.fromJson(jsonEncuestas, typeEncuestas);
            listaEncuestas = listaEncuestasConfig;
        }

        if (listaEncuestasConfig == null || listaEncuestasConfig.isEmpty()) {
            listaEncuestasConfig = new ArrayList<>();
            listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Casa Matriz", SUCURSALES_OFICIALES[0], "https://forms.google.com/sample_matriz", true));
            listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Santa Bárbara", SUCURSALES_OFICIALES[1], "https://forms.google.com/sample_santabarbara", false));
            listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Centro", SUCURSALES_OFICIALES[2], "https://forms.google.com/sample_centro", false));
            listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Ilobasco", SUCURSALES_OFICIALES[3], "https://forms.google.com/sample_ilobasco", false));
            listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - Ciudad Dolores", SUCURSALES_OFICIALES[4], "https://forms.google.com/sample_dolores", false));
            listaEncuestasConfig.add(new EncuestaConfig("Encuesta de Servicio - San Isidro", SUCURSALES_OFICIALES[5], "https://forms.google.com/sample_sanisidro", false));
            listaEncuestas = listaEncuestasConfig;
        }

        String jsonCortes = prefs.getString(KEY_CORTES, null);
        if (jsonCortes != null) {
            Type typeCortes = new TypeToken<ArrayList<CorteTurno>>() {}.getType();
            listaCortesTurno = gson.fromJson(jsonCortes, typeCortes);
            listaCortes = listaCortesTurno;
        }

        if (listaCortesTurno == null || listaCortesTurno.isEmpty()) {
            listaCortesTurno = new ArrayList<>();
            CorteTurno corteMatriz = new CorteTurno("Encuesta de Servicio - Casa Matriz", SUCURSALES_OFICIALES[0], "Mañana", 18);
            corteMatriz.getPreguntas().add(new PreguntaVaciada("Amabilidad del Personal", "Escala 1-10", 0, 0, 9.0));
            corteMatriz.getPreguntas().add(new PreguntaVaciada("Calidad del Pan Dulce", "Escala 1-10", 0, 0, 9.4));
            corteMatriz.getPreguntas().add(new PreguntaVaciada("Rapidez en Caja", "Escala 1-10", 0, 0, 8.2));
            corteMatriz.getPreguntas().add(new PreguntaVaciada("¿Fue atendido con amabilidad?", "Sí/No", 17, 1, 0.0));
            corteMatriz.getNotasIncidencias().add("Fila moderada de 8:30 AM a 9:15 AM por hora pico de desayuno.");
            listaCortesTurno.add(corteMatriz);
            listaCortes = listaCortesTurno;
        }

        datosCargados = true;
        guardarTodo(context);
    }

    public static void guardarSucursales(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_SUCURSALES, gson.toJson(listaSucursales));
        editor.apply();
    }

    public static void guardarEncuestas(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_ENCUESTAS, gson.toJson(listaEncuestasConfig));
        editor.apply();
    }

    public static void guardarUsuarios(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_USUARIOS, gson.toJson(listaUsuarios));
        editor.apply();
    }

    public static void guardarCortes(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_CORTES, gson.toJson(listaCortesTurno));
        editor.apply();
    }

    public static void guardarTodo(Context context) {
        guardarSucursales(context);
        guardarEncuestas(context);
        guardarUsuarios(context);
        guardarCortes(context);
    }

    // Retorna o crea el corte específico asociando nombre de encuesta, sucursal y turno de forma única
    public static CorteTurno getCorte(String nombreEncuesta, String sucursal, String turno) {
        if (nombreEncuesta == null) nombreEncuesta = "Encuesta de Servicio";
        if (sucursal == null) sucursal = SUCURSALES_OFICIALES[0];
        if (turno == null) turno = "Mañana";

        for (CorteTurno c : listaCortesTurno) {
            if (c.getNombreEncuesta().trim().equalsIgnoreCase(nombreEncuesta.trim())
                    && c.getNombreSucursal().trim().equalsIgnoreCase(sucursal.trim())
                    && c.getTurno().trim().equalsIgnoreCase(turno.trim())) {
                return c;
            }
        }

        CorteTurno nuevoCorte = new CorteTurno(nombreEncuesta, sucursal, turno, 0);
        listaCortesTurno.add(nuevoCorte);
        return nuevoCorte;
    }

    // Sobrecarga por compatibilidad con sucursal y turno
    public static CorteTurno getCorte(String sucursal, String turno) {
        if (sucursal == null) sucursal = SUCURSALES_OFICIALES[0];
        if (turno == null) turno = "Mañana";

        for (CorteTurno c : listaCortesTurno) {
            if (c.getNombreSucursal().trim().equalsIgnoreCase(sucursal.trim())
                    && c.getTurno().trim().equalsIgnoreCase(turno.trim())) {
                return c;
            }
        }

        CorteTurno nuevoCorte = new CorteTurno("Encuesta de Servicio", sucursal, turno, 0);
        listaCortesTurno.add(nuevoCorte);
        return nuevoCorte;
    }

    public static int verificarCredenciales(String usuario, String password) {
        for (Usuario u : listaUsuarios) {
            if (u.getUsuario().equalsIgnoreCase(usuario) && u.getPassword().equals(password)) {
                if (u.isActivo()) {
                    usuarioSesionActiva = u;
                    usuarioLogueado = u;
                    sucursalSeleccionada = u.getSucursal();
                    return 1;
                } else {
                    return 2;
                }
            }
        }
        return 0;
    }

    public static EncuestaConfig getEncuestaActiva() {
        for (EncuestaConfig ec : listaEncuestasConfig) {
            if (ec.isActiva()) {
                return ec;
            }
        }
        return listaEncuestasConfig.isEmpty() ? null : listaEncuestasConfig.get(0);
    }
}