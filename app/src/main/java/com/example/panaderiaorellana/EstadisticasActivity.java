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

    // Toolbar y selector libre de encuesta
    private MaterialToolbar barraSuperior;
    private Spinner spSeleccionarEncuestaEstadisticas;

    // Componentes del Resumen General
    private CardView tarjetaResumenGeneral;
    private TextView tvEtiquetaTurno;
    private TextView tvNotaPromedio;
    private View vistaSemaforo;
    private TextView tvEstadoSemaforo;

    // Contenedores visuales para gráficas y desglose
    private LinearLayout graficaBarras;
    private LinearLayout rvMetricasDetalladas;

    // Encuesta seleccionada actualmente
    private EncuestaConfig encuestaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        // Respetamos la barra de estado superior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootEstadisticas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlazamos las vistas con sus IDs
        barraSuperior = findViewById(R.id.barraSuperior);
        spSeleccionarEncuestaEstadisticas = findViewById(R.id.spSeleccionarEncuestaEstadisticas);
        tarjetaResumenGeneral = findViewById(R.id.tarjetaResumenGeneral);
        tvEtiquetaTurno = findViewById(R.id.tvEtiquetaTurno);
        tvNotaPromedio = findViewById(R.id.tvNotaPromedio);
        vistaSemaforo = findViewById(R.id.vistaSemaforo);
        tvEstadoSemaforo = findViewById(R.id.tvEstadoSemaforo);

        graficaBarras = findViewById(R.id.graficaBarras);
        rvMetricasDetalladas = findViewById(R.id.rvMetricasDetalladas);

        // Botón Volver al Panel
        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        // Cargamos el desplegable para elegir libremente la encuesta
        cargarSpinnerEncuestas();
    }

    // Configura el selector de encuestas
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

        spSeleccionarEncuestaEstadisticas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < DatosMemoria.listaEncuestasConfig.size()) {
                    encuestaSeleccionada = DatosMemoria.listaEncuestasConfig.get(position);
                    cargarReporteEstadistico();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Calcula el reporte general exclusivo de la sucursal seleccionada con escala normalizada
    private void cargarReporteEstadistico() {
        if (encuestaSeleccionada == null) return;

        String sucursal = encuestaSeleccionada.getSucursal();
        String turno = DatosMemoria.turnoActual;

        // Obtenemos el corte específico de esa sucursal
        CorteTurno corte = DatosMemoria.getCorte(sucursal, turno);

        tvEtiquetaTurno.setText(String.format(Locale.getDefault(),
                "%s (%s)\nTurno: %s | Total Encuestados: %d",
                encuestaSeleccionada.getNombreEncuesta(), sucursal, corte.getTurno(), corte.getTotalRespuestas()));

        // Calculamos el promedio normalizado (en escala de 10) para no distorsionar las escalas mixtas
        double promedio = corte.getPromedioGeneral();

        if (promedio == 0.0) {
            tvNotaPromedio.setText("Sin datos");
            apagarSemaforo();
            tvEstadoSemaforo.setText("Sin preguntas registradas en este turno");
            tvEstadoSemaforo.setTextColor(getColor(R.color.cafe_negro));
        } else {
            tvNotaPromedio.setText(String.format(Locale.getDefault(), "%.1f / 10.0", promedio));

            // Actualizamos el Semáforo de Calidad sobre la escala estandarizada de 10
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
        }

        // Dibujamos la Gráfica de Barras Comparativa
        dibujarGraficaBarras(corte.getPreguntas());

        // Dibujamos las Métricas Detalladas
        dibujarMetricasDetalladas(corte);
    }

    private void apagarSemaforo() {
        vistaSemaforo.setBackgroundTintList(getColorStateList(R.color.blanco_crema));
    }

    // Genera barras visuales proporcionales (porcentaje) para cada pregunta
    private void dibujarGraficaBarras(List<PreguntaVaciada> preguntas) {
        graficaBarras.removeAllViews();

        if (preguntas == null || preguntas.isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay datos de preguntas para esta sucursal.");
            tvVacio.setTextColor(getColor(R.color.cafe_negro));
            graficaBarras.addView(tvVacio);
            return;
        }

        for (PreguntaVaciada pv : preguntas) {
            LinearLayout barItem = new LinearLayout(this);
            barItem.setOrientation(LinearLayout.VERTICAL);
            barItem.setPadding(0, 8, 0, 16);

            TextView tvNombre = new TextView(this);
            tvNombre.setText(pv.getPregunta());
            tvNombre.setTextSize(14);
            tvNombre.setTextColor(getColor(R.color.cafe_negro));
            barItem.addView(tvNombre);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);

            if (pv.getTipoPregunta().contains("Sí/No")) {
                int perc = (int) pv.getPorcentajeSi();
                pb.setProgress(perc);
                pb.setProgressTintList(getColorStateList(R.color.marron_tostado));
            } else if (pv.getTipoPregunta().contains("1-10")) {
                int perc = (int) ((pv.getNotaEscala() / 10.0) * 100);
                pb.setProgress(perc);
                pb.setProgressTintList(getColorStateList(R.color.cafe_oscuro));
            } else {
                int perc = (int) ((pv.getNotaEscala() / 5.0) * 100);
                pb.setProgress(perc);
                pb.setProgressTintList(getColorStateList(R.color.cafe_oscuro));
            }

            barItem.addView(pb);
            graficaBarras.addView(barItem);
        }
    }

    // Genera las tarjetas con desgloses detallados e incluye las notas de bitácora
    private void dibujarMetricasDetalladas(CorteTurno corte) {
        rvMetricasDetalladas.removeAllViews();
        List<PreguntaVaciada> preguntas = corte.getPreguntas();

        for (PreguntaVaciada pv : preguntas) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(16, 16, 16, 16);
            card.setBackgroundColor(getColor(R.color.blanco_crema));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);

            TextView tvP = new TextView(this);
            tvP.setText(pv.getPregunta() + " (" + pv.getTipoPregunta() + ")");
            tvP.setTextSize(14);
            tvP.setTypeface(null, android.graphics.Typeface.BOLD);
            tvP.setTextColor(getColor(R.color.cafe_negro));
            card.addView(tvP);

            TextView tvRes = new TextView(this);
            if (pv.getTipoPregunta().contains("Sí/No")) {
                tvRes.setText(String.format(Locale.getDefault(), "Aprobación: %.1f%% (Sí: %d / No: %d)",
                        pv.getPorcentajeSi(), pv.getConteoSi(), pv.getConteoNo()));
            } else if (pv.getTipoPregunta().contains("1-10")) {
                tvRes.setText(String.format(Locale.getDefault(), "Nota Original: %.1f / 10.0 (Equiv. %.1f / 10)",
                        pv.getNotaEscala(), pv.getNotaNormalizada10()));
            } else {
                tvRes.setText(String.format(Locale.getDefault(), "Nota Original: %.1f / 5.0 (Equiv. %.1f / 10)",
                        pv.getNotaEscala(), pv.getNotaNormalizada10()));
            }
            tvRes.setTextSize(13);
            tvRes.setTextColor(getColor(R.color.cafe_oscuro));
            card.addView(tvRes);

            rvMetricasDetalladas.addView(card);
        }

        // Mostramos las notas explicativas de bitácora de esa sucursal
        if (!corte.getNotasIncidencias().isEmpty()) {
            TextView tvHeaderNotas = new TextView(this);
            tvHeaderNotas.setText("Justificantes / Notas del Turno:");
            tvHeaderNotas.setTextSize(14);
            tvHeaderNotas.setTypeface(null, android.graphics.Typeface.BOLD);
            tvHeaderNotas.setTextColor(getColor(R.color.terracota));
            tvHeaderNotas.setPadding(0, 16, 0, 8);
            rvMetricasDetalladas.addView(tvHeaderNotas);

            for (String nota : corte.getNotasIncidencias()) {
                TextView tvN = new TextView(this);
                tvN.setText("• " + nota);
                tvN.setTextSize(13);
                tvN.setTextColor(getColor(R.color.cafe_negro));
                tvN.setPadding(0, 0, 0, 8);
                rvMetricasDetalladas.addView(tvN);
            }
        }
    }
}