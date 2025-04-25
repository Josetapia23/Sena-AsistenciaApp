package com.example.cedula_scanner;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class about extends AppCompatActivity{

    EditText tvFirstName;
    EditText tvSecondName;
    EditText tvLastName;
    EditText tvSecondLastName;
    EditText tvDocumentID;
    EditText tvGender;
    EditText tvDate;
    EditText tvRH, tvNumeroFicha,tvProgrmaformacion, tvjordanaFormacion, tvModFormacion, tvnivelFormacion, tvCentroFormacion, editCorreo, tvTelefono;

    RadioGroup radioGroupGenero;
    RadioGroup radioGroupTipoDocumento;
    private Toolbar toolbar;

    Button btn;
    Calendar calendar;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acercade);

        tvFirstName = findViewById(R.id.tvFirstName1);
        //tvSecondName = findViewById(R.id.tvSecondName1);
        tvLastName = findViewById(R.id.tvLastName1);
        //tvSecondLastName = findViewById(R.id.tvSecondLastName1);
        tvDocumentID = findViewById(R.id.tvDocumentID1);
        //tvDate = findViewById(R.id.tvDate1);
        //tvRH = findViewById(R.id.tvRH1);

        radioGroupGenero = findViewById(R.id.radioGroupGenero);
        radioGroupTipoDocumento = findViewById(R.id.radioGroupTipoDocumento);

        btn = findViewById(R.id.Siguiente);
        calendar = Calendar.getInstance();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String idEvento = prefs.getString("idEvento", null);
        btn = findViewById(R.id.Siguiente);
        btn.setOnClickListener(view -> insertar(idEvento));
        View btnConsultar = findViewById(R.id.consultarAprendiz);
        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consultarAprendiz(idEvento);
            }
        });
        tvProgrmaformacion = findViewById(R.id.tvProgrmaformacion);
        tvNumeroFicha = findViewById(R.id.tvNumeroFicha);
        tvjordanaFormacion = findViewById(R.id.tvjordanaFormacion);
        tvModFormacion = findViewById(R.id.tvModFormacion);
        tvnivelFormacion = findViewById(R.id.tvnivelFormacion);
        tvCentroFormacion = findViewById(R.id.tvCentroFormacion);
        editCorreo = findViewById(R.id.editCorreo);
        tvTelefono = findViewById(R.id.tvTelefono);


        setUpToolbar();
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);



        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
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
        switch (menuItem.getItemId()) {
            case R.id.item4:
                Intent acerca = new Intent(this, MainActivity.class);
                startActivity(acerca);
                break;
            default:
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Crea una Calendar con la fecha seleccionada
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Actualiza el texto del EditText con la fecha seleccionada
                        String selectedDateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
                        tvDate.setText(selectedDateStr);

                        // Almacena la fecha seleccionada en la variable "calendar"
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    }
                },
                year,
                month,
                day
        );

        // Establece el límite máximo como el año actual
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    // metodo para obtener datos de la cedula aprendiz
    private void consultarAprendiz(String idEvento) {
        EditText etDocumentID = findViewById(R.id.tvDocumentID1);
        String documentID = etDocumentID.getText().toString().trim();
        int tipoDocumentoId = radioGroupTipoDocumento.getCheckedRadioButtonId();
        if (tipoDocumentoId == -1) {
            Toast.makeText(this, "Selecciona un tipo de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (documentID.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese la identificación", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton tipoDocumentoRadioButton = findViewById(tipoDocumentoId);
        String tipoDocumento = tipoDocumentoRadioButton.getText().toString();
        // URL del servidor
        //String url = "http://192.168.45.106/AsistenciaApi/consultarAprendiz.php";
        String url = "https://asistenciasena.proyectoswork.com/api/consultarAprendiz.php";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Consultando...");
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.has("error")) {
                        Toast.makeText(about.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Obtener los datos del JSON
                    String apellidos = jsonObject.getString("apellidos");
                    String nombre = jsonObject.getString("nombre");
                    String ficha = jsonObject.getString("ficha");
                    String programaFormacion = jsonObject.getString("programaFor");
                    String jornadaFormacion = jsonObject.getString("jornada");
                    String modalidadFor = jsonObject.getString("modalidad");
                    String nivelFormacion = jsonObject.getString("nivelFormacion");
                    String correoElectronico = jsonObject.getString("correo");
                    String telefonoContacto = jsonObject.getString("telefono");
                    String centroFormacion = jsonObject.getString("centroForm");

                    // Mapear los datos a los campos de la vista

                    ((EditText) findViewById(R.id.tvLastName1)).setText(apellidos);
                    ((EditText) findViewById(R.id.tvFirstName1)).setText(nombre);
                    ((EditText) findViewById(R.id.tvNumeroFicha)).setText(ficha);
                    ((EditText) findViewById(R.id.tvProgrmaformacion)).setText(programaFormacion);
                    ((EditText) findViewById(R.id.tvjordanaFormacion)).setText(jornadaFormacion);
                    ((EditText) findViewById(R.id.tvModFormacion)).setText(modalidadFor);
                    ((EditText) findViewById(R.id.tvnivelFormacion)).setText(nivelFormacion);
                    ((EditText) findViewById(R.id.tvCentroFormacion)).setText(centroFormacion);
                    ((EditText) findViewById(R.id.editCorreo)).setText(correoElectronico);
                    ((EditText) findViewById(R.id.tvTelefono)).setText(telefonoContacto);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(about.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(about.this, "Error en la consulta", Toast.LENGTH_SHORT).show();
                Log.e("VolleyError", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cedula", documentID);
                params.put("idEvento", idEvento);
                params.put("tipo_doc", tipoDocumento);
                return params;
            }
        };

        queue.add(stringRequest);
    }
    public void insertar(String idEvento) {
        int generoId = radioGroupGenero.getCheckedRadioButtonId();
        int tipoDocumentoId = radioGroupTipoDocumento.getCheckedRadioButtonId();

        if (generoId == -1) {
            Toast.makeText(this, "Selecciona un género", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipoDocumentoId == -1) {
            Toast.makeText(this, "Selecciona un tipo de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton generoRadioButton = findViewById(generoId);
        String genero = generoRadioButton.getText().toString();
        RadioButton tipoDocumentoRadioButton = findViewById(tipoDocumentoId);
        String tipoDocumento = tipoDocumentoRadioButton.getText().toString();
        String identificacion = tvDocumentID.getText().toString().trim();
        String nombreU = tvFirstName.getText().toString().trim();
        String apellidoU = tvLastName.getText().toString().trim();
        String programaFor = tvProgrmaformacion.getText().toString().trim();
        String numeroFicha = tvNumeroFicha.getText().toString().trim();
        String jornada = tvjordanaFormacion.getText().toString().trim();
        String modalidad = tvModFormacion.getText().toString().trim();
        String nivelFormacion = tvnivelFormacion.getText().toString().trim();
        String centroForma = tvCentroFormacion.getText().toString().trim();
        String correo = editCorreo.getText().toString().trim();
        String telefono = tvTelefono.getText().toString().trim();
        ProgressDialog progressDialog = new ProgressDialog(this);

        if (identificacion.isEmpty()) {
            tvDocumentID.setError("Diligenciar Documento");
            Toast.makeText(this, "Diligenciar Identificacion", Toast.LENGTH_SHORT).show();

        } else if (nombreU.isEmpty()) {
            tvFirstName.setError("Diligencia primer Nombre");
            Toast.makeText(this, "Diligenciar Primer nombre", Toast.LENGTH_SHORT).show();
        } else if (genero.isEmpty()) {
            generoRadioButton.setError("Diligencia tipo Genero");
            Toast.makeText(this, "Diligenciar tipo de genero", Toast.LENGTH_SHORT).show();
        } else if (tipoDocumento.isEmpty()) {
            tipoDocumentoRadioButton.setError("Selecciona tipo de documento");
        } else if (apellidoU.isEmpty()) {
            tvLastName.setError("Diligenciar primer apellido");
            Toast.makeText(this, "Diligenciar Primer apellido", Toast.LENGTH_SHORT).show();
        } else if (programaFor.isEmpty()) {
            tvProgrmaformacion.setError("Diligenciar Programa de Formacion");
            Toast.makeText(this, "Diligenciar Programa de Fromacion", Toast.LENGTH_SHORT).show();
        } else if (numeroFicha.isEmpty()) {
            tvNumeroFicha.setError("Diligenciar Numero de Ficha");
            Toast.makeText(this, "Diligenciar Numero de Ficha", Toast.LENGTH_SHORT).show();
        } else if (jornada.isEmpty()) {
            tvLastName.setError("Diligenciar Jornada");
            Toast.makeText(this, "Diligenciar Jornada", Toast.LENGTH_SHORT).show();
        } else if (modalidad.isEmpty()) {
            tvLastName.setError("Diligenciar Modalidad");
            Toast.makeText(this, "Diligenciar Modalidad", Toast.LENGTH_SHORT).show();
        } else if (nivelFormacion.isEmpty()) {
            tvLastName.setError("Diligenciar Nivel de formación");
            Toast.makeText(this, "Diligenciar Nivel de formación", Toast.LENGTH_SHORT).show();
        } else if (centroForma.isEmpty()) {
            tvLastName.setError("Diligenciar Centro de Formación");
            Toast.makeText(this, "Centro de Formación", Toast.LENGTH_SHORT).show();
        } else if (correo.isEmpty()){
            editCorreo.setError("Diligencia tu correo electrónico");
            Toast.makeText(this, "Diligenciar Correo", Toast.LENGTH_SHORT).show();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            editCorreo.setError("Formato de correo electrónico no válido");
            Toast.makeText(this, "Formato de correo invalido", Toast.LENGTH_SHORT).show();
        } else if (telefono.isEmpty()){
            tvTelefono.setError("Diligenciar Numero de contacto");
            Toast.makeText(this, "Diligenciar Numero de contacto", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
           // String url = "http://192.168.45.106/AsistenciaApi/digitalizable.php";
            String url = "https://asistenciasena.proyectoswork.com/api/digitalizable.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("ServerResponse", response);
                    if (response.equalsIgnoreCase("datos insertados")) {
                        Toast.makeText(about.this, "Datos ingresados", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(about.this, AccionesMainActivity.class);
                        intent.putExtra("identificacionExtra", identificacion);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(about.this, response, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(about.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }) {
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("idEvento", idEvento);
                    params.put("identificacion", identificacion);
                    params.put("nombreU", nombreU);
                    //params.put("nombreD", nombreD);
                    params.put("apellidoU", apellidoU);
                    //params.put("apellidoD", apellidoD);
                    params.put("genero", genero);
                    params.put("tipoDocumento", tipoDocumento);
                    //params.put("fechanacimiento", fNacimiento);
                    //params.put("tipoSangre", tipoSangre);

                    params.put("programaFor",programaFor);
                    params.put("numeroFicha", numeroFicha);
                    params.put("jornada", jornada);
                    params.put("modalidad", modalidad);
                    params.put("nivelFormacion", nivelFormacion);
                    params.put("centroForma", centroForma);
                    params.put("correo", correo);
                    params.put("telefono", telefono);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(about.this);
            requestQueue.add(stringRequest);
        }
    }
}