package com.example.StressOverflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity  {
    private EditText email_username_field;
    private EditText password_in_field;
    private Button sign_in_button;
    private TextView forgot_password;
    private TextView sign_up_button;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_page);
        mAuth = FirebaseAuth.getInstance();

        this.email_username_field = findViewById(R.id.email_username_field);
        this.password_in_field = findViewById(R.id.password_field);
        this.sign_in_button = findViewById(R.id.sign_in_button);
        this.forgot_password = findViewById(R.id.forgot_password_text);
        this.sign_up_button = findViewById(R.id.sign_up_text);

        this.sign_up_button.setOnClickListener((v) -> {
            Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(i);
        });

        this.forgot_password.setOnClickListener((v) -> {
            Intent i = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(i);
        });

        this.sign_in_button.setOnClickListener((v) -> {
            String newUsername = email_username_field.getText().toString();
            String newPassword = password_in_field.getText().toString();
            boolean valid = true;
            if (newUsername.isEmpty()) {
                email_username_field.setError("This field cannot be blank");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                email_username_field.startAnimation(animation);
                valid = false;
            }
            if (newPassword.isEmpty()) {
                password_in_field.setError("This field cannot be blank");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                password_in_field.startAnimation(animation);
                valid = false;
            }
            if (!valid) {
                return;
            }

            mAuth.signInWithEmailAndPassword(newUsername, newPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("SIGNIN STATUS", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent i = new Intent(SignInActivity.this, ListActivity.class);
                                startActivity(i);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("SIGNIN STATUS", "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                                password_in_field.startAnimation(animation);
                            }
                        }
                    });
        });

    }
}
