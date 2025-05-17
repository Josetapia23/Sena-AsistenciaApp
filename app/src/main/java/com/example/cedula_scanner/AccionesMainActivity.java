package com.example.cedula_scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AccionesMainActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onResume() {
        super.onResume();

        // Verificar si hay encuestas pendientes y hay conexión a internet
        verificarYSincronizarEncuestasPendientes();

        // Invalidar el menú para actualizar el contador de encuestas pendientes
        invalidateOptionsMenu();
    }

    private void verificarYSincronizarEncuestasPendientes() {
        EncuestasPendientesManager encuestasPendientesManager = new EncuestasPendientesManager(this);

        if (encuestasPendientesManager.hayConexionInternet() && encuestasPendientesManager.hayEncuestasPendientes()) {
            int numEncuestas = encuestasPendientesManager.getNumeroEncuestasPendientes();

            // Mostrar diálogo de confirmación
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Encuestas pendientes");
            builder.setMessage("Hay " + numEncuestas + " encuestas pendientes de sincronización. ¿Desea sincronizarlas ahora?");

            builder.setPositiveButton("Sincronizar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sincronizarEncuestasPendientes();
                }
            });

            builder.setNegativeButton("Más tarde", null);
            builder.show();
        }
    }

    private void sincronizarEncuestasPendientes() {
        // Verificar si hay encuestas pendientes
        EncuestasPendientesManager encuestasPendientesManager = new EncuestasPendientesManager(this);

        if (!encuestasPendientesManager.hayEncuestasPendientes()) {
            Toast.makeText(this, "No hay encuestas pendientes para sincronizar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sincronizando encuestas pendientes...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Sincronizar encuestas pendientes
        encuestasPendientesManager.sincronizarEncuestasPendientes(new EncuestasPendientesManager.OnSincronizacionCompletaListener() {
            @Override
            public void onSincronizacionCompleta(boolean exito, String mensaje) {
                progressDialog.dismiss();

                Toast.makeText(AccionesMainActivity.this, mensaje, Toast.LENGTH_LONG).show();

                // Actualizar el menú después de la sincronización
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acciones_main);

        setUpToolbar();
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
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

        // Actualizar visibilidad del ítem de sincronización
        MenuItem syncItem = menu.findItem(R.id.menu_sincronizar);
        EncuestasPendientesManager manager = new EncuestasPendientesManager(this);

        if (manager.hayEncuestasPendientes()) {
            // Hay encuestas pendientes, mostrar el ítem con contador
            syncItem.setVisible(true);
            syncItem.setTitle("Sincronizar Encuestas (" + manager.getNumeroEncuestasPendientes() + ")");
        } else {
            // No hay encuestas pendientes, ocultar el ítem
            syncItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item3:
                Intent acerca = new Intent(this, about.class);
                startActivity(acerca);
                break;
            case R.id.menu_sincronizar:
                // Opción para sincronizar manualmente
                sincronizarEncuestasPendientes();
                break;
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
            case R.id.item4:
                // Iniciar escaneo
                onClick(findViewById(R.id.button));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.PDF_417);
            integrator.setPrompt("Acerca el codigo de barras de la cedula");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setTorchEnabled(false);
            integrator.initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Escaneo exitoso, enviar los datos a MainActivity
                Intent intent = new Intent(this, com.example.cedula_scanner.MainActivity.class);
                intent.putExtra("scan_data", result.getContents());
                startActivity(intent);
            } else {
                // Escaneo cancelado o fallido
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void about(View view) {
        Intent about = new Intent(this, about.class);
        startActivity(about);
    }

    public void CambiarEvent(View view) {
        Intent about = new Intent(this, SeletEventoMain.class);
        startActivity(about);
        finish();
    }
}