package com.example.panaderiaorellana;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Vista 6: Pantalla de Bitácora y Notas del Turno
public class NotasActivity extends AppCompatActivity {

    // Componentes de la interfaz
    private MaterialToolbar barraSuperior;
    private Spinner spSeleccionarSucursalNotas;
    private TextView tvEncabezadoNotas;
    private EditText etNotasTurno;
    private Button btnGuardarNotas;
    private Button btnLimpiarTurno;
    private LinearLayout containerNotasHistorial;

    // Encuesta / Sucursal seleccionada
    private EncuestaConfig encuestaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas);

        // Respetamos la barra de estado superior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootNotas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlazamos componentes con sus IDs
        barraSuperior = findViewById(R.id.barraSuperior);
        spSeleccionarSucursalNotas = findViewById(R.id.spSeleccionarSucursalNotas);
        tvEncabezadoNotas = findViewById(R.id.tvEncabezadoNotas);
        etNotasTurno = findViewById(R.id.etNotasTurno);
        btnGuardarNotas = findViewById(R.id.btnGuardarNotas);
        btnLimpiarTurno = findViewById(R.id.btnLimpiarTurno);
        containerNotasHistorial = findViewById(R.id.containerNotasHistorial);

        // Botón Volver al Panel
        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        // Cargar selector de sucursal
        cargarSpinnerSucursales();

        // Evento para guardar la nota en la sucursal seleccionada
        btnGuardarNotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarNota();
            }
        });

        // Evento para resetear el turno de esta sucursal previa confirmación
        btnLimpiarTurno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarLimpiarTurno();
            }
        });
    }

    // Configura el desplegable de sucursales
    private void cargarSpinnerSucursales() {
        List<String> nombres = new ArrayList<>();
        for (EncuestaConfig ec : DatosMemoria.listaEncuestasConfig) {
            nombres.add(ec.getNombreEncuesta() + " (" + ec.getSucursal() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, nombres);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spSeleccionarSucursalNotas.setAdapter(adapter);

        spSeleccionarSucursalNotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < DatosMemoria.listaEncuestasConfig.size()) {
                    encuestaSeleccionada = DatosMemoria.listaEncuestasConfig.get(position);
                    actualizarHistorialNotas();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Agrega la observación a la bitácora de la sucursal activa
    private void guardarNota() {
        if (encuestaSeleccionada == null) return;

        String texto = etNotasTurno.getText().toString().trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, "Por favor redacte una observación o nota", Toast.LENGTH_SHORT).show();
            return;
        }

        String horaActual = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        String autor = (DatosMemoria.usuarioSesionActiva != null) ? DatosMemoria.usuarioSesionActiva.getNombre() : "Jefe de Área";
        String notaFormateada = "[" + horaActual + " - " + autor + "] " + texto;

        CorteTurno corte = DatosMemoria.getCorte(encuestaSeleccionada.getSucursal(), DatosMemoria.turnoActual);
        corte.getNotasIncidencias().add(notaFormateada);

        Toast.makeText(this, "Nota registrada en la bitácora de " + encuestaSeleccionada.getSucursal(), Toast.LENGTH_SHORT).show();

        etNotasTurno.setText("");
        actualizarHistorialNotas();
    }

    // Muestra cuadro de diálogo pidiendo confirmación antes de borrar los datos
    private void confirmarLimpiarTurno() {
        if (encuestaSeleccionada == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Reinicio de Corte")
                .setMessage("¿Está seguro que desea borrar todas las preguntas y notas del turno para la sucursal " + encuestaSeleccionada.getSucursal() + "?")
                .setPositiveButton("Sí, Resetear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        limpiarTurno();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Reinicia los datos del turno de esta sucursal específica
    private void limpiarTurno() {
        if (encuestaSeleccionada == null) return;

        CorteTurno corte = DatosMemoria.getCorte(encuestaSeleccionada.getSucursal(), DatosMemoria.turnoActual);
        corte.getNotasIncidencias().clear();
        corte.getPreguntas().clear();
        corte.setTotalRespuestas(0);

        etNotasTurno.setText("");
        actualizarHistorialNotas();
        Toast.makeText(this, "Bitácora y corte reseteados para " + encuestaSeleccionada.getSucursal(), Toast.LENGTH_SHORT).show();
    }

    // Renderiza las notas de la sucursal seleccionada
    private void actualizarHistorialNotas() {
        containerNotasHistorial.removeAllViews();
        if (encuestaSeleccionada == null) return;

        CorteTurno corte = DatosMemoria.getCorte(encuestaSeleccionada.getSucursal(), DatosMemoria.turnoActual);
        List<String> notas = corte.getNotasIncidencias();

        if (notas.isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay incidencias guardadas para " + encuestaSeleccionada.getSucursal() + ".");
            tvVacio.setTextColor(getColor(R.color.cafe_negro));
            tvVacio.setPadding(0, 16, 0, 16);
            containerNotasHistorial.addView(tvVacio);
            return;
        }

        for (String n : notas) {
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

            TextView tvTexto = new TextView(this);
            tvTexto.setText(n);
            tvTexto.setTextSize(14);
            tvTexto.setTextColor(getColor(R.color.cafe_negro));
            item.addView(tvTexto);

            containerNotasHistorial.addView(item);
        }
    }
}