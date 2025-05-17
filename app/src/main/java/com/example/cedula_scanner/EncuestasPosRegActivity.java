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
    private RadioGroup rgTipoPersona, rgRepresentanteEmpresa;
    private RadioButton rbPersonaNatural, rbPersonaJuridica, rbSiRepresentante, rbNoRepresentante;
    private LinearLayout layoutPersonaJuridica, layoutDatosEmpresa, layoutIntereses;
    private Spinner spinnerProyectoSena, spinnerInteres1, spinnerInteres2, spinnerInteres3;

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
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto);
        tvCedula = findViewById(R.id.tvCedula);

        rgTipoPersona = findViewById(R.id.rgTipoPersona);
        rbPersonaNatural = findViewById(R.id.rbPersonaNatural);
        rbPersonaJuridica = findViewById(R.id.rbPersonaJuridica);

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

        // Nuevas vistas
        layoutFortalecimientoEmpresarial = findViewById(R.id.layoutFortalecimientoEmpresarial);
        etRequerimientoFortalecimiento = findViewById(R.id.etRequerimientoFortalecimiento);

        // Inicializar EmpresaManager
        empresaManager = new EmpresaManager(this);

        // Inicializar botón de búsqueda de empresas
        btnBuscarEmpresa = findViewById(R.id.btnBuscarEmpresa);
        btnBuscarEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoBusquedaEmpresa();
            }
        });

        // Inicializar botón de crear empresa
        btnCrearNuevaEmpresa = findViewById(R.id.btnCrearNuevaEmpresa);
        btnCrearNuevaEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearYGuardarEmpresa();
            }
        });

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

        // Configurar los listeners para mostrar/ocultar secciones
        rgTipoPersona.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbPersonaJuridica) {
                    // Para personas jurídicas:
                    // - Mostrar sección de proyecto SENA
                    sectionProyectoSena.setVisibility(View.VISIBLE);
                    // - Mostrar selección de representante de empresa
                    layoutPersonaJuridica.setVisibility(View.VISIBLE);
                    // - Ocultar sección de intereses
                    layoutIntereses.setVisibility(View.GONE);
                    // - Ocultar sección de tipo de programa
                    sectionTipoPrograma.setVisibility(View.GONE);
                } else {
                    // Para personas naturales:
                    // - Ocultar sección de proyecto SENA
                    sectionProyectoSena.setVisibility(View.GONE);
                    // - Ocultar selección de representante de empresa
                    layoutPersonaJuridica.setVisibility(View.GONE);
                    // - Mostrar sección de intereses
                    layoutIntereses.setVisibility(View.VISIBLE);
                    // - La sección de tipo de programa dependerá del interés seleccionado
                    actualizarVisibilidadTipoPrograma();
                }
            }
        });

        rgRepresentanteEmpresa.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbSiRepresentante) {
                    layoutDatosEmpresa.setVisibility(View.VISIBLE);
                } else {
                    layoutDatosEmpresa.setVisibility(View.GONE);
                }
            }
        });

        // Configurar listener para el spinner de interés 1
        spinnerInteres1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarVisibilidadTipoPrograma();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
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

        // Llenar los spinners con datos
        cargarDatosSpinners();

        // Establecer estado inicial basado en el tipo de persona seleccionado
        if (rbPersonaJuridica.isChecked()) {
            sectionProyectoSena.setVisibility(View.VISIBLE);
            layoutPersonaJuridica.setVisibility(View.VISIBLE);
            layoutIntereses.setVisibility(View.GONE);
            sectionTipoPrograma.setVisibility(View.GONE);
        } else {
            sectionProyectoSena.setVisibility(View.GONE);
            layoutPersonaJuridica.setVisibility(View.GONE);
            layoutIntereses.setVisibility(View.VISIBLE);
            actualizarVisibilidadTipoPrograma();
        }
    }

    // Método para actualizar la visibilidad de la sección de tipo de programa
    // basado en si "Estudiar" está seleccionado como interés principal
    private void actualizarVisibilidadTipoPrograma() {
        if (rbPersonaNatural.isChecked() && spinnerInteres1.getSelectedItemPosition() > 0) {
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
    // Método para normalizar la visualización de texto con caracteres especiales
    private String mostrarTexto(String texto) {
        if (texto == null) return "";

        // Si no hay caracteres especiales latinoamericanos visibles, intentamos arreglar
        if (!texto.matches(".*[áéíóúÁÉÍÓÚñÑüÜ].*")) {
            try {
                // Intenta interpretar como Latin-1 (ISO-8859-1)
                return new String(texto.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                return texto;
            }
        }

        return texto;
    }

    // Método para mostrar el diálogo de búsqueda de empresas
    private void mostrarDialogoBusquedaEmpresa() {
        SelectorEmpresaDialog dialog = new SelectorEmpresaDialog(this, empresaManager,
                new SelectorEmpresaDialog.OnEmpresaSelectedListener() {
                    @Override
                    public void onEmpresaSelected(EmpresaManager.Empresa empresa) {
                        // Guardar la empresa seleccionada
                        empresaSeleccionada = empresa;

                        // Actualizar los campos con la información de la empresa
                        etNombreEmpresa.setText(empresa.getNombreCorregido());
                        etNitEmpresa.setText(empresa.nit);

                        Toast.makeText(EncuestasPosRegActivity.this,
                                "Empresa seleccionada: " + empresa.getNombreCorregido(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNuevaEmpresa(String nombreEmpresa) {
                        // Limpiar los campos y establecer solo el nombre para una nueva empresa
                        etNombreEmpresa.setText(nombreEmpresa);
                        etNitEmpresa.setText("");

                        // Limpiar referencia a empresa seleccionada
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

        // Crear una nueva empresa usando el empresaManager
        empresaSeleccionada = empresaManager.crearNuevaEmpresa(
                nombreEmpresa,  // Nombre
                nitEmpresa,     // NIT
                "",             // Teléfono (vacío)
                "",             // Correo (vacío)
                ""              // Dirección (vacía)
        );

        Toast.makeText(this, "Empresa '" + nombreEmpresa + "' guardada localmente", Toast.LENGTH_SHORT).show();
    }

    // Clase interna para representar un curso
    private static class Curso {
        int id;
        String nombre;
        String tipo;

        Curso(int id, String nombre, String tipo) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
        }

        // Método para obtener el nombre corregido
        public String getNombreCorregido() {
            try {
                // Intentar corregir la codificación
                return new String(nombre.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                return nombre; // Si falla, devolver el nombre original
            }
        }
    }




    // Este método reemplaza tu método actual cargarDatosSpinners()
    private void cargarDatosSpinners() {
        // 1. Cargar datos originales como lo tenías
        cargarListasOriginales();

        // 2. Crear listas filtradas para cada spinner (inicialmente iguales a la original)
        listaInteresesFiltrada1 = new ArrayList<>(listaIntereses);
        listaInteresesFiltrada2 = new ArrayList<>(listaIntereses);
        listaInteresesFiltrada3 = new ArrayList<>(listaIntereses);

        listaInteresesIdsFiltrada1 = new ArrayList<>(listaInteresesIds);
        listaInteresesIdsFiltrada2 = new ArrayList<>(listaInteresesIds);
        listaInteresesIdsFiltrada3 = new ArrayList<>(listaInteresesIds);

        // 3. Crear adaptadores independientes para cada spinner
        adapterInteres1 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaInteresesFiltrada1);
        adapterInteres1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapterInteres2 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaInteresesFiltrada2);
        adapterInteres2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapterInteres3 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaInteresesFiltrada3);
        adapterInteres3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 4. Configurar los adaptadores a los spinners
        spinnerInteres1.setAdapter(adapterInteres1);
        spinnerInteres2.setAdapter(adapterInteres2);
        spinnerInteres3.setAdapter(adapterInteres3);

        // 5. Configurar listeners para los spinners
        configurarListenersSpinners();

        // 6. El resto del código de carga (proyectos SENA, tipos de programas) permanece igual
        // Cargar lista de proyectos SENA (con nuevas opciones)
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

        // Nuevas opciones
        listaProyectosSenaIds.add(5);
        listaProyectosSena.add("Fortalecimiento Empresarial");

        listaProyectosSenaIds.add(6);
        listaProyectosSena.add("Red de Aliados");

        // Crear adaptador para el spinner de proyectos SENA
        ArrayAdapter<String> adapterProyectos = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaProyectosSena);
        adapterProyectos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerProyectoSena.setAdapter(adapterProyectos);

        // Cargar tipos de programas
        listaTiposProgramas.clear();
        listaTiposProgramas.add("Seleccione un tipo de programa...");
        listaTiposProgramas.add("Técnico");
        listaTiposProgramas.add("Tecnólogo");
        listaTiposProgramas.add("Curso Corto");

        // Crear adaptador para el spinner de tipos de programa
        ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaTiposProgramas);
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTipoPrograma.setAdapter(adapterTipos);
    }

    // Este es un nuevo método que debes añadir
    private void cargarListasOriginales() {
        // Limpiar listas primero para evitar duplicados si se llama múltiples veces
        listaInteresesIds.clear();
        listaIntereses.clear();

        // Cargar lista de intereses de la Red de Oportunidades
        listaInteresesIds.add(-1);
        listaIntereses.add("Seleccione una opción...");

        // Añadir intereses según la tabla que mostraste
        listaInteresesIds.add(1);
        listaIntereses.add("Encontrar un empleo");

        listaInteresesIds.add(2);
        listaIntereses.add("Emprender");

        listaInteresesIds.add(3);
        listaIntereses.add("Fortalecer mi negocio");

        listaInteresesIds.add(4);
        listaIntereses.add("Encontrar una práctica laboral");

        listaInteresesIds.add(5);
        listaIntereses.add("Estudiar");

        listaInteresesIds.add(6);
        listaIntereses.add("Certificar mi conocimiento empírico");

        listaInteresesIds.add(7);
        listaIntereses.add("Graduarme del SENA");

        listaInteresesIds.add(8);
        listaIntereses.add("Cambiar de oficio");
    }


    private void configurarListenersSpinners() {
        // Configurar el listener para el primer spinner
        spinnerInteres1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Guardar la selección actual
                seleccionInteres1 = listaInteresesFiltrada1.get(position);
                seleccionInteresId1 = listaInteresesIdsFiltrada1.get(position);

                // Actualizar los otros spinners
                actualizarSpinnersIntereses();

                // Mostrar sección de tipo de programa si selecciona "Estudiar"
                if (seleccionInteres1.equals("Estudiar")) {
                    findViewById(R.id.sectionTipoPrograma).setVisibility(View.VISIBLE);
                } else if (!seleccionInteres2.equals("Estudiar") && !seleccionInteres3.equals("Estudiar")) {
                    findViewById(R.id.sectionTipoPrograma).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionInteres1 = "Seleccione una opción...";
                seleccionInteresId1 = -1;
            }
        });

        // Configurar el listener para el segundo spinner
        spinnerInteres2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Guardar la selección actual
                seleccionInteres2 = listaInteresesFiltrada2.get(position);
                seleccionInteresId2 = listaInteresesIdsFiltrada2.get(position);

                // Actualizar los otros spinners
                actualizarSpinnersIntereses();

                // Mostrar sección de tipo de programa si selecciona "Estudiar"
                if (seleccionInteres2.equals("Estudiar")) {
                    findViewById(R.id.sectionTipoPrograma).setVisibility(View.VISIBLE);
                } else if (!seleccionInteres1.equals("Estudiar") && !seleccionInteres3.equals("Estudiar")) {
                    findViewById(R.id.sectionTipoPrograma).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionInteres2 = "Seleccione una opción...";
                seleccionInteresId2 = -1;
            }
        });

        // Configurar el listener para el tercer spinner
        spinnerInteres3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Guardar la selección actual
                seleccionInteres3 = listaInteresesFiltrada3.get(position);
                seleccionInteresId3 = listaInteresesIdsFiltrada3.get(position);

                // Mostrar sección de tipo de programa si selecciona "Estudiar"
                if (seleccionInteres3.equals("Estudiar")) {
                    findViewById(R.id.sectionTipoPrograma).setVisibility(View.VISIBLE);
                } else if (!seleccionInteres1.equals("Estudiar") && !seleccionInteres2.equals("Estudiar")) {
                    findViewById(R.id.sectionTipoPrograma).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionInteres3 = "Seleccione una opción...";
                seleccionInteresId3 = -1;
            }
        });
    }


    private void actualizarSpinnersIntereses() {
        // Guardar las selecciones actuales antes de actualizar los adaptadores
        int posicionSpinner2 = spinnerInteres2.getSelectedItemPosition();
        int posicionSpinner3 = spinnerInteres3.getSelectedItemPosition();

        // 1. Reiniciar las listas filtradas para spinner 2 y 3
        listaInteresesFiltrada2.clear();
        listaInteresesIdsFiltrada2.clear();
        listaInteresesFiltrada3.clear();
        listaInteresesIdsFiltrada3.clear();

        // 2. Repoblar las listas filtradas excluyendo las selecciones actuales
        // Para spinner 2, excluir la selección del spinner 1
        for (int i = 0; i < listaIntereses.size(); i++) {
            String interes = listaIntereses.get(i);
            int interesId = listaInteresesIds.get(i);

            // Si es la primera opción (Seleccione...) o si no es la opción seleccionada en spinner 1
            if (interesId == -1 || !interes.equals(seleccionInteres1) || seleccionInteresId1 == -1) {
                listaInteresesFiltrada2.add(interes);
                listaInteresesIdsFiltrada2.add(interesId);
            }
        }

        // Para spinner 3, excluir las selecciones de spinner 1 y 2
        for (int i = 0; i < listaIntereses.size(); i++) {
            String interes = listaIntereses.get(i);
            int interesId = listaInteresesIds.get(i);

            // Si es la primera opción o si no es la opción seleccionada en spinner 1 ni spinner 2
            if (interesId == -1 ||
                    ((!interes.equals(seleccionInteres1) || seleccionInteresId1 == -1) &&
                            (!interes.equals(seleccionInteres2) || seleccionInteresId2 == -1))) {
                listaInteresesFiltrada3.add(interes);
                listaInteresesIdsFiltrada3.add(interesId);
            }
        }

        // 3. Notificar a los adaptadores que los datos han cambiado
        adapterInteres2.notifyDataSetChanged();
        adapterInteres3.notifyDataSetChanged();

        // 4. Intentar mantener las mismas selecciones si aún están disponibles
        try {
            if (posicionSpinner2 >= 0 && posicionSpinner2 < listaInteresesFiltrada2.size()) {
                spinnerInteres2.setSelection(posicionSpinner2);
            } else {
                spinnerInteres2.setSelection(0); // Seleccionar primera opción
            }

            if (posicionSpinner3 >= 0 && posicionSpinner3 < listaInteresesFiltrada3.size()) {
                spinnerInteres3.setSelection(posicionSpinner3);
            } else {
                spinnerInteres3.setSelection(0); // Seleccionar primera opción
            }
        } catch (Exception e) {
            Log.e("EncuestasPosReg", "Error al actualizar selección de spinners: " + e.getMessage());
        }
    }

    // Método para actualizar el spinner de tipos de programas
    private void actualizarTiposProgramas() {
        // Obtener tipos únicos de todos los cursos
        Set<String> tiposUnicos = new HashSet<>();


        // Convertir a lista y ordenar
        List<String> tiposOriginales = new ArrayList<>(tiposUnicos);
        Collections.sort(tiposOriginales);

        // Corregir la codificación de los tipos
        listaTiposProgramas.clear();
        for (String tipo : tiposOriginales) {
            try {
                // Corregir la codificación
                String tipoCorregido = new String(tipo.getBytes("ISO-8859-1"), "UTF-8");
                listaTiposProgramas.add(tipoCorregido);
            } catch (Exception e) {
                // Si falla, usar el tipo original
                listaTiposProgramas.add(tipo);
            }
        }

        // Crear adaptador para el spinner de tipos
        ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaTiposProgramas);
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTipoPrograma.setAdapter(adapterTipos);

    }

    // Método para cargar ejemplos de cursos en caso de error


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
                // Cerrar sesión
                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                // Redirigir al login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item3:
                // Código para la opción "Acerca de"
                Intent about = new Intent(this, about.class);
                startActivity(about);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void guardarEncuesta(View view) {
        boolean esPersonaJuridica = rbPersonaJuridica.isChecked();

        // Validar que se haya seleccionado un proyecto SENA (solo para persona jurídica)
        if (esPersonaJuridica) {
            int posProyecto = spinnerProyectoSena.getSelectedItemPosition();
            if (posProyecto == 0) {
                Toast.makeText(this, "Por favor seleccione un proyecto SENA", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar el campo de requerimiento si seleccionó Fortalecimiento Empresarial
            String proyectoSeleccionado = spinnerProyectoSena.getSelectedItem().toString();
            if ("Fortalecimiento Empresarial".equals(proyectoSeleccionado)) {
                String requerimiento = etRequerimientoFortalecimiento.getText().toString().trim();
                if (requerimiento.isEmpty()) {
                    etRequerimientoFortalecimiento.setError("Por favor indique qué requiere del SENA");
                    etRequerimientoFortalecimiento.requestFocus();
                    return;
                }
            }
        }

        // Validar que se hayan seleccionado intereses para personas naturales
        if (!esPersonaJuridica) {
            int posInteres1 = spinnerInteres1.getSelectedItemPosition();
            int posInteres2 = spinnerInteres2.getSelectedItemPosition();
            int posInteres3 = spinnerInteres3.getSelectedItemPosition();

            if (posInteres1 == 0) {
                Toast.makeText(this, "Por favor seleccione su interés principal", Toast.LENGTH_SHORT).show();
                return;
            }

            if (posInteres2 == 0) {
                Toast.makeText(this, "Por favor seleccione su segundo interés", Toast.LENGTH_SHORT).show();
                return;
            }

            if (posInteres3 == 0) {
                Toast.makeText(this, "Por favor seleccione su tercer interés", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que no se repitan los intereses
            String interes1 = spinnerInteres1.getSelectedItem().toString();
            String interes2 = spinnerInteres2.getSelectedItem().toString();
            String interes3 = spinnerInteres3.getSelectedItem().toString();

            // Verificamos si alguno es "Seleccione una opción..." o si están repetidos
            if (interes1.equals("Seleccione una opción...") ||
                    interes2.equals("Seleccione una opción...") ||
                    interes3.equals("Seleccione una opción...")) {
                Toast.makeText(this, "Por favor, seleccione todos sus intereses", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar el tipo de programa si el interés principal es "Estudiar"
            String interesSeleccionado = spinnerInteres1.getSelectedItem().toString();
            if ("Estudiar".equals(interesSeleccionado)) {
                int posTipoPrograma = spinnerTipoPrograma.getSelectedItemPosition();
                if (posTipoPrograma == 0) {
                    Toast.makeText(this, "Por favor seleccione un tipo de programa", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Si es representante de empresa, validar datos de empresa (solo para personas jurídicas)
        if (esPersonaJuridica) {
            boolean esRepresentanteEmpresa = rbSiRepresentante.isChecked();
            if (esRepresentanteEmpresa) {
                String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
                if (nombreEmpresa.isEmpty()) {
                    etNombreEmpresa.setError("Ingrese el nombre de la empresa");
                    etNombreEmpresa.requestFocus();
                    return;
                }
            }
        }

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Guardando encuesta...");
        progressDialog.show();

        // Preparar datos para enviar al servidor
        final String tipoPersona = rbPersonaJuridica.isChecked() ? "Juridica" : "Natural";
        final String esRepresentante = (esPersonaJuridica && rbSiRepresentante.isChecked()) ? "Si" : "No";

        final String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
        final String nitEmpresa = etNitEmpresa.getText().toString().trim();

        // Si tenemos una empresa seleccionada, usamos su ID
        final String idEmpresa = (empresaSeleccionada != null) ?
                String.valueOf(empresaSeleccionada.id) : "-1";

        // Solo obtener proyecto SENA si es persona jurídica, sino usar valor por defecto
        final int proyectoSenaId = esPersonaJuridica ?
                listaProyectosSenaIds.get(spinnerProyectoSena.getSelectedItemPosition()) : -1;

        // Obtener requerimiento para Fortalecimiento Empresarial
        final String requerimientoFortalecimiento = (esPersonaJuridica &&
                "Fortalecimiento Empresarial".equals(spinnerProyectoSena.getSelectedItem().toString())) ?
                etRequerimientoFortalecimiento.getText().toString().trim() : "";

        // Para intereses, solo si es persona natural
        final int interes1Id = !esPersonaJuridica ? listaInteresesIds.get(spinnerInteres1.getSelectedItemPosition()) : -1;
        final int interes2Id = !esPersonaJuridica ? listaInteresesIds.get(spinnerInteres2.getSelectedItemPosition()) : -1;
        final int interes3Id = !esPersonaJuridica ? listaInteresesIds.get(spinnerInteres3.getSelectedItemPosition()) : -1;

        // Obtener tipo de programa si corresponde
        final String tipoPrograma = (!esPersonaJuridica && "Estudiar".equals(spinnerInteres1.getSelectedItem().toString())) ?
                spinnerTipoPrograma.getSelectedItem().toString() : "";

        // Enviar datos al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/insertarEncuesta.php";
        //String url = "http://192.168.0.14/AsistenciaApi/insertarEncuesta.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        // Imprimir la respuesta completa para depuración
                        //Log.d(TAG, "Respuesta del servidor: " + response);

                        // Mostrar la respuesta sin procesar en un Toast para depuración
//                        Toast.makeText(EncuestasPosRegActivity.this,
//                                "Respuesta: " + response,
//                                Toast.LENGTH_LONG).show();

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");

                            // Mostrar alerta con el mensaje del servidor
                            AlertDialog.Builder builder = new AlertDialog.Builder(EncuestasPosRegActivity.this);
                            builder.setTitle(success ? "Éxito" : "Error");
                            builder.setMessage(message);
                            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (success) {
                                        // Solo sincronizar empresas si la encuesta se guardó con éxito
                                        empresaManager.sincronizarNuevasEmpresas(new EmpresaManager.OnSincronizacionCompletaListener() {
                                            @Override
                                            public void onSincronizacionCompleta(boolean exitoso, String mensaje) {
                                                Log.d(TAG, "Sincronización de empresas: " + mensaje);
                                                // Opcional: mostrar un toast si hay errores
                                                if (!exitoso) {
                                                    Toast.makeText(EncuestasPosRegActivity.this,
                                                            "Algunas empresas no se sincronizaron: " + mensaje,
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                                // Volver a la pantalla principal después de sincronizar
                                                Intent intent = new Intent(EncuestasPosRegActivity.this, AccionesMainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    } else {
                                        // Si no se guardó correctamente, solo volver a la pantalla principal sin sincronizar
                                        Intent intent = new Intent(EncuestasPosRegActivity.this, AccionesMainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                            builder.show();

                        } catch (JSONException e) {
                            Toast.makeText(EncuestasPosRegActivity.this, "Error al procesar la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al procesar respuesta JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                        Log.e(TAG, "Error en la solicitud: " + error.toString());

                        // Mostrar el mensaje de error
                        AlertDialog.Builder builder = new AlertDialog.Builder(EncuestasPosRegActivity.this);
                        builder.setTitle("Error de conexión");
                        builder.setMessage("No se pudo conectar con el servidor. Los datos se guardarán localmente y se enviarán cuando haya conexión.");

                        // Guardar los datos localmente para enviarlos después
                        guardarEncuestaLocalmente(tipoPersona, esRepresentante, nombreEmpresa, nitEmpresa,
                                proyectoSenaId, interes1Id, interes2Id, interes3Id, tipoPrograma, requerimientoFortalecimiento, idEmpresa);

                        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Volver a la pantalla principal
                                Intent intent = new Intent(EncuestasPosRegActivity.this, AccionesMainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                        builder.show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("cedula", numeroCedula);
                params.put("idEvento", idEvento);
                params.put("idSubevento", idSubevento);
                params.put("tipo_persona", tipoPersona);

                // Solo enviar proyecto SENA si es persona jurídica
                if (tipoPersona.equals("Juridica")) {
                    params.put("proyecto_sena", String.valueOf(proyectoSenaId));

                    // Si eligió Fortalecimiento Empresarial, enviar el requerimiento
                    if ("Fortalecimiento Empresarial".equals(spinnerProyectoSena.getSelectedItem().toString())) {
                        params.put("requerimiento_fortalecimiento", requerimientoFortalecimiento);
                    } else {
                        params.put("requerimiento_fortalecimiento", "");
                    }
                } else {
                    params.put("proyecto_sena", "-1"); // Valor por defecto para persona natural
                    params.put("requerimiento_fortalecimiento", "");
                }

                // Separar nombres y apellidos del nombre completo
                if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                    // Obtener el primer nombre
                    int primerEspacio = nombreCompleto.indexOf(' ');
                    if (primerEspacio > 0) {
                        // Si hay espacio, divide en primer nombre y el resto
                        String primerNombre = nombreCompleto.substring(0, primerEspacio);
                        String restoNombre = nombreCompleto.substring(primerEspacio + 1);
                        params.put("nombres", primerNombre);
                        params.put("apellidos", restoNombre);
                    } else {
                        // Si no hay espacio, todo es nombre
                        params.put("nombres", nombreCompleto);
                        params.put("apellidos", "");
                    }
                } else {
                    params.put("nombres", "");
                    params.put("apellidos", "");
                }

                // Intereses (si es persona natural)
                if (!tipoPersona.equals("Juridica")) {
                    params.put("interes1", String.valueOf(interes1Id));
                    params.put("interes2", String.valueOf(interes2Id));
                    params.put("interes3", String.valueOf(interes3Id));

                    // Agregar tipo de programa si el interés principal es "Estudiar"
                    if ("Estudiar".equals(spinnerInteres1.getSelectedItem().toString())) {
                        params.put("tipo_programa", tipoPrograma);
                    } else {
                        params.put("tipo_programa", "");
                    }
                } else {
                    params.put("interes1", "-1");
                    params.put("interes2", "-1");
                    params.put("interes3", "-1");
                    params.put("tipo_programa", "");
                }

                // Si es representante de empresa (solo para persona jurídica)
                if (tipoPersona.equals("Juridica") && esRepresentante.equals("Si")) {
                    params.put("es_representante", "Si");
                    params.put("nombre_empresa", nombreEmpresa);
                    params.put("nit_empresa", nitEmpresa);

                    // Añadir el ID de la empresa (si existe)
                    params.put("id_empresa", idEmpresa);
                } else {
                    params.put("es_representante", "No");
                }

                // Imprimir los parámetros para depuración
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    Log.d(TAG, "Param: " + entry.getKey() + " = " + entry.getValue());
                }

                return params;
            }
        };

        // Aumentar el tiempo de espera para la solicitud
        stringRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                15000, // 15 segundos de timeout
                1, // Sin reintentos
                1.0f));

        queue.add(stringRequest);
    }

    private void guardarEncuestaLocalmente(String tipoPersona, String esRepresentante,
                                           String nombreEmpresa, String nitEmpresa,
                                           int proyectoSenaId, int interes1Id,
                                           int interes2Id, int interes3Id,
                                           String tipoPrograma, String requerimientoFortalecimiento,
                                           String idEmpresa) {
        try {
            // Crear un objeto JSON con todos los datos
            JSONObject jsonData = new JSONObject();
            jsonData.put("cedula", numeroCedula);
            jsonData.put("idEvento", idEvento);
            jsonData.put("idSubevento", idSubevento);
            jsonData.put("tipo_persona", tipoPersona);

            if (tipoPersona.equals("Juridica")) {
                jsonData.put("proyecto_sena", proyectoSenaId);
                jsonData.put("requerimiento_fortalecimiento", requerimientoFortalecimiento);
            } else {
                jsonData.put("proyecto_sena", -1);
                jsonData.put("requerimiento_fortalecimiento", "");
            }

            // Intereses (solo para persona natural)
            if (!tipoPersona.equals("Juridica")) {
                jsonData.put("interes1", interes1Id);
                jsonData.put("interes2", interes2Id);
                jsonData.put("interes3", interes3Id);
                jsonData.put("tipo_programa", tipoPrograma);
            } else {
                jsonData.put("interes1", -1);
                jsonData.put("interes2", -1);
                jsonData.put("interes3", -1);
                jsonData.put("tipo_programa", "");
            }

            if (esRepresentante.equals("Si") && tipoPersona.equals("Juridica")) {
                jsonData.put("es_representante", "Si");
                jsonData.put("nombre_empresa", nombreEmpresa);
                jsonData.put("nit_empresa", nitEmpresa);

                // Guardar el ID de la empresa
                jsonData.put("id_empresa", idEmpresa);
            } else {
                jsonData.put("es_representante", "No");
            }

            // Usar el gestor de encuestas pendientes para guardar
            EncuestasPendientesManager encuestasPendientesManager = new EncuestasPendientesManager(this);
            encuestasPendientesManager.guardarEncuestaPendiente(jsonData);

            Log.d(TAG, "Encuesta guardada localmente: " + jsonData.toString());
            Toast.makeText(this, "Encuesta guardada localmente. Se sincronizará cuando haya conexión.", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            Log.e(TAG, "Error al guardar encuesta localmente: " + e.getMessage());
            Toast.makeText(this, "Error al guardar datos localmente", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Verificar si hay encuestas pendientes y hay conexión a internet
        EncuestasPendientesManager encuestasPendientesManager = new EncuestasPendientesManager(this);
        if (encuestasPendientesManager.hayEncuestasPendientes() && encuestasPendientesManager.hayConexionInternet()) {
            // Mostrar alerta para sincronizar las encuestas pendientes
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Encuestas pendientes");
            builder.setMessage("Hay encuestas pendientes de sincronización. ¿Desea sincronizarlas ahora?");

            builder.setPositiveButton("Sincronizar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Mostrar diálogo de progreso
                    ProgressDialog progressDialog = new ProgressDialog(EncuestasPosRegActivity.this);
                    progressDialog.setMessage("Sincronizando encuestas pendientes...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    // Sincronizar encuestas pendientes
                    encuestasPendientesManager.sincronizarEncuestasPendientes(new EncuestasPendientesManager.OnSincronizacionCompletaListener() {
                        @Override
                        public void onSincronizacionCompleta(boolean exito, String mensaje) {
                            progressDialog.dismiss();

                            Toast.makeText(EncuestasPosRegActivity.this, mensaje, Toast.LENGTH_LONG).show();

                            // Volver a la pantalla principal
                            Intent intent = new Intent(EncuestasPosRegActivity.this, AccionesMainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            });

            builder.setNegativeButton("Más tarde", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Volver a la pantalla principal sin sincronizar
                    Intent intent = new Intent(EncuestasPosRegActivity.this, AccionesMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            builder.show();
        } else {
            // Mostrar alerta antes de salir sin guardar (comportamiento original)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¿Salir sin guardar?");
            builder.setMessage("Si sale ahora, no se guardarán los datos de la encuesta.");
            builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(EncuestasPosRegActivity.this, AccionesMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }
    }
}