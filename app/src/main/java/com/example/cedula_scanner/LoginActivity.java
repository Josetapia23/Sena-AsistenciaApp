package com.example.cedula_scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                // Verifica si se ingresaron credenciales válidas
                if (!username.isEmpty() && !password.isEmpty()) {
                    loginUser(username, password);
                } else {
                    Toast.makeText(LoginActivity.this, "Por favor, ingresa tu usuario y contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void loginUser(final String username, final String password) {

        String url = "http://192.168.1.117/AsistenciaApi/login.php";


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        boolean success = false;
                        try {
                            success = jsonObject.getBoolean("success");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String message = null;
                        try {
                            message = jsonObject.getString("message");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                        if (success) {
                            // Inicio de sesión exitoso
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            // Aquí puedes redirigir a la siguiente actividad o realizar otra acción
                            Intent intent = new Intent(LoginActivity.this, com.example.cedula_scanner.SeletEventoMain.class);
                            startActivity(intent);

                            finish();
                        } else {
                            // Inicio de sesión fallido
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}