package com.example.cedula_scanner;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;

    // Referencias a los campos de la UI
    private EditText tvDocumentID, tvNombres, tvApellidos, tvGenero, tvFechaNacimiento;
    private EditText tvTipoSangre, tvEdad, tvTipoDocumento, tvNacionalidad;
    private EditText editCorreo, tvTelefono, tvDireccion;

    private Toolbar toolbar;
    private Button btnRegistrar;
    private String edad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar las referencias a la UI
        tvDocumentID = findViewById(R.id.tvDocumentID);
        tvNombres = findViewById(R.id.tvNombres);
        tvApellidos = findViewById(R.id.tvApellidos);
        tvGenero = findViewById(R.id.tvGenero);
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento);
        tvTipoSangre = findViewById(R.id.tvTipoSangre);
        tvEdad = findViewById(R.id.tvEdad);
        tvTipoDocumento = findViewById(R.id.tvTipoDocumento);
        tvNacionalidad = findViewById(R.id.tvNacionalidad);
        editCorreo = findViewById(R.id.editCorreo);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvDireccion = findViewById(R.id.tvDireccion);

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

        // Procesar datos de escaneo si vienen como extras
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("scan_data")) {
            String scanData = extras.getString("scan_data");
            parseDataCode(scanData);
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
        if (menuItem.getItemId() == R.id.item3) {
            Intent acerca = new Intent(this, about.class);
            startActivity(acerca);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            // Iniciar el escaneo de la cédula
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.PDF_417);
            integrator.setPrompt("Acerca el código de barras de la cédula");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setTorchEnabled(false);
            integrator.initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result.getContents() == null) {
            // El escaneo fue cancelado o falló
            Intent originalIntent = result.getOriginalIntent();
            if (originalIntent == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_LONG).show();
            }
        } else {
            // El escaneo fue exitoso
            Log.d("MainActivity", "Escaneado: " + result.getContents());
            parseDataCode(result.getContents());
        }
    }

    private void parseDataCode(String barcode) {
        // Imprimir el código de barras completo para análisis
        Log.d("BARCODE_RAW", "Código de barras completo: " + barcode);

        if (barcode != null) {
            String primerApellido = "", segundoApellido = "", primerNombre = "", segundoNombre = "",
                    cedula = "", rh = "", fechaNacimiento = "", sexo = "", edad = "";

            // Limpiar el código de barras y dividirlo en partes
            String alphaAndDigits = barcode.replaceAll("[^\\p{Alpha}\\p{Digit}\\+\\_]+", " ");
            String[] splitStr = alphaAndDigits.split("\\s+");

            // Imprimir todas las partes del código para análisis
            for (int i = 0; i < splitStr.length; i++) {
                Log.d("BARCODE_PART", "Parte " + i + ": " + splitStr[i]);
            }

            // Procesar el código de barras según su formato
            try {
                if (!alphaAndDigits.contains("PubDSK")) {
                    // Formato antiguo de cédula
                    int corrimiento = 0;

                    Pattern pat = Pattern.compile("[A-Z]");
                    Matcher match = pat.matcher(splitStr[2 + corrimiento]);
                    int lastCapitalIndex = -1;
                    if (match.find()) {
                        lastCapitalIndex = match.start();
                    }

                    cedula = splitStr[2 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
                    primerApellido = splitStr[2 + corrimiento].substring(lastCapitalIndex);
                    segundoApellido = splitStr[3 + corrimiento];
                    primerNombre = splitStr[4 + corrimiento];

                    if (Character.isDigit(splitStr[5 + corrimiento].charAt(0))) {
                        corrimiento--;
                    } else {
                        segundoNombre = splitStr[5 + corrimiento];
                    }

                    sexo = splitStr[6 + corrimiento];
                    rh = splitStr[6 + corrimiento].substring(splitStr[6 + corrimiento].length() - 2);
                    fechaNacimiento = splitStr[6 + corrimiento].substring(2, 10);
                } else {
                    // Formato nuevo de cédula (con PubDSK)
                    int corrimiento = 0;
                    Pattern pat = Pattern.compile("[A-Z]");
                    if (splitStr[2 + corrimiento].length() > 7) {
                        corrimiento--;
                    }

                    Matcher match = pat.matcher(splitStr[3 + corrimiento]);
                    int lastCapitalIndex = -1;
                    if (match.find()) {
                        lastCapitalIndex = match.start();
                    }

                    cedula = splitStr[3 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
                    primerApellido = splitStr[3 + corrimiento].substring(lastCapitalIndex);
                    segundoApellido = splitStr[4 + corrimiento];

                    if (splitStr[5 + corrimiento].startsWith("0")) { // UN NOMBRE UN APELLIDO
                        segundoApellido = " ";
                        primerNombre = splitStr[4 + corrimiento];
                        sexo = splitStr[5 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                        rh = splitStr[5 + corrimiento].substring(splitStr[5 + corrimiento].length() - 2);
                        fechaNacimiento = splitStr[5 + corrimiento].substring(2, 10);
                    } else if (splitStr[6 + corrimiento].startsWith("0")) { // DOS APELLIDOS UN NOMBRE
                        primerNombre = splitStr[5 + corrimiento];
                        segundoNombre = " ";
                        sexo = splitStr[6 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                        rh = splitStr[6 + corrimiento].substring(splitStr[6 + corrimiento].length() - 2);
                        fechaNacimiento = splitStr[6 + corrimiento].substring(2, 10);
                    } else { // DOS APELLIDOS DOS NOMBRES
                        primerNombre = splitStr[5 + corrimiento];
                        segundoNombre = splitStr[6 + corrimiento];
                        sexo = splitStr[7 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                        rh = splitStr[7 + corrimiento].substring(splitStr[7 + corrimiento].length() - 2);
                        fechaNacimiento = splitStr[7 + corrimiento].substring(2, 10);
                    }
                }

                // Calcular edad a partir de la fecha de nacimiento
                if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                    try {
                        // Formatear fecha: ddMMyyyy a yyyy-MM-dd
                        String fechaFormateada = "20" + fechaNacimiento.substring(4, 6) + "-" +
                                fechaNacimiento.substring(2, 4) + "-" +
                                fechaNacimiento.substring(0, 2);
                        Log.d("CEDULA_DATA", "Fecha formateada: " + fechaFormateada);

                        // Calcular edad
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date fechaNac = sdf.parse(fechaFormateada);
                        Calendar dob = Calendar.getInstance();
                        Calendar today = Calendar.getInstance();
                        dob.setTime(fechaNac);

                        int edadCalculada = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                            edadCalculada--;
                        }
                        edad = String.valueOf(edadCalculada);
                        this.edad = edad;
                        Log.d("CEDULA_DATA", "Edad calculada: " + edad);
                    } catch (Exception e) {
                        Log.e("CEDULA_DATA", "Error calculando edad: " + e.getMessage());
                    }
                }

                // Imprimir todos los datos extraídos para depuración
                Log.d("CEDULA_DATA", "Cédula: " + cedula);
                Log.d("CEDULA_DATA", "Nombres: " + primerNombre + " " + segundoNombre);
                Log.d("CEDULA_DATA", "Apellidos: " + primerApellido + " " + segundoApellido);
                Log.d("CEDULA_DATA", "Sexo: " + sexo);
                Log.d("CEDULA_DATA", "Fecha Nacimiento: " + fechaNacimiento);
                Log.d("CEDULA_DATA", "RH: " + rh);
                Log.d("CEDULA_DATA", "Edad: " + edad);

                // Formatear fecha para mostrar
                String fechaMostrar = "";
                if (fechaNacimiento.length() >= 8) {
                    try {
                        String dia, mes, anio;

                        if (!alphaAndDigits.contains("PubDSK")) {
                            // Formato antiguo: DDMMYY
                            dia = fechaNacimiento.substring(0, 2);
                            mes = fechaNacimiento.substring(2, 4);
                            anio = "20" + fechaNacimiento.substring(4, 6); // Asumiendo años 2000+
                        } else {
                            // Formato nuevo PubDSK: YYYYMMDD
                            anio = fechaNacimiento.substring(0, 4);
                            mes = fechaNacimiento.substring(4, 6);
                            dia = fechaNacimiento.substring(6, 8);
                        }

                        // Formato para mostrar al usuario
                        fechaMostrar = dia + "/" + mes + "/" + anio;

                        // El formato de fecha para enviar al servidor debe ser consistente
                        fechaNacimiento = fechaMostrar; // Mantener formato dd/mm/yyyy para enviar al servidor
                    } catch (Exception e) {
                        Log.e("PARSE_ERROR", "Error formateando fecha: " + e.getMessage());
                        fechaMostrar = fechaNacimiento;
                    }
                }
                // Nombres y apellidos completos
                String nombresCompletos = primerNombre;
                if (segundoNombre != null && !segundoNombre.trim().isEmpty() && !segundoNombre.equals(" ")) {
                    nombresCompletos += " " + segundoNombre;
                }

                String apellidosCompletos = primerApellido;
                if (segundoApellido != null && !segundoApellido.trim().isEmpty() && !segundoApellido.equals(" ")) {
                    apellidosCompletos += " " + segundoApellido;
                }

                // Actualizar la UI con los datos extraídos
                tvDocumentID.setText(cedula);
                tvNombres.setText(nombresCompletos);
                tvApellidos.setText(apellidosCompletos);
                tvGenero.setText(sexo);
                tvFechaNacimiento.setText(fechaMostrar);
                tvTipoSangre.setText(rh);
                tvEdad.setText(edad);
                tvTipoDocumento.setText("CC"); // Por defecto para cédulas colombianas
                tvNacionalidad.setText("Colombiana"); // Por defecto

            } catch (Exception e) {
                Log.e("PARSE_ERROR", "Error procesando código de barras: " + e.getMessage());
                Toast.makeText(this, "Error al procesar la cédula: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("TAG", "No se capturó ningún código de barras");
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

        // Obtener valores de la UI
        String tipoDocumento = tvTipoDocumento.getText().toString().trim();
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

        // Validaciones
        if (identificacion.isEmpty()) {
            Toast.makeText(this, "Por favor escanee una cédula primero", Toast.LENGTH_SHORT).show();
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

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando asistencia...");
        progressDialog.show();

        // Enviar datos al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.1.117/AsistenciaApi/insertarCedulaSc.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");

                            // Mostrar alerta con el mensaje del servidor
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(success ? "Éxito" : "Aviso");
                            builder.setMessage(message);

                            if (success || jsonResponse.optBoolean("already_registered", false)) {
                                // En caso de éxito o usuario ya registrado, ir a AccionesMainActivity
                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(MainActivity.this, AccionesMainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else {
                                // En caso de error, solo cerrar el diálogo
                                builder.setPositiveButton("Aceptar", null);
                            }

                            builder.show();

                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Error al procesar la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        String errorMessage = "Error de red";

                        if (error.networkResponse != null) {
                            errorMessage += " (código: " + error.networkResponse.statusCode + ")";
                        }

                        // Mostrar alerta con el error
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public void about(View view) {
        Intent about = new Intent(this, about.class);
        startActivity(about);
    }
}