package com.example.cedula_scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeletEventoMain extends AppCompatActivity {
    private static final String TAG = "SeletEventoMain";
    private Spinner spinnerEvento;
    private Map<String, String> subeventosIdMap;
    private Map<String, String> eventosIdMap;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 60000; // 1 minuto en milisegundos
    private boolean datosMunicipiosCargados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selet_evento_main);

        // Verificar si el usuario está logueado
        checkLoginStatus();

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Selección de Eventos");
        }

        spinnerEvento = findViewById(R.id.spinnerEvento);
        subeventosIdMap = new HashMap<>();
        eventosIdMap = new HashMap<>();

        // Configurar actualización automática
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                cargarEventos();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };

        // Cargar datos geográficos en segundo plano
        cargarDatosGeograficos();

        // Cargar eventos
        cargarEventos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Iniciar la actualización automática
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener la actualización automática
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // Si no está logueado, redirigir al login
            Intent intent = new Intent(SeletEventoMain.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflamos el menú que ya tienes creado
        MenuItem logoutItem = menu.add(Menu.NONE, R.id.itemCerrarSesion, Menu.NONE, "Cerrar Sesión");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.itemCerrarSesion) {
            logoutUser();
            return true;
        } else if (id == R.id.item3) {
            // Código para la opción "Acerca de"
            Intent intent = new Intent(this, about.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.item4) {
            // Código para la opción "Escanear"
            // Intent intent = new Intent(this, EscanearActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Función de escanear no implementada", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        // Limpiar las preferencias de sesión
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Redirigir al login
        Intent intent = new Intent(SeletEventoMain.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    // Método del botón Continuar
    public void onClickContinuar(View view) {
        int selectedPosition = spinnerEvento.getSelectedItemPosition();
        if (selectedPosition > 0) {
            // Se ha seleccionado un evento
            String nombreEventoSeleccionado = spinnerEvento.getItemAtPosition(selectedPosition).toString();

            // Obtener los IDs directamente de los mapas locales
            String idSubevento = subeventosIdMap.get(nombreEventoSeleccionado);
            String idEvento = eventosIdMap.get(nombreEventoSeleccionado);

            if (idSubevento != null && idEvento != null) {
                // Si ambos IDs existen en los mapas, continuar
                continuarConIds(idSubevento, idEvento);
            } else {
                // Si por alguna razón no se encuentran en los mapas, mostrar error
                Toast.makeText(this, "Error: No se encontraron los IDs para el evento seleccionado", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No se ha seleccionado ningún evento
            Toast.makeText(this, "Por favor, selecciona un evento", Toast.LENGTH_SHORT).show();
        }
    }

    private void continuarConIds(String idSubevento, String idEvento) {
        if (idSubevento != null && idEvento != null) {
            // Guardar los IDs en SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
            editor.putString("idSubevento", idSubevento);
            editor.putString("idEvento", idEvento);
            editor.apply();

            // Verificar si los datos geográficos están cargados
            if (!datosMunicipiosCargados) {
                // Si no están cargados, intentar cargarlos ahora
                cargarDatosGeograficos();
            }

            // Se han obtenido los IDs, pasar a la siguiente actividad
            Intent intent = new Intent(SeletEventoMain.this, AccionesMainActivity.class);
            intent.putExtra("id_subevento", idSubevento);
            intent.putExtra("id_evento", idEvento);
            startActivity(intent);
            finish();
        } else {
            // No se han obtenido los IDs
            Toast.makeText(SeletEventoMain.this, "Error: No se pudieron obtener los IDs del evento", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarEventos(){
        String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/eventoSena.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                JSONArray eventosArray = jsonObject.getJSONArray("eventos");
                                List<String> eventos = new ArrayList<>();
                                eventos.add("Seleccionar Evento");

                                // Limpiar los mapas antes de llenarlos con nuevos datos
                                subeventosIdMap.clear();
                                eventosIdMap.clear();

                                for (int i = 0; i < eventosArray.length(); i++) {
                                    JSONObject evento = eventosArray.getJSONObject(i);
                                    String idSubevento = evento.getString("id");
                                    String nombreSubevento = evento.getString("nombre");
                                    String idEventoPadre = evento.getString("idevento");

                                    // Agregar el nombre a la lista para el spinner
                                    eventos.add(nombreSubevento);

                                    // Guardar las relaciones nombre-id en los mapas
                                    subeventosIdMap.put(nombreSubevento, idSubevento);
                                    eventosIdMap.put(nombreSubevento, idEventoPadre);
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(SeletEventoMain.this,
                                        android.R.layout.simple_spinner_dropdown_item, eventos);
                                spinnerEvento.setAdapter(adapter);
                                spinnerEvento.setSelection(0);
                            } else {
                                Toast.makeText(SeletEventoMain.this, "No hay eventos activos", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SeletEventoMain.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(SeletEventoMain.this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    /**
     * Carga los datos de departamentos y municipios desde el servidor y los guarda en caché
     */
    private void cargarDatosGeograficos() {
        // Primero verificar si ya están en caché y si son recientes (menos de 24 horas)
        SharedPreferences prefs = getSharedPreferences("DatosMunicipios", MODE_PRIVATE);
        long ultimaActualizacion = prefs.getLong("ultima_actualizacion", 0);
        long ahora = System.currentTimeMillis();
        boolean actualizacionNecesaria = (ahora - ultimaActualizacion) > 86400000; // 24 horas en milisegundos

        if (!actualizacionNecesaria && prefs.contains("departamentos_json")) {
            // Si hay datos en caché y son recientes, usarlos
            Log.d(TAG, "Usando datos geográficos de caché");
            datosMunicipiosCargados = true;
            return;
        }

        // Si no hay datos en caché o son antiguos, cargarlos del servidor
        //String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/obtenerDepartamentos.php";
        String url = "http://192.168.68.162/AsistenciaApi/obtenerDepartamentos.php";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                // Guardar en caché
                                SharedPreferences.Editor editor = getSharedPreferences("DatosMunicipios", MODE_PRIVATE).edit();
                                editor.putString("departamentos_json", response);
                                editor.putLong("ultima_actualizacion", System.currentTimeMillis());
                                editor.apply();

                                datosMunicipiosCargados = true;
                                Log.d(TAG, "Datos geográficos cargados y guardados en caché");
                            } else {
                                Log.e(TAG, "Error al cargar datos geográficos: " + jsonObject.optString("message"));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error al procesar datos geográficos: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error de red al cargar datos geográficos: " + error.getMessage());
                    }
                });

        // Aumentar el tiempo de espera para esta solicitud
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                15000, // 15 segundos de timeout
                1, // Sin reintentos
                1.0f));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}