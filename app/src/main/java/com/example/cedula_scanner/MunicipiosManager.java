package com.example.cedula_scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MunicipiosManager {
    private static final String TAG = "MunicipiosManager";
    private Context context;
    private List<String> listaMunicipios = new ArrayList<>();
    private Map<String, Integer> mapaMunicipiosIds = new HashMap<>();

    public MunicipiosManager(Context context) {
        this.context = context;
    }

    /**
     * Carga los municipios del Atlántico y los coloca en el spinner proporcionado
     */
    public void cargarMunicipiosEnSpinner(Spinner spinnerMunicipio) {
        // Intentar cargar desde la caché
        if (!cargarMunicipiosDesdeCache(spinnerMunicipio)) {
            // Si falla, cargar la lista por defecto
            cargarMunicipiosPorDefecto(spinnerMunicipio);
        }
    }

    /**
     * Intenta cargar los municipios desde la caché
     */
    private boolean cargarMunicipiosDesdeCache(Spinner spinnerMunicipio) {
        SharedPreferences prefs = context.getSharedPreferences("DatosMunicipios", Context.MODE_PRIVATE);
        String datosJson = prefs.getString("departamentos_json", null);

        if (datosJson != null) {
            try {
                JSONObject jsonObject = new JSONObject(datosJson);
                JSONArray departamentosArray = jsonObject.getJSONArray("departamentos");

                // Buscar el departamento "Atlántico"
                for (int i = 0; i < departamentosArray.length(); i++) {
                    JSONObject departamentoObj = departamentosArray.getJSONObject(i);
                    String nombreDepartamento = decodificarTexto(departamentoObj.getString("nombre"));

                    // Si encontramos Atlántico, cargar sus municipios
                    if (nombreDepartamento.equalsIgnoreCase("Atlántico")) {
                        JSONArray municipiosArray = departamentoObj.getJSONArray("municipios");
                        listaMunicipios.clear();

                        // Añadir opción "Seleccione municipio" al inicio
                        listaMunicipios.add("Seleccione municipio");

                        for (int j = 0; j < municipiosArray.length(); j++) {
                            JSONObject municipioObj = municipiosArray.getJSONObject(j);
                            int idMunicipio = municipioObj.getInt("id");
                            String nombreMunicipio = decodificarTexto(municipioObj.getString("nombre"));

                            // Añadir municipio a la lista
                            listaMunicipios.add(nombreMunicipio);

                            // Guardar ID del municipio
                            mapaMunicipiosIds.put(nombreMunicipio, idMunicipio);
                        }

                        // Actualizar el spinner de municipios
                        actualizarSpinner(spinnerMunicipio);
                        return true;
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al procesar datos de caché: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Carga una lista de municipios del Atlántico por defecto
     */
    private void cargarMunicipiosPorDefecto(Spinner spinnerMunicipio) {
        listaMunicipios.clear();
        listaMunicipios.add("Seleccione municipio");
        listaMunicipios.add("BARRANQUILLA");
        listaMunicipios.add("SOLEDAD");
        listaMunicipios.add("MALAMBO");
        listaMunicipios.add("PUERTO COLOMBIA");
        listaMunicipios.add("GALAPA");
        listaMunicipios.add("BARANOA");
        listaMunicipios.add("SABANAGRANDE");
        listaMunicipios.add("SANTO TOMÁS");
        listaMunicipios.add("PALMAR DE VARELA");
        listaMunicipios.add("POLONUEVO");
        listaMunicipios.add("SABANALARGA");
        listaMunicipios.add("PIOJÓ");
        listaMunicipios.add("JUAN DE ACOSTA");
        listaMunicipios.add("TUBARÁ");
        listaMunicipios.add("USIACURÍ");
        listaMunicipios.add("REPELÓN");
        listaMunicipios.add("LURUACO");
        listaMunicipios.add("MANATÍ");
        listaMunicipios.add("CAMPO DE LA CRUZ");
        listaMunicipios.add("CANDELARIA");
        listaMunicipios.add("SANTA LUCÍA");
        listaMunicipios.add("SUÁN");
        listaMunicipios.add("PONEDERA");
        listaMunicipios.add("OTRO");

        // También asignamos IDs por defecto a los municipios
        for (int i = 1; i < listaMunicipios.size(); i++) {
            mapaMunicipiosIds.put(listaMunicipios.get(i), i);
        }

        actualizarSpinner(spinnerMunicipio);
    }

    /**
     * Actualiza el adaptador del spinner
     */
    private void actualizarSpinner(Spinner spinnerMunicipio) {
        ArrayAdapter<String> adapterMunicipios = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listaMunicipios);
        spinnerMunicipio.setAdapter(adapterMunicipios);
        spinnerMunicipio.setSelection(0);
    }

    /**
     * Obtiene el ID del municipio seleccionado
     */
    public int getIdMunicipio(String nombreMunicipio) {
        Integer id = mapaMunicipiosIds.get(nombreMunicipio);
        return (id != null) ? id : -1;
    }

    /**
     * Método para decodificar texto con caracteres especiales
     */
    private String decodificarTexto(String texto) {
        try {
            // Intenta diferentes métodos de decodificación hasta encontrar el correcto
            return new String(texto.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Error al decodificar texto: " + e.getMessage());
            return texto; // Devuelve el original si falla
        }
    }
}