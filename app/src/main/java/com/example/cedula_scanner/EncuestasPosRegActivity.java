package com.example.cedula_scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EncuestasPosRegActivity extends AppCompatActivity {

    private static final String TAG = "EncuestasPosRegActivity";
    private static final String PREFS_NAME = "CursosPrefs";

    private Toolbar toolbar;
    private EditText tvNombreCompleto, tvCedula;
    private EditText etNombreEmpresa, etNitEmpresa;
    private RadioGroup rgRepresentanteEmpresa;
    private RadioButton rbSiRepresentante, rbNoRepresentante;
    private LinearLayout layoutPersonaJuridica, layoutDatosEmpresa, layoutIntereses;
    private Spinner spinnerTipoPersona, spinnerProyectoSena, spinnerInteres1, spinnerInteres2, spinnerInteres3;
    private TextInputLayout tilNombreAsociacion;
    private EditText etNombreAsociacion;

    // Variables para la funcionalidad de empresas
    private EmpresaManager empresaManager;
    private Button btnBuscarEmpresa;
    private EmpresaManager.Empresa empresaSeleccionada;
    private Button btnCrearNuevaEmpresa;
    private String nombreCompleto;
    private String numeroCedula;
    private String idEvento;
    private String idSubevento;

    // Arrays para los spinners
    private List<String> listaIntereses = new ArrayList<>();
    private List<Integer> listaInteresesIds = new ArrayList<>();
    private List<String> listaProyectosSena = new ArrayList<>();
    private List<Integer> listaProyectosSenaIds = new ArrayList<>();

    private LinearLayout sectionProyectoSena;

    private Spinner spinnerTipoPrograma;
    private LinearLayout sectionTipoPrograma;
    private List<String> listaTiposProgramas = new ArrayList<>();
    private LinearLayout layoutFortalecimientoEmpresarial;
    private EditText etRequerimientoFortalecimiento;

    private List<String> listaInteresesFiltrada1;
    private List<String> listaInteresesFiltrada2;
    private List<String> listaInteresesFiltrada3;
    private List<Integer> listaInteresesIdsFiltrada1;
    private List<Integer> listaInteresesIdsFiltrada2;
    private List<Integer> listaInteresesIdsFiltrada3;
    private ArrayAdapter<String> adapterInteres1;
    private ArrayAdapter<String> adapterInteres2;
    private ArrayAdapter<String> adapterInteres3;
    private String seleccionInteres1 = "";
    private String seleccionInteres2 = "";
    private String seleccionInteres3 = "";
    private int seleccionInteresId1 = -1;
    private int seleccionInteresId2 = -1;
    private int seleccionInteresId3 = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuestas_pos_reg);

        // Configurar toolbar
        setUpToolbar();

        // Inicializar vistas
        initializeViews();

        // Inicializar EmpresaManager
        empresaManager = new EmpresaManager(this);

        // Configurar listeners de botones
        setupButtonListeners();

        // Obtener IDs de evento y subevento
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        idEvento = prefs.getString("idEvento", null);
        idSubevento = prefs.getString("idSubevento", null);

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nombreCompleto = extras.getString("nombre_completo", "");
            numeroCedula = extras.getString("numero_cedula", "");
            tvNombreCompleto.setText(nombreCompleto);
            tvCedula.setText(numeroCedula);
        }

        // Configurar listeners
        setupSpinnerListeners();

        // Llenar los spinners con datos
        cargarDatosSpinners();

        // Establecer estado inicial
        setupInitialState();
    }

    private void initializeViews() {
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto);
        tvCedula = findViewById(R.id.tvCedula);

        spinnerTipoPersona = findViewById(R.id.spinnerTipoPersona);
        tilNombreAsociacion = findViewById(R.id.tilNombreAsociacion);
        etNombreAsociacion = findViewById(R.id.etNombreAsociacion);

        layoutPersonaJuridica = findViewById(R.id.layoutPersonaJuridica);
        layoutIntereses = findViewById(R.id.layoutIntereses);

        rgRepresentanteEmpresa = findViewById(R.id.rgRepresentanteEmpresa);
        rbSiRepresentante = findViewById(R.id.rbSiRepresentante);
        rbNoRepresentante = findViewById(R.id.rbNoRepresentante);

        layoutDatosEmpresa = findViewById(R.id.layoutDatosEmpresa);

        etNombreEmpresa = findViewById(R.id.etNombreEmpresa);
        etNitEmpresa = findViewById(R.id.etNitEmpresa);

        spinnerProyectoSena = findViewById(R.id.spinnerProyectoSena);
        spinnerInteres1 = findViewById(R.id.spinnerInteres1);
        spinnerInteres2 = findViewById(R.id.spinnerInteres2);
        spinnerInteres3 = findViewById(R.id.spinnerInteres3);

        spinnerTipoPrograma = findViewById(R.id.spinnerTipoPrograma);
        sectionProyectoSena = findViewById(R.id.sectionProyectoSena);
        sectionTipoPrograma = findViewById(R.id.sectionTipoPrograma);

        layoutFortalecimientoEmpresarial = findViewById(R.id.layoutFortalecimientoEmpresarial);
        etRequerimientoFortalecimiento = findViewById(R.id.etRequerimientoFortalecimiento);

        btnBuscarEmpresa = findViewById(R.id.btnBuscarEmpresa);
        btnCrearNuevaEmpresa = findViewById(R.id.btnCrearNuevaEmpresa);
    }

    private void setupButtonListeners() {
        btnBuscarEmpresa.setOnClickListener(v -> mostrarDialogoBusquedaEmpresa());
        btnCrearNuevaEmpresa.setOnClickListener(v -> crearYGuardarEmpresa());
    }

    private void setupSpinnerListeners() {
        // Configurar spinner de tipo de persona
        String[] tiposPersona = {"Persona Natural", "Persona Jurídica", "Pertenezco a una Asociación Campesina"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposPersona);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoPersona.setAdapter(adapter);

        spinnerTipoPersona.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipoSeleccionado = parent.getItemAtPosition(position).toString();
                actualizarVisibilidadSegunTipoPersona(tipoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        rgRepresentanteEmpresa.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSiRepresentante) {
                layoutDatosEmpresa.setVisibility(View.VISIBLE);
            } else {
                layoutDatosEmpresa.setVisibility(View.GONE);
            }
        });

        // Listener para el spinner de proyectos SENA
        spinnerProyectoSena.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String proyectoSeleccionado = parent.getItemAtPosition(position).toString();
                if ("Fortalecimiento Empresarial".equals(proyectoSeleccionado)) {
                    layoutFortalecimientoEmpresarial.setVisibility(View.VISIBLE);
                } else {
                    layoutFortalecimientoEmpresarial.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutFortalecimientoEmpresarial.setVisibility(View.GONE);
            }
        });
    }

    private void actualizarVisibilidadSegunTipoPersona(String tipoSeleccionado) {
        switch (tipoSeleccionado) {
            case "Persona Natural":
                // Persona Natural: mostrar intereses, ocultar resto
                layoutIntereses.setVisibility(View.VISIBLE);
                layoutPersonaJuridica.setVisibility(View.GONE);
                sectionProyectoSena.setVisibility(View.GONE);
                tilNombreAsociacion.setVisibility(View.GONE);
                actualizarVisibilidadTipoPrograma();
                break;

            case "Persona Jurídica":
                // Persona Jurídica: mostrar proyectos SENA y representante empresa
                layoutIntereses.setVisibility(View.GONE);
                layoutPersonaJuridica.setVisibility(View.VISIBLE);
                sectionProyectoSena.setVisibility(View.VISIBLE);
                tilNombreAsociacion.setVisibility(View.GONE);
                sectionTipoPrograma.setVisibility(View.GONE);
                break;

            case "Pertenezco a una Asociación Campesina":
                // Asociación Campesina: mostrar intereses y campo asociación
                layoutIntereses.setVisibility(View.VISIBLE);
                layoutPersonaJuridica.setVisibility(View.GONE);
                sectionProyectoSena.setVisibility(View.GONE);
                tilNombreAsociacion.setVisibility(View.VISIBLE);
                actualizarVisibilidadTipoPrograma();
                break;
        }
    }

    private void setupInitialState() {
        // Establecer "Persona Natural" como selección inicial
        spinnerTipoPersona.setSelection(0);
        actualizarVisibilidadSegunTipoPersona("Persona Natural");
    }

    private void actualizarVisibilidadTipoPrograma() {
        String tipoSeleccionado = spinnerTipoPersona.getSelectedItem().toString();

        if ((tipoSeleccionado.equals("Persona Natural") || tipoSeleccionado.equals("Pertenezco a una Asociación Campesina"))
                && spinnerInteres1.getSelectedItemPosition() > 0) {

            String interesSeleccionado = spinnerInteres1.getSelectedItem().toString();

            if ("Estudiar".equals(interesSeleccionado)) {
                sectionTipoPrograma.setVisibility(View.VISIBLE);
            } else {
                sectionTipoPrograma.setVisibility(View.GONE);
            }
        } else {
            sectionTipoPrograma.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoBusquedaEmpresa() {
        SelectorEmpresaDialog dialog = new SelectorEmpresaDialog(this, empresaManager,
                new SelectorEmpresaDialog.OnEmpresaSelectedListener() {
                    @Override
                    public void onEmpresaSelected(EmpresaManager.Empresa empresa) {
                        empresaSeleccionada = empresa;
                        etNombreEmpresa.setText(empresa.getNombreCorregido());
                        etNitEmpresa.setText(empresa.nit);
                        Toast.makeText(EncuestasPosRegActivity.this,
                                "Empresa seleccionada: " + empresa.getNombreCorregido(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNuevaEmpresa(String nombreEmpresa) {
                        etNombreEmpresa.setText(nombreEmpresa);
                        etNitEmpresa.setText("");
                        empresaSeleccionada = null;
                        Toast.makeText(EncuestasPosRegActivity.this,
                                "Nueva empresa: " + nombreEmpresa,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDialogCancelled() {
                        // No hacer nada cuando se cancela
                    }
                });

        dialog.show();
    }

    private void crearYGuardarEmpresa() {
        String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
        String nitEmpresa = etNitEmpresa.getText().toString().trim();

        if (nombreEmpresa.isEmpty()) {
            etNombreEmpresa.setError("Ingrese el nombre de la empresa");
            etNombreEmpresa.requestFocus();
            return;
        }

        empresaSeleccionada = empresaManager.crearNuevaEmpresa(
                nombreEmpresa, nitEmpresa, "", "", "");

        Toast.makeText(this, "Empresa '" + nombreEmpresa + "' guardada localmente", Toast.LENGTH_SHORT).show();
    }

    private void cargarDatosSpinners() {
        cargarListasOriginales();

        // Crear listas filtradas para cada spinner
        listaInteresesFiltrada1 = new ArrayList<>(listaIntereses);
        listaInteresesFiltrada2 = new ArrayList<>(listaIntereses);
        listaInteresesFiltrada3 = new ArrayList<>(listaIntereses);

        listaInteresesIdsFiltrada1 = new ArrayList<>(listaInteresesIds);
        listaInteresesIdsFiltrada2 = new ArrayList<>(listaInteresesIds);
        listaInteresesIdsFiltrada3 = new ArrayList<>(listaInteresesIds);

        // Crear adaptadores independientes para cada spinner
        adapterInteres1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaInteresesFiltrada1);
        adapterInteres1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapterInteres2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaInteresesFiltrada2);
        adapterInteres2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapterInteres3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaInteresesFiltrada3);
        adapterInteres3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerInteres1.setAdapter(adapterInteres1);
        spinnerInteres2.setAdapter(adapterInteres2);
        spinnerInteres3.setAdapter(adapterInteres3);

        configurarListenersSpinners();

        // Cargar proyectos SENA
        cargarProyectosSena();

        // Cargar tipos de programas
        cargarTiposProgramas();
    }

    private void cargarListasOriginales() {
        listaInteresesIds.clear();
        listaIntereses.clear();

        listaInteresesIds.add(-1);
        listaIntereses.add("Seleccione una opción...");

        listaInteresesIds.add(1);
        listaIntereses.add("Encontrar un empleo");

        listaInteresesIds.add(2);
        listaIntereses.add("Emprender");

        listaInteresesIds.add(3);
        listaIntereses.add("Fortalecer mi negocio");

        listaInteresesIds.add(4);
        listaIntereses.add("Deseo comprar");

        listaInteresesIds.add(5);
        listaIntereses.add("Deseo vender");

        listaInteresesIds.add(6);
        listaIntereses.add("Encontrar una práctica laboral");

        listaInteresesIds.add(7);
        listaIntereses.add("Estudiar");

        listaInteresesIds.add(8);
        listaIntereses.add("Certificar mi conocimiento empírico");

        listaInteresesIds.add(9);
        listaIntereses.add("Graduarme del SENA");

        listaInteresesIds.add(10);
        listaIntereses.add("Cambiar de oficio");
    }

    private void cargarProyectosSena() {
        listaProyectosSenaIds.clear();
        listaProyectosSena.clear();

        listaProyectosSenaIds.add(-1);
        listaProyectosSena.add("Seleccione un proyecto...");

        listaProyectosSenaIds.add(1);
        listaProyectosSena.add("Tesoros del atlantico");

        listaProyectosSenaIds.add(2);
        listaProyectosSena.add("Economia popular");

        listaProyectosSenaIds.add(3);
        listaProyectosSena.add("Campesena");

        listaProyectosSenaIds.add(4);
        listaProyectosSena.add("Red Tecnoparque");

        listaProyectosSenaIds.add(5);
        listaProyectosSena.add("Fortalecimiento Empresarial");

        listaProyectosSenaIds.add(6);
        listaProyectosSena.add("Red de Aliados");

        ArrayAdapter<String> adapterProyectos = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaProyectosSena);
        adapterProyectos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyectoSena.setAdapter(adapterProyectos);
    }

    private void cargarTiposProgramas() {
        listaTiposProgramas.clear();
        listaTiposProgramas.add("Seleccione un tipo de programa...");
        listaTiposProgramas.add("Técnico");
        listaTiposProgramas.add("Tecnólogo");
        listaTiposProgramas.add("Curso Corto");

        ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaTiposProgramas);
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoPrograma.setAdapter(adapterTipos);
    }

    private void configurarListenersSpinners() {
        spinnerInteres1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                seleccionInteres1 = listaInteresesFiltrada1.get(position);
                seleccionInteresId1 = listaInteresesIdsFiltrada1.get(position);
                actualizarSpinnersIntereses();
                actualizarVisibilidadTipoPrograma();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionInteres1 = "Seleccione una opción...";
                seleccionInteresId1 = -1;
            }
        });

        spinnerInteres2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                seleccionInteres2 = listaInteresesFiltrada2.get(position);
                seleccionInteresId2 = listaInteresesIdsFiltrada2.get(position);
                actualizarSpinnersIntereses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionInteres2 = "Seleccione una opción...";
                seleccionInteresId2 = -1;
            }
        });

        spinnerInteres3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                seleccionInteres3 = listaInteresesFiltrada3.get(position);
                seleccionInteresId3 = listaInteresesIdsFiltrada3.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionInteres3 = "Seleccione una opción...";
                seleccionInteresId3 = -1;
            }
        });
    }

    private void actualizarSpinnersIntereses() {
        int posicionSpinner2 = spinnerInteres2.getSelectedItemPosition();
        int posicionSpinner3 = spinnerInteres3.getSelectedItemPosition();

        listaInteresesFiltrada2.clear();
        listaInteresesIdsFiltrada2.clear();
        listaInteresesFiltrada3.clear();
        listaInteresesIdsFiltrada3.clear();

        // Para spinner 2, excluir la selección del spinner 1
        for (int i = 0; i < listaIntereses.size(); i++) {
            String interes = listaIntereses.get(i);
            int interesId = listaInteresesIds.get(i);

            if (interesId == -1 || !interes.equals(seleccionInteres1) || seleccionInteresId1 == -1) {
                listaInteresesFiltrada2.add(interes);
                listaInteresesIdsFiltrada2.add(interesId);
            }
        }

        // Para spinner 3, excluir las selecciones de spinner 1 y 2
        for (int i = 0; i < listaIntereses.size(); i++) {
            String interes = listaIntereses.get(i);
            int interesId = listaInteresesIds.get(i);

            if (interesId == -1 ||
                    ((!interes.equals(seleccionInteres1) || seleccionInteresId1 == -1) &&
                            (!interes.equals(seleccionInteres2) || seleccionInteresId2 == -1))) {
                listaInteresesFiltrada3.add(interes);
                listaInteresesIdsFiltrada3.add(interesId);
            }
        }

        adapterInteres2.notifyDataSetChanged();
        adapterInteres3.notifyDataSetChanged();

        try {
            if (posicionSpinner2 >= 0 && posicionSpinner2 < listaInteresesFiltrada2.size()) {
                spinnerInteres2.setSelection(posicionSpinner2);
            } else {
                spinnerInteres2.setSelection(0);
            }

            if (posicionSpinner3 >= 0 && posicionSpinner3 < listaInteresesFiltrada3.size()) {
                spinnerInteres3.setSelection(posicionSpinner3);
            } else {
                spinnerInteres3.setSelection(0);
            }
        } catch (Exception e) {
            Log.e("EncuestasPosReg", "Error al actualizar selección de spinners: " + e.getMessage());
        }
    }

    public void setUpToolbar() {
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Encuesta - Red de Oportunidades");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemCerrarSesion:
                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item3:
                Intent about = new Intent(this, about.class);
                startActivity(about);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void guardarEncuesta(View view) {
        String tipoPersonaSeleccionada = spinnerTipoPersona.getSelectedItem().toString();

        final boolean esPersonaJuridica = tipoPersonaSeleccionada.equals("Persona Jurídica");
        final boolean esAsociacionCampesina = tipoPersonaSeleccionada.equals("Pertenezco a una Asociación Campesina");
        final String tipoPersona = esPersonaJuridica ? "Juridica" : (esAsociacionCampesina ? "Asociacion" : "Natural");

        // Validaciones según el tipo de persona
        if (!validarFormulario(esPersonaJuridica, esAsociacionCampesina)) {
            return;
        }

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Guardando encuesta...");
        progressDialog.show();

        // Preparar datos
        final boolean esRepresentante = (esPersonaJuridica && rbSiRepresentante.isChecked());
        final String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
        final String nitEmpresa = etNitEmpresa.getText().toString().trim();
        final String idEmpresa = (empresaSeleccionada != null) ? String.valueOf(empresaSeleccionada.id) : "-1";

        final int proyectoSenaId = esPersonaJuridica ?
                listaProyectosSenaIds.get(spinnerProyectoSena.getSelectedItemPosition()) : -1;

        final String requerimientoFortalecimiento = (esPersonaJuridica &&
                "Fortalecimiento Empresarial".equals(spinnerProyectoSena.getSelectedItem().toString())) ?
                etRequerimientoFortalecimiento.getText().toString().trim() : "";

        final int interes1Id = !esPersonaJuridica ? seleccionInteresId1 : -1;
        final int interes2Id = !esPersonaJuridica ? seleccionInteresId2 : -1;
        final int interes3Id = !esPersonaJuridica ? seleccionInteresId3 : -1;

        final String tipoPrograma = (!esPersonaJuridica && "Estudiar".equals(seleccionInteres1)) ?
                spinnerTipoPrograma.getSelectedItem().toString() : "";

        final String nombreAsociacion = esAsociacionCampesina ?
                etNombreAsociacion.getText().toString().trim() : "";

        // Enviar datos al servidor
        enviarEncuestaAlServidor(progressDialog, tipoPersona, esRepresentante, nombreEmpresa, nitEmpresa,
                idEmpresa, proyectoSenaId, requerimientoFortalecimiento, interes1Id, interes2Id, interes3Id,
                tipoPrograma, nombreAsociacion);
    }

    private boolean validarFormulario(boolean esPersonaJuridica, boolean esAsociacionCampesina) {
        // Validaciones para persona jurídica
        if (esPersonaJuridica) {
            int posProyecto = spinnerProyectoSena.getSelectedItemPosition();
            if (posProyecto == 0) {
                Toast.makeText(this, "Por favor seleccione un proyecto SENA", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Validar requerimiento para Fortalecimiento Empresarial
            String proyectoSeleccionado = spinnerProyectoSena.getSelectedItem().toString();
            if ("Fortalecimiento Empresarial".equals(proyectoSeleccionado)) {
                String requerimiento = etRequerimientoFortalecimiento.getText().toString().trim();
                if (requerimiento.isEmpty()) {
                    etRequerimientoFortalecimiento.setError("Por favor indique qué requiere del SENA");
                    etRequerimientoFortalecimiento.requestFocus();
                    return false;
                }
            }

            // Validar datos de empresa si es representante
            if (rbSiRepresentante.isChecked()) {
                String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
                if (nombreEmpresa.isEmpty()) {
                    etNombreEmpresa.setError("Ingrese el nombre de la empresa");
                    etNombreEmpresa.requestFocus();
                    return false;
                }
            }
        }

        // Validaciones para persona natural y asociación campesina
        if (!esPersonaJuridica) {
            // Validar intereses
            if (seleccionInteresId1 == -1 || seleccionInteresId2 == -1 || seleccionInteresId3 == -1) {
                Toast.makeText(this, "Por favor seleccione todos sus intereses", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Validar tipo de programa si seleccionó "Estudiar"
            if ("Estudiar".equals(seleccionInteres1)) {
                int posTipoPrograma = spinnerTipoPrograma.getSelectedItemPosition();
                if (posTipoPrograma == 0) {
                    Toast.makeText(this, "Por favor seleccione un tipo de programa", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            // Validar nombre de asociación
            if (esAsociacionCampesina) {
                String nombreAsociacion = etNombreAsociacion.getText().toString().trim();
                if (nombreAsociacion.isEmpty()) {
                    etNombreAsociacion.setError("Ingrese el nombre de la asociación");
                    etNombreAsociacion.requestFocus();
                    return false;
                }
            }
        }

        return true;
    }

    private void enviarEncuestaAlServidor(ProgressDialog progressDialog, String tipoPersona, boolean esRepresentante,
                                          String nombreEmpresa, String nitEmpresa, String idEmpresa, int proyectoSenaId,
                                          String requerimientoFortalecimiento, int interes1Id, int interes2Id,
                                          int interes3Id, String tipoPrograma, String nombreAsociacion) {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/insertarEncuesta.php";
        //String url = "http://192.168.5.106/AsistenciaApi/insertarEncuesta.php"; // URL de desarrollo

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");

                        mostrarResultadoGuardado(success, message);

                    } catch (JSONException e) {
                        Toast.makeText(EncuestasPosRegActivity.this,
                                "Error al procesar respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al procesar respuesta JSON: " + e.getMessage());
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Error en la solicitud: " + error.toString());

                    // Guardar localmente si hay error de conexión
                    guardarEncuestaLocalmente(tipoPersona, esRepresentante ? "Si" : "No", nombreEmpresa, nitEmpresa,
                            proyectoSenaId, interes1Id, interes2Id, interes3Id, tipoPrograma,
                            requerimientoFortalecimiento, idEmpresa, nombreAsociacion);

                    mostrarErrorConexion();
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return construirParametrosEncuesta(tipoPersona, esRepresentante, nombreEmpresa, nitEmpresa,
                        idEmpresa, proyectoSenaId, requerimientoFortalecimiento, interes1Id, interes2Id,
                        interes3Id, tipoPrograma, nombreAsociacion);
            }
        };

        stringRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                15000, 1, 1.0f));

        queue.add(stringRequest);
    }

    private Map<String, String> construirParametrosEncuesta(String tipoPersona, boolean esRepresentante,
                                                            String nombreEmpresa, String nitEmpresa, String idEmpresa,
                                                            int proyectoSenaId, String requerimientoFortalecimiento,
                                                            int interes1Id, int interes2Id, int interes3Id,
                                                            String tipoPrograma, String nombreAsociacion) {
        Map<String, String> params = new HashMap<>();

        // Parámetros básicos
        params.put("cedula", numeroCedula);
        params.put("idEvento", idEvento);
        params.put("idSubevento", idSubevento);
        params.put("tipo_persona", tipoPersona);

        // Separar nombres y apellidos
        if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
            int primerEspacio = nombreCompleto.indexOf(' ');
            if (primerEspacio > 0) {
                params.put("nombres", nombreCompleto.substring(0, primerEspacio));
                params.put("apellidos", nombreCompleto.substring(primerEspacio + 1));
            } else {
                params.put("nombres", nombreCompleto);
                params.put("apellidos", "");
            }
        } else {
            params.put("nombres", "");
            params.put("apellidos", "");
        }

        // Parámetros según tipo de persona
        if (tipoPersona.equals("Juridica")) {
            params.put("proyecto_sena", String.valueOf(proyectoSenaId));
            params.put("requerimiento_fortalecimiento", requerimientoFortalecimiento);
            params.put("interes1", "-1");
            params.put("interes2", "-1");
            params.put("interes3", "-1");
            params.put("tipo_programa", "");
            params.put("nombre_asociacion", "");

            // Datos de empresa si es representante
            if (esRepresentante) {
                params.put("es_representante", "Si");
                params.put("nombre_empresa", nombreEmpresa);
                params.put("nit_empresa", nitEmpresa);
                params.put("id_empresa", idEmpresa);
            } else {
                params.put("es_representante", "No");
                params.put("nombre_empresa", "");
                params.put("nit_empresa", "");
                params.put("id_empresa", "-1");
            }
        } else {
            // Persona Natural o Asociación Campesina
            params.put("proyecto_sena", "-1");
            params.put("requerimiento_fortalecimiento", "");
            params.put("interes1", String.valueOf(interes1Id));
            params.put("interes2", String.valueOf(interes2Id));
            params.put("interes3", String.valueOf(interes3Id));
            params.put("tipo_programa", tipoPrograma);
            params.put("es_representante", "No");
            params.put("nombre_empresa", "");
            params.put("nit_empresa", "");
            params.put("id_empresa", "-1");

            // Nombre de asociación solo para asociaciones campesinas
            params.put("nombre_asociacion", nombreAsociacion);
        }

        // Log para depuración
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Log.d(TAG, "Param: " + entry.getKey() + " = " + entry.getValue());
        }

        return params;
    }

    private void mostrarResultadoGuardado(boolean success, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(success ? "Éxito" : "Error");
        builder.setMessage(message);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            if (success) {
                // Sincronizar empresas si la encuesta se guardó exitosamente
                sincronizarEmpresasYVolver();
            } else {
                volverAPantallaPrincipal();
            }
        });
        builder.show();
    }

    private void mostrarErrorConexion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error de conexión");
        builder.setMessage("No se pudo conectar con el servidor. Los datos se guardarán localmente y se enviarán cuando haya conexión.");
        builder.setPositiveButton("Aceptar", (dialog, which) -> volverAPantallaPrincipal());
        builder.show();
    }

    private void sincronizarEmpresasYVolver() {
        if (empresaManager != null) {
            empresaManager.sincronizarNuevasEmpresas(new EmpresaManager.OnSincronizacionCompletaListener() {
                @Override
                public void onSincronizacionCompleta(boolean exitoso, String mensaje) {
                    Log.d(TAG, "Sincronización de empresas: " + mensaje);
                    if (!exitoso) {
                        Toast.makeText(EncuestasPosRegActivity.this,
                                "Algunas empresas no se sincronizaron: " + mensaje,
                                Toast.LENGTH_SHORT).show();
                    }
                    volverAPantallaPrincipal();
                }
            });
        } else {
            volverAPantallaPrincipal();
        }
    }

    private void volverAPantallaPrincipal() {
        Intent intent = new Intent(EncuestasPosRegActivity.this, AccionesMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void guardarEncuestaLocalmente(String tipoPersona, String esRepresentante, String nombreEmpresa,
                                           String nitEmpresa, int proyectoSenaId, int interes1Id, int interes2Id,
                                           int interes3Id, String tipoPrograma, String requerimientoFortalecimiento,
                                           String idEmpresa, String nombreAsociacion) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("cedula", numeroCedula);
            jsonData.put("idEvento", idEvento);
            jsonData.put("idSubevento", idSubevento);
            jsonData.put("tipo_persona", tipoPersona);

            // Separar nombres y apellidos
            if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                int primerEspacio = nombreCompleto.indexOf(' ');
                if (primerEspacio > 0) {
                    jsonData.put("nombres", nombreCompleto.substring(0, primerEspacio));
                    jsonData.put("apellidos", nombreCompleto.substring(primerEspacio + 1));
                } else {
                    jsonData.put("nombres", nombreCompleto);
                    jsonData.put("apellidos", "");
                }
            } else {
                jsonData.put("nombres", "");
                jsonData.put("apellidos", "");
            }

            if (tipoPersona.equals("Juridica")) {
                jsonData.put("proyecto_sena", proyectoSenaId);
                jsonData.put("requerimiento_fortalecimiento", requerimientoFortalecimiento);
                jsonData.put("interes1", -1);
                jsonData.put("interes2", -1);
                jsonData.put("interes3", -1);
                jsonData.put("tipo_programa", "");
                jsonData.put("nombre_asociacion", "");

                if (esRepresentante.equals("Si")) {
                    jsonData.put("es_representante", "Si");
                    jsonData.put("nombre_empresa", nombreEmpresa);
                    jsonData.put("nit_empresa", nitEmpresa);
                    jsonData.put("id_empresa", idEmpresa);
                } else {
                    jsonData.put("es_representante", "No");
                    jsonData.put("nombre_empresa", "");
                    jsonData.put("nit_empresa", "");
                    jsonData.put("id_empresa", "-1");
                }
            } else {
                jsonData.put("proyecto_sena", -1);
                jsonData.put("requerimiento_fortalecimiento", "");
                jsonData.put("interes1", interes1Id);
                jsonData.put("interes2", interes2Id);
                jsonData.put("interes3", interes3Id);
                jsonData.put("tipo_programa", tipoPrograma);
                jsonData.put("es_representante", "No");
                jsonData.put("nombre_empresa", "");
                jsonData.put("nit_empresa", "");
                jsonData.put("id_empresa", "-1");
                jsonData.put("nombre_asociacion", nombreAsociacion);
            }

            // Guardar usando el gestor de encuestas pendientes
            EncuestasPendientesManager encuestasPendientesManager = new EncuestasPendientesManager(this);
            encuestasPendientesManager.guardarEncuestaPendiente(jsonData);

            Log.d(TAG, "Encuesta guardada localmente: " + jsonData.toString());
            Toast.makeText(this, "Encuesta guardada localmente. Se sincronizará cuando haya conexión.",
                    Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            Log.e(TAG, "Error al guardar encuesta localmente: " + e.getMessage());
            Toast.makeText(this, "Error al guardar datos localmente", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        EncuestasPendientesManager encuestasPendientesManager = new EncuestasPendientesManager(this);

        if (encuestasPendientesManager.hayEncuestasPendientes() && encuestasPendientesManager.hayConexionInternet()) {
            mostrarDialogoSincronizacion(encuestasPendientesManager);
        } else {
            mostrarDialogoSalirSinGuardar();
        }
    }

    private void mostrarDialogoSincronizacion(EncuestasPendientesManager encuestasPendientesManager) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Encuestas pendientes");
        builder.setMessage("Hay encuestas pendientes de sincronización. ¿Desea sincronizarlas ahora?");

        builder.setPositiveButton("Sincronizar", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(EncuestasPosRegActivity.this);
            progressDialog.setMessage("Sincronizando encuestas pendientes...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            encuestasPendientesManager.sincronizarEncuestasPendientes(
                    new EncuestasPendientesManager.OnSincronizacionCompletaListener() {
                        @Override
                        public void onSincronizacionCompleta(boolean exito, String mensaje) {
                            progressDialog.dismiss();
                            Toast.makeText(EncuestasPosRegActivity.this, mensaje, Toast.LENGTH_LONG).show();
                            volverAPantallaPrincipal();
                        }
                    });
        });

        builder.setNegativeButton("Más tarde", (dialog, which) -> volverAPantallaPrincipal());
        builder.show();
    }

    private void mostrarDialogoSalirSinGuardar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Salir sin guardar?");
        builder.setMessage("Si sale ahora, no se guardarán los datos de la encuesta.");
        builder.setPositiveButton("Salir", (dialog, which) -> volverAPantallaPrincipal());
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}