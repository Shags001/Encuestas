package com.example.panaderiaorellana;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

// Vista 3: Pantalla del Código QR y Verificación de Enlace
public class QrActivity extends AppCompatActivity {

    // Componentes de la vista QR
    private MaterialToolbar barraSuperior;
    private Spinner spSeleccionarEncuesta;
    private TextView tvEstadoQr;
    private TextView tvTituloEncuestaActiva;
    private TextView tvSubtituloSucursal;
    private CardView tarjetaContenedorQr;
    private ImageView ivCodigoQr;
    private TextView tvInstrucciones;
    private Button btnProbarEnlace;
    private Button btnCompartirEnlace;

    // Encuesta actualmente seleccionada
    private EncuestaConfig encuestaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        // Respetamos la barra de estado superior del celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootQr), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlazamos las vistas por su ID
        barraSuperior = findViewById(R.id.barraSuperior);
        spSeleccionarEncuesta = findViewById(R.id.spSeleccionarEncuesta);
        tvEstadoQr = findViewById(R.id.tvEstadoQr);
        tvTituloEncuestaActiva = findViewById(R.id.tvTituloEncuestaActiva);
        tvSubtituloSucursal = findViewById(R.id.tvSubtituloSucursal);
        tarjetaContenedorQr = findViewById(R.id.tarjetaContenedorQr);
        ivCodigoQr = findViewById(R.id.ivCodigoQr);
        tvInstrucciones = findViewById(R.id.tvInstrucciones);
        btnProbarEnlace = findViewById(R.id.btnProbarEnlace);
        btnCompartirEnlace = findViewById(R.id.btnCompartirEnlace);

        // Configuración del botón Volver al Panel
        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        // Cargar encuestas en el Spinner selector
        cargarSpinnerEncuestas();

        // Botón para probar el enlace en el navegador del celular
        btnProbarEnlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probarEnlace();
            }
        });

        // Botón para compartir el enlace de Google Forms
        btnCompartirEnlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartirEnlace();
            }
        });
    }

    // Llena el desplegable con las encuestas guardadas en memoria
    private void cargarSpinnerEncuestas() {
        List<String> nombresEncuestas = new ArrayList<>();
        for (EncuestaConfig ec : DatosMemoria.listaEncuestasConfig) {
            nombresEncuestas.add(ec.getNombreEncuesta() + " (" + ec.getSucursal() + ")");
        }

        if (nombresEncuestas.isEmpty()) {
            nombresEncuestas.add("Sin encuestas registradas");
        }

        // Usamos nuestros layouts personalizados con texto oscuro y visible
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, nombresEncuestas);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spSeleccionarEncuesta.setAdapter(adapter);

        // Seleccionamos por defecto la encuesta que esté activa
        int indexActiva = 0;
        for (int i = 0; i < DatosMemoria.listaEncuestasConfig.size(); i++) {
            if (DatosMemoria.listaEncuestasConfig.get(i).isActiva()) {
                indexActiva = i;
                break;
            }
        }
        spSeleccionarEncuesta.setSelection(indexActiva);

        spSeleccionarEncuesta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < DatosMemoria.listaEncuestasConfig.size()) {
                    encuestaSeleccionada = DatosMemoria.listaEncuestasConfig.get(position);
                    actualizarDetallesEncuesta();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Actualiza los textos y el indicador de estado según la encuesta elegida
    private void actualizarDetallesEncuesta() {
        if (encuestaSeleccionada != null) {
            tvTituloEncuestaActiva.setText(encuestaSeleccionada.getNombreEncuesta());
            tvSubtituloSucursal.setText(encuestaSeleccionada.getSucursal());

            if (encuestaSeleccionada.isActiva()) {
                tvEstadoQr.setText("ESTADO: ACTIVO");
                tvEstadoQr.setTextColor(getColor(R.color.semaforo_verde_on));
            } else {
                tvEstadoQr.setText("ESTADO: INACTIVO");
                tvEstadoQr.setTextColor(getColor(R.color.terracota));
            }
        }
    }

    // Abre el enlace de Google Forms en el navegador del teléfono
    private void probarEnlace() {
        if (encuestaSeleccionada != null && encuestaSeleccionada.getEnlaceGoogleForms() != null
                && !encuestaSeleccionada.getEnlaceGoogleForms().isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(encuestaSeleccionada.getEnlaceGoogleForms()));
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "No se pudo abrir el enlace de Google Forms", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay un enlace configurado para esta encuesta", Toast.LENGTH_SHORT).show();
        }
    }

    // Comparte el enlace directo mediante apps instaladas en el celular
    private void compartirEnlace() {
        if (encuestaSeleccionada != null && encuestaSeleccionada.getEnlaceGoogleForms() != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Le invitamos a calificar nuestro servicio en Panadería Orellana: "
                    + encuestaSeleccionada.getEnlaceGoogleForms());
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, "Compartir Encuesta");
            startActivity(shareIntent);
        } else {
            Toast.makeText(this, "No hay una encuesta seleccionada para compartir", Toast.LENGTH_SHORT).show();
        }
    }
}