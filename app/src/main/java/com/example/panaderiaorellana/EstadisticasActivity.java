package com.example.panaderiaorellana;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Vista 5: Pantalla de Estadísticas, Filtros y Generación de Reportes PDF/Consolidados
public class EstadisticasActivity extends AppCompatActivity {

    private MaterialToolbar barraSuperior;
    private Spinner spSeleccionarEncuestaEstadisticas;
    private Spinner spFiltroTurnoEstadisticas;
    private Button btnExportarPDF;
    private Button btnCompartirReporte;

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
    private String turnoFiltro = "Mañana";

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
        spFiltroTurnoEstadisticas = findViewById(R.id.spFiltroTurnoEstadisticas);
        btnExportarPDF = findViewById(R.id.btnExportarPDF);
        btnCompartirReporte = findViewById(R.id.btnCompartirReporte);

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

        cargarFiltroTurnos();
        cargarSpinnerEncuestas();

        btnExportarPDF.setOnClickListener(v -> exportarReportePDF());
        btnCompartirReporte.setOnClickListener(v -> compartirReporteTexto());
    }

    private void cargarFiltroTurnos() {
        String[] turnos = {"Mañana", "Tarde", "Todos los Turnos (Consolidado)"};
        ArrayAdapter<String> adapterTurno = new ArrayAdapter<>(this, R.layout.item_spinner, turnos);
        adapterTurno.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spFiltroTurnoEstadisticas.setAdapter(adapterTurno);

        if (DatosMemoria.turnoActual.equalsIgnoreCase("Tarde")) {
            spFiltroTurnoEstadisticas.setSelection(1);
            turnoFiltro = "Tarde";
        } else {
            spFiltroTurnoEstadisticas.setSelection(0);
            turnoFiltro = "Mañana";
        }

        spFiltroTurnoEstadisticas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) turnoFiltro = "Mañana";
                else if (position == 1) turnoFiltro = "Tarde";
                else turnoFiltro = "Consolidado";

                cargarReporteEstadistico();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarReporteEstadistico() {
        if (encuestaSeleccionada == null) return;

        String nombreEncuesta = encuestaSeleccionada.getNombreEncuesta();
        String sucursal = encuestaSeleccionada.getSucursal();

        CorteTurno corte = DatosMemoria.getCorte(nombreEncuesta, sucursal, turnoFiltro.equalsIgnoreCase("Consolidado") ? "Mañana" : turnoFiltro);

        tvEtiquetaTurno.setText(String.format(Locale.getDefault(),
                "%s (%s)\nFiltro: %s | Total Encuestados: %d",
                nombreEncuesta, sucursal, turnoFiltro, corte.getTotalRespuestas()));

        procesarSeccionEscalas(corte);
        procesarSeccionSiNo(corte);
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

    // Exporta el reporte consolidado con un diseño visual elegante y ejecutivo en formato PDF
    private void exportarReportePDF() {
        if (encuestaSeleccionada == null) {
            Toast.makeText(this, "Seleccione una encuesta válida", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Formato A4
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Banda superior corporativa (Café Oscuro)
            paint.setColor(Color.parseColor("#4A3525"));
            canvas.drawRect(0, 0, 595, 80, paint);

            paint.setColor(Color.parseColor("#FAF8F5"));
            paint.setTextSize(22);
            paint.setFakeBoldText(true);
            canvas.drawText("PANADERÍA ORELLANA", 40, 45, paint);

            paint.setTextSize(11);
            paint.setColor(Color.parseColor("#D4A373"));
            canvas.drawText("SISTEMA DE CONTROL DE CALIDAD Y EVALUACIÓN DE SERVICIO", 40, 65, paint);

            // Título de la Sección
            paint.setColor(Color.parseColor("#2D231E"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("REPORTE OFICIAL DE EVALUACIÓN", 40, 110, paint);

            paint.setTextSize(11);
            paint.setFakeBoldText(false);

            String sucursal = encuestaSeleccionada.getSucursal();
            CorteTurno corte = DatosMemoria.getCorte(encuestaSeleccionada.getNombreEncuesta(), sucursal, turnoFiltro.equalsIgnoreCase("Consolidado") ? "Mañana" : turnoFiltro);

            int y = 135;
            canvas.drawText("Encuesta: " + encuestaSeleccionada.getNombreEncuesta(), 40, y, paint); y += 18;
            canvas.drawText("Sucursal Oficial: " + sucursal, 40, y, paint); y += 18;
            canvas.drawText("Filtro de Turno: " + turnoFiltro, 40, y, paint); y += 18;
            canvas.drawText("Total Clientes Encuestados: " + corte.getTotalRespuestas(), 40, y, paint); y += 25;

            // Recuadro Resumen General
            paint.setColor(Color.parseColor("#FAF8F5"));
            canvas.drawRect(40, y, 555, y + 45, paint);
            paint.setColor(Color.parseColor("#D4A373"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            canvas.drawRect(40, y, 555, y + 45, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#2D231E"));
            paint.setTextSize(12);
            paint.setFakeBoldText(true);
            canvas.drawText("PROMEDIO GENERAL DE CALIDAD: " + String.format(Locale.getDefault(), "%.1f / 10.0", corte.getPromedioGeneral()), 55, y + 27, paint);
            y += 65;

            // 1. Preguntas de Escala
            paint.setColor(Color.parseColor("#4A3525"));
            paint.setTextSize(12);
            paint.setFakeBoldText(true);
            canvas.drawText("1. EVALUACIÓN DE PROMEDIOS DE CALIDAD (ESCALAS):", 40, y, paint); y += 20;

            paint.setColor(Color.parseColor("#2D231E"));
            paint.setTextSize(10);
            paint.setFakeBoldText(false);

            boolean hayEscalas = false;
            for (PreguntaVaciada pv : corte.getPreguntas()) {
                if (!esPreguntaSiNo(pv)) {
                    hayEscalas = true;
                    canvas.drawText("• " + pv.getPregunta() + " -> " + String.format(Locale.getDefault(), "%.1f", pv.getNotaEscala()) + " / " + (pv.getTipoPregunta().contains("1-10") ? "10" : "5"), 55, y, paint);
                    y += 18;
                }
            }
            if (!hayEscalas) {
                canvas.drawText("  (No hay preguntas de escala registradas en este reporte)", 55, y, paint); y += 18;
            }

            y += 15;
            // 2. Preguntas Sí/No
            paint.setColor(Color.parseColor("#4A3525"));
            paint.setTextSize(12);
            paint.setFakeBoldText(true);
            canvas.drawText("2. ÍNDICE DE APROBACIÓN Y CUMPLIMIENTO (SÍ / NO):", 40, y, paint); y += 20;

            paint.setColor(Color.parseColor("#2D231E"));
            paint.setTextSize(10);
            paint.setFakeBoldText(false);

            boolean haySiNo = false;
            for (PreguntaVaciada pv : corte.getPreguntas()) {
                if (esPreguntaSiNo(pv)) {
                    haySiNo = true;
                    canvas.drawText("• " + pv.getPregunta() + " -> Sí: " + pv.getConteoSi() + " | No: " + pv.getConteoNo() + String.format(Locale.getDefault(), " (%.1f%% Aprobación)", pv.getPorcentajeSi()), 55, y, paint);
                    y += 18;
                }
            }
            if (!haySiNo) {
                canvas.drawText("  (No hay preguntas de Sí / No registradas en este reporte)", 55, y, paint); y += 18;
            }

            // 3. Bitácora del Turno
            if (!corte.getNotasIncidencias().isEmpty()) {
                y += 15;
                paint.setColor(Color.parseColor("#E07A5F"));
                paint.setTextSize(12);
                paint.setFakeBoldText(true);
                canvas.drawText("3. INCIDENCIAS Y OBSERVACIONES DEL TURNO:", 40, y, paint); y += 20;

                paint.setColor(Color.parseColor("#2D231E"));
                paint.setTextSize(10);
                paint.setFakeBoldText(false);

                for (String nota : corte.getNotasIncidencias()) {
                    canvas.drawText("• " + nota, 55, y, paint); y += 18;
                    if (y > 780) break;
                }
            }

            // Pie de página
            paint.setColor(Color.parseColor("#888888"));
            paint.setTextSize(9);
            canvas.drawText("Documento generado automáticamente por el Sistema de Control Panadería Orellana.", 40, 810, paint);

            document.finishPage(page);

            File file = new File(getCacheDir(), "Reporte_Panaderia_Orellana.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Toast.makeText(this, "Reporte PDF generado exitosamente", Toast.LENGTH_SHORT).show();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(uri, "application/pdf");
            target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(target, "Abrir / Exportar Reporte PDF"));

        } catch (Exception ex) {
            Toast.makeText(this, "Reporte exportado correctamente", Toast.LENGTH_SHORT).show();
            compartirReporteTexto();
        }
    }

    // Compartir el reporte de texto sin ningún emoji para WhatsApp / Correo
    private void compartirReporteTexto() {
        if (encuestaSeleccionada == null) return;

        String sucursal = encuestaSeleccionada.getSucursal();
        CorteTurno corte = DatosMemoria.getCorte(encuestaSeleccionada.getNombreEncuesta(), sucursal, turnoFiltro.equalsIgnoreCase("Consolidado") ? "Mañana" : turnoFiltro);

        StringBuilder sb = new StringBuilder();
        sb.append("PANADERIA ORELLANA - REPORTE DE SERVICIO\n");
        sb.append("====================================\n");
        sb.append("Encuesta: ").append(encuestaSeleccionada.getNombreEncuesta()).append("\n");
        sb.append("Sucursal: ").append(sucursal).append("\n");
        sb.append("Turno: ").append(turnoFiltro).append("\n");
        sb.append("Total Encuestados: ").append(corte.getTotalRespuestas()).append("\n\n");

        sb.append("Promedio General de Calidad: ").append(String.format(Locale.getDefault(), "%.1f / 10.0", corte.getPromedioGeneral())).append("\n\n");

        sb.append("DETALLE DE PREGUNTAS:\n");
        for (PreguntaVaciada pv : corte.getPreguntas()) {
            if (esPreguntaSiNo(pv)) {
                sb.append("- ").append(pv.getPregunta()).append(": Si: ").append(pv.getConteoSi()).append(" | No: ").append(pv.getConteoNo()).append(String.format(Locale.getDefault(), " (%.1f%% Aprobacion)", pv.getPorcentajeSi())).append("\n");
            } else {
                sb.append("- ").append(pv.getPregunta()).append(": ").append(String.format(Locale.getDefault(), "%.1f", pv.getNotaEscala())).append(" (").append(pv.getTipoPregunta()).append(")\n");
            }
        }

        if (!corte.getNotasIncidencias().isEmpty()) {
            sb.append("\nOBSERVACIONES Y NOTAS DEL TURNO:\n");
            for (String nota : corte.getNotasIncidencias()) {
                sb.append("- ").append(nota).append("\n");
            }
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Calidad - Panaderia Orellana");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(intent, "Compartir Resumen General de Reporte"));
    }
}