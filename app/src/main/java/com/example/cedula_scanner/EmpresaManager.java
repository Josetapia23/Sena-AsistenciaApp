package com.example.cedula_scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmpresaManager {
    private static final String TAG = "EmpresaManager";
    private static final String PREFS_NAME = "EmpresasPrefs";

    private Context context;
    private Map<Integer, Empresa> todasLasEmpresas = new HashMap<>();
    private boolean datosAlmacenados = false;
    private List<OnEmpresasLoadedListener> listeners = new ArrayList<>();

    // Interfaz para callbacks cuando se cargan empresas
    public interface OnEmpresasLoadedListener {
        void onEmpresasLoaded(List<Empresa> empresas);
    }

    // Clase interna para representar una empresa
    public static class Empresa {
        int id;
        String nombre;
        String nit;
        String telefono;
        String correo;
        String direccion;

        public Empresa(int id, String nombre, String nit, String telefono, String correo, String direccion) {
            this.id = id;
            this.nombre = nombre;
            this.nit = nit;
            this.telefono = telefono;
            this.correo = correo;
            this.direccion = direccion;
        }

        // Método para obtener el nombre corregido con caracteres especiales
        public String getNombreCorregido() {
            try {
                // Intentar corregir la codificación
                return new String(nombre.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                return nombre; // Si falla, devolver el nombre original
            }
        }

        @Override
        public String toString() {
            return getNombreCorregido();
        }
    }

    // Método para crear una nueva empresa y guardarla en la caché local
    public EmpresaManager.Empresa crearNuevaEmpresa(String nombre, String nit, String telefono, String correo, String direccion) {
        // Asignar ID temporal negativo (se actualizará cuando se guarde en servidor)
        int idTemporal = -1 * (todasLasEmpresas.size() + 1);

        Empresa nuevaEmpresa = new Empresa(idTemporal, nombre, nit, telefono, correo, direccion);
        todasLasEmpresas.put(idTemporal, nuevaEmpresa);

        // Guardar en preferencias
        guardarEmpresasEnPreferencias();

        return nuevaEmpresa;
    }

    // Método para sincronizar nuevas empresas con el servidor
    public void sincronizarNuevasEmpresas(final OnSincronizacionCompletaListener listener) {
        // Identificar empresas con IDs negativos (nuevas empresas creadas localmente)
        final List<Empresa> empresasNuevas = new ArrayList<>();
        for (Empresa empresa : todasLasEmpresas.values()) {
            if (empresa.id < 0) {
                empresasNuevas.add(empresa);
            }
        }

        // Si no hay empresas nuevas para sincronizar, notificar y salir
        if (empresasNuevas.isEmpty()) {
            if (listener != null) {
                listener.onSincronizacionCompleta(true, "No hay empresas nuevas para sincronizar");
            }
            return;
        }

        // Contador para seguimiento
        final int[] empresasSincronizadas = {0};
        final int[] empresasConError = {0};
        final StringBuilder mensajesError = new StringBuilder();

        // Sincronizar cada empresa secuencialmente
        sincronizarEmpresa(empresasNuevas, 0, empresasSincronizadas, empresasConError, mensajesError, listener);
    }

    // Método recursivo para sincronizar empresas secuencialmente
    private void sincronizarEmpresa(final List<Empresa> empresas, final int indice,
                                    final int[] sincronizadas, final int[] errores,
                                    final StringBuilder mensajesError,
                                    final OnSincronizacionCompletaListener listener) {
        // Si hemos terminado con todas las empresas, notificar y salir
        if (indice >= empresas.size()) {
            // Guardar los cambios en SharedPreferences
            guardarEmpresasEnPreferencias();

            // Notificar éxito
            if (listener != null) {
                String mensaje = "Empresas sincronizadas: " + sincronizadas[0];
                if (errores[0] > 0) {
                    mensaje += ", Con errores: " + errores[0] + "\n" + mensajesError.toString();
                }
                listener.onSincronizacionCompleta(errores[0] == 0, mensaje);
            }
            return;
        }

        // Obtener la empresa actual
        final Empresa empresa = empresas.get(indice);

        // Preparar parámetros
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/agregarEmpresa.php";
        //String url = "http://192.168.0.14/AsistenciaApi/agregarEmpresa.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                // Obtener el ID asignado por el servidor
                                int idReal = jsonResponse.getInt("id_empresa");

                                // Crear una nueva empresa con el ID real
                                Empresa empresaActualizada = new Empresa(
                                        idReal,
                                        empresa.nombre,
                                        empresa.nit,
                                        empresa.telefono,
                                        empresa.correo,
                                        empresa.direccion
                                );

                                // Reemplazar en el mapa
                                todasLasEmpresas.remove(empresa.id);
                                todasLasEmpresas.put(idReal, empresaActualizada);

                                // Incrementar contador de sincronizadas
                                sincronizadas[0]++;

                                // Continuar con la siguiente empresa
                                sincronizarEmpresa(empresas, indice + 1, sincronizadas, errores, mensajesError, listener);

                            } else {
                                // Registrar error
                                errores[0]++;
                                String mensaje = jsonResponse.optString("message", "Error desconocido");
                                mensajesError.append("Error en empresa ").append(empresa.nombre)
                                        .append(": ").append(mensaje).append("\n");

                                // Continuar con la siguiente empresa
                                sincronizarEmpresa(empresas, indice + 1, sincronizadas, errores, mensajesError, listener);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error al procesar respuesta: " + e.getMessage());

                            // Registrar error
                            errores[0]++;
                            mensajesError.append("Error en empresa ").append(empresa.nombre)
                                    .append(": Error de formato en la respuesta\n");

                            // Continuar con la siguiente empresa
                            sincronizarEmpresa(empresas, indice + 1, sincronizadas, errores, mensajesError, listener);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error de red: " + error.getMessage());

                        // Registrar error
                        errores[0]++;
                        mensajesError.append("Error en empresa ").append(empresa.nombre)
                                .append(": Error de conexión\n");

                        // Continuar con la siguiente empresa
                        sincronizarEmpresa(empresas, indice + 1, sincronizadas, errores, mensajesError, listener);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nombre", empresa.nombre);
                params.put("nit", empresa.nit != null ? empresa.nit : "");
                params.put("telefono", empresa.telefono != null ? empresa.telefono : "");
                params.put("correo", empresa.correo != null ? empresa.correo : "");
                params.put("direccion", empresa.direccion != null ? empresa.direccion : "");
                return params;
            }
        };

        // Aumentar el tiempo de espera para la solicitud
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 segundos de timeout
                1, // Sin reintentos
                1.0f));

        queue.add(stringRequest);
    }

    // Interfaz para notificar cuando la sincronización ha terminado
    public interface OnSincronizacionCompletaListener {
        void onSincronizacionCompleta(boolean exitoso, String mensaje);
    }

    // Constructor
    public EmpresaManager(Context context) {
        this.context = context;
        // Cargar empresas desde preferencias al inicializar
        cargarEmpresasDesdePreferencias();
    }

    // Agregar listener para notificaciones
    public void addOnEmpresasLoadedListener(OnEmpresasLoadedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // Remover listener
    public void removeOnEmpresasLoadedListener(OnEmpresasLoadedListener listener) {
        listeners.remove(listener);
    }

    // Notificar a todos los listeners
    private void notifyListeners(List<Empresa> empresas) {
        for (OnEmpresasLoadedListener listener : listeners) {
            listener.onEmpresasLoaded(empresas);
        }
    }

    // Cargar empresas desde SharedPreferences
    private void cargarEmpresasDesdePreferencias() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Obtener la fecha de la última actualización
        long ultimaActualizacion = prefs.getLong("ultima_actualizacion", 0);
        long ahora = System.currentTimeMillis();

        // Si han pasado más de 24 horas (86400000 ms), forzar una nueva carga
        boolean actualizacionNecesaria = (ahora - ultimaActualizacion) > 86400000;

        if (!actualizacionNecesaria) {
            try {
                // Intentar cargar los datos guardados
                String empresasJson = prefs.getString("empresas_json", null);

                if (empresasJson != null) {
                    JSONArray jsonArray = new JSONArray(empresasJson);
                    todasLasEmpresas.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int id = jsonObject.getInt("id");
                        String nombre = jsonObject.getString("nombre");
                        String nit = jsonObject.getString("nit");
                        String telefono = jsonObject.getString("telefono");
                        String correo = jsonObject.getString("correo");
                        String direccion = jsonObject.optString("direccion", "");

                        todasLasEmpresas.put(id, new Empresa(id, nombre, nit, telefono, correo, direccion));
                    }

                    datosAlmacenados = true;
                    Log.d(TAG, "Empresas cargadas desde preferencias: " + todasLasEmpresas.size());

                    // Notificar a los listeners
                    notifyListeners(getListaEmpresas());
                    return;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al cargar empresas desde preferencias: " + e.getMessage());
            }
        }

        datosAlmacenados = false;
        cargarEmpresasDesdeServidor();
    }

    // Guardar empresas en SharedPreferences
    private void guardarEmpresasEnPreferencias() {
        if (todasLasEmpresas.isEmpty()) {
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray();

            for (Empresa empresa : todasLasEmpresas.values()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", empresa.id);
                jsonObject.put("nombre", empresa.nombre);
                jsonObject.put("nit", empresa.nit);
                jsonObject.put("telefono", empresa.telefono);
                jsonObject.put("correo", empresa.correo);
                jsonObject.put("direccion", empresa.direccion);
                jsonArray.put(jsonObject);
            }

            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString("empresas_json", jsonArray.toString());
            editor.putLong("ultima_actualizacion", System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "Empresas guardadas en preferencias: " + todasLasEmpresas.size());

        } catch (JSONException e) {
            Log.e(TAG, "Error al guardar empresas en preferencias: " + e.getMessage());
        }
    }

    // Cargar empresas desde el servidor
    public void cargarEmpresasDesdeServidor() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://tecnoparqueatlantico.com/red_oportunidades/AsistenciaApi/obtenerEmpresas.php";
        //String url = "http://192.168.0.14/AsistenciaApi/obtenerEmpresas.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                JSONArray empresas = jsonResponse.getJSONArray("empresas");
                                todasLasEmpresas.clear();

                                for (int i = 0; i < empresas.length(); i++) {
                                    JSONObject empresa = empresas.getJSONObject(i);
                                    int id = empresa.getInt("id_empre");
                                    String nombre = empresa.getString("nombre_empre");
                                    String nit = empresa.getString("nit");
                                    String telefono = empresa.getString("telefono");
                                    String correo = empresa.getString("correo");
                                    String direccion = empresa.optString("direccion", "");

                                    todasLasEmpresas.put(id, new Empresa(id, nombre, nit, telefono, correo, direccion));
                                }

                                // Guardar en SharedPreferences para uso futuro
                                guardarEmpresasEnPreferencias();

                                Log.d(TAG, "Empresas cargadas desde servidor: " + todasLasEmpresas.size());
                                datosAlmacenados = true;

                                // Notificar a los listeners
                                notifyListeners(getListaEmpresas());

                            } else {
                                Log.e(TAG, "Error al cargar empresas: " + jsonResponse.optString("message"));
                                // Si hay error, intentar usar datos almacenados
                                if (datosAlmacenados) {
                                    notifyListeners(getListaEmpresas());
                                } else {
                                    // Si no hay datos almacenados, notificar lista vacía
                                    notifyListeners(new ArrayList<Empresa>());
                                }
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error al procesar respuesta: " + e.getMessage());
                            // Si hay error, intentar usar datos almacenados
                            if (datosAlmacenados) {
                                notifyListeners(getListaEmpresas());
                            } else {
                                // Si no hay datos almacenados, notificar lista vacía
                                notifyListeners(new ArrayList<Empresa>());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error de red: " + error.getMessage());

                        // Si hay error, intentar usar datos almacenados
                        if (datosAlmacenados) {
                            notifyListeners(getListaEmpresas());
                        } else {
                            // Si no hay datos almacenados, notificar lista vacía
                            notifyListeners(new ArrayList<Empresa>());
                        }
                    }
                });

        // Aumentar el tiempo de espera para la solicitud
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 segundos de timeout
                1, // Sin reintentos
                1.0f));

        queue.add(stringRequest);
    }

    // Obtener lista ordenada de todas las empresas
    public List<Empresa> getListaEmpresas() {
        List<Empresa> lista = new ArrayList<>(todasLasEmpresas.values());

        // Ordenar por nombre
        Collections.sort(lista, new Comparator<Empresa>() {
            @Override
            public int compare(Empresa e1, Empresa e2) {
                return e1.getNombreCorregido().compareToIgnoreCase(e2.getNombreCorregido());
            }
        });

        return lista;
    }

    // Buscar empresas por nombre (para filtrado)
    public List<Empresa> buscarEmpresasPorNombre(String query) {
        List<Empresa> resultados = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return getListaEmpresas();
        }

        String queryLower = query.toLowerCase();

        for (Empresa empresa : todasLasEmpresas.values()) {
            String nombreCorregido = empresa.getNombreCorregido().toLowerCase();
            if (nombreCorregido.contains(queryLower)) {
                resultados.add(empresa);
            }
        }

        // Ordenar resultados
        Collections.sort(resultados, new Comparator<Empresa>() {
            @Override
            public int compare(Empresa e1, Empresa e2) {
                return e1.getNombreCorregido().compareToIgnoreCase(e2.getNombreCorregido());
            }
        });

        return resultados;
    }

    // Obtener empresa por ID
    public Empresa getEmpresaPorId(int id) {
        return todasLasEmpresas.get(id);
    }

    // Verificar si los datos están cargados
    public boolean isDatosCargados() {
        return datosAlmacenados;
    }
}