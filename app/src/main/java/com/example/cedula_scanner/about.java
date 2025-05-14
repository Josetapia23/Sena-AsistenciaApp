package com.example.cedula_scanner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class about extends AppCompatActivity {

    // Referencias a los campos de la UI
    private EditText tvDocumentID, tvNombres, tvApellidos, tvFechaNacimiento;
    private EditText tvEdad, tvNacionalidad, editCorreo, tvTelefono, tvDireccion;
    private AutoCompleteTextView tvGenero, tvTipoSangre;
    private RadioGroup rgTipoDocumento;
    private RadioButton rbCC, rbTI, rbEX;
    private Button btnRegistrar;
    private Toolbar toolbar;
    private Calendar calendar;
    // Variables para departamentos y municipios
    private Spinner spinnerDepartamento, spinnerMunicipio;
    private List<String> listaDepartamentos = new ArrayList<>();
    private Map<String, List<String>> mapaMunicipiosPorDepartamento = new HashMap<>();
    private Map<String, Integer> mapaMunicipiosIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digitalizable);

        // Inicializar el calendario
        calendar = Calendar.getInstance();

        // Inicializar las referencias a la UI
        tvDocumentID = findViewById(R.id.tvDocumentID);
        tvNombres = findViewById(R.id.tvNombres);
        tvApellidos = findViewById(R.id.tvApellidos);
        tvGenero = findViewById(R.id.tvGenero);
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento);
        tvTipoSangre = findViewById(R.id.tvTipoSangre);
        tvEdad = findViewById(R.id.tvEdad);
        tvNacionalidad = findViewById(R.id.tvNacionalidad);
        editCorreo = findViewById(R.id.editCorreo);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvDireccion = findViewById(R.id.tvDireccion);
        rgTipoDocumento = findViewById(R.id.rgTipoDocumento);
        rbCC = findViewById(R.id.rbCC);
        rbTI = findViewById(R.id.rbTI);
        rbEX = findViewById(R.id.rbEX);
        // Inicializar los spinners de departamento y municipio
        spinnerDepartamento = findViewById(R.id.spinnerDepartamento);
        spinnerMunicipio = findViewById(R.id.spinnerMunicipio);

        // Cargar datos geográficos y configurar adaptadores
        inicializarSpinnersDepartamentoMunicipio();

        // Configurar desplegables
        setupDropdowns();

        // Configurar selector de fecha
        setupDatePicker();

        // Obtener ID del evento de las preferencias
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        final String idEvento = prefs.getString("idEvento", null);
        final String idSubevento = prefs.getString("idSubevento", null);

        // Configurar botón de registro
        btnRegistrar = findViewById(R.id.Siguiente);
        btnRegistrar.setOnClickListener(view -> insertarAsistencia(idEvento));

        // Configurar toolbar
        setUpToolbar();
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Configuración para permitir operaciones de red en el hilo principal (no recomendado en producción)
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    // Configurar los desplegables de género y tipo de sangre
    private void setupDropdowns() {
        // Configurar opciones de género
        String[] opcionesGenero = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> generoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesGenero);
        tvGenero.setAdapter(generoAdapter);

        // Configurar opciones de tipo de sangre
        String[] opcionesTipoSangre = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        ArrayAdapter<String> tipoSangreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesTipoSangre);
        tvTipoSangre.setAdapter(tipoSangreAdapter);
    }

    // Configurar selector de fecha con DatePickerDialog
    private void setupDatePicker() {
        tvFechaNacimiento.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    about.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            // Actualizar el calendario
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            // Actualizar el campo de texto con la fecha seleccionada
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            tvFechaNacimiento.setText(dateFormat.format(calendar.getTime()));

                            // Calcular y actualizar la edad automáticamente
                            actualizarEdad();
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Establecer el límite máximo como el año actual
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            datePickerDialog.show();
        });
    }

    // Calcular la edad basada en la fecha de nacimiento
    private void actualizarEdad() {
        String fechaNacimiento = tvFechaNacimiento.getText().toString();
        if (!fechaNacimiento.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date fechaNac = sdf.parse(fechaNacimiento);
                Calendar dob = Calendar.getInstance();
                Calendar today = Calendar.getInstance();
                dob.setTime(fechaNac);

                int edadCalculada = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                    edadCalculada--;
                }
                tvEdad.setText(String.valueOf(edadCalculada));
            } catch (ParseException e) {
                Log.e("EDAD_ERROR", "Error calculando edad: " + e.getMessage());
            }
        }
    }

    public void setUpToolbar() {
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.item4) {
            Intent acerca = new Intent(this, MainActivity.class);
            startActivity(acerca);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    // Método para inicializar los spinners de departamento y municipio
    private void inicializarSpinnersDepartamentoMunicipio() {
        // Crear una lista para los departamentos con un ítem inicial
        List<String> listaDeptConSeleccion = new ArrayList<>();
        listaDeptConSeleccion.add("Seleccione departamento");

        // Configurar adaptadores iniciales
        ArrayAdapter<String> adapterDepartamentos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, listaDeptConSeleccion);
        spinnerDepartamento.setAdapter(adapterDepartamentos);

        // Lista vacía para municipios con mensaje inicial
        List<String> listaMuniInicial = new ArrayList<>();
        listaMuniInicial.add("Seleccione primero un departamento");

        ArrayAdapter<String> adapterMunicipios = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, listaMuniInicial);
        spinnerMunicipio.setAdapter(adapterMunicipios);

        // Cargar datos geográficos
        cargarDatosGeograficosDesdeCache();

        // Listener para el cambio de departamento
        spinnerDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Si seleccionaron la opción "Seleccione departamento"
                    List<String> listaMuniVacia = new ArrayList<>();
                    listaMuniVacia.add("Seleccione primero un departamento");

                    ArrayAdapter<String> adapterMunicipiosVacio = new ArrayAdapter<>(
                            about.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            listaMuniVacia);
                    spinnerMunicipio.setAdapter(adapterMunicipiosVacio);
                } else {
                    // Posición - 1 porque tenemos el item "Seleccione departamento" al inicio
                    String departamentoSeleccionado = listaDepartamentos.get(position - 1);
                    actualizarSpinnerMunicipios(departamentoSeleccionado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    // Método para cargar datos geográficos desde caché
    private void cargarDatosGeograficosDesdeCache() {
        SharedPreferences prefs = getSharedPreferences("DatosMunicipios", MODE_PRIVATE);
        String datosJson = prefs.getString("departamentos_json", null);

        if (datosJson != null) {
            try {
                // Procesar los datos guardados en caché
                Log.d("about", "Usando datos geográficos de caché");
                procesarDatosDepartamentosMunicipios(new JSONObject(datosJson));
            } catch (JSONException e) {
                Log.e("about", "Error al procesar datos de caché: " + e.getMessage());
                // Si hay un error en los datos de caché, mostrar mensaje
                Toast.makeText(about.this,
                        "Error al cargar datos geográficos. Por favor regrese y seleccione un evento nuevamente.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            // No hay datos en caché, mostrar mensaje
            Log.e("about", "No se encontraron datos geográficos en caché");
            Toast.makeText(about.this,
                    "Datos geográficos no disponibles. Por favor regrese y seleccione un evento nuevamente.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Método para procesar los datos de departamentos y municipios desde JSON
    private void procesarDatosDepartamentosMunicipios(JSONObject jsonObject) {
        try {
            // Limpiar datos existentes
            listaDepartamentos.clear();
            mapaMunicipiosPorDepartamento.clear();
            mapaMunicipiosIds.clear();

            // Procesar el array de departamentos
            JSONArray departamentosArray = jsonObject.getJSONArray("departamentos");

            for (int i = 0; i < departamentosArray.length(); i++) {
                JSONObject departamentoObj = departamentosArray.getJSONObject(i);
                String nombreDepartamento = decodificarTexto(departamentoObj.getString("nombre"));

                // Añadir a la lista de departamentos
                listaDepartamentos.add(nombreDepartamento);

                // Procesar los municipios de este departamento
                JSONArray municipiosArray = departamentoObj.getJSONArray("municipios");
                List<String> municipiosDelDepartamento = new ArrayList<>();

                // Añadir opción "Seleccione municipio" al inicio
                municipiosDelDepartamento.add("Seleccione municipio");

                for (int j = 0; j < municipiosArray.length(); j++) {
                    JSONObject municipioObj = municipiosArray.getJSONObject(j);
                    int idMunicipio = municipioObj.getInt("id");
                    String nombreMunicipio = decodificarTexto(municipioObj.getString("nombre"));

                    // Añadir a la lista de municipios de este departamento
                    municipiosDelDepartamento.add(nombreMunicipio);

                    // Guardar la relación nombre-id del municipio
                    mapaMunicipiosIds.put(nombreMunicipio, idMunicipio);
                }

                // Guardar la lista de municipios para este departamento
                mapaMunicipiosPorDepartamento.put(nombreDepartamento, municipiosDelDepartamento);
            }

            // Crear lista con opción inicial
            List<String> listaDeptConSeleccion = new ArrayList<>();
            listaDeptConSeleccion.add("Seleccione departamento");
            listaDeptConSeleccion.addAll(listaDepartamentos);

            // Actualizar el spinner de departamentos
            ArrayAdapter<String> adapterDepartamentos = new ArrayAdapter<>(
                    about.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    listaDeptConSeleccion);
            spinnerDepartamento.setAdapter(adapterDepartamentos);

            // Seleccionar la opción inicial "Seleccione departamento"
            spinnerDepartamento.setSelection(0);

        } catch (JSONException e) {
            Log.e("about", "Error al procesar JSON de departamentos: " + e.getMessage());
            Toast.makeText(about.this,
                    "Error al procesar datos geográficos",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Método para actualizar el spinner de municipios según el departamento seleccionado
    private void actualizarSpinnerMunicipios(String departamento) {
        List<String> municipiosDelDepartamento = mapaMunicipiosPorDepartamento.get(departamento);

        if (municipiosDelDepartamento != null && !municipiosDelDepartamento.isEmpty()) {
            ArrayAdapter<String> adapterMunicipios = new ArrayAdapter<>(
                    about.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    municipiosDelDepartamento);
            spinnerMunicipio.setAdapter(adapterMunicipios);
            // Seleccionar "Seleccione municipio"
            spinnerMunicipio.setSelection(0);
        } else {
            // Si no hay municipios, mostrar lista vacía con mensaje
            List<String> noMunicipios = new ArrayList<>();
            noMunicipios.add("No hay municipios disponibles");

            spinnerMunicipio.setAdapter(new ArrayAdapter<>(
                    about.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    noMunicipios));
        }
    }

    // Método para decodificar texto con caracteres especiales
    private String decodificarTexto(String texto) {
        try {
            // Intenta diferentes métodos de decodificación hasta encontrar el correcto
            return new String(texto.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            Log.e("Codificación", "Error al decodificar texto: " + e.getMessage());
            return texto; // Devuelve el original si falla
        }
    }

    private void insertarAsistencia(String idEvento) {
        // Obtener también el ID del subevento desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String idSubevento = prefs.getString("idSubevento", null);

        // Verificar que tenemos ambos IDs
        if (idEvento == null || idSubevento == null) {
            Toast.makeText(this, "Error: No se encontraron los IDs del evento", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener tipo de documento seleccionado
        String tipoDocumento;
        int selectedRadioButtonId = rgTipoDocumento.getCheckedRadioButtonId();
        if (selectedRadioButtonId == R.id.rbCC) {
            tipoDocumento = "CC";
        } else if (selectedRadioButtonId == R.id.rbTI) {
            tipoDocumento = "TI";
        } else if (selectedRadioButtonId == R.id.rbEX) {
            tipoDocumento = "EX";
        } else {
            tipoDocumento = "CC"; // Valor por defecto
        }

        // Obtener valores de la UI
        String identificacion = tvDocumentID.getText().toString().trim();
        String nombres = tvNombres.getText().toString().trim();
        String apellidos = tvApellidos.getText().toString().trim();
        String fechaNacimiento = tvFechaNacimiento.getText().toString().trim();
        String edad = tvEdad.getText().toString().trim();
        String genero = tvGenero.getText().toString().trim();
        String tipoSangre = tvTipoSangre.getText().toString().trim();
        String nacionalidad = tvNacionalidad.getText().toString().trim();
        String correo = editCorreo.getText().toString().trim();
        String telefono = tvTelefono.getText().toString().trim();
        String direccion = tvDireccion.getText().toString().trim();

        // Obtener departamento y municipio seleccionados - declarados como final
        final String departamento;
        final String municipio;

        // Verificar que no esté seleccionada la opción "Seleccione departamento"
        if (spinnerDepartamento.getSelectedItemPosition() > 0) {
            // Posición - 1 porque agregamos "Seleccione departamento" al inicio
            departamento = listaDepartamentos.get(spinnerDepartamento.getSelectedItemPosition() - 1);

            // Verificar que no esté seleccionada la opción "Seleccione municipio"
            if (spinnerMunicipio.getSelectedItemPosition() > 0) {
                municipio = spinnerMunicipio.getSelectedItem().toString();
            } else {
                municipio = "";
            }
        } else {
            departamento = "";
            municipio = "";
        }

        // Validaciones
        if (identificacion.isEmpty()) {
            tvDocumentID.setError("Este campo es obligatorio");
            return;
        }

        if (nombres.isEmpty()) {
            tvNombres.setError("Este campo es obligatorio");
            return;
        }

        if (apellidos.isEmpty()) {
            tvApellidos.setError("Este campo es obligatorio");
            return;
        }

        if (genero.isEmpty()) {
            tvGenero.setError("Este campo es obligatorio");
            return;
        }

        if (correo.isEmpty()) {
            editCorreo.setError("Ingrese un correo electrónico");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            editCorreo.setError("Formato de correo inválido");
            return;
        }

        if (telefono.isEmpty()) {
            tvTelefono.setError("Ingrese un número de teléfono");
            return;
        }

        if (direccion.isEmpty()) {
            tvDireccion.setError("Ingrese una dirección");
            return;
        }

        // Validar que se haya seleccionado departamento y municipio válidos
        if (departamento.isEmpty() || municipio.isEmpty() ||
                municipio.equals("Seleccione municipio") ||
                municipio.equals("Seleccione primero un departamento") ||
                municipio.equals("No hay municipios disponibles")) {

            Toast.makeText(this, "Por favor seleccione departamento y municipio válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar todos los valores que se enviarán
        Log.d("PARAMS_CHECK", "idEvento: " + idEvento);
        Log.d("PARAMS_CHECK", "idSubevento: " + idSubevento);
        Log.d("PARAMS_CHECK", "tipoDocumento: " + tipoDocumento);
        Log.d("PARAMS_CHECK", "cedula: " + identificacion);
        Log.d("PARAMS_CHECK", "nombres: " + nombres);
        Log.d("PARAMS_CHECK", "apellidos: " + apellidos);
        Log.d("PARAMS_CHECK", "fechanacimiento: " + fechaNacimiento);
        Log.d("PARAMS_CHECK", "edad: " + edad);
        Log.d("PARAMS_CHECK", "genero: " + genero);
        Log.d("PARAMS_CHECK", "nacionalidad: " + nacionalidad);
        Log.d("PARAMS_CHECK", "tiposangre: " + tipoSangre);
        Log.d("PARAMS_CHECK", "correo: " + correo);
        Log.d("PARAMS_CHECK", "celular: " + telefono);
        Log.d("PARAMS_CHECK", "direccion: " + direccion);
        Log.d("PARAMS_CHECK", "departamento: " + departamento);
        Log.d("PARAMS_CHECK", "municipio: " + municipio);

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando asistencia...");
        progressDialog.show();

        // Agregar log para depurar dirección
        Log.d("DIRECCION_DEBUG", "Valor de dirección a enviar: " + direccion);
        Log.d("UBICACION_DEBUG", "Departamento: " + departamento + ", Municipio: " + municipio);

        // Enviar datos al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        // URL del servidor - verificar que sea la correcta
        // String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/insertarCedulaSc.php";
        String url = "http://192.168.68.176/AsistenciaApi/insertarCedulaSc.php"; // Cambia a tu URL local si estás probando

        Log.d("URL_DEBUG", "URL del servidor: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Agregar log para ver la respuesta completa del servidor
                        Log.d("RESPUESTA_SERVER", "Respuesta recibida: " + response);

                        progressDialog.dismiss();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");
                            boolean encuestaCompletada = jsonResponse.optBoolean("encuesta_completada", false);

                            // Mostrar alerta con el mensaje del servidor
                            AlertDialog.Builder builder = new AlertDialog.Builder(about.this);
                            builder.setTitle(success ? "Éxito" : "Aviso");

                            if (encuestaCompletada) {
                                // Si ya completó la encuesta, informar y volver al menú principal
                                builder.setMessage("Esta persona ya ha completado la encuesta para este evento.");
                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Volver a la pantalla principal para escanear otra cédula
                                        Intent intent = new Intent(about.this, AccionesMainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else if (success || jsonResponse.optBoolean("already_registered", false)) {
                                // En caso de éxito o usuario ya registrado (pero sin encuesta), ir a la encuesta
                                builder.setMessage(message);
                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Pasar a la pantalla de encuestas con todos los datos necesarios
                                        Intent intent = new Intent(about.this, EncuestasPosRegActivity.class);
                                        intent.putExtra("nombre_completo", nombres + " " + apellidos);
                                        intent.putExtra("numero_cedula", identificacion);

                                        // Pasar también idEvento e idSubevento (aunque ya están en SharedPreferences)
                                        // esto es para garantizar que siempre estén disponibles
                                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                        String idEvento = prefs.getString("idEvento", "");
                                        String idSubevento = prefs.getString("idSubevento", "");

                                        intent.putExtra("idEvento", idEvento);
                                        intent.putExtra("idSubevento", idSubevento);

                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else {
                                // En caso de error, solo cerrar el diálogo
                                builder.setMessage(message);
                                builder.setPositiveButton("Aceptar", null);
                            }

                            builder.show();
                        } catch (JSONException e) {
                            Toast.makeText(about.this, "Error al procesar la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        String errorMessage = "Error de red";

                        // Obtener más detalles sobre el error
                        if (error.networkResponse != null) {
                            errorMessage += " (código: " + error.networkResponse.statusCode + ")";

                            // Intentar obtener el cuerpo de la respuesta
                            if (error.networkResponse.data != null) {
                                try {
                                    String errorBody = new String(error.networkResponse.data, "UTF-8");
                                    Log.e("ERROR_BODY", "Cuerpo del error: " + errorBody);
                                    errorMessage += "\nDetalle: " + errorBody;
                                } catch (Exception e) {
                                    Log.e("ERROR_PARSE", "Error al parsear el cuerpo del error: " + e.getMessage());
                                }
                            }
                        }

                        // Obtener la causa raíz del error
                        if (error.getCause() != null) {
                            Log.e("ERROR_CAUSE", "Causa: " + error.getCause().getMessage());
                        }

                        // Obtener el mensaje de error
                        Log.e("ERROR_MESSAGE", "Mensaje de error: " + error.getMessage());

                        // Mostrar alerta con el error
                        AlertDialog.Builder builder = new AlertDialog.Builder(about.this);
                        builder.setTitle("Error");
                        builder.setMessage(errorMessage);
                        builder.setPositiveButton("Aceptar", null);
                        builder.show();

                        Log.e("InsertError", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idEvento", idEvento);
                params.put("idSubevento", idSubevento);
                params.put("tipoDocumento", tipoDocumento);
                params.put("cedula", identificacion);
                params.put("nombres", nombres);
                params.put("apellidos", apellidos);
                params.put("fechanacimiento", fechaNacimiento);
                params.put("edad", edad);
                params.put("genero", genero);
                params.put("nacionalidad", nacionalidad);
                params.put("tiposangre", tipoSangre);
                params.put("correo", correo);
                params.put("celular", telefono);
                params.put("direccion", direccion);

                // Añadir los nuevos parámetros de ubicación
                params.put("departamento", departamento);
                params.put("municipio", municipio);

                return params;
            }
        };

        // Configurar un tiempo de espera más largo para la solicitud
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 segundos
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);

    /* Solución temporal para probar (descomentar si necesitas probar la interfaz sin servidor)
    // Comentar la línea queue.add(stringRequest) y descomentar este bloque para pruebas
    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            progressDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(about.this);
            builder.setTitle("Éxito (Simulado)");
            builder.setMessage("Asistencia registrada correctamente.");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(about.this, AccionesMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.show();
        }
    }, 2000); // Simular una demora de 2 segundos
    */
    }
}