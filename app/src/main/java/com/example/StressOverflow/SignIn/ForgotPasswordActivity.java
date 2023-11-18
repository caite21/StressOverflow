package com.example.StressOverflow.SignIn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.StressOverflow.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Prompts the user to fill in email. User is able to reset the password or go back
 * to SignInActivity. If the email is valid, sends an email with the link
 * to reset the password. Directs the user to SignInActivity page upon successful completion.
 */
public class ForgotPasswordActivity extends AppCompatActivity {
    private TextView back_button;
    private EditText email_field;
    private Button reset_password_button;
    private FirebaseAuth mAuth;
    private String emailToSend;
    /**
     * Called upon creation of activity. Sets the behavior of buttons and fields.
     * Upon clicking Reset Password button, sends the email from a no-reply address to allow
     * users to reset the password
     */
    // NEEDS REFACTORING (SAGI)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_page);
        setup();

        this.back_button.setOnClickListener((v) -> {
            finish();
        });

        this.reset_password_button.setOnClickListener((v) -> {
            if (getData()) {
                sendReset(emailToSend);
            }
            return;
        });

    }

    /**
     * This checks the provided email and sends reset password link upon success
     * @param email
     *      Email to receive reset password link
     */
    protected void sendReset(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("RESET PASSWORD EMAIL STATUS:", "Email sent.");
                            Intent i = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                            startActivity(i);
                        } else {
                            Log.w("RESET PASSWORD EMAIL STATUS:", "Reset password:failure", task.getException());
                            Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset password email.",
                                    Toast.LENGTH_SHORT).show();
                            shakeError(email_field, "Failed to reset password");
                        }
                    }
                });
    }
    /**
     * This makes the interactive field shake and display an error message
     * @param field interactive field to be shaking
     * @param errorMessage message to be displayed
     */
    protected void shakeError(EditText field, String errorMessage) {
        field.setError(errorMessage);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
        field.startAnimation(animation);
    }
    /**
     * Setup and initialize all variables and interactive elements
     */
    protected void setup() {
        mAuth = FirebaseAuth.getInstance();
        this.back_button = findViewById(R.id.back_button);
        this.email_field = findViewById(R.id.email_field);
        this.reset_password_button = findViewById(R.id.reset_password_button);
    }
    /**
     * This retrieves data from all interactive fields and checks data validity
     * @return true if all data is valid, false otherwise
     */
    protected Boolean getData() {
        emailToSend = email_field.getText().toString();
        if (emailToSend.isEmpty()) {
            shakeError(email_field, "This field cannot be blank");
            return false;
        }
        return true;
    }
}
