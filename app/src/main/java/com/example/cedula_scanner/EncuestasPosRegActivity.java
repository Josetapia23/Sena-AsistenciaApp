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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncuestasPosRegActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText tvNombreCompleto, tvCedula;
    private EditText etNombreEmpresa, etNitEmpresa, etTelefonoEmpresa, etCorreoEmpresa;
    private RadioGroup rgTipoPersona, rgRepresentanteEmpresa;
    private RadioButton rbPersonaNatural, rbPersonaJuridica, rbSiRepresentante, rbNoRepresentante;
    private LinearLayout layoutPersonaJuridica, layoutDatosEmpresa;
    private Spinner spinnerProyectoSena, spinnerInteres1, spinnerInteres2, spinnerInteres3;

    private String nombreCompleto;
    private String numeroCedula;
    private String idEvento;
    private String idSubevento;

    // Arrays para los spinners
    private List<String> listaIntereses = new ArrayList<>();
    private List<Integer> listaInteresesIds = new ArrayList<>();
    private List<String> listaProyectosSena = new ArrayList<>();
    private List<Integer> listaProyectosSenaIds = new ArrayList<>();

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

        // Llenar los spinners con datos
        cargarDatosSpinners();
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

        // Cargar lista de proyectos SENA (esto podría obtenerse de una API/base de datos)
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

            // Validar que se haya seleccionado un proyecto SENA
            if (spinnerProyectoSena.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Por favor seleccione un proyecto SENA", Toast.LENGTH_SHORT).show();
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

        final int proyectoSenaId = esPersonaJuridica && esRepresentanteEmpresa ?
                listaProyectosSenaIds.get(spinnerProyectoSena.getSelectedItemPosition()) : -1;

        final int interes1Id = listaInteresesIds.get(posInteres1);
        final int interes2Id = listaInteresesIds.get(posInteres2);
        final int interes3Id = listaInteresesIds.get(posInteres3);

        // Enviar datos al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/insertarEncuesta.php";

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
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(EncuestasPosRegActivity.this);
                        builder.setTitle("Error de conexión");
                        builder.setMessage("No se pudo conectar con el servidor. Por favor, intente nuevamente.");
                        builder.setPositiveButton("Aceptar", null);
                        builder.show();
                        Log.e("EncuestaError", error.toString());
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

                // Si es persona jurídica y representante, enviar datos de la empresa
                if (tipoPersona.equals("Juridica") && esRepresentante.equals("Si")) {
                    params.put("es_representante", "Si");
                    params.put("nombre_empresa", nombreEmpresa);
                    params.put("nit_empresa", nitEmpresa);
                    params.put("telefono_empresa", telefonoEmpresa);
                    params.put("correo_empresa", correoEmpresa);
                    params.put("proyecto_sena", String.valueOf(proyectoSenaId));
                } else {
                    params.put("es_representante", "No");
                }

                // Enviar los intereses seleccionados
                params.put("interes1", String.valueOf(interes1Id));
                params.put("interes2", String.valueOf(interes2Id));
                params.put("interes3", String.valueOf(interes3Id));

                return params;
            }
        };

        queue.add(stringRequest);
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