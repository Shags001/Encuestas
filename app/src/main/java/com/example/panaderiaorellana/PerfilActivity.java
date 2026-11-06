package com.example.panaderiaorellana;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

// Configuración de Perfil de Usuario, Sucursales y Cuentas con Pestañas
public class PerfilActivity extends AppCompatActivity {

    private MaterialToolbar barraSuperior;
    private TabLayout tabLayoutPerfil;

    private LinearLayout panelMiPerfil;
    private LinearLayout panelSucursales;
    private LinearLayout panelUsuarios;

    private EditText etPerfilNombre;
    private EditText etPerfilUsuario;
    private EditText etPerfilPassword;
    private Spinner spPerfilSucursal;
    private Button btnActualizarPerfil;

    private EditText etNuevaSucursal;
    private Button btnAgregarSucursal;
    private LinearLayout rvListaSucursales;

    private EditText etNuevoNombre;
    private EditText etNuevoUsuario;
    private EditText etNuevoPassword;
    private Spinner spNuevoSucursal;
    private Button btnRegistrarUsuario;
    private LinearLayout rvListaUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootPerfil), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barraSuperior = findViewById(R.id.barraSuperior);
        tabLayoutPerfil = findViewById(R.id.tabLayoutPerfil);

        panelMiPerfil = findViewById(R.id.panelMiPerfil);
        panelSucursales = findViewById(R.id.panelSucursales);
        panelUsuarios = findViewById(R.id.panelUsuarios);

        etPerfilNombre = findViewById(R.id.etPerfilNombre);
        etPerfilUsuario = findViewById(R.id.etPerfilUsuario);
        etPerfilPassword = findViewById(R.id.etPerfilPassword);
        spPerfilSucursal = findViewById(R.id.spPerfilSucursal);
        btnActualizarPerfil = findViewById(R.id.btnActualizarPerfil);

        etNuevaSucursal = findViewById(R.id.etNuevaSucursal);
        btnAgregarSucursal = findViewById(R.id.btnAgregarSucursal);
        rvListaSucursales = findViewById(R.id.rvListaSucursales);

        etNuevoNombre = findViewById(R.id.etNuevoNombre);
        etNuevoUsuario = findViewById(R.id.etNuevoUsuario);
        etNuevoPassword = findViewById(R.id.etNuevoPassword);
        spNuevoSucursal = findViewById(R.id.spNuevoSucursal);
        btnRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario);
        rvListaUsuarios = findViewById(R.id.rvListaUsuarios);

        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        configurarPestanas();
        cargarListaSucursales();
        cargarDatosMiPerfil();

        actualizarListaSucursales();
        actualizarListaUsuarios();

        btnActualizarPerfil.setOnClickListener(v -> actualizarMiPerfil());
        btnAgregarSucursal.setOnClickListener(v -> agregarNuevaSucursal());
        btnRegistrarUsuario.setOnClickListener(v -> registrarNuevoJefe());
    }

    private void configurarPestanas() {
        tabLayoutPerfil.addTab(tabLayoutPerfil.newTab().setText("Mi Perfil"));
        tabLayoutPerfil.addTab(tabLayoutPerfil.newTab().setText("Sucursales"));
        tabLayoutPerfil.addTab(tabLayoutPerfil.newTab().setText("Usuarios"));

        tabLayoutPerfil.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                panelMiPerfil.setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
                panelSucursales.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
                panelUsuarios.setVisibility(pos == 2 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void cargarListaSucursales() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, DatosMemoria.listaSucursales);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);

        spPerfilSucursal.setAdapter(adapter);
        spNuevoSucursal.setAdapter(adapter);
    }

    private void cargarDatosMiPerfil() {
        Usuario activo = DatosMemoria.usuarioSesionActiva;
        if (activo != null) {
            etPerfilNombre.setText(activo.getNombre());
            etPerfilUsuario.setText(activo.getUsuario());
            etPerfilPassword.setText(activo.getPassword());

            int index = DatosMemoria.listaSucursales.indexOf(activo.getSucursal());
            if (index >= 0) {
                spPerfilSucursal.setSelection(index);
            }
        }
    }

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

        DatosMemoria.guardarUsuarios(this);

        Toast.makeText(this, "Perfil y contraseña actualizados correctamente", Toast.LENGTH_SHORT).show();
        actualizarListaUsuarios();
    }

    private void agregarNuevaSucursal() {
        String nombreSucursal = etNuevaSucursal.getText().toString().trim();
        if (nombreSucursal.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre de la nueva sucursal", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String s : DatosMemoria.listaSucursales) {
            if (s.equalsIgnoreCase(nombreSucursal)) {
                Toast.makeText(this, "Esta sucursal ya existe en el sistema", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        DatosMemoria.listaSucursales.add(nombreSucursal);

        String slug = nombreSucursal.toLowerCase().replaceAll("[^a-z0-9]", "_");
        EncuestaConfig nuevaConfig = new EncuestaConfig("Encuesta de Servicio - " + nombreSucursal, nombreSucursal, "https://forms.google.com/sample_" + slug, false);
        DatosMemoria.listaEncuestasConfig.add(nuevaConfig);

        DatosMemoria.guardarTodo(this);

        Toast.makeText(this, "Sucursal " + nombreSucursal + " agregada exitosamente", Toast.LENGTH_SHORT).show();

        etNuevaSucursal.setText("");
        cargarListaSucursales();
        actualizarListaSucursales();
    }

    private void actualizarListaSucursales() {
        rvListaSucursales.removeAllViews();

        for (int i = 0; i < DatosMemoria.listaSucursales.size(); i++) {
            String sucursal = DatosMemoria.listaSucursales.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(16, 12, 16, 12);
            card.setBackgroundColor(getColor(R.color.blanco_crema));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            card.setLayoutParams(params);

            TextView tvNombre = new TextView(this);
            tvNombre.setText((i + 1) + ". " + sucursal);
            tvNombre.setTextSize(14);
            tvNombre.setTextColor(getColor(R.color.cafe_negro));

            LinearLayout.LayoutParams pNombre = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvNombre.setLayoutParams(pNombre);
            card.addView(tvNombre);

            rvListaSucursales.addView(card);
        }
    }

    private void registrarNuevoJefe() {
        String nombre = etNuevoNombre.getText().toString().trim();
        String usuario = etNuevoUsuario.getText().toString().trim();
        String password = etNuevoPassword.getText().toString().trim();

        if (spNuevoSucursal.getSelectedItem() == null) {
            Toast.makeText(this, "Debe seleccionar una sucursal", Toast.LENGTH_SHORT).show();
            return;
        }

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

        DatosMemoria.guardarUsuarios(this);

        Toast.makeText(this, "Jefe de área registrado exitosamente", Toast.LENGTH_SHORT).show();

        etNuevoNombre.setText("");
        etNuevoUsuario.setText("");
        etNuevoPassword.setText("");
        actualizarListaUsuarios();
    }

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

            MaterialSwitch switchActivo = new MaterialSwitch(this);
            switchActivo.setChecked(u.isActivo());

            if (DatosMemoria.usuarioSesionActiva != null && DatosMemoria.usuarioSesionActiva.getUsuario().equalsIgnoreCase(u.getUsuario())) {
                switchActivo.setEnabled(false);
            }

            switchActivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
                u.setActivo(isChecked);
                DatosMemoria.guardarUsuarios(PerfilActivity.this);
                Toast.makeText(PerfilActivity.this, "Estado de " + u.getUsuario() + " cambiado a " + (isChecked ? "Activo" : "Inactivo"), Toast.LENGTH_SHORT).show();
            });
            rowHead.addView(switchActivo);

            card.addView(rowHead);

            TextView tvDetalle = new TextView(this);
            tvDetalle.setText("Sucursal: " + u.getSucursal());
            tvDetalle.setTextSize(13);
            tvDetalle.setTextColor(getColor(R.color.cafe_oscuro));
            card.addView(tvDetalle);

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

    private void abrirDialogoEditarUsuario(Usuario u) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_usuario, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText etEditNombre = dialogView.findViewById(R.id.etEditNombreUsuario);
        TextInputEditText etEditClave = dialogView.findViewById(R.id.etEditPasswordUsuario);
        Spinner spEditSucursal = dialogView.findViewById(R.id.spEditSucursalUsuario);
        Button btnDialogGuardar = dialogView.findViewById(R.id.btnDialogGuardarUsuario);
        Button btnDialogCancelar = dialogView.findViewById(R.id.btnDialogCancelarUsuario);

        etEditNombre.setText(u.getNombre());
        etEditClave.setText(u.getPassword());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, DatosMemoria.listaSucursales);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spEditSucursal.setAdapter(adapter);

        int pos = DatosMemoria.listaSucursales.indexOf(u.getSucursal());
        if (pos >= 0) {
            spEditSucursal.setSelection(pos);
        }

        btnDialogCancelar.setOnClickListener(v -> dialog.dismiss());

        btnDialogGuardar.setOnClickListener(v -> {
            String nuevoN = (etEditNombre.getText() != null) ? etEditNombre.getText().toString().trim() : "";
            String nuevaC = (etEditClave.getText() != null) ? etEditClave.getText().toString().trim() : "";
            String nuevaS = spEditSucursal.getSelectedItem().toString();

            if (!nuevoN.isEmpty() && !nuevaC.isEmpty()) {
                u.setNombre(nuevoN);
                u.setPassword(nuevaC);
                u.setSucursal(nuevaS);

                DatosMemoria.guardarUsuarios(PerfilActivity.this);
                Toast.makeText(PerfilActivity.this, "Datos de " + u.getUsuario() + " actualizados", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                actualizarListaUsuarios();
            } else {
                Toast.makeText(PerfilActivity.this, "No se pueden dejar campos vacíos", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}