package com.example.StressOverflow;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.loginButton = findViewById(R.id.big__fat__login__buton);

        this.loginButton.setOnClickListener((v) -> {
            String uname = "demo@test.com";
            Intent i = new Intent(MainActivity.this, ListActivity.class);
            i.putExtra("login", uname);
            startActivity(i);
        });
    }
}