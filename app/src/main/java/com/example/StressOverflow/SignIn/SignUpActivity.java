package com.example.StressOverflow.SignIn;
import com.example.StressOverflow.Item.ListActivity;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Prompts the user to fill in username, email and password. User is able to
 * sign up or go back to SignInActivity. Checks username uniqueness, checks if the email is valid,
 * registers the user upon successful completion into the database, encrypts the password
 */
public class SignUpActivity extends AppCompatActivity {
    private TextView back_button;
    private EditText username_field;
    private EditText email_field;
    private EditText password_field;
    private EditText reenter_password_field;
    private Button sign_up_button;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Called upon creation of activity. Sets the behavior of buttons and fields.
     * Upon clicking sign-up button, checks if all fields are filled, checks if the username
     * already exists, checks if the passwords match (password and re-enter password fields),
     * registers the user and directs to the ListActivity page.
     */
    // NEEDS REFACTORING (SAGI)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_page);
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        this.back_button = findViewById(R.id.back_button);
        this.username_field = findViewById(R.id.username_field);
        this.email_field = findViewById(R.id.email_field);
        this.password_field = findViewById(R.id.password_field);
        this.reenter_password_field = findViewById(R.id.reenter_password_field);
        this.sign_up_button = findViewById(R.id.sign_up_button);

        this.back_button.setOnClickListener((v) -> {
            finish();
        });

        this.sign_up_button.setOnClickListener((v) -> {
            String newUsername = username_field.getText().toString();
            String newEmail = email_field.getText().toString();
            String newPassword = password_field.getText().toString();
            String newPasswordReenter = reenter_password_field.getText().toString();
            boolean valid = true;
            if (newUsername.isEmpty()) {
                username_field.setError("This field cannot be blank");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                username_field.startAnimation(animation);
                valid = false;
            }
            if (newEmail.isEmpty()) {
                email_field.setError("This field cannot be blank");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                email_field.startAnimation(animation);
                valid = false;
            }
            if (newPassword.isEmpty()) {
                password_field.setError("This field cannot be blank");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                password_field.startAnimation(animation);
                valid = false;
            }
            if (newPasswordReenter.isEmpty()) {
                reenter_password_field.setError("This field cannot be blank");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                reenter_password_field.startAnimation(animation);
                valid = false;
            }
            if (!newPasswordReenter.equals(newPassword)) {
                reenter_password_field.setError("Password don't match!");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                reenter_password_field.startAnimation(animation);
                password_field.startAnimation(animation);
                valid = false;
            }
            if (!valid) {
                return;
            }
            DocumentReference userRef = db.collection("users").document(newUsername);
            userRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    username_field.setError("Username is already taken");
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                    username_field.startAnimation(animation);
                } else {
                    createUser(newUsername, newEmail, newPassword);
                }
            });
        });

    }

    /**
     * This creates a new user in Firestore authentication storage and
     * application's database
     * @param username
     *      Username of the new user (unique)
     * @param email
     *      Email of the new user
     * @param password
     *      Password of the new user
     */
    protected void createUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Map<String, Object> updateMap = new HashMap();
                            updateMap.put("email", email);
                            db.collection("users").document(username).set(updateMap);
                            Log.d("SIGNUP STATUS", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent i = new Intent(SignUpActivity.this, ListActivity.class);
                            startActivity(i);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGNUP STATUS", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.basics);
                            email_field.startAnimation(animation);
                        }
                    }
                });
    }
}
