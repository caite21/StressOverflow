package com.example.StressOverflow;

import androidx.appcompat.app.AppCompatActivity;

//import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button itemListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.loginButton = findViewById(R.id.big__fat__login__buton);

        this.loginButton.setOnClickListener((v) -> {
            Intent i = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(i);
        });

    }
}