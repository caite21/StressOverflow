package com.example.StressOverflow.SignIn;
import com.example.StressOverflow.AppGlobals;
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
    private TextView backButton;
    private EditText usernameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText reenterPasswordField;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String newUsername;
    private String newEmail;
    private String newPassword;
    private String newPasswordReenter;

    /**
     * Called upon creation of activity. Sets the behavior of buttons and fields.
     * Upon clicking sign-up button, checks if all fields are filled, checks if the username
     * already exists, checks if the passwords match (password and re-enter password fields),
     * registers the user and directs to the ListActivity page.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_page);
        setup();
        this.backButton.setOnClickListener((v) -> {
            finish();
        });

        this.signUpButton.setOnClickListener((v) -> {
            if (!getData()) {
                return;
            }
            DocumentReference userRef = db.collection("users").document(newUsername);
            userRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    shakeError(usernameField, "Username is already taken");
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
                            AppGlobals.getInstance().setOwnerName(username);
                            startActivity(i);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGNUP STATUS", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            shakeError(emailField, "Sign-up failed");
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
        db = FirebaseFirestore.getInstance();
        this.backButton = findViewById(R.id.back_button);
        this.usernameField = findViewById(R.id.username_field);
        this.emailField = findViewById(R.id.email_field);
        this.passwordField = findViewById(R.id.password_field);
        this.reenterPasswordField = findViewById(R.id.reenter_password_field);
        this.signUpButton = findViewById(R.id.sign_up_button);
    }

    /**
     * This retrieves data from all interactive fields and checks data validity
     * @return true if all data is valid, false otherwise
     */
    protected Boolean getData() {
        newUsername = usernameField.getText().toString();
        newEmail = emailField.getText().toString();
        newPassword = passwordField.getText().toString();
        newPasswordReenter = reenterPasswordField.getText().toString();
        boolean valid = true;
        if (newUsername.isEmpty()) {
            shakeError(usernameField, "This field cannot be blank");
            valid = false;
        }
        if (newEmail.isEmpty()) {
            shakeError(emailField, "This field cannot be blank");
            valid = false;
        }
        if (newPassword.isEmpty()) {
            shakeError(passwordField, "This field cannot be blank");
            valid = false;
        }
        if (newPasswordReenter.isEmpty()) {
            shakeError(reenterPasswordField, "This field cannot be blank");
            valid = false;
        }
        if (!newPasswordReenter.equals(newPassword)) {
            shakeError(reenterPasswordField, "Passwords don't match!");
            shakeError(passwordField, "Passwords don't match!");
            valid = false;
        }
        return valid;
    }
}
