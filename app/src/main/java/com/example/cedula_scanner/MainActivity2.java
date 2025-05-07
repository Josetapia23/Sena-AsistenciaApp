package com.example.cedula_scanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Animation animacion1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba);

        ImageView Logo = findViewById(R.id.img);

        Logo.setAnimation(animacion1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Verificar si el usuario ya inició sesión
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

                Log.d("MiApp", "SplashScreen - isLoggedIn: " + isLoggedIn); // Log para depuración

                Intent intent;
                if (isLoggedIn) {
                    // Si ya hay sesión, ir a SeletEventoMain
                    intent = new Intent(MainActivity2.this, SeletEventoMain.class);
                } else {
                    // Si no hay sesión, ir a LoginActivity
                    intent = new Intent(MainActivity2.this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}