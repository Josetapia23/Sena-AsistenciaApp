package com.example.cedula_scanner;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;

    // Referencias a los campos de la UI
    private EditText tvDocumentID, tvNombres, tvApellidos, tvGenero, tvFechaNacimiento;
    private EditText tvTipoSangre, tvEdad, tvTipoDocumento, tvNacionalidad;
    private EditText editCorreo, tvTelefono, tvDireccion;

    private Toolbar toolbar;
    private Button btnRegistrar;
    private String edad;

    // Variables para departamentos y municipios
    private Spinner spinnerDepartamento, spinnerMunicipio;
    private List<String> listaDepartamentos = new ArrayList<>();
    private Map<String, List<String>> mapaMunicipiosPorDepartamento = new HashMap<>();
    private Map<String, Integer> mapaMunicipiosIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar las referencias a la UI
        tvDocumentID = findViewById(R.id.tvDocumentID);
        tvNombres = findViewById(R.id.tvNombres);
        tvApellidos = findViewById(R.id.tvApellidos);
        tvGenero = findViewById(R.id.tvGenero);
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento);
        tvTipoSangre = findViewById(R.id.tvTipoSangre);
        tvEdad = findViewById(R.id.tvEdad);
        tvTipoDocumento = findViewById(R.id.tvTipoDocumento);
        tvNacionalidad = findViewById(R.id.tvNacionalidad);
        editCorreo = findViewById(R.id.editCorreo);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvDireccion = findViewById(R.id.tvDireccion);


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

        // Procesar datos de escaneo si vienen como extras
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("scan_data")) {
            String scanData = extras.getString("scan_data");
            parseDataCode(scanData);
        }

        // Inicializar los spinners de departamento y municipio
        inicializarSpinnersDepartamentoMunicipio();

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Añadir log para depuración
        Log.d("MainActivity", "onOptionsItemSelected: item id = " + id);

        if (id == R.id.itemCerrarSesion) {
            Log.d("MainActivity", "Cerrando sesión");
            // Cerrar sesión
            SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
            editor.clear();
            editor.commit();

            // Redirigir al login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.item3) {
            Log.d("MainActivity", "Abriendo acerca de");
            // Código para la opción "Acerca de"
            Intent intent = new Intent(this, about.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.item4) {
            Log.d("MainActivity", "Iniciando escaneo");
            try {
                // Iniciar el escaneo directamente
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.PDF_417);
                integrator.setPrompt("Acerca el código de barras de la cédula");
                integrator.setOrientationLocked(false);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.setTorchEnabled(false);
                integrator.initiateScan();
                Log.d("MainActivity", "Escaneo iniciado correctamente");
            } catch (Exception e) {
                Log.e("MainActivity", "Error al iniciar escaneo: " + e.getMessage());
                Toast.makeText(this, "Error al iniciar el escaneo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        Log.d("MainActivity", "No se manejó ninguna opción del menú");
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            // Iniciar el escaneo de la cédula
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.PDF_417);
            integrator.setPrompt("Acerca el código de barras de la cédula");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setTorchEnabled(false);
            integrator.initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result.getContents() == null) {
            // El escaneo fue cancelado o falló
            Intent originalIntent = result.getOriginalIntent();
            if (originalIntent == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_LONG).show();
            }
        } else {
            // El escaneo fue exitoso
            Log.d("MainActivity", "Escaneado: " + result.getContents());
            parseDataCode(result.getContents());
        }
    }

    private void parseDataCode(String barcode) {
        // Imprimir el código de barras completo para análisis
        Log.d("BARCODE_RAW", "Código de barras completo: " + barcode);

        if (barcode != null) {
            String primerApellido = "", segundoApellido = "", primerNombre = "", segundoNombre = "",
                    cedula = "", rh = "", fechaNacimiento = "", sexo = "", edad = "";
            String tipoDocumento = "CC"; // Valor predeterminado

            // Limpiar el código de barras y dividirlo en partes
            String alphaAndDigits = barcode.replaceAll("[^\\p{Alpha}\\p{Digit}\\+\\_\\-]+", " ");
            String[] splitStr = alphaAndDigits.split("\\s+");

            // Imprimir todas las partes del código para análisis
            for (int i = 0; i < splitStr.length; i++) {
                Log.d("BARCODE_PART", "Parte " + i + ": " + splitStr[i]);
            }

            // Detectar si es una TI o una CC
            if (barcode.startsWith("I")) {
                tipoDocumento = "TI"; // Es una Tarjeta de Identidad
                Log.d("DOCUMENTO_TIPO", "Detectado tipo: TI");
            } else {
                tipoDocumento = "CC"; // Es una Cédula de Ciudadanía
                Log.d("DOCUMENTO_TIPO", "Detectado tipo: CC");
            }

            // Procesar el código de barras según su formato
            try {
                if (!alphaAndDigits.contains("PubDSK")) {
                    // Formato antiguo de cédula
                    int corrimiento = 0;

                    Pattern pat = Pattern.compile("[A-Z]");
                    Matcher match = pat.matcher(splitStr[2 + corrimiento]);
                    int lastCapitalIndex = -1;
                    if (match.find()) {
                        lastCapitalIndex = match.start();
                    }

                    // Extraer número de documento según el tipo
                    if (tipoDocumento.equals("CC")) {
                        // Para cédulas (10 dígitos)
                        cedula = splitStr[2 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
                    } else {
                        // Para TI (longitud variable, buscar el número en el código completo)
                        cedula = extraerNumeroTI(barcode, splitStr);
                    }

                    primerApellido = splitStr[2 + corrimiento].substring(lastCapitalIndex);
                    segundoApellido = splitStr[3 + corrimiento];
                    primerNombre = splitStr[4 + corrimiento];

                    if (Character.isDigit(splitStr[5 + corrimiento].charAt(0))) {
                        corrimiento--;
                    } else {
                        segundoNombre = splitStr[5 + corrimiento];
                    }

                    sexo = splitStr[6 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                    rh = obtenerTipoSangre(barcode, splitStr, 6 + corrimiento);
                    fechaNacimiento = splitStr[6 + corrimiento].substring(2, 10);
                } else {
                    // Formato nuevo de cédula (con PubDSK)
                    int corrimiento = 0;
                    Pattern pat = Pattern.compile("[A-Z]");
                    if (splitStr[2 + corrimiento].length() > 7) {
                        corrimiento--;
                    }

                    // Para documentos PubDSK
                    if (tipoDocumento.equals("TI")) {
                        // Extraer el número de la TI
                        cedula = extraerNumeroTI(barcode, splitStr);

                        // Extraer el resto de la información
                        try {
                            // Para TI, los apellidos y nombres pueden estar en posiciones diferentes
                            primerApellido = splitStr[3];
                            segundoApellido = splitStr[4];
                            primerNombre = splitStr[5];
                            segundoNombre = splitStr[6];

                            // La información de sexo, fecha nacimiento y RH estará en parte 7
                            String infoParte = splitStr[7];
                            if (infoParte.contains("M")) {
                                sexo = "Masculino";
                            } else if (infoParte.contains("F")) {
                                sexo = "Femenino";
                            }

                            // Extraer fecha de nacimiento (si sigue el mismo patrón)
                            // Formato nuevo PubDSK: YYYYMMDD para TI
                            if (infoParte.length() >= 10) {
                                fechaNacimiento = infoParte.substring(2, 10);
                            }

                            // Tipo de sangre
                            rh = obtenerTipoSangre(barcode, splitStr, 7);
                        } catch (Exception e) {
                            Log.e("TI_PARSE_ERROR", "Error procesando datos de TI: " + e.getMessage());
                        }
                    } else {
                        // Código existente para CC
                        Matcher match = pat.matcher(splitStr[3 + corrimiento]);
                        int lastCapitalIndex = -1;
                        if (match.find()) {
                            lastCapitalIndex = match.start();
                        }

                        cedula = splitStr[3 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
                        primerApellido = splitStr[3 + corrimiento].substring(lastCapitalIndex);
                        segundoApellido = splitStr[4 + corrimiento];

                        if (splitStr[5 + corrimiento].startsWith("0")) { // UN NOMBRE UN APELLIDO
                            segundoApellido = " ";
                            primerNombre = splitStr[4 + corrimiento];
                            sexo = splitStr[5 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                            rh = obtenerTipoSangre(barcode, splitStr, 5 + corrimiento);
                            fechaNacimiento = splitStr[5 + corrimiento].substring(2, 10);
                        } else if (splitStr[6 + corrimiento].startsWith("0")) { // DOS APELLIDOS UN NOMBRE
                            primerNombre = splitStr[5 + corrimiento];
                            segundoNombre = " ";
                            sexo = splitStr[6 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                            rh = obtenerTipoSangre(barcode, splitStr, 6 + corrimiento);
                            fechaNacimiento = splitStr[6 + corrimiento].substring(2, 10);
                        } else { // DOS APELLIDOS DOS NOMBRES
                            primerNombre = splitStr[5 + corrimiento];
                            segundoNombre = splitStr[6 + corrimiento];
                            sexo = splitStr[7 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                            rh = obtenerTipoSangre(barcode, splitStr, 7 + corrimiento);
                            fechaNacimiento = splitStr[7 + corrimiento].substring(2, 10);
                        }
                    }
                }

                // Calcular edad a partir de la fecha de nacimiento
                if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                    try {
                        String fechaFormateada;

                        if (!alphaAndDigits.contains("PubDSK")) {
                            // Formato antiguo: DDMMYY -> yyyy-MM-dd
                            fechaFormateada = "20" + fechaNacimiento.substring(4, 6) + "-" +
                                    fechaNacimiento.substring(2, 4) + "-" +
                                    fechaNacimiento.substring(0, 2);
                        } else {
                            // Formato nuevo PubDSK: YYYYMMDD -> yyyy-MM-dd
                            fechaFormateada = fechaNacimiento.substring(0, 4) + "-" +
                                    fechaNacimiento.substring(4, 6) + "-" +
                                    fechaNacimiento.substring(6, 8);
                        }

                        Log.d("CEDULA_DATA", "Fecha formateada para cálculo: " + fechaFormateada);

                        // Calcular edad con la fecha correctamente formateada
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date fechaNac = sdf.parse(fechaFormateada);
                        Calendar dob = Calendar.getInstance();
                        Calendar today = Calendar.getInstance();
                        dob.setTime(fechaNac);

                        int edadCalculada = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                            edadCalculada--;
                        }
                        edad = String.valueOf(edadCalculada);
                        this.edad = edad;
                        Log.d("CEDULA_DATA", "Edad calculada: " + edad);
                    } catch (Exception e) {
                        Log.e("CEDULA_DATA", "Error calculando edad: " + e.getMessage());
                    }
                }

                // Imprimir todos los datos extraídos para depuración
                Log.d("CEDULA_DATA", "Cédula: " + cedula);
                Log.d("CEDULA_DATA", "Nombres: " + primerNombre + " " + segundoNombre);
                Log.d("CEDULA_DATA", "Apellidos: " + primerApellido + " " + segundoApellido);
                Log.d("CEDULA_DATA", "Sexo: " + sexo);
                Log.d("CEDULA_DATA", "Fecha Nacimiento: " + fechaNacimiento);
                Log.d("CEDULA_DATA", "RH: " + rh);
                Log.d("CEDULA_DATA", "Edad: " + edad);
                Log.d("CEDULA_DATA", "Tipo Documento: " + tipoDocumento);

                // Formatear fecha para mostrar
                String fechaMostrar = "";
                String fechaParaServidor = fechaNacimiento; // Guardar el valor original para cálculos

                if (fechaNacimiento != null && fechaNacimiento.length() >= 8) {
                    try {
                        String dia, mes, anio;

                        if (!alphaAndDigits.contains("PubDSK")) {
                            // Formato antiguo: DDMMYY
                            dia = fechaNacimiento.substring(0, 2);
                            mes = fechaNacimiento.substring(2, 4);
                            anio = "20" + fechaNacimiento.substring(4, 6); // Asumiendo años 2000+
                        } else {
                            // Formato nuevo PubDSK: YYYYMMDD
                            anio = fechaNacimiento.substring(0, 4);
                            mes = fechaNacimiento.substring(4, 6);
                            dia = fechaNacimiento.substring(6, 8);
                        }

                        // Formato para mostrar al usuario (dd/mm/yyyy)
                        fechaMostrar = dia + "/" + mes + "/" + anio;

                        // Para enviar al servidor
                        fechaParaServidor = fechaMostrar;

                        Log.d("FECHA_DEBUG", "Fecha original: " + fechaNacimiento);
                        Log.d("FECHA_DEBUG", "Fecha formateada: " + fechaMostrar);
                    } catch (Exception e) {
                        Log.e("PARSE_ERROR", "Error formateando fecha: " + e.getMessage());
                        fechaMostrar = fechaNacimiento; // En caso de error, usar el original
                    }
                }

                // Nombres y apellidos completos
                String nombresCompletos = primerNombre;
                if (segundoNombre != null && !segundoNombre.trim().isEmpty() && !segundoNombre.equals(" ")) {
                    nombresCompletos += " " + segundoNombre;
                }

                String apellidosCompletos = primerApellido;
                if (segundoApellido != null && !segundoApellido.trim().isEmpty() && !segundoApellido.equals(" ")) {
                    apellidosCompletos += " " + segundoApellido;
                }

                // Actualizar la UI con los datos extraídos
                tvDocumentID.setText(cedula);
                tvNombres.setText(nombresCompletos);
                tvApellidos.setText(apellidosCompletos);
                tvGenero.setText(sexo);
                tvFechaNacimiento.setText(fechaMostrar);
                tvTipoSangre.setText(rh);
                tvEdad.setText(edad);
                tvTipoDocumento.setText(tipoDocumento); // Ahora usamos el tipo detectado
                tvNacionalidad.setText("COLOMBIANA"); // Por defecto

            } catch (Exception e) {
                Log.e("PARSE_ERROR", "Error procesando código de barras: " + e.getMessage());
                Toast.makeText(this, "Error al procesar la cédula: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("TAG", "No se capturó ningún código de barras");
        }
    }

    // Método para extraer el número de TI
    private String extraerNumeroTI(String barcode, String[] splitStr) {
        // Para TI el formato puede variar, primero buscamos en la parte 2
        String cedula = "";

        try {
            // Buscar un patrón de dígitos largos en la parte 2 (común en TI)
            String parte2 = splitStr[2];
            Pattern patronNumerico = Pattern.compile("\\d{8,}");
            Matcher matcher = patronNumerico.matcher(parte2);

            if (matcher.find()) {
                // Extraer el número de la TI
                cedula = matcher.group();
                Log.d("TI_NUMBER", "Número TI extraído de parte 2: " + cedula);
                return cedula;
            }

            // Si no se encuentra en la parte 2, buscamos en todo el código de barras
            matcher = patronNumerico.matcher(barcode);
            while (matcher.find()) {
                String posibleNumero = matcher.group();
                if (posibleNumero.length() >= 10) {
                    cedula = posibleNumero;
                    Log.d("TI_NUMBER", "Número TI extraído del código completo: " + cedula);
                    return cedula;
                }
            }

            // Si aún no encontramos, extraemos de la parte 2 con una estrategia diferente
            // Buscar cualquier secuencia de dígitos
            for (int i = 2; i < Math.min(5, splitStr.length); i++) {
                parte2 = splitStr[i];
                for (int j = 0; j < parte2.length(); j++) {
                    if (Character.isDigit(parte2.charAt(j))) {
                        int start = j;
                        while (j < parte2.length() && Character.isDigit(parte2.charAt(j))) {
                            j++;
                        }
                        String numero = parte2.substring(start, j);
                        if (numero.length() >= 8) {
                            cedula = numero;
                            Log.d("TI_NUMBER", "Número TI extraído con estrategia alternativa: " + cedula);
                            return cedula;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("TI_EXTRACT_ERROR", "Error al extraer número de TI: " + e.getMessage());
        }

        // Si todo falla, extraemos los primeros 10 dígitos encontrados en el código
        StringBuilder digits = new StringBuilder();
        for (char c : barcode.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
                if (digits.length() >= 10) break;
            }
        }

        if (digits.length() > 0) {
            cedula = digits.toString();
            Log.d("TI_NUMBER", "Número TI extraído como último recurso: " + cedula);
        } else {
            cedula = "0000000000"; // Valor por defecto si no se puede extraer
            Log.d("TI_NUMBER", "No se pudo extraer número de TI, usando valor por defecto");
        }

        return cedula;
    }

    // Método auxiliar para extraer el tipo de sangre
    private String obtenerTipoSangre(String barcode, String[] splitStr, int index) {
        // Verificar si el código de barras completo contiene patrones específicos de tipo de sangre
        if (barcode.contains("B-")) return "B-";
        if (barcode.contains("B+")) return "B+";
        if (barcode.contains("O-")) return "O-";
        if (barcode.contains("O+")) return "O+";
        if (barcode.contains("A-")) return "A-";
        if (barcode.contains("A+")) return "A+";
        if (barcode.contains("AB-")) return "AB-";
        if (barcode.contains("AB+")) return "AB+";

        // Si no se encuentra un patrón directo, tratar de construirlo
        String parte = splitStr[index];
        // Buscar la última letra que podría ser un tipo de sangre (O, A, B)
        char letraSangre = 'O'; // Por defecto

        // Buscar desde el final hacia atrás para encontrar O, A o B
        for (int i = parte.length() - 1; i >= 0; i--) {
            char c = parte.charAt(i);
            if (c == 'O' || c == 'A' || c == 'B') {
                letraSangre = c;
                break;
            }
        }

        // Determinar el signo (+ o -) basado en varias estrategias
        String signo = "+"; // Por defecto es positivo

        // Estrategia 1: Buscar literal "B-" o similar en el código completo
        if ((letraSangre == 'B' && barcode.contains("B-")) ||
                (letraSangre == 'O' && barcode.contains("O-")) ||
                (letraSangre == 'A' && barcode.contains("A-"))) {
            signo = "-";
        }

        // Estrategia 2: Buscar el signo en el código de barras cerca de la letra
        int posLetra = barcode.indexOf(letraSangre);
        if (posLetra >= 0 && posLetra + 1 < barcode.length()) {
            char nextChar = barcode.charAt(posLetra + 1);
            if (nextChar == '-' || nextChar == '+') {
                signo = String.valueOf(nextChar);
            }
        }

        // Log para ayudar a diagnosticar
        Log.d("TIPO_SANGRE", "Letra encontrada: " + letraSangre + ", Signo determinado: " + signo);

        return String.valueOf(letraSangre) + signo;
    }

    // Método auxiliar para verificar si un carácter es una letra de tipo de sangre
    private boolean esLetraTipoSangre(char c) {
        return c == 'O' || c == 'A' || c == 'B';
    }
    // Método para inicializar los spinners de departamento y municipio
    private void inicializarSpinnersDepartamentoMunicipio() {
        // Inicializar spinners
        spinnerDepartamento = findViewById(R.id.spinnerDepartamento);
        spinnerMunicipio = findViewById(R.id.spinnerMunicipio);

        // Configurar adaptadores iniciales vacíos
        ArrayAdapter<String> adapterDepartamentos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerDepartamento.setAdapter(adapterDepartamentos);

        ArrayAdapter<String> adapterMunicipios = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerMunicipio.setAdapter(adapterMunicipios);

        // Cargar datos geográficos
        cargarDatosGeograficosDesdeCache();

        // Listener para el cambio de departamento
        spinnerDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listaDepartamentos.size()) {
                    String departamentoSeleccionado = listaDepartamentos.get(position);
                    actualizarSpinnerMunicipios(departamentoSeleccionado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    // Método para cargar datos geográficos desde caché
    private void cargarDatosGeograficosDesdeCache() {
        SharedPreferences prefs = getSharedPreferences("DatosMunicipios", MODE_PRIVATE);
        String datosJson = prefs.getString("departamentos_json", null);

        if (datosJson != null) {
            try {
                // Procesar los datos guardados en caché
                Log.d("MainActivity", "Usando datos geográficos de caché");
                procesarDatosDepartamentosMunicipios(new JSONObject(datosJson));
            } catch (JSONException e) {
                Log.e("MainActivity", "Error al procesar datos de caché: " + e.getMessage());
                // Si hay un error en los datos de caché, mostrar mensaje
                Toast.makeText(MainActivity.this,
                        "Error al cargar datos geográficos. Por favor regrese y seleccione un evento nuevamente.",
                        Toast.LENGTH_LONG).show();

                // Dejamos los spinners vacíos
                ArrayAdapter<String> adapterDepartamentos = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        new ArrayList<>());
                spinnerDepartamento.setAdapter(adapterDepartamentos);
            }
        } else {
            // No hay datos en caché, mostrar mensaje
            Log.e("MainActivity", "No se encontraron datos geográficos en caché");
            Toast.makeText(MainActivity.this,
                    "Datos geográficos no disponibles. Por favor regrese y seleccione un evento nuevamente.",
                    Toast.LENGTH_LONG).show();

            // Dejamos los spinners vacíos
            ArrayAdapter<String> adapterDepartamentos = new ArrayAdapter<>(
                    MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new ArrayList<>());
            spinnerDepartamento.setAdapter(adapterDepartamentos);
        }
    }

    // Método para procesar los datos de departamentos y municipios desde JSON
    private void procesarDatosDepartamentosMunicipios(JSONObject jsonObject) {
        try {
            // Limpiar datos existentes
            listaDepartamentos.clear();
            mapaMunicipiosPorDepartamento.clear();
            mapaMunicipiosIds.clear();

            // Procesar el array de departamentos
            JSONArray departamentosArray = jsonObject.getJSONArray("departamentos");

            for (int i = 0; i < departamentosArray.length(); i++) {
                JSONObject departamentoObj = departamentosArray.getJSONObject(i);
                String nombreDepartamento = departamentoObj.getString("nombre");

                // Añadir a la lista de departamentos
                listaDepartamentos.add(nombreDepartamento);

                // Procesar los municipios de este departamento
                JSONArray municipiosArray = departamentoObj.getJSONArray("municipios");
                List<String> municipiosDelDepartamento = new ArrayList<>();

                for (int j = 0; j < municipiosArray.length(); j++) {
                    JSONObject municipioObj = municipiosArray.getJSONObject(j);
                    int idMunicipio = municipioObj.getInt("id");
                    String nombreMunicipio = municipioObj.getString("nombre");

                    // Añadir a la lista de municipios de este departamento
                    municipiosDelDepartamento.add(nombreMunicipio);

                    // Guardar la relación nombre-id del municipio
                    mapaMunicipiosIds.put(nombreMunicipio, idMunicipio);
                }

                // Guardar la lista de municipios para este departamento
                mapaMunicipiosPorDepartamento.put(nombreDepartamento, municipiosDelDepartamento);
            }

            // Actualizar el spinner de departamentos
            ArrayAdapter<String> adapterDepartamentos = new ArrayAdapter<>(
                    MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    listaDepartamentos);
            spinnerDepartamento.setAdapter(adapterDepartamentos);

            // Si hay departamentos, seleccionar el primero para llenar el spinner de municipios
            if (!listaDepartamentos.isEmpty()) {
                spinnerDepartamento.setSelection(0);
                actualizarSpinnerMunicipios(listaDepartamentos.get(0));
            }

        } catch (JSONException e) {
            Log.e("MainActivity", "Error al procesar JSON de departamentos: " + e.getMessage());
            Toast.makeText(MainActivity.this,
                    "Error al procesar datos geográficos",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Método para actualizar el spinner de municipios según el departamento seleccionado
    private void actualizarSpinnerMunicipios(String departamento) {
        List<String> municipiosDelDepartamento = mapaMunicipiosPorDepartamento.get(departamento);

        if (municipiosDelDepartamento != null && !municipiosDelDepartamento.isEmpty()) {
            ArrayAdapter<String> adapterMunicipios = new ArrayAdapter<>(
                    MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    municipiosDelDepartamento);
            spinnerMunicipio.setAdapter(adapterMunicipios);
        } else {
            // Si no hay municipios, mostrar lista vacía
            spinnerMunicipio.setAdapter(new ArrayAdapter<>(
                    MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new ArrayList<>()));
        }
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

        // Obtener valores de la UI
        final String tipoDocumento = tvTipoDocumento.getText().toString().trim();
        final String identificacion = tvDocumentID.getText().toString().trim();
        final String nombres = tvNombres.getText().toString().trim();
        final String apellidos = tvApellidos.getText().toString().trim();
        final String fechaNacimiento = tvFechaNacimiento.getText().toString().trim();
        final String edad = tvEdad.getText().toString().trim();
        final String genero = tvGenero.getText().toString().trim();
        final String tipoSangre = tvTipoSangre.getText().toString().trim();
        final String nacionalidad = tvNacionalidad.getText().toString().trim();
        final String correo = editCorreo.getText().toString().trim();
        final String telefono = tvTelefono.getText().toString().trim();
        final String direccion = tvDireccion.getText().toString().trim();

        // Obtener departamento y municipio seleccionados - declarados como final
        final String departamento;
        final String municipio;
        final int idMunicipio;

        if (spinnerDepartamento.getSelectedItemPosition() >= 0 &&
                spinnerDepartamento.getSelectedItemPosition() < listaDepartamentos.size()) {
            departamento = listaDepartamentos.get(spinnerDepartamento.getSelectedItemPosition());

            if (spinnerMunicipio.getSelectedItem() != null) {
                municipio = spinnerMunicipio.getSelectedItem().toString();
                // Obtener el ID del municipio seleccionado
                if (mapaMunicipiosIds.containsKey(municipio)) {
                    idMunicipio = mapaMunicipiosIds.get(municipio);
                } else {
                    idMunicipio = 0;
                }
            } else {
                municipio = "";
                idMunicipio = 0;
            }
        } else {
            departamento = "";
            municipio = "";
            idMunicipio = 0;
        }

        // Validaciones
        if (identificacion.isEmpty()) {
            Toast.makeText(this, "Por favor escanee una cédula primero", Toast.LENGTH_SHORT).show();
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

        // Validar que se haya seleccionado departamento y municipio
        if (departamento.isEmpty() || municipio.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione departamento y municipio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando asistencia...");
        progressDialog.show();

        // Enviar datos al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.68.162/AsistenciaApi/insertarCedulaSc.php";

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
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(success ? "Éxito" : "Aviso");
                            builder.setMessage(message);

                            if (success || jsonResponse.optBoolean("already_registered", false)) {
                                // En caso de éxito o usuario ya registrado, ir a la encuesta
                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Pasar a la pantalla de encuestas en lugar de AccionesMainActivity
                                        Intent intent = new Intent(MainActivity.this, EncuestasPosRegActivity.class);
                                        intent.putExtra("nombre_completo", nombres + " " + apellidos);
                                        intent.putExtra("numero_cedula", identificacion);
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
                            Toast.makeText(MainActivity.this, "Error al procesar la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

                // Añadir los nuevos parámetros de ubicación
                params.put("departamento", departamento);
                params.put("municipio", municipio);

                return params;
            }
        };

        queue.add(stringRequest);
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public void about(View view) {
        Intent about = new Intent(this, about.class);
        startActivity(about);
    }

    @Override
    public void onBackPressed() {
        // Regresar a la pantalla de selección de eventos
        Intent intent = new Intent(this, SeletEventoMain.class);
        startActivity(intent);
        finish();
    }
}