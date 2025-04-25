package com.example.cedula_scanner;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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

import java.util.HashMap;
import java.util.Map;

public class caracterizacion extends AppCompatActivity {

    private Spinner spinner, spinner2, spinner3, spinner4, spinner5;
    private TextView poblacion, discapacidad, tipoDiscapacidad, nivelFormacion, Servicio;

    private Toolbar toolbar;
    Button btn;
    TextView editTextIdentificacion;
    LinearLayout spinner2Container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caracterizacion);

        poblacion = findViewById(R.id.poblacionVulnerable);
        discapacidad = findViewById(R.id.discapacidad);
        tipoDiscapacidad = findViewById(R.id.tipoDiscapacidad);
        nivelFormacion = findViewById(R.id.nivelFormacion);
        Servicio = findViewById(R.id.servicio);

        btn = findViewById(R.id.terminar); // Asegúrate de tener un botón con el id "btn" en tu layout
        btn.setOnClickListener(view -> showConfirmationDialog());

        String identificacion = getIntent().getStringExtra("identificacionExtra");

        // Obtener referencia al campo de entrada (EditText)
        editTextIdentificacion = findViewById(R.id.editTextIdentificacion);

        // Establecer el valor de "identificacion" en el campo de entrada
        editTextIdentificacion.setText(identificacion);

        spinner = findViewById(R.id.spinner);
        poblacion = findViewById(R.id.poblacionVulnerable);

        spinner5 = findViewById(R.id.spinner5);
        discapacidad = findViewById(R.id.discapacidad);

        spinner3 = findViewById(R.id.spinner3);
        nivelFormacion = findViewById(R.id.nivelFormacion);

        spinner4 = findViewById(R.id.spinner4);
        Servicio = findViewById(R.id.servicio);

        spinner2 = findViewById(R.id.spinner2);
        tipoDiscapacidad = findViewById(R.id.tipoDiscapacidad);
        spinner2Container = findViewById(R.id.spinner2Container); // Asegúrate de que el ID sea el correcto en tu layout XML


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.spinner_items, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                poblacion.setText(" " + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                poblacion.setText("");
            }
        });

        ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(
                this, R.array.spinner_items5,  R.layout.spinner_item);
        adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner5.setAdapter(adapter5);

        spinner5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                discapacidad.setText("" + selectedItem);

                // Verifica si la opción seleccionada en Spinner5 es "si" y muestra Spinner2 si es el caso.
                if ("si".equalsIgnoreCase(selectedItem)) {
                    spinner2Container.setVisibility(View.VISIBLE);
                } else {
                    // Si no es "si", oculta Spinner2
                    spinner2Container.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                discapacidad.setText("");
            }
        });

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.spinner_items2, R.layout.spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                tipoDiscapacidad.setText("" + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tipoDiscapacidad.setText("");
            }
        });

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this, R.array.spinner_items3,  R.layout.spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                nivelFormacion.setText("" + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                nivelFormacion.setText("");
            }
        });

        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
                this, R.array.spinner_items4,  R.layout.spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner4.setAdapter(adapter4);

        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                Servicio.setText("" + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Servicio.setText("");
            }
        });
    }


    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmación");
        builder.setMessage("¿Estás seguro de que quieres enviar los datos?");
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            // Cuando se hace clic en "Confirmar", llama a tu método insertar()
            insertar();
            dialog.dismiss(); // Cierra el cuadro de diálogo
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // Cuando se hace clic en "Cancelar", simplemente cierra el cuadro de diálogo
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void insertar() {
        // Obtén los valores de los nuevos campos
        String poblaciones = poblacion.getText().toString().trim();
        String discapacidades = discapacidad.getText().toString().trim();
        String tipoDiscapacidades = tipoDiscapacidad.getText().toString().trim();
        String nivelFormaciones = nivelFormacion.getText().toString().trim();
        String Servicios = Servicio.getText().toString().trim();
        String identificacion = editTextIdentificacion.getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(this);

        if (poblaciones.isEmpty()){
            poblacion.setError("Ingrese el poblacion");
        }
        else if (discapacidades.isEmpty()){
            discapacidad.setError("Ingrese discapacidad");
        }
        else if (tipoDiscapacidades.isEmpty()){
            tipoDiscapacidad.setError("Ingrese tipo Discapacidad");
        }
        else if (nivelFormaciones.isEmpty()){
            nivelFormacion.setError("Ingrese el nivelFormacion");
        }
        else if (Servicios.isEmpty()){
            Servicio.setError("Ingrese la Servicio");
        }
        else {
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            //https://www.plataforma50.com/pruebas/caracterizacion.php
            String url = "https://www.plataforma50.com/pruebas/caracterizacion.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("ServerResponse", response);
                    if (response.equalsIgnoreCase("datos insertados")) {
                        Toast.makeText(caracterizacion.this, "Datos ingresados", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(caracterizacion.this, MainActivity.class);
                        intent.putExtra("identificacionExtra", identificacion);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(caracterizacion.this, "No se pudieron cargar los datos", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(caracterizacion.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }) {
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();

                    // Agrega solo los nuevos campos al mapa de parámetros
                    params.put("poblacionVulnerable", poblaciones);
                    params.put("discapacidad", discapacidades);
                    params.put("tipoDiscapacidad", tipoDiscapacidades);
                    params.put("nivelFormacion", nivelFormaciones);
                    params.put("servicio", Servicios);
                    params.put("identificacion", identificacion);

                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(caracterizacion.this);
            requestQueue.add(stringRequest);
        }
    }
}

