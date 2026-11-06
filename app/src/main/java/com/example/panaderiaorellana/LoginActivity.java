package com.example.panaderiaorellana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

// Vista 1: Inicio de Sesión
public class LoginActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextView tvTitulo;
    private TextInputLayout tilUsuario;
    private EditText etUsuario;
    private TextInputLayout tilPassword;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa SharedPreferences y carga la persisitencia
        DatosMemoria.inicializarDatos(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivLogo = findViewById(R.id.ivLogo);
        tvTitulo = findViewById(R.id.tvTitulo);
        tilUsuario = findViewById(R.id.tilUsuario);
        etUsuario = findViewById(R.id.etUsuario);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> validarAcceso());
    }

    // Valida las credenciales ingresadas
    private void validarAcceso() {
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            tvError.setText(R.string.error_campos_vacios);
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        int resultado = DatosMemoria.verificarCredenciales(usuario, password);

        if (resultado == 1) {
            tvError.setVisibility(View.GONE);
            Toast.makeText(this, "Bienvenido " + DatosMemoria.usuarioSesionActiva.getNombre(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        } else if (resultado == 2) {
            tvError.setText("Su cuenta de usuario está inactiva. Contacte a la administración.");
            tvError.setVisibility(View.VISIBLE);
        } else {
            tvError.setText(R.string.error_credenciales_invalidas);
            tvError.setVisibility(View.VISIBLE);
        }
    }
}