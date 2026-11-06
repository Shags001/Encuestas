package com.example.panaderiaorellana;

import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

// Vista 3: Pantalla del Código QR, Descarga de Imagen PNG y Compartir
public class QrActivity extends AppCompatActivity {

    private MaterialToolbar barraSuperior;
    private Spinner spSeleccionarEncuesta;
    private TextView tvEstadoQr;
    private TextView tvTituloEncuestaActiva;
    private TextView tvSubtituloSucursal;
    private CardView tarjetaContenedorQr;
    private ImageView ivCodigoQr;
    private TextView tvInstrucciones;

    private Button btnGuardarQr;
    private Button btnCompartirQr;
    private Button btnProbarEnlace;

    private EncuestaConfig encuestaSeleccionada = null;
    private Bitmap qrBitmapActual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        DatosMemoria.inicializarDatos(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootQr), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barraSuperior = findViewById(R.id.barraSuperior);
        spSeleccionarEncuesta = findViewById(R.id.spSeleccionarEncuesta);
        tvEstadoQr = findViewById(R.id.tvEstadoQr);
        tvTituloEncuestaActiva = findViewById(R.id.tvTituloEncuestaActiva);
        tvSubtituloSucursal = findViewById(R.id.tvSubtituloSucursal);
        tarjetaContenedorQr = findViewById(R.id.tarjetaContenedorQr);
        ivCodigoQr = findViewById(R.id.ivCodigoQr);
        tvInstrucciones = findViewById(R.id.tvInstrucciones);

        btnGuardarQr = findViewById(R.id.btnGuardarQr);
        btnCompartirQr = findViewById(R.id.btnCompartirQr);
        btnProbarEnlace = findViewById(R.id.btnProbarEnlace);

        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        cargarSpinnerEncuestas();

        btnGuardarQr.setOnClickListener(v -> guardarImagenQR());
        btnCompartirQr.setOnClickListener(v -> compartirImagenQR());
        btnProbarEnlace.setOnClickListener(v -> probarEnlace());
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
        spSeleccionarEncuesta.setAdapter(adapter);

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

            generarCodigoQRReal(encuestaSeleccionada.getEnlaceGoogleForms());
        }
    }

    private void generarCodigoQRReal(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                int width = 500;
                int height = 500;
                BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, width, height);

                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++) {
                    int offset = y * width;
                    for (int x = 0; x < width; x++) {
                        pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF2D231E : 0xFFFFFFFF;
                    }
                }

                qrBitmapActual = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                qrBitmapActual.setPixels(pixels, 0, width, 0, 0, width, height);
                ivCodigoQr.setImageBitmap(qrBitmapActual);
            } catch (Exception e) {
                ivCodigoQr.setImageResource(R.drawable.ic_qr_code);
                qrBitmapActual = null;
            }
        } else {
            ivCodigoQr.setImageResource(R.drawable.ic_qr_code);
            qrBitmapActual = null;
        }
    }

    // Guarda el código QR actual como archivo PNG y permite visualizarlo / descargarlo
    private void guardarImagenQR() {
        if (qrBitmapActual == null) {
            Toast.makeText(this, "No hay una imagen de código QR disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = new File(getCacheDir(), "QR_Panaderia_Orellana.png");
            FileOutputStream out = new FileOutputStream(imageFile);
            qrBitmapActual.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            Toast.makeText(this, "Código QR descargado / generado correctamente", Toast.LENGTH_SHORT).show();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/png");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Ver / Abrir Código QR Descargado"));

        } catch (Exception e) {
            Toast.makeText(this, "Error al descargar el código QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Compartir el código QR como imagen PNG adjunta vía WhatsApp, Email, etc.
    private void compartirImagenQR() {
        if (qrBitmapActual == null) {
            Toast.makeText(this, "No hay una imagen de código QR disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = new File(getCacheDir(), "QR_Panaderia_Orellana.png");
            FileOutputStream out = new FileOutputStream(imageFile);
            qrBitmapActual.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Código QR para evaluación de servicio - Panadería Orellana ("
                    + (encuestaSeleccionada != null ? encuestaSeleccionada.getSucursal() : "") + ")\nLink: "
                    + (encuestaSeleccionada != null ? encuestaSeleccionada.getEnlaceGoogleForms() : ""));
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Compartir Imagen del QR"));

        } catch (Exception e) {
            Toast.makeText(this, "Error al compartir la imagen del QR", Toast.LENGTH_SHORT).show();
        }
    }

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
}