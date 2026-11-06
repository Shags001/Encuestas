package com.example.panaderiaorellana;

import android.content.Intent;
import android.net.Uri;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

// Vista 7: Pantalla de Gestión de Encuestas (Google Forms) con CRUD completo, Pestañas y Buscador
public class EncuestasActivity extends AppCompatActivity {

    private MaterialToolbar barraSuperior;
    private TabLayout tabLayoutEncuestas;

    // Paneles de Pestañas
    private LinearLayout panelCatalogoEncuestas;
    private LinearLayout panelRegistrarEncuesta;

    // Buscador
    private SearchView svBuscarEncuesta;
    private String filtroBusqueda = "";

    // Formulario Registrar
    private EditText etNombreEncuesta;
    private Spinner spSucursalEncuesta;
    private EditText etEnlaceEncuesta;
    private Button btnGuardarEncuesta;
    private LinearLayout rvListaEncuestas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuestas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootEncuestas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barraSuperior = findViewById(R.id.barraSuperior);
        tabLayoutEncuestas = findViewById(R.id.tabLayoutEncuestas);

        panelCatalogoEncuestas = findViewById(R.id.panelCatalogoEncuestas);
        panelRegistrarEncuesta = findViewById(R.id.panelRegistrarEncuesta);

        svBuscarEncuesta = findViewById(R.id.svBuscarEncuesta);

        etNombreEncuesta = findViewById(R.id.etNombreEncuesta);
        spSucursalEncuesta = findViewById(R.id.spSucursalEncuesta);
        etEnlaceEncuesta = findViewById(R.id.etEnlaceEncuesta);
        btnGuardarEncuesta = findViewById(R.id.btnGuardarEncuesta);
        rvListaEncuestas = findViewById(R.id.rvListaEncuestas);

        if (barraSuperior != null) {
            barraSuperior.setNavigationOnClickListener(v -> finish());
        }

        configurarPestanas();
        configurarBuscador();
        cargarSpinnerSucursales();

        actualizarListaEncuestas();

        btnGuardarEncuesta.setOnClickListener(v -> confirmarGuardarEncuesta());
    }

    private void configurarPestanas() {
        tabLayoutEncuestas.addTab(tabLayoutEncuestas.newTab().setText("Catálogo"));
        tabLayoutEncuestas.addTab(tabLayoutEncuestas.newTab().setText("Registrar Nueva"));

        tabLayoutEncuestas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                panelCatalogoEncuestas.setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
                panelRegistrarEncuesta.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void configurarBuscador() {
        svBuscarEncuesta.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtroBusqueda = query.trim().toLowerCase();
                actualizarListaEncuestas();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtroBusqueda = newText.trim().toLowerCase();
                actualizarListaEncuestas();
                return false;
            }
        });
    }

    private void cargarSpinnerSucursales() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, DatosMemoria.listaSucursales);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spSucursalEncuesta.setAdapter(adapter);
    }

    private void confirmarGuardarEncuesta() {
        String nombre = etNombreEncuesta.getText().toString().trim();
        if (spSucursalEncuesta.getSelectedItem() == null) {
            Toast.makeText(this, "Debe haber al menos una sucursal registrada", Toast.LENGTH_SHORT).show();
            return;
        }

        String sucursal = spSucursalEncuesta.getSelectedItem().toString();
        String enlace = etEnlaceEncuesta.getText().toString().trim();

        if (nombre.isEmpty() || enlace.isEmpty()) {
            Toast.makeText(this, R.string.error_campos_encuesta_vacios, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!enlace.startsWith("http://") && !enlace.startsWith("https://")) {
            Toast.makeText(this, R.string.error_enlace_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirmar Guardado de Encuesta")
                .setMessage("¿Desea registrar la encuesta '" + nombre + "' para la " + sucursal + "?")
                .setPositiveButton("Sí, Guardar", (d, which) -> guardarEncuesta(nombre, sucursal, enlace))
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();
    }

    private void guardarEncuesta(String nombre, String sucursal, String enlace) {
        EncuestaConfig nuevaConfig = new EncuestaConfig(nombre, sucursal, enlace, true);
        DatosMemoria.listaEncuestasConfig.add(nuevaConfig);

        DatosMemoria.guardarEncuestas(this);

        Toast.makeText(this, R.string.msg_encuesta_config_guardada, Toast.LENGTH_SHORT).show();

        etNombreEncuesta.setText("");
        etEnlaceEncuesta.setText("");

        TabLayout.Tab tabCatalogo = tabLayoutEncuestas.getTabAt(0);
        if (tabCatalogo != null) {
            tabCatalogo.select();
        }
        actualizarListaEncuestas();
    }

    // Dibuja la lista filtrada con botones de Probar Enlace, Editar y Eliminar
    private void actualizarListaEncuestas() {
        rvListaEncuestas.removeAllViews();

        List<EncuestaConfig> filtradas = new ArrayList<>();
        for (EncuestaConfig ec : DatosMemoria.listaEncuestasConfig) {
            if (filtroBusqueda.isEmpty()
                    || ec.getNombreEncuesta().toLowerCase().contains(filtroBusqueda)
                    || ec.getSucursal().toLowerCase().contains(filtroBusqueda)) {
                filtradas.add(ec);
            }
        }

        if (filtradas.isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No se encontraron encuestas que coincidan con la búsqueda.");
            tvVacio.setTextColor(getColor(R.color.cafe_negro));
            tvVacio.setPadding(0, 16, 0, 16);
            rvListaEncuestas.addView(tvVacio);
            return;
        }

        for (int i = 0; i < filtradas.size(); i++) {
            final EncuestaConfig config = filtradas.get(i);

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

            // Fila superior: Nombre de la Encuesta e Switch
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

            MaterialSwitch interruptorActiva = new MaterialSwitch(this);
            interruptorActiva.setChecked(config.isActiva());
            interruptorActiva.setOnCheckedChangeListener((buttonView, isChecked) -> {
                config.setActiva(isChecked);
                DatosMemoria.guardarEncuestas(EncuestasActivity.this);
                Toast.makeText(EncuestasActivity.this, "Encuesta " + (isChecked ? "Habilitada" : "Deshabilitada"), Toast.LENGTH_SHORT).show();
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

            // Fila de Botones
            LinearLayout rowButtons = new LinearLayout(this);
            rowButtons.setOrientation(LinearLayout.HORIZONTAL);
            rowButtons.setPadding(0, 8, 0, 0);

            Button btnAbrirEnlaceItem = new Button(this);
            btnAbrirEnlaceItem.setText("Probar");
            btnAbrirEnlaceItem.setTextSize(11);
            btnAbrirEnlaceItem.setBackgroundTintList(getColorStateList(R.color.marron_tostado));
            btnAbrirEnlaceItem.setTextColor(getColor(R.color.cafe_negro));
            btnAbrirEnlaceItem.setOnClickListener(v -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.getEnlaceGoogleForms()));
                    startActivity(browserIntent);
                } catch (Exception ex) {
                    Toast.makeText(EncuestasActivity.this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
                }
            });
            rowButtons.addView(btnAbrirEnlaceItem);

            Button btnEditarItem = new Button(this);
            btnEditarItem.setText("Editar");
            btnEditarItem.setTextSize(11);
            btnEditarItem.setBackgroundTintList(getColorStateList(R.color.cafe_oscuro));
            btnEditarItem.setTextColor(getColor(R.color.blanco_crema));

            LinearLayout.LayoutParams pBtnEdit = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            pBtnEdit.setMargins(8, 0, 0, 0);
            btnEditarItem.setLayoutParams(pBtnEdit);

            btnEditarItem.setOnClickListener(v -> abrirDialogoEditarEncuesta(config));
            rowButtons.addView(btnEditarItem);

            Button btnEliminarItem = new Button(this);
            btnEliminarItem.setText("Eliminar");
            btnEliminarItem.setTextSize(11);
            btnEliminarItem.setBackgroundTintList(getColorStateList(R.color.terracota));
            btnEliminarItem.setTextColor(getColor(R.color.blanco_crema));

            LinearLayout.LayoutParams pBtnDel = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            pBtnDel.setMargins(8, 0, 0, 0);
            btnEliminarItem.setLayoutParams(pBtnDel);

            btnEliminarItem.setOnClickListener(v -> confirmarEliminarEncuesta(config));
            rowButtons.addView(btnEliminarItem);

            itemCard.addView(rowButtons);

            rvListaEncuestas.addView(itemCard);
        }
    }

    // Diálogo personalizado elegante con botones integrados para EDITAR el enlace y datos
    private void abrirDialogoEditarEncuesta(EncuestaConfig config) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_encuesta, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText etEditNombre = dialogView.findViewById(R.id.etEditNombreEncuesta);
        Spinner spEditSucursal = dialogView.findViewById(R.id.spEditSucursalEncuesta);
        TextInputEditText etEditEnlace = dialogView.findViewById(R.id.etEditEnlaceEncuesta);
        Button btnDialogGuardar = dialogView.findViewById(R.id.btnDialogGuardar);
        Button btnDialogCancelar = dialogView.findViewById(R.id.btnDialogCancelar);

        etEditNombre.setText(config.getNombreEncuesta());
        etEditEnlace.setText(config.getEnlaceGoogleForms());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, DatosMemoria.listaSucursales);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spEditSucursal.setAdapter(adapter);

        int pos = DatosMemoria.listaSucursales.indexOf(config.getSucursal());
        if (pos >= 0) {
            spEditSucursal.setSelection(pos);
        }

        btnDialogCancelar.setOnClickListener(v -> dialog.dismiss());

        btnDialogGuardar.setOnClickListener(v -> {
            String nuevoNombre = (etEditNombre.getText() != null) ? etEditNombre.getText().toString().trim() : "";
            String nuevaSucursal = spEditSucursal.getSelectedItem().toString();
            String nuevoEnlace = (etEditEnlace.getText() != null) ? etEditEnlace.getText().toString().trim() : "";

            if (nuevoNombre.isEmpty() || nuevoEnlace.isEmpty()) {
                Toast.makeText(EncuestasActivity.this, "No puede dejar campos vacíos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!nuevoEnlace.startsWith("http://") && !nuevoEnlace.startsWith("https://")) {
                Toast.makeText(EncuestasActivity.this, R.string.error_enlace_invalido, Toast.LENGTH_SHORT).show();
                return;
            }

            config.setNombreEncuesta(nuevoNombre);
            config.setSucursal(nuevaSucursal);
            config.setEnlaceGoogleForms(nuevoEnlace);

            DatosMemoria.guardarEncuestas(EncuestasActivity.this);
            Toast.makeText(EncuestasActivity.this, "Encuesta actualizada correctamente", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            actualizarListaEncuestas();
        });

        dialog.show();
    }

    private void confirmarEliminarEncuesta(EncuestaConfig config) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Encuesta")
                .setMessage("¿Desea eliminar definitivamente la encuesta '" + config.getNombreEncuesta() + "' (" + config.getSucursal() + ")?")
                .setPositiveButton("Sí, Eliminar", (d, which) -> {
                    DatosMemoria.listaEncuestasConfig.remove(config);
                    DatosMemoria.guardarEncuestas(EncuestasActivity.this);
                    Toast.makeText(EncuestasActivity.this, "Encuesta eliminada del catálogo", Toast.LENGTH_SHORT).show();
                    actualizarListaEncuestas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}