package com.example.cedula_scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncuestasPendientesManager {
    private static final String TAG = "EncuestasPendientesManager";
    private static final String PREFS_NAME = "EncuestasPendientes";
    private static final String ENCUESTAS_KEY = "encuestas";

    private Context context;
    private RequestQueue requestQueue;
    private OnSincronizacionCompletaListener listener;

    public EncuestasPendientesManager(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Guarda una encuesta pendiente para ser sincronizada después
     */
    public void guardarEncuestaPendiente(JSONObject encuestaData) {
        try {
            // Obtener lista actual de encuestas pendientes
            List<JSONObject> encuestasPendientes = obtenerEncuestasPendientes();

            // Añadir la nueva encuesta a la lista
            encuestasPendientes.add(encuestaData);

            // Guardar la lista actualizada
            guardarListaEncuestas(encuestasPendientes);

            Log.d(TAG, "Encuesta guardada localmente: " + encuestaData.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar encuesta pendiente: " + e.getMessage());
            Toast.makeText(context, "Error al guardar encuesta localmente", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Obtiene la lista de encuestas pendientes de sincronización
     */
    public List<JSONObject> obtenerEncuestasPendientes() {
        List<JSONObject> encuestasPendientes = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encuestasJson = prefs.getString(ENCUESTAS_KEY, "[]");

        try {
            JSONArray encuestasArray = new JSONArray(encuestasJson);

            for (int i = 0; i < encuestasArray.length(); i++) {
                encuestasPendientes.add(encuestasArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error al obtener encuestas pendientes: " + e.getMessage());
        }

        return encuestasPendientes;
    }

    /**
     * Guarda la lista de encuestas pendientes en SharedPreferences
     */
    private void guardarListaEncuestas(List<JSONObject> encuestas) {
        JSONArray encuestasArray = new JSONArray();

        for (JSONObject encuesta : encuestas) {
            encuestasArray.put(encuesta);
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(ENCUESTAS_KEY, encuestasArray.toString());
        editor.apply();
    }

    /**
     * Sincroniza todas las encuestas pendientes
     */
    public void sincronizarEncuestasPendientes(OnSincronizacionCompletaListener listener) {
        this.listener = listener;

        // Verificar si hay conexión a internet
        if (!hayConexionInternet()) {
            if (listener != null) {
                listener.onSincronizacionCompleta(false, "No hay conexión a internet");
            }
            return;
        }

        // Obtener encuestas pendientes
        List<JSONObject> encuestasPendientes = obtenerEncuestasPendientes();

        if (encuestasPendientes.isEmpty()) {
            if (listener != null) {
                listener.onSincronizacionCompleta(true, "No hay encuestas pendientes");
            }
            return;
        }

        // Procesar cada encuesta pendiente
        procesarSiguienteEncuesta(encuestasPendientes, 0, new ArrayList<>());
    }

    /**
     * Procesa recursivamente las encuestas pendientes
     */
    private void procesarSiguienteEncuesta(final List<JSONObject> encuestas, final int indice,
                                           final List<String> errores) {
        if (indice >= encuestas.size()) {
            // Hemos terminado de procesar todas las encuestas
            boolean exito = errores.isEmpty();

            String mensaje = exito ?
                    "Todas las encuestas fueron sincronizadas correctamente" :
                    "Algunas encuestas no pudieron sincronizarse: " + String.join(", ", errores);

            // Si hubo éxito, limpiamos las encuestas pendientes
            if (exito) {
                limpiarEncuestasPendientes();
            } else {
                // Si hubo errores, eliminar solo las encuestas sincronizadas
                List<JSONObject> encuestasPendientes = new ArrayList<>();
                for (int i = 0; i < encuestas.size(); i++) {
                    if (i >= indice || !errores.contains("Encuesta " + (i + 1))) {
                        encuestasPendientes.add(encuestas.get(i));
                    }
                }
                guardarListaEncuestas(encuestasPendientes);
            }

            if (listener != null) {
                listener.onSincronizacionCompleta(exito, mensaje);
            }
            return;
        }

        // Obtener la encuesta actual
        final JSONObject encuestaActual = encuestas.get(indice);

        // Enviar la encuesta al servidor
        enviarEncuestaAlServidor(encuestaActual, new OnEncuestaEnviadaListener() {
            @Override
            public void onEncuestaEnviada(boolean exito, String mensaje) {
                if (!exito) {
                    errores.add("Encuesta " + (indice + 1) + ": " + mensaje);
                }

                // Procesar la siguiente encuesta
                procesarSiguienteEncuesta(encuestas, indice + 1, errores);
            }
        });
    }

    /**
     * Envía una encuesta al servidor
     */
    private void enviarEncuestaAlServidor(final JSONObject encuestaData, final OnEncuestaEnviadaListener listener) {
        // URL del servidor
        String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/insertarEncuesta.php";
        //String url = "http://192.168.0.14/AsistenciaApi/insertarEncuesta.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");

                            if (listener != null) {
                                listener.onEncuestaEnviada(success, message);
                            }
                        } catch (JSONException e) {
                            if (listener != null) {
                                listener.onEncuestaEnviada(false, "Error al procesar la respuesta: " + e.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error de red";

                        if (error.networkResponse != null) {
                            errorMessage += " (código: " + error.networkResponse.statusCode + ")";
                        }

                        if (listener != null) {
                            listener.onEncuestaEnviada(false, errorMessage);
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                try {
                    // Extraer todos los parámetros del JSONObject
                    for (int i = 0; i < encuestaData.names().length(); i++) {
                        String key = encuestaData.names().getString(i);
                        String value = encuestaData.get(key).toString();
                        params.put(key, value);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al extraer parámetros: " + e.getMessage());
                }

                return params;
            }
        };

        // Aumentar el tiempo de espera para la solicitud
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 segundos de timeout
                1, // Sin reintentos
                1.0f));

        requestQueue.add(stringRequest);
    }

    /**
     * Limpia todas las encuestas pendientes
     */
    public void limpiarEncuestasPendientes() {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(ENCUESTAS_KEY);
        editor.apply();
    }

    /**
     * Verifica si hay conexión a internet
     */
    public boolean hayConexionInternet() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Verifica si hay encuestas pendientes
     */
    public boolean hayEncuestasPendientes() {
        List<JSONObject> encuestas = obtenerEncuestasPendientes();
        return !encuestas.isEmpty();
    }

    /**
     * Obtiene el número de encuestas pendientes
     */
    public int getNumeroEncuestasPendientes() {
        return obtenerEncuestasPendientes().size();
    }

    /**
     * Interface para notificar cuando se completa la sincronización
     */
    public interface OnSincronizacionCompletaListener {
        void onSincronizacionCompleta(boolean exito, String mensaje);
    }

    /**
     * Interface para notificar cuando se envía una encuesta
     */
    private interface OnEncuestaEnviadaListener {
        void onEncuestaEnviada(boolean exito, String mensaje);
    }
}