package com.example.panaderiaorellana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Vista 4: Pantalla de Vaciado Dinámico de Datos
public class VaciadoActivity extends AppCompatActivity {

    // Toolbar y componente identificador
    private MaterialToolbar barraSuperior;
    private Spinner spSeleccionarSucursalVaciado;

    // Componentes del turno y total
    private RadioGroup rgTurno;
    private RadioButton rbManana;
    private RadioButton rbTarde;
    private EditText etTotalRespuestas;

    // Componentes para trascripción de preguntas
    private EditText etPregunta;
    private Spinner spTipoPregunta;
    private LinearLayout contenedorCamposDinamicos;
    private TextInputLayout tilNotaEscala;
    private LinearLayout layoutSiNo;
    private EditText etConteoSi;
    private EditText etConteoNo;
    private EditText etNotaEscala;

    // Bitácora del turno
    private EditText etNotaIncidencia;
    private Button btnAgregarNotaTurno;

    // Botones y lista acumulada
    private Button btnAgregarPregunta;
    private TextView tvContadorPreguntas;
    private LinearLayout rvListaPreguntas;
    private Button btnCalcularReporte;

    // Corte activo en edición
    private CorteTurno corteActivo = null;
    private EncuestaConfig encuestaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaciado);

        // Respetamos la barra de estado del celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootVaciado), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlazamos componentes por su ID
        barraSuperior = findViewById(R.id.barraSuperior);
        spSeleccionarSucursalVaciado = findViewById(R.id.spSeleccionarSucursalVaciado);
        rgTurno = findViewById(R.id.rgTurno);
        rbManana = findViewById(R.id.rbManana);
        rbTarde = findViewById(R.id.rbTarde);
        etTotalRespuestas = findViewById(R.id.etTotalRespuestas);

        etPregunta = findViewById(R.id.etPregunta);
        spTipoPregunta = findViewById(R.id.spTipoPregunta);
        contenedorCamposDinamicos = findViewById(R.id.contenedorCamposDinamicos);
        tilNotaEscala = findViewById(R.id.tilNotaEscala);
        layoutSiNo = findViewById(R.id.layoutSiNo);
        etConteoSi = findViewById(R.id.etConteoSi);
        etConteoNo = findViewById(R.id.etConteoNo);
        etNotaEscala = findViewById(R.id.etNotaEscala);

        etNotaIncidencia = findViewById(R.id.etNotaIncidencia);
        btnAgregarNotaTurno = findViewById(R.id.btnAgregarNotaTurno);

        btnAgregarPregunta = findViewById(R.id.btnAgregarPregunta);
        tvContadorPreguntas = findViewById(R.id.tvContadorPreguntas);
        rvListaPreguntas = findViewById(R.id.rvListaPreguntas);
        btnCalcularReporte = findViewById(R.id.btnCalcularReporte);

        // Botón Volver al Panel
        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        // Spinner tipos de pregunta con layout sin opacidad
        ArrayAdapter<CharSequence> adapterTipos = ArrayAdapter.createFromResource(
                this, R.array.tipos_pregunta_array, R.layout.item_spinner
        );
        adapterTipos.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spTipoPregunta.setAdapter(adapterTipos);

        // Cargar selector de sucursales
        cargarSpinnerSucursales();

        // Escuchador para cambios de turno (Mañana/Tarde)
        rgTurno.setOnCheckedChangeListener((group, checkedId) -> cargarCorteActivo());

        // Alternar campos dinámicos
        spTipoPregunta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) { // "Sí/No"
                    tilNotaEscala.setVisibility(View.GONE);
                    layoutSiNo.setVisibility(View.VISIBLE);
                } else { // Escala 1-5, Escala 1-10 u Opciones
                    tilNotaEscala.setVisibility(View.VISIBLE);
                    layoutSiNo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Botón agregar pregunta al corte de la sucursal
        btnAgregarPregunta.setOnClickListener(v -> agregarPregunta());

        // Botón agregar nota de incidencia
        btnAgregarNotaTurno.setOnClickListener(v -> agregarNotaTurno());

        // Botón consolidar e ir a Estadísticas con confirmación
        btnCalcularReporte.setOnClickListener(v -> confirmarGenerarReporte());
    }

    // Configura el selector de sucursales
    private void cargarSpinnerSucursales() {
        List<String> nombres = new ArrayList<>();
        for (EncuestaConfig ec : DatosMemoria.listaEncuestasConfig) {
            nombres.add(ec.getNombreEncuesta() + " (" + ec.getSucursal() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, nombres);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spSeleccionarSucursalVaciado.setAdapter(adapter);

        spSeleccionarSucursalVaciado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < DatosMemoria.listaEncuestasConfig.size()) {
                    encuestaSeleccionada = DatosMemoria.listaEncuestasConfig.get(position);
                    cargarCorteActivo();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Carga o crea el corte específico de la sucursal y turno seleccionado
    private void cargarCorteActivo() {
        if (encuestaSeleccionada == null) return;

        String sucursal = encuestaSeleccionada.getSucursal();
        String turno = rbManana.isChecked() ? "Mañana" : "Tarde";

        corteActivo = DatosMemoria.getCorte(sucursal, turno);
        etTotalRespuestas.setText(String.valueOf(corteActivo.getTotalRespuestas()));

        actualizarListaPreguntas();
    }

    // Agrega la pregunta transcrita al corte de esta sucursal con validación estricta de rangos
    private void agregarPregunta() {
        if (corteActivo == null) return;

        String preguntaStr = etPregunta.getText().toString().trim();
        int tipoPos = spTipoPregunta.getSelectedItemPosition();
        String tipoStr = spTipoPregunta.getSelectedItem().toString();

        if (preguntaStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese el enunciado de la pregunta", Toast.LENGTH_SHORT).show();
            return;
        }

        int si = 0, no = 0;
        double nota = 0.0;

        if (tipoPos == 2) { // Sí/No
            String siStr = etConteoSi.getText().toString().trim();
            String noStr = etConteoNo.getText().toString().trim();
            if (siStr.isEmpty() && noStr.isEmpty()) {
                Toast.makeText(this, "Ingrese el conteo de votos Sí / No", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!siStr.isEmpty()) si = Integer.parseInt(siStr);
            if (!noStr.isEmpty()) no = Integer.parseInt(noStr);
        } else {
            String notaStr = etNotaEscala.getText().toString().trim();
            if (notaStr.isEmpty()) {
                Toast.makeText(this, "Ingrese la nota o promedio de la pregunta", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                nota = Double.parseDouble(notaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor ingrese una nota válida", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validación estricta según la escala elegida
            if (tipoPos == 0) { // Escala 1-5
                if (nota < 0.0 || nota > 5.0) {
                    Toast.makeText(this, "En escala de 1 a 5 la nota no puede ser mayor a 5.0 ni menor a 0", Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (tipoPos == 1) { // Escala 1-10
                if (nota < 0.0 || nota > 10.0) {
                    Toast.makeText(this, "En escala de 1 a 10 la nota no puede ser mayor a 10.0 ni menor a 0", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        PreguntaVaciada pv = new PreguntaVaciada(preguntaStr, tipoStr, si, no, nota);
        corteActivo.getPreguntas().add(pv);

        Toast.makeText(this, "Pregunta agregada al corte de " + corteActivo.getNombreSucursal(), Toast.LENGTH_SHORT).show();

        etPregunta.setText("");
        etNotaEscala.setText("");
        etConteoSi.setText("");
        etConteoNo.setText("");
        actualizarListaPreguntas();
    }

    // Guarda una nota explicativa en el corte de esta sucursal
    private void agregarNotaTurno() {
        if (corteActivo == null) return;

        String nota = etNotaIncidencia.getText().toString().trim();
        if (nota.isEmpty()) {
            Toast.makeText(this, "Escriba la causa u observación antes de guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        corteActivo.getNotasIncidencias().add(nota);
        etNotaIncidencia.setText("");
        Toast.makeText(this, "Nota registrada en el corte de " + corteActivo.getNombreSucursal(), Toast.LENGTH_SHORT).show();
    }

    // Diálogo de confirmación antes de consolidar el reporte
    private void confirmarGenerarReporte() {
        if (corteActivo == null || corteActivo.getPreguntas().isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos una pregunta al corte", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Generación de Reporte")
                .setMessage("¿Desea consolidar las " + corteActivo.getPreguntas().size() + " preguntas transcritas para " + corteActivo.getNombreSucursal() + " e ir a las Estadísticas?")
                .setPositiveButton("Sí, Generar Reporte", (dialog, which) -> calcularReporte())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Consolida los datos del corte y abre Estadísticas
    private void calcularReporte() {
        String totalStr = etTotalRespuestas.getText().toString().trim();
        if (!totalStr.isEmpty()) {
            corteActivo.setTotalRespuestas(Integer.parseInt(totalStr));
        }

        DatosMemoria.turnoActual = corteActivo.getTurno();

        // Abrimos la pantalla de estadísticas (Vista 5)
        Intent intent = new Intent(VaciadoActivity.this, EstadisticasActivity.class);
        startActivity(intent);
    }

    // Muestra en pantalla las preguntas del corte de esta sucursal
    private void actualizarListaPreguntas() {
        rvListaPreguntas.removeAllViews();
        if (corteActivo == null) return;

        int total = corteActivo.getPreguntas().size();
        tvContadorPreguntas.setText("4. Preguntas Acumuladas en este Corte (" + total + "):");

        if (total == 0) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay preguntas agregadas para esta sucursal todavía.");
            tvVacio.setTextColor(getColor(R.color.cafe_negro));
            tvVacio.setPadding(0, 16, 0, 16);
            rvListaPreguntas.addView(tvVacio);
            return;
        }

        for (int i = 0; i < total; i++) {
            final int index = i;
            PreguntaVaciada pv = corteActivo.getPreguntas().get(i);

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(16, 16, 16, 16);
            item.setBackgroundColor(getColor(R.color.blanco_crema));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            item.setLayoutParams(params);

            TextView tvTitulo = new TextView(this);
            tvTitulo.setText(String.format(Locale.getDefault(), "Pregunta %d: %s (%s)", (i + 1), pv.getPregunta(), pv.getTipoPregunta()));
            tvTitulo.setTextSize(15);
            tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitulo.setTextColor(getColor(R.color.cafe_negro));
            item.addView(tvTitulo);

            TextView tvDetalle = new TextView(this);
            if (pv.getTipoPregunta().contains("Sí/No")) {
                tvDetalle.setText(String.format(Locale.getDefault(), "Votos Sí: %d | Votos No: %d (%.1f%% Aprobación)",
                        pv.getConteoSi(), pv.getConteoNo(), pv.getPorcentajeSi()));
            } else {
                tvDetalle.setText(String.format(Locale.getDefault(), "Nota Promedio: %.2f", pv.getNotaEscala()));
            }
            tvDetalle.setTextSize(13);
            tvDetalle.setTextColor(getColor(R.color.cafe_oscuro));
            item.addView(tvDetalle);

            Button btnEliminar = new Button(this);
            btnEliminar.setText("Quitar Pregunta");
            btnEliminar.setTextSize(11);
            btnEliminar.setTextColor(getColor(R.color.terracota));
            btnEliminar.setBackgroundTintList(getColorStateList(R.color.white));

            LinearLayout.LayoutParams pBtn = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            pBtn.setMargins(0, 8, 0, 0);
            btnEliminar.setLayoutParams(pBtn);

            btnEliminar.setOnClickListener(v -> {
                corteActivo.getPreguntas().remove(index);
                actualizarListaPreguntas();
                Toast.makeText(VaciadoActivity.this, "Pregunta eliminada", Toast.LENGTH_SHORT).show();
            });
            item.addView(btnEliminar);

            rvListaPreguntas.addView(item);
        }
    }
}