package com.example.cedula_scanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class RegistroEscaneable extends AppCompatActivity {

    TextView etCorreo;
    TextView etCelular;
    TextView etDepartamento;
    TextView etMunicipio;
    TextView etDireccion;
    TextView etCiudadNacimiento;

    private Spinner Municipio;

    TextView editTextIdentificacion, municipios;
    private Toolbar toolbar;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_escaneable);

        etCorreo = findViewById(R.id.editCorreo);
        etCelular = findViewById(R.id.editCelular);
        etDepartamento = findViewById(R.id.editDepartamento);
        etDepartamento.setText("Atlántico");
        etDireccion = findViewById(R.id.editDireccion);
        etCiudadNacimiento = findViewById(R.id.editCiudad);

        Municipio = findViewById(R.id.municipio);
        municipios = findViewById(R.id.municipios);

        btn = findViewById(R.id.Siguiente);
        btn.setOnClickListener(view -> mostrarDialogoConfirmacion());

        String identificacion = getIntent().getStringExtra("identificacionExtra");

        editTextIdentificacion = findViewById(R.id.editTextIdentificacion);
        editTextIdentificacion.setText(identificacion);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.Municipio, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Municipio.setAdapter(adapter);

        Municipio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                municipios.setText(" " + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                municipios.setText("");
            }
        });
    }

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Datos");
        builder.setMessage("¿Deseas insertar estos datos en la base de datos?");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Inserta los datos en la base de datos
                insertar();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss(); // Cierra el cuadro de diálogo
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void insertar() {
        // Obtén los valores de los nuevos campos
        String correo = etCorreo.getText().toString().trim();
        String celular = etCelular.getText().toString().trim();
        String departamento = etDepartamento.getText().toString().trim();
        String municipio = municipios.getText().toString().trim(); // Obtén el valor del Spinner
        String direccion = etDireccion.getText().toString().trim();
        String ciudadNacimiento = etCiudadNacimiento.getText().toString().trim();
        String identificacion = editTextIdentificacion.getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(this);

        if (departamento.isEmpty()){
            etDepartamento.setError("Ingrese el departamento");
        }
        else if (municipio.isEmpty()){
            municipios.setError("Ingrese el municipio");
        }
        else if (ciudadNacimiento.isEmpty()){
            etCiudadNacimiento.setError("Ingrese la ciudad de nacimiento");
        }
        else {
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            // Cambia la URL por la de tu servidor
            String url = "https://www.plataforma50.com/pruebas/prueba.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("ServerResponse", response);
                    if (response.equalsIgnoreCase("datos insertados")) {
                        Toast.makeText(RegistroEscaneable.this, "Datos ingresados", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(RegistroEscaneable.this, "Datos ingresados", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(RegistroEscaneable.this, caracterizacion.class);
                        intent.putExtra("identificacionExtra", identificacion);
                        startActivity(intent);
                        finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(RegistroEscaneable.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }) {
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();

                    // Agrega todos los campos al mapa de parámetros
                    params.put("correo", correo);
                    params.put("celular", celular);
                    params.put("departamento", departamento);
                    params.put("municipio", municipio); // Agrega el valor del Spinner como "municipio"
                    params.put("direccion", direccion);
                    params.put("ciudadnacimiento", ciudadNacimiento);
                    params.put("identificacion", identificacion);

                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(RegistroEscaneable.this);
            requestQueue.add(stringRequest);
        }
    }

}
