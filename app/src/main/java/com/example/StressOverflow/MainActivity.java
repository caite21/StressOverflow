package com.example.StressOverflow;

import androidx.appcompat.app.AppCompatActivity;

//import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth instance = FirebaseAuth.getInstance();
        FirebaseUser user = instance.getCurrentUser();
        if (user != null) {
            Intent i = new Intent(MainActivity.this, ListActivity.class);
            startActivity(i);
        } else {
            Intent i = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(i);
        }
    }
}