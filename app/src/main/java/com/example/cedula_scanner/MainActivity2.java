package com.example.cedula_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2  extends AppCompatActivity {

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
                Intent intent = new Intent( MainActivity2.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }



}