package com.example.panaderiaorellana;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Vista 5: Pantalla de Estadísticas, Gráficas y Semáforo
public class EstadisticasActivity extends AppCompatActivity {

    private MaterialToolbar barraSuperior;
    private Spinner spSeleccionarEncuestaEstadisticas;

    // Sección A: Semáforo y Calidad (Escalas)
    private CardView tarjetaResumenGeneral;
    private TextView tvEtiquetaTurno;
    private TextView tvNotaPromedio;
    private View vistaSemaforo;
    private TextView tvEstadoSemaforo;
    private LinearLayout containerGraficaEscalas;

    // Sección B: Aprobación y Cumplimiento (Sí / No)
    private TextView tvPorcentajeAprobacion;
    private LinearLayout containerGraficaSiNo;

    // Sección C: Bitácora de Observaciones
    private LinearLayout containerNotasJustificantes;

    private EncuestaConfig encuestaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        DatosMemoria.inicializarDatos(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootEstadisticas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barraSuperior = findViewById(R.id.barraSuperior);
        spSeleccionarEncuestaEstadisticas = findViewById(R.id.spSeleccionarEncuestaEstadisticas);

        tarjetaResumenGeneral = findViewById(R.id.tarjetaResumenGeneral);
        tvEtiquetaTurno = findViewById(R.id.tvEtiquetaTurno);
        tvNotaPromedio = findViewById(R.id.tvNotaPromedio);
        vistaSemaforo = findViewById(R.id.vistaSemaforo);
        tvEstadoSemaforo = findViewById(R.id.tvEstadoSemaforo);
        containerGraficaEscalas = findViewById(R.id.containerGraficaEscalas);

        tvPorcentajeAprobacion = findViewById(R.id.tvPorcentajeAprobacion);
        containerGraficaSiNo = findViewById(R.id.containerGraficaSiNo);

        containerNotasJustificantes = findViewById(R.id.containerNotasJustificantes);

        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        cargarSpinnerEncuestas();
    }

    private void cargarSpinnerEncuestas() {
        List<String> nombresEncuestas = new ArrayList<>();
        for (EncuestaConfig ec : DatosMemoria.listaEncuestasConfig) {
            nombresEncuestas.add(ec.getNombreEncuesta() + " (" + ec.getSucursal() + ")");
        }

        if (nombresEncuestas.isEmpty()) {
            nombresEncuestas.add("Sin encuestas registradas");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, nombresEncuestas);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spSeleccionarEncuestaEstadisticas.setAdapter(adapter);

        int indexSucursal = 0;
        for (int i = 0; i < DatosMemoria.listaEncuestasConfig.size(); i++) {
            if (DatosMemoria.listaEncuestasConfig.get(i).getSucursal().equalsIgnoreCase(DatosMemoria.sucursalSeleccionada)) {
                indexSucursal = i;
                break;
            }
        }
        spSeleccionarEncuestaEstadisticas.setSelection(indexSucursal);

        spSeleccionarEncuestaEstadisticas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < DatosMemoria.listaEncuestasConfig.size()) {
                    encuestaSeleccionada = DatosMemoria.listaEncuestasConfig.get(position);
                    DatosMemoria.sucursalSeleccionada = encuestaSeleccionada.getSucursal();
                    cargarReporteEstadistico();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Procesa y separa limpiamente las preguntas de Escala (Semáforo) y las de Sí/No (Aprobación) para cada encuesta de forma aislada
    private void cargarReporteEstadistico() {
        if (encuestaSeleccionada == null) return;

        String nombreEncuesta = encuestaSeleccionada.getNombreEncuesta();
        String sucursal = encuestaSeleccionada.getSucursal();
        String turno = DatosMemoria.turnoActual;

        // Obtenemos el corte único asociando el nombre de encuesta + sucursal + turno
        CorteTurno corte = DatosMemoria.getCorte(nombreEncuesta, sucursal, turno);

        tvEtiquetaTurno.setText(String.format(Locale.getDefault(),
                "%s (%s)\nTurno: %s | Total Encuestados: %d",
                nombreEncuesta, sucursal, corte.getTurno(), corte.getTotalRespuestas()));

        // 1. Procesar preguntas de Escala
        procesarSeccionEscalas(corte);

        // 2. Procesar preguntas de Sí / No
        procesarSeccionSiNo(corte);

        // 3. Procesar notas de Bitácora
        procesarSeccionNotas(corte);
    }

    private boolean esPreguntaSiNo(PreguntaVaciada pv) {
        if (pv == null) return false;
        String tipo = pv.getTipoPregunta() != null ? pv.getTipoPregunta().toLowerCase() : "";
        return tipo.contains("sí") || tipo.contains("si") || tipo.contains("dicotó") || tipo.contains("dicoto")
                || (pv.getConteoSi() > 0 || pv.getConteoNo() > 0);
    }

    private void procesarSeccionEscalas(CorteTurno corte) {
        containerGraficaEscalas.removeAllViews();

        List<PreguntaVaciada> preguntasEscala = new ArrayList<>();
        for (PreguntaVaciada pv : corte.getPreguntas()) {
            if (!esPreguntaSiNo(pv)) {
                preguntasEscala.add(pv);
            }
        }

        if (preguntasEscala.isEmpty()) {
            tvNotaPromedio.setText("N/A");
            apagarSemaforo();
            tvEstadoSemaforo.setText("Sin preguntas de escala registradas en este turno");
            tvEstadoSemaforo.setTextColor(getColor(R.color.cafe_negro));

            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay preguntas de escala registradas para esta sucursal.");
            tvVacio.setTextColor(getColor(R.color.cafe_negro));
            containerGraficaEscalas.addView(tvVacio);
            return;
        }

        double promedio = corte.getPromedioGeneral();
        tvNotaPromedio.setText(String.format(Locale.getDefault(), "%.1f / 10.0", promedio));

        if (promedio >= 8.0) {
            vistaSemaforo.setBackgroundTintList(getColorStateList(R.color.semaforo_verde_on));
            tvEstadoSemaforo.setText("Servicio Óptimo - Excelente nivel de calidad");
            tvEstadoSemaforo.setTextColor(getColor(R.color.semaforo_verde_on));
        } else if (promedio >= 6.0) {
            vistaSemaforo.setBackgroundTintList(getColorStateList(R.color.semaforo_amarillo_on));
            tvEstadoSemaforo.setText("Precaución - Requiere atención en el turno");
            tvEstadoSemaforo.setTextColor(getColor(R.color.cafe_negro));
        } else {
            vistaSemaforo.setBackgroundTintList(getColorStateList(R.color.semaforo_rojo_on));
            tvEstadoSemaforo.setText("Crítico - Intervención operativa inmediata");
            tvEstadoSemaforo.setTextColor(getColor(R.color.semaforo_rojo_on));
        }

        for (PreguntaVaciada pv : preguntasEscala) {
            LinearLayout barItem = new LinearLayout(this);
            barItem.setOrientation(LinearLayout.VERTICAL);
            barItem.setPadding(0, 8, 0, 16);

            TextView tvNombre = new TextView(this);
            tvNombre.setText(pv.getPregunta() + String.format(Locale.getDefault(), " (%.1f / %s)", pv.getNotaEscala(), pv.getTipoPregunta().contains("1-10") ? "10" : "5"));
            tvNombre.setTextSize(14);
            tvNombre.setTextColor(getColor(R.color.cafe_negro));
            barItem.addView(tvNombre);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);

            int perc = (int) ((pv.getNotaNormalizada10() / 10.0) * 100);
            pb.setProgress(perc);
            pb.setProgressTintList(getColorStateList(R.color.cafe_oscuro));

            barItem.addView(pb);
            containerGraficaEscalas.addView(barItem);
        }
    }

    private void procesarSeccionSiNo(CorteTurno corte) {
        containerGraficaSiNo.removeAllViews();

        List<PreguntaVaciada> preguntasSiNo = new ArrayList<>();
        int sumaSi = 0;
        int sumaTotalVotos = 0;

        for (PreguntaVaciada pv : corte.getPreguntas()) {
            if (esPreguntaSiNo(pv)) {
                preguntasSiNo.add(pv);
                sumaSi += pv.getConteoSi();
                sumaTotalVotos += (pv.getConteoSi() + pv.getConteoNo());
            }
        }

        if (preguntasSiNo.isEmpty()) {
            tvPorcentajeAprobacion.setText("N/A");
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay preguntas de Sí / No registradas para esta sucursal.");
            tvVacio.setTextColor(getColor(R.color.cafe_negro));
            containerGraficaSiNo.addView(tvVacio);
            return;
        }

        double porcentajeAprobacionGeneral = (sumaTotalVotos > 0) ? (sumaSi * 100.0 / sumaTotalVotos) : 0.0;
        tvPorcentajeAprobacion.setText(String.format(Locale.getDefault(), "%.1f%% Aprobación General", porcentajeAprobacionGeneral));

        for (PreguntaVaciada pv : preguntasSiNo) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(0, 8, 0, 16);

            TextView tvNombre = new TextView(this);
            tvNombre.setText(pv.getPregunta() + String.format(Locale.getDefault(), " — Sí: %d | No: %d (%.1f%% Aprobación)", pv.getConteoSi(), pv.getConteoNo(), pv.getPorcentajeSi()));
            tvNombre.setTextSize(14);
            tvNombre.setTextColor(getColor(R.color.cafe_negro));
            item.addView(tvNombre);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress((int) pv.getPorcentajeSi());
            pb.setProgressTintList(getColorStateList(R.color.marron_tostado));

            item.addView(pb);
            containerGraficaSiNo.addView(item);
        }
    }

    private void procesarSeccionNotas(CorteTurno corte) {
        containerNotasJustificantes.removeAllViews();

        if (corte.getNotasIncidencias().isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay observaciones o justificantes registrados en este turno.");
            tvVacio.setTextColor(getColor(R.color.cafe_negro));
            containerNotasJustificantes.addView(tvVacio);
            return;
        }

        for (String nota : corte.getNotasIncidencias()) {
            TextView tvN = new TextView(this);
            tvN.setText("• " + nota);
            tvN.setTextSize(13);
            tvN.setTextColor(getColor(R.color.cafe_negro));
            tvN.setPadding(0, 0, 0, 8);
            containerNotasJustificantes.addView(tvN);
        }
    }

    private void apagarSemaforo() {
        vistaSemaforo.setBackgroundTintList(getColorStateList(R.color.blanco_crema));
    }
}