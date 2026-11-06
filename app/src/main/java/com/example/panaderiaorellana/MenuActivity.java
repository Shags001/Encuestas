package com.example.panaderiaorellana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Vista 2: Menú Principal o Panel de Control
public class MenuActivity extends AppCompatActivity {

    private CardView tarjetaInfoUsuario;
    private TextView tvNombreUsuario;
    private TextView tvNombreSucursal;
    private Button btnConfiguracion;
    private Button btnLogout;

    private CardView btnIrQr;
    private CardView btnIrVaciado;
    private CardView btnIrEstadisticas;
    private CardView btnIrNotas;
    private CardView btnIrEncuestas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootMenu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tarjetaInfoUsuario = findViewById(R.id.tarjetaInfoUsuario);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        tvNombreSucursal = findViewById(R.id.tvNombreSucursal);
        btnConfiguracion = findViewById(R.id.btnConfiguracion);
        btnLogout = findViewById(R.id.btnLogout);

        btnIrQr = findViewById(R.id.btnIrQr);
        btnIrVaciado = findViewById(R.id.btnIrVaciado);
        btnIrEstadisticas = findViewById(R.id.btnIrEstadisticas);
        btnIrNotas = findViewById(R.id.btnIrNotas);
        btnIrEncuestas = findViewById(R.id.btnIrEncuestas);

        cargarDatosSesion();

        tarjetaInfoUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, PerfilActivity.class);
            startActivity(intent);
        });

        if (btnConfiguracion != null) {
            btnConfiguracion.setOnClickListener(v ->
                    startActivity(new Intent(MenuActivity.this, PerfilActivity.class)));
        }

        btnLogout.setOnClickListener(v -> cerrarSesion());

        btnIrQr.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, QrActivity.class)));
        btnIrVaciado.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, VaciadoActivity.class)));
        btnIrEstadisticas.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EstadisticasActivity.class)));
        btnIrNotas.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, NotasActivity.class)));
        btnIrEncuestas.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EncuestasActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosSesion();
    }

    // Carga el usuario y sucursal en sesión
    private void cargarDatosSesion() {
        if (DatosMemoria.usuarioSesionActiva != null) {
            tvNombreUsuario.setText(DatosMemoria.usuarioSesionActiva.getNombre());
            tvNombreSucursal.setText(DatosMemoria.usuarioSesionActiva.getSucursal());
        } else {
            cerrarSesion();
        }
    }

    // Cierra la sesión activa
    private void cerrarSesion() {
        DatosMemoria.usuarioSesionActiva = null;
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MenuActivity.this, LoginActivity.class));
        finish();
    }
}