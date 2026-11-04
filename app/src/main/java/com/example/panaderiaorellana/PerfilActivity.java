package com.example.panaderiaorellana;

import android.os.Bundle;
import android.view.View;
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
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

// Configuración de Perfil de Usuario y Gestión de Cuentas
public class PerfilActivity extends AppCompatActivity {

    // Componentes Mi Perfil
    private MaterialToolbar barraSuperior;
    private EditText etPerfilNombre;
    private EditText etPerfilUsuario;
    private EditText etPerfilPassword;
    private Spinner spPerfilSucursal;
    private Button btnActualizarPerfil;

    // Componentes Registrar Nuevo Jefe de Área
    private EditText etNuevoNombre;
    private EditText etNuevoUsuario;
    private EditText etNuevoPassword;
    private Spinner spNuevoSucursal;
    private Button btnRegistrarUsuario;

    // Lista de cuentas
    private LinearLayout rvListaUsuarios;

    // Lista de nombres de sucursales oficiales
    private List<String> sucursalesOficiales = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Respetamos la barra de estado superior del celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootPerfil), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlazamos componentes
        barraSuperior = findViewById(R.id.barraSuperior);
        etPerfilNombre = findViewById(R.id.etPerfilNombre);
        etPerfilUsuario = findViewById(R.id.etPerfilUsuario);
        etPerfilPassword = findViewById(R.id.etPerfilPassword);
        spPerfilSucursal = findViewById(R.id.spPerfilSucursal);
        btnActualizarPerfil = findViewById(R.id.btnActualizarPerfil);

        etNuevoNombre = findViewById(R.id.etNuevoNombre);
        etNuevoUsuario = findViewById(R.id.etNuevoUsuario);
        etNuevoPassword = findViewById(R.id.etNuevoPassword);
        spNuevoSucursal = findViewById(R.id.spNuevoSucursal);
        btnRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario);

        rvListaUsuarios = findViewById(R.id.rvListaUsuarios);

        // Botón Volver al Panel
        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        // Cargar las 6 sucursales oficiales
        cargarListaSucursales();

        // Cargar datos de mi perfil activo
        cargarDatosMiPerfil();

        // Cargar lista general de usuarios
        actualizarListaUsuarios();

        // Botón Guardar Cambios de Mi Perfil
        btnActualizarPerfil.setOnClickListener(v -> actualizarMiPerfil());

        // Botón Registrar Nuevo Jefe de Área
        btnRegistrarUsuario.setOnClickListener(v -> registrarNuevoJefe());
    }

    // Carga la lista de las 6 sucursales en los Spinners
    private void cargarListaSucursales() {
        sucursalesOficiales.clear();
        for (EncuestaConfig ec : DatosMemoria.listaEncuestasConfig) {
            sucursalesOficiales.add(ec.getSucursal());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, sucursalesOficiales);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);

        spPerfilSucursal.setAdapter(adapter);
        spNuevoSucursal.setAdapter(adapter);
    }

    // Carga los datos del usuario en sesión activa
    private void cargarDatosMiPerfil() {
        Usuario activo = DatosMemoria.usuarioSesionActiva;
        if (activo != null) {
            etPerfilNombre.setText(activo.getNombre());
            etPerfilUsuario.setText(activo.getUsuario());
            etPerfilPassword.setText(activo.getPassword());

            int index = sucursalesOficiales.indexOf(activo.getSucursal());
            if (index >= 0) {
                spPerfilSucursal.setSelection(index);
            }
        }
    }

    // Guarda los cambios del usuario en sesión
    private void actualizarMiPerfil() {
        Usuario activo = DatosMemoria.usuarioSesionActiva;
        if (activo == null) return;

        String nombre = etPerfilNombre.getText().toString().trim();
        String usuario = etPerfilUsuario.getText().toString().trim();
        String password = etPerfilPassword.getText().toString().trim();
        String sucursal = spPerfilSucursal.getSelectedItem().toString();

        if (nombre.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos de perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        activo.setNombre(nombre);
        activo.setUsuario(usuario);
        activo.setPassword(password);
        activo.setSucursal(sucursal);

        Toast.makeText(this, "Perfil y contraseña actualizados correctamente", Toast.LENGTH_SHORT).show();
        actualizarListaUsuarios();
    }

    // Registra una nueva cuenta de jefe de área
    private void registrarNuevoJefe() {
        String nombre = etNuevoNombre.getText().toString().trim();
        String usuario = etNuevoUsuario.getText().toString().trim();
        String password = etNuevoPassword.getText().toString().trim();
        String sucursal = spNuevoSucursal.getSelectedItem().toString();

        if (nombre.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete los datos del nuevo usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Usuario u : DatosMemoria.listaUsuarios) {
            if (u.getUsuario().equalsIgnoreCase(usuario)) {
                Toast.makeText(this, "El nombre de usuario ya existe en el sistema", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Usuario nuevo = new Usuario(nombre, usuario, password, sucursal, true);
        DatosMemoria.listaUsuarios.add(nuevo);

        Toast.makeText(this, "Jefe de área registrado exitosamente", Toast.LENGTH_SHORT).show();

        etNuevoNombre.setText("");
        etNuevoUsuario.setText("");
        etNuevoPassword.setText("");
        actualizarListaUsuarios();
    }

    // Genera el listado de usuarios con switches de activación y edición
    private void actualizarListaUsuarios() {
        rvListaUsuarios.removeAllViews();

        for (Usuario u : DatosMemoria.listaUsuarios) {
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

            // Fila Nombre + Switch Activo
            LinearLayout rowHead = new LinearLayout(this);
            rowHead.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvNombre = new TextView(this);
            tvNombre.setText(u.getNombre() + " (" + u.getUsuario() + ")");
            tvNombre.setTextSize(15);
            tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
            tvNombre.setTextColor(getColor(R.color.cafe_negro));

            LinearLayout.LayoutParams pNombre = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvNombre.setLayoutParams(pNombre);
            rowHead.addView(tvNombre);

            // Switch Activo / Inactivo
            MaterialSwitch switchActivo = new MaterialSwitch(this);
            switchActivo.setChecked(u.isActivo());

            // Impedir desactivar tu propia cuenta activa
            if (DatosMemoria.usuarioSesionActiva != null && DatosMemoria.usuarioSesionActiva.getUsuario().equalsIgnoreCase(u.getUsuario())) {
                switchActivo.setEnabled(false);
            }

            switchActivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
                u.setActivo(isChecked);
                Toast.makeText(PerfilActivity.this, "Estado de " + u.getUsuario() + " cambiado a " + (isChecked ? "Activo" : "Inactivo"), Toast.LENGTH_SHORT).show();
            });
            rowHead.addView(switchActivo);

            card.addView(rowHead);

            // Detalle Sucursal
            TextView tvDetalle = new TextView(this);
            tvDetalle.setText("Sucursal: " + u.getSucursal());
            tvDetalle.setTextSize(13);
            tvDetalle.setTextColor(getColor(R.color.cafe_oscuro));
            card.addView(tvDetalle);

            // Botón Editar / Reestablecer Clave
            Button btnEditar = new Button(this);
            btnEditar.setText("Editar / Restablecer Clave");
            btnEditar.setTextSize(11);
            btnEditar.setBackgroundTintList(getColorStateList(R.color.marron_tostado));
            btnEditar.setTextColor(getColor(R.color.cafe_negro));

            LinearLayout.LayoutParams pBtn = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            pBtn.setMargins(0, 8, 0, 0);
            btnEditar.setLayoutParams(pBtn);

            btnEditar.setOnClickListener(v -> abrirDialogoEditarUsuario(u));
            card.addView(btnEditar);

            rvListaUsuarios.addView(card);
        }
    }

    // Diálogo para editar nombre o restablecer contraseña de un usuario
    private void abrirDialogoEditarUsuario(Usuario u) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Usuario: " + u.getUsuario());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etEditNombre = new EditText(this);
        etEditNombre.setHint("Nombre completo");
        etEditNombre.setText(u.getNombre());
        layout.addView(etEditNombre);

        final EditText etEditClave = new EditText(this);
        etEditClave.setHint("Nueva contraseña");
        etEditClave.setText(u.getPassword());
        layout.addView(etEditClave);

        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoN = etEditNombre.getText().toString().trim();
            String nuevaC = etEditClave.getText().toString().trim();

            if (!nuevoN.isEmpty() && !nuevaC.isEmpty()) {
                u.setNombre(nuevoN);
                u.setPassword(nuevaC);
                Toast.makeText(PerfilActivity.this, "Datos de " + u.getUsuario() + " actualizados", Toast.LENGTH_SHORT).show();
                actualizarListaUsuarios();
            } else {
                Toast.makeText(PerfilActivity.this, "No se pueden dejar campos vacíos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}