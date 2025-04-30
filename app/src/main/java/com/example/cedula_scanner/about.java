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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando asistencia...");
        progressDialog.show();

        // Agregar log para depurar dirección
        Log.d("DIRECCION_DEBUG", "Valor de dirección a enviar: " + direccion);

        // Enviar datos al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.1.106/AsistenciaApi/insertarCedulaSc.php";

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
                            AlertDialog.Builder builder = new AlertDialog.Builder(about.this);
                            builder.setTitle(success ? "Éxito" : "Aviso");
                            builder.setMessage(message);

                            if (success || jsonResponse.optBoolean("already_registered", false)) {
                                // En caso de éxito o usuario ya registrado, ir a AccionesMainActivity
                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(about.this, AccionesMainActivity.class);
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
                            Toast.makeText(about.this, "Error al procesar la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                return params;
            }
        };

        queue.add(stringRequest);
    }
}