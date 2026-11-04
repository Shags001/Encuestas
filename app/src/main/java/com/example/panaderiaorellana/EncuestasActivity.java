package com.example.panaderiaorellana;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

// Vista 7: Pantalla de Gestión de Encuestas (Google Forms)
public class EncuestasActivity extends AppCompatActivity {

    // Toolbar y componentes del formulario
    private MaterialToolbar barraSuperior;
    private EditText etNombreEncuesta;
    private EditText etSucursalEncuesta;
    private EditText etEnlaceEncuesta;
    private Button btnGuardarEncuesta;
    private LinearLayout rvListaEncuestas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuestas);

        // Respetamos la barra de estado del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootEncuestas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlazamos las vistas por su ID
        barraSuperior = findViewById(R.id.barraSuperior);
        etNombreEncuesta = findViewById(R.id.etNombreEncuesta);
        etSucursalEncuesta = findViewById(R.id.etSucursalEncuesta);
        etEnlaceEncuesta = findViewById(R.id.etEnlaceEncuesta);
        btnGuardarEncuesta = findViewById(R.id.btnGuardarEncuesta);
        rvListaEncuestas = findViewById(R.id.rvListaEncuestas);

        // Botón Volver al Panel
        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        // Mostramos las encuestas configuradas en memoria
        actualizarListaEncuestas();

        // Evento para guardar una nueva encuesta con confirmación
        btnGuardarEncuesta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarGuardarEncuesta();
            }
        });
    }

    // Diálogo de confirmación antes de guardar la encuesta de Google Forms
    private void confirmarGuardarEncuesta() {
        String nombre = etNombreEncuesta.getText().toString().trim();
        String sucursal = etSucursalEncuesta.getText().toString().trim();
        String enlace = etEnlaceEncuesta.getText().toString().trim();

        // Validamos que los campos no estén vacíos
        if (nombre.isEmpty() || sucursal.isEmpty() || enlace.isEmpty()) {
            Toast.makeText(this, R.string.error_campos_encuesta_vacios, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validamos que el enlace empiece con http/https
        if (!enlace.startsWith("http://") && !enlace.startsWith("https://")) {
            Toast.makeText(this, R.string.error_enlace_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Guardado de Encuesta")
                .setMessage("¿Desea registrar la encuesta '" + nombre + "' para la sucursal " + sucursal + "?")
                .setPositiveButton("Sí, Guardar", (dialog, which) -> guardarEncuesta(nombre, sucursal, enlace))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Registra una nueva encuesta en el catálogo
    private void guardarEncuesta(String nombre, String sucursal, String enlace) {
        boolean esPrimera = DatosMemoria.listaEncuestasConfig.isEmpty();
        EncuestaConfig nuevaConfig = new EncuestaConfig(nombre, sucursal, enlace, esPrimera);
        DatosMemoria.listaEncuestasConfig.add(nuevaConfig);

        Toast.makeText(this, R.string.msg_encuesta_config_guardada, Toast.LENGTH_SHORT).show();

        // Limpiamos los campos y refrescamos la lista
        etNombreEncuesta.setText("");
        etSucursalEncuesta.setText("");
        etEnlaceEncuesta.setText("");
        actualizarListaEncuestas();
    }

    // Muestra en pantalla cada encuesta guardada con su enlace y su selector de activa
    private void actualizarListaEncuestas() {
        rvListaEncuestas.removeAllViews();

        for (int i = 0; i < DatosMemoria.listaEncuestasConfig.size(); i++) {
            final EncuestaConfig config = DatosMemoria.listaEncuestasConfig.get(i);

            LinearLayout itemCard = new LinearLayout(this);
            itemCard.setOrientation(LinearLayout.VERTICAL);
            itemCard.setPadding(16, 16, 16, 16);
            itemCard.setBackgroundColor(getColor(R.color.blanco_crema));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            itemCard.setLayoutParams(params);

            // Fila superior: Nombre de la Encuesta e interruptorActiva
            LinearLayout rowHeader = new LinearLayout(this);
            rowHeader.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvNombre = new TextView(this);
            tvNombre.setText(config.getNombreEncuesta());
            tvNombre.setTextSize(15);
            tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
            tvNombre.setTextColor(getColor(R.color.cafe_negro));

            LinearLayout.LayoutParams pNombre = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            );
            tvNombre.setLayoutParams(pNombre);
            rowHeader.addView(tvNombre);

            // Switch interruptorActiva
            MaterialSwitch interruptorActiva = new MaterialSwitch(this);
            interruptorActiva.setChecked(config.isActiva());
            interruptorActiva.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Desactivamos todas las demás encuestas para dejar esta activa
                        for (EncuestaConfig ec : DatosMemoria.listaEncuestasConfig) {
                            ec.setActiva(false);
                        }
                        config.setActiva(true);
                        actualizarListaEncuestas();
                        Toast.makeText(EncuestasActivity.this, "Encuesta activada para código QR", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            rowHeader.addView(interruptorActiva);

            itemCard.addView(rowHeader);

            // Texto de Sucursal y Enlace
            TextView tvSucursal = new TextView(this);
            tvSucursal.setText("Sucursal: " + config.getSucursal());
            tvSucursal.setTextSize(13);
            tvSucursal.setTextColor(getColor(R.color.cafe_negro));
            itemCard.addView(tvSucursal);

            TextView tvEnlace = new TextView(this);
            tvEnlace.setText("URL: " + config.getEnlaceGoogleForms());
            tvEnlace.setTextSize(12);
            tvEnlace.setTextColor(getColor(R.color.cafe_oscuro));
            itemCard.addView(tvEnlace);

            // Botón btnAbrirEnlaceItem para probar la URL en el navegador
            Button btnAbrirEnlaceItem = new Button(this);
            btnAbrirEnlaceItem.setText("Probar Enlace en Navegador");
            btnAbrirEnlaceItem.setTextSize(12);
            btnAbrirEnlaceItem.setBackgroundTintList(getColorStateList(R.color.marron_tostado));
            btnAbrirEnlaceItem.setTextColor(getColor(R.color.cafe_negro));

            LinearLayout.LayoutParams pBtn = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            pBtn.setMargins(0, 8, 0, 0);
            btnAbrirEnlaceItem.setLayoutParams(pBtn);

            btnAbrirEnlaceItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.getEnlaceGoogleForms()));
                        startActivity(browserIntent);
                    } catch (Exception ex) {
                        Toast.makeText(EncuestasActivity.this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            itemCard.addView(btnAbrirEnlaceItem);

            rvListaEncuestas.addView(itemCard);
        }
    }
}