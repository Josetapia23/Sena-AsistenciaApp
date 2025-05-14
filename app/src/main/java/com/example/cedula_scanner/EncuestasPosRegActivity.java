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
    private EditText etNombreEmpresa, etNitEmpresa, etTelefonoEmpresa, etCorreoEmpresa;
    private RadioGroup rgTipoPersona, rgRepresentanteEmpresa;
    private RadioButton rbPersonaNatural, rbPersonaJuridica, rbSiRepresentante, rbNoRepresentante;
    private LinearLayout layoutPersonaJuridica, layoutDatosEmpresa;
    private Spinner spinnerProyectoSena, spinnerInteres1, spinnerInteres2, spinnerInteres3;
    private Spinner spinnerTipoPrograma;

    // Nuevas variables para la selección mejorada de programas
    private Button btnSeleccionarProgramas;
    private TextView tvProgramasSeleccionados;
    private LinearLayout layoutProgramasSeleccionados;

    private String nombreCompleto;
    private String numeroCedula;
    private String idEvento;
    private String idSubevento;

    // Arrays para los spinners
    private List<String> listaIntereses = new ArrayList<>();
    private List<Integer> listaInteresesIds = new ArrayList<>();
    private List<String> listaProyectosSena = new ArrayList<>();
    private List<Integer> listaProyectosSenaIds = new ArrayList<>();
    private List<String> listaTiposProgramas = new ArrayList<>();

    // Lista para los cursos seleccionados
    private List<Integer> cursosSeleccionados = new ArrayList<>();

    // Lista completa de todos los cursos (para almacenamiento local)
    private Map<Integer, Curso> todosLosCursos = new HashMap<>();
    private boolean datosAlmacenados = false;

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

        rgRepresentanteEmpresa = findViewById(R.id.rgRepresentanteEmpresa);
        rbSiRepresentante = findViewById(R.id.rbSiRepresentante);
        rbNoRepresentante = findViewById(R.id.rbNoRepresentante);

        layoutDatosEmpresa = findViewById(R.id.layoutDatosEmpresa);

        etNombreEmpresa = findViewById(R.id.etNombreEmpresa);
        etNitEmpresa = findViewById(R.id.etNitEmpresa);
        etTelefonoEmpresa = findViewById(R.id.etTelefonoEmpresa);
        etCorreoEmpresa = findViewById(R.id.etCorreoEmpresa);

        spinnerProyectoSena = findViewById(R.id.spinnerProyectoSena);
        spinnerInteres1 = findViewById(R.id.spinnerInteres1);
        spinnerInteres2 = findViewById(R.id.spinnerInteres2);
        spinnerInteres3 = findViewById(R.id.spinnerInteres3);

        spinnerTipoPrograma = findViewById(R.id.spinnerTipoPrograma);

        // Inicializar nuevas vistas para la selección de programas
        btnSeleccionarProgramas = findViewById(R.id.btnSeleccionarProgramas);
        tvProgramasSeleccionados = findViewById(R.id.tvProgramasSeleccionados);
        layoutProgramasSeleccionados = findViewById(R.id.layoutProgramasSeleccionados);

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
                    layoutPersonaJuridica.setVisibility(View.VISIBLE);
                } else {
                    layoutPersonaJuridica.setVisibility(View.GONE);
                    layoutDatosEmpresa.setVisibility(View.GONE);
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

        // Configurar Spinner para tipo de programa
        spinnerTipoPrograma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipoSeleccionado = (String) parent.getItemAtPosition(position);
                filtrarCursosPorTipo(tipoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        // Configurar el botón para seleccionar programas
        btnSeleccionarProgramas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoProgramas();
            }
        });

        // Llenar los spinners con datos
        cargarDatosSpinners();

        // Verificar si hay datos guardados en SharedPreferences
        cargarCursosDesdePreferencias();

        // Si no hay datos locales o si han pasado más de 24 horas desde la última actualización,
        // intentar cargar cursos desde el servidor
        if (!datosAlmacenados) {
            cargarCursosDesdeServidor();
        } else {
            // Si hay datos locales, actualizar el spinner de tipos y filtrar con el primer tipo
            actualizarTiposProgramas();
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

    // Método para mostrar el diálogo de selección de programas
    private void mostrarDialogoProgramas() {
        // Obtener el tipo seleccionado actualmente
        String tipoMostrado = "";
        if (spinnerTipoPrograma.getSelectedItem() != null) {
            tipoMostrado = spinnerTipoPrograma.getSelectedItem().toString();
        } else {
            Toast.makeText(this, "Seleccione un tipo de programa primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Necesitamos encontrar el tipo original que corresponde al tipo mostrado
        String tipoOriginal = null;
        for (Curso curso : todosLosCursos.values()) {
            String tipoCursoCorregido;
            try {
                tipoCursoCorregido = new String(curso.tipo.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                tipoCursoCorregido = curso.tipo;
            }

            if (tipoCursoCorregido.equals(tipoMostrado)) {
                tipoOriginal = curso.tipo;
                break;
            }
        }

        // Si no encontramos el tipo original, usamos el tipo mostrado
        if (tipoOriginal == null) {
            tipoOriginal = tipoMostrado;
        }

        final String tipoFinal = tipoOriginal;

        // Filtrar cursos por el tipo seleccionado
        final List<String> programasNombres = new ArrayList<>();
        final List<Integer> programasIds = new ArrayList<>();

        for (Curso curso : todosLosCursos.values()) {
            if (curso.tipo.equals(tipoFinal)) {
                // Usar el método getNombreCorregido para mostrar nombre con acentos correctos
                programasNombres.add(curso.getNombreCorregido());
                programasIds.add(curso.id);
            }
        }

        if (programasNombres.isEmpty()) {
            Toast.makeText(this, "No hay programas disponibles para este tipo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear array para guardar el estado de las selecciones
        final boolean[] seleccionados = new boolean[programasNombres.size()];

        // Marcar los que ya estaban seleccionados
        for (int i = 0; i < programasIds.size(); i++) {
            seleccionados[i] = cursosSeleccionados.contains(programasIds.get(i));
        }

        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione los programas (máximo 3)");

        // Implementar el selector de múltiples items
        builder.setMultiChoiceItems(
                programasNombres.toArray(new String[0]),
                seleccionados,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // Si está marcando un nuevo elemento
                        if (isChecked) {
                            // Calcular cuántos elementos estarían seleccionados si aceptamos este cambio
                            int totalSeleccionadosSimulado = cursosSeleccionados.size();

                            // Si este elemento no estaba ya seleccionado, sumamos 1
                            if (!cursosSeleccionados.contains(programasIds.get(which))) {
                                totalSeleccionadosSimulado++;
                            }

                            // Verificar si excede el límite
                            if (totalSeleccionadosSimulado > 3) {
                                seleccionados[which] = false;
                                ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                                Toast.makeText(EncuestasPosRegActivity.this,
                                        "Puede seleccionar máximo 3 programas",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Botones para confirmar o cancelar
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Primero, actualizamos las selecciones de cursos del tipo actual
                for (int i = 0; i < seleccionados.length; i++) {
                    Integer cursoId = programasIds.get(i);

                    if (seleccionados[i]) {
                        // Si está seleccionado y no estaba ya en la lista, añadirlo
                        if (!cursosSeleccionados.contains(cursoId)) {
                            cursosSeleccionados.add(cursoId);
                        }
                    } else {
                        // Si no está seleccionado pero estaba en la lista, quitarlo
                        if (cursosSeleccionados.contains(cursoId)) {
                            cursosSeleccionados.remove(cursoId);
                        }
                    }
                }

                // Verificar que no excedamos el límite de 3 en total
                if (cursosSeleccionados.size() > 3) {
                    // En caso que exceda, mostramos un mensaje y dejamos solo los primeros 3
                    Toast.makeText(EncuestasPosRegActivity.this,
                            "Se han seleccionado los primeros 3 programas",
                            Toast.LENGTH_SHORT).show();

                    // Mantenemos solo los primeros 3 elementos
                    while (cursosSeleccionados.size() > 3) {
                        cursosSeleccionados.remove(cursosSeleccionados.size() - 1);
                    }
                }

                // Actualizar la vista de programas seleccionados
                actualizarVistaProgramasSeleccionados();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para actualizar la vista de programas seleccionados
    private void actualizarVistaProgramasSeleccionados() {
        if (cursosSeleccionados.isEmpty()) {
            tvProgramasSeleccionados.setText("No hay programas seleccionados");
            return;
        }

        // Construir texto con los nombres de los programas seleccionados
        StringBuilder sb = new StringBuilder();
        sb.append("Programas seleccionados:\n\n");

        for (Integer cursoId : cursosSeleccionados) {
            Curso curso = todosLosCursos.get(cursoId);
            if (curso != null) {
                // Usar getNombreCorregido para mostrar nombre con acentos correctos
                sb.append("• ").append(curso.getNombreCorregido()).append("\n");
            }
        }

        tvProgramasSeleccionados.setText(sb.toString());
    }

    private void cargarCursosDesdePreferencias() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Obtener la fecha de la última actualización
        long ultimaActualizacion = prefs.getLong("ultima_actualizacion", 0);
        long ahora = System.currentTimeMillis();

        // Si han pasado más de 24 horas (86400000 ms), forzar una nueva carga
        boolean actualizacionNecesaria = (ahora - ultimaActualizacion) > 86400000;

        if (!actualizacionNecesaria) {
            try {
                // Intentar cargar los datos guardados
                String cursosJson = prefs.getString("cursos_json", null);

                if (cursosJson != null) {
                    JSONArray jsonArray = new JSONArray(cursosJson);
                    todosLosCursos.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int id = jsonObject.getInt("id");
                        String nombre = jsonObject.getString("nombre");
                        String tipo = jsonObject.getString("tipo");

                        todosLosCursos.put(id, new Curso(id, nombre, tipo));
                    }

                    datosAlmacenados = true;
                    Log.d(TAG, "Cursos cargados desde preferencias: " + todosLosCursos.size());
                    return;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al cargar cursos desde preferencias: " + e.getMessage());
            }
        }

        datosAlmacenados = false;
    }

    private void guardarCursosEnPreferencias() {
        if (todosLosCursos.isEmpty()) {
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray();

            for (Curso curso : todosLosCursos.values()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", curso.id);
                jsonObject.put("nombre", curso.nombre);
                jsonObject.put("tipo", curso.tipo);
                jsonArray.put(jsonObject);
            }

            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("cursos_json", jsonArray.toString());
            editor.putLong("ultima_actualizacion", System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "Cursos guardados en preferencias: " + todosLosCursos.size());

        } catch (JSONException e) {
            Log.e(TAG, "Error al guardar cursos en preferencias: " + e.getMessage());
        }
    }

    private void cargarDatosSpinners() {
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

        // Crear adaptador para los spinners de intereses
        ArrayAdapter<String> adapterIntereses = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaIntereses);
        adapterIntereses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerInteres1.setAdapter(adapterIntereses);
        spinnerInteres2.setAdapter(adapterIntereses);
        spinnerInteres3.setAdapter(adapterIntereses);

        // Cargar lista de proyectos SENA
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

        // Crear adaptador para el spinner de proyectos SENA
        ArrayAdapter<String> adapterProyectos = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaProyectosSena);
        adapterProyectos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerProyectoSena.setAdapter(adapterProyectos);
    }

    // Método para actualizar el spinner de tipos de programas
    private void actualizarTiposProgramas() {
        // Obtener tipos únicos de todos los cursos
        Set<String> tiposUnicos = new HashSet<>();

        for (Curso curso : todosLosCursos.values()) {
            tiposUnicos.add(curso.tipo);
        }

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

        // Si hay tipos disponibles, filtrar por el primero
        if (!listaTiposProgramas.isEmpty()) {
            filtrarCursosPorTipo(listaTiposProgramas.get(0));
        }
    }

    private void filtrarCursosPorTipo(String tipoMostrado) {
        // Actualizar el texto del botón para indicar el tipo seleccionado
        btnSeleccionarProgramas.setText("Seleccionar programas de tipo: " + tipoMostrado);

        // Necesitamos encontrar el tipo original que corresponde al tipo mostrado
        String tipoOriginal = null;
        for (Curso curso : todosLosCursos.values()) {
            String tipoCursoCorregido;
            try {
                tipoCursoCorregido = new String(curso.tipo.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                tipoCursoCorregido = curso.tipo;
            }

            if (tipoCursoCorregido.equals(tipoMostrado)) {
                tipoOriginal = curso.tipo;
                break;
            }
        }

        // Si no encontramos el tipo original, usamos el tipo mostrado
        if (tipoOriginal == null) {
            tipoOriginal = tipoMostrado;
        }

        // Simplemente actualizamos el tipo actual sin eliminar selecciones previas
        // Esto permitirá al usuario seleccionar programas de diferentes tipos

        // No se eliminan las selecciones previas al cambiar de tipo de programa
        // Los usuarios pueden seleccionar hasta 3 programas de cualquier tipo
    }

    // Método para cargar cursos desde el servidor
    private void cargarCursosDesdeServidor() {
        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando programas del SENA...");
        progressDialog.show();

        // Realizar petición al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.68.176/AsistenciaApi/obtenerTodosCursos.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                JSONArray cursos = jsonResponse.getJSONArray("cursos");
                                todosLosCursos.clear();

                                for (int i = 0; i < cursos.length(); i++) {
                                    JSONObject curso = cursos.getJSONObject(i);
                                    int id = curso.getInt("id_curso");
                                    String nombre = curso.getString("nombre");
                                    String tipo = curso.getString("tipo");

                                    todosLosCursos.put(id, new Curso(id, nombre, tipo));
                                }

                                // Guardar en SharedPreferences para uso futuro
                                guardarCursosEnPreferencias();

                                // Actualizar spinner de tipos y filtrar cursos
                                actualizarTiposProgramas();

                                Log.d(TAG, "Cursos cargados desde servidor: " + todosLosCursos.size());

                            } else {
                                Toast.makeText(EncuestasPosRegActivity.this,
                                        "No se pudieron cargar los programas",
                                        Toast.LENGTH_SHORT).show();
                                // Intentar usar datos almacenados si existen
                                if (datosAlmacenados) {
                                    actualizarTiposProgramas();
                                }
                            }

                        } catch (JSONException e) {
                            Toast.makeText(EncuestasPosRegActivity.this,
                                    "Error al procesar los datos: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al procesar respuesta: " + e.getMessage());

                            // Intentar usar datos almacenados si existen
                            if (datosAlmacenados) {
                                actualizarTiposProgramas();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Error de red: " + error.getMessage());

                        // Mostrar mensaje de error
                        Toast.makeText(EncuestasPosRegActivity.this,
                                "Error de conexión. Usando datos almacenados.",
                                Toast.LENGTH_SHORT).show();

                        // Intentar usar datos almacenados si existen
                        if (datosAlmacenados) {
                            actualizarTiposProgramas();
                        } else {
                            // Cargar datos de ejemplo si no hay datos almacenados
                            cargarCursosDeEjemplo();
                            actualizarTiposProgramas();
                        }
                    }
                });

        // Aumentar el tiempo de espera para la solicitud
        stringRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                10000, // 10 segundos de timeout
                1, // Sin reintentos
                1.0f));

        queue.add(stringRequest);
    }

    // Método para cargar ejemplos de cursos en caso de error
    private void cargarCursosDeEjemplo() {
        todosLosCursos.clear();

        // Ejemplos de cursos con los tipos reales que encontramos en la base de datos
        todosLosCursos.put(1, new Curso(1, "Técnico en Sistemas", "TECNICO"));
        todosLosCursos.put(2, new Curso(2, "Técnico en Electricidad", "TECNICO"));
        todosLosCursos.put(3, new Curso(3, "Promoción de Productos", "AUXILIAR"));
        todosLosCursos.put(4, new Curso(4, "Cocina", "AUXILIAR"));
        todosLosCursos.put(5, new Curso(5, "Servicios de Alimentación y Limpieza", "AUXILIAR"));
        todosLosCursos.put(6, new Curso(6, "Tecnólogo en Desarrollo de Software", "TECNOLOGO"));
        todosLosCursos.put(7, new Curso(7, "Tecnólogo en Gestión Empresarial", "TECNOLOGO"));
        todosLosCursos.put(8, new Curso(8, "Bisutería Artesanal", "OPERARIO"));
        todosLosCursos.put(9, new Curso(9, "Cuidado Estético de Manos y Pies", "OPERARIO"));
        todosLosCursos.put(10, new Curso(10, "Construcción de Estructuras en Concreto", "OPERARIO"));

        // Guardar estos ejemplos en SharedPreferences
        guardarCursosEnPreferencias();
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
        // Validar que se haya seleccionado un proyecto SENA
        int posProyecto = spinnerProyectoSena.getSelectedItemPosition();
        if (posProyecto == 0) {
            Toast.makeText(this, "Por favor seleccione un proyecto SENA", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que se hayan seleccionado intereses
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
        if (posInteres1 == posInteres2 || posInteres1 == posInteres3 || posInteres2 == posInteres3) {
            Toast.makeText(this, "Por favor, seleccione intereses diferentes", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que se haya seleccionado al menos un programa
        if (cursosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione al menos un programa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si es persona jurídica y representa empresa, validar datos de empresa
        boolean esPersonaJuridica = rbPersonaJuridica.isChecked();
        boolean esRepresentanteEmpresa = rbSiRepresentante.isChecked();

        if (esPersonaJuridica && esRepresentanteEmpresa) {
            String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
            String nitEmpresa = etNitEmpresa.getText().toString().trim();
            String telefonoEmpresa = etTelefonoEmpresa.getText().toString().trim();
            String correoEmpresa = etCorreoEmpresa.getText().toString().trim();

            if (nombreEmpresa.isEmpty()) {
                etNombreEmpresa.setError("Ingrese el nombre de la empresa");
                etNombreEmpresa.requestFocus();
                return;
            }

            if (nitEmpresa.isEmpty()) {
                etNitEmpresa.setError("Ingrese el NIT de la empresa");
                etNitEmpresa.requestFocus();
                return;
            }

            if (telefonoEmpresa.isEmpty()) {
                etTelefonoEmpresa.setError("Ingrese el teléfono de la empresa");
                etTelefonoEmpresa.requestFocus();
                return;
            }

            if (correoEmpresa.isEmpty()) {
                etCorreoEmpresa.setError("Ingrese el correo de la empresa");
                etCorreoEmpresa.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoEmpresa).matches()) {
                etCorreoEmpresa.setError("Formato de correo inválido");
                etCorreoEmpresa.requestFocus();
                return;
            }
        }

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Guardando encuesta...");
        progressDialog.show();

        // Preparar datos para enviar al servidor
        final String tipoPersona = rbPersonaJuridica.isChecked() ? "Juridica" : "Natural";
        final String esRepresentante = rbSiRepresentante.isChecked() ? "Si" : "No";

        final String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
        final String nitEmpresa = etNitEmpresa.getText().toString().trim();
        final String telefonoEmpresa = etTelefonoEmpresa.getText().toString().trim();
        final String correoEmpresa = etCorreoEmpresa.getText().toString().trim();

        final int proyectoSenaId = listaProyectosSenaIds.get(posProyecto);
        final int interes1Id = listaInteresesIds.get(posInteres1);
        final int interes2Id = listaInteresesIds.get(posInteres2);
        final int interes3Id = listaInteresesIds.get(posInteres3);

        // Convertir los IDs de cursos seleccionados a un formato con comas (por ejemplo, "202,59")
        StringBuilder cursosSeleccionadosString = new StringBuilder();
        for (int i = 0; i < cursosSeleccionados.size(); i++) {
            cursosSeleccionadosString.append(cursosSeleccionados.get(i));
            if (i < cursosSeleccionados.size() - 1) {
                cursosSeleccionadosString.append(",");
            }
        }

        // Enviar datos al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        //String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/insertarEncuesta.php";
        String url = "http://192.168.68.176/AsistenciaApi/insertarEncuesta.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        // Imprimir la respuesta completa para depuración
                        Log.d(TAG, "Respuesta del servidor: " + response);

                        // Mostrar la respuesta sin procesar en un Toast para depuración
                        Toast.makeText(EncuestasPosRegActivity.this,
                                "Respuesta: " + response,
                                Toast.LENGTH_LONG).show();

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
                                        // Volver a la pantalla principal
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
                                telefonoEmpresa, correoEmpresa, proyectoSenaId, interes1Id,
                                interes2Id, interes3Id, cursosSeleccionadosString.toString());

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
                params.put("proyecto_sena", String.valueOf(proyectoSenaId));

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

                // Intereses
                params.put("interes1", String.valueOf(interes1Id));
                params.put("interes2", String.valueOf(interes2Id));
                params.put("interes3", String.valueOf(interes3Id));

                // Cursos seleccionados (formato "202,59")
                params.put("cursos_seleccionados", cursosSeleccionadosString.toString());

                // Si es persona jurídica y representante, enviar datos de la empresa
                if (tipoPersona.equals("Juridica") && esRepresentante.equals("Si")) {
                    params.put("es_representante", "Si");
                    params.put("nombre_empresa", nombreEmpresa);
                    params.put("nit_empresa", nitEmpresa);
                    params.put("telefono_empresa", telefonoEmpresa);
                    params.put("correo_empresa", correoEmpresa);
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
                                           String telefonoEmpresa, String correoEmpresa,
                                           int proyectoSenaId, int interes1Id,
                                           int interes2Id, int interes3Id,
                                           String cursosSeleccionados) {
        try {
            // Crear un objeto JSON con todos los datos
            JSONObject jsonData = new JSONObject();
            jsonData.put("cedula", numeroCedula);
            jsonData.put("idEvento", idEvento);
            jsonData.put("idSubevento", idSubevento);
            jsonData.put("tipo_persona", tipoPersona);
            jsonData.put("proyecto_sena", proyectoSenaId);
            jsonData.put("interes1", interes1Id);
            jsonData.put("interes2", interes2Id);
            jsonData.put("interes3", interes3Id);
            jsonData.put("cursos_seleccionados", cursosSeleccionados);

            if (tipoPersona.equals("Juridica") && esRepresentante.equals("Si")) {
                jsonData.put("es_representante", "Si");
                jsonData.put("nombre_empresa", nombreEmpresa);
                jsonData.put("nit_empresa", nitEmpresa);
                jsonData.put("telefono_empresa", telefonoEmpresa);
                jsonData.put("correo_empresa", correoEmpresa);
            } else {
                jsonData.put("es_representante", "No");
            }

            // Guardar en SharedPreferences
            SharedPreferences prefs = getSharedPreferences("EncuestasPendientes", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Obtener encuestas pendientes previas
            String encuestasPendientesJson = prefs.getString("encuestas", "[]");
            JSONArray encuestasPendientes = new JSONArray(encuestasPendientesJson);

            // Agregar la nueva encuesta
            encuestasPendientes.put(jsonData);

            // Guardar el array actualizado
            editor.putString("encuestas", encuestasPendientes.toString());
            editor.apply();

            Log.d(TAG, "Encuesta guardada localmente: " + jsonData.toString());

        } catch (JSONException e) {
            Log.e(TAG, "Error al guardar encuesta localmente: " + e.getMessage());
            Toast.makeText(this, "Error al guardar datos localmente", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Mostrar alerta antes de salir sin guardar
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