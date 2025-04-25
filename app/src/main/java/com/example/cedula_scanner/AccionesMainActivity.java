package com.example.cedula_scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
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
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item3:
                Intent acerca = new Intent(this, about.class);
                startActivity(acerca);
                break;
            default:
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