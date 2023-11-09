package com.example.StressOverflow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextView back_button;
    private EditText email_field;
    private Button reset_password_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_page);

        this.back_button = findViewById(R.id.back_button);
        this.email_field = findViewById(R.id.email_field);
        this.reset_password_button = findViewById(R.id.reset_password_button);
    }
}
