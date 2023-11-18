package com.example.StressOverflow.SignIn;
import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.Item.ListActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.StressOverflow.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

/**
 * Prompts the user to fill in username/email and password. User is able to
 * sign up or reset the password. Checks email validity, checks if the username
 * exists in the database, authenticates the user.
 */
public class SignInActivity extends AppCompatActivity  {
    private EditText emailUsernameField;
    private EditText passwordInField;
    private Button signInButton;
    private TextView forgotPassword;
    private TextView signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String newLogin;
    private String newPassword;

    /**
     * Called upon creation of activity. Sets the behavior of buttons and fields.
     * Upon clicking sign-in button, checks if all fields are filled, checks if the username exists,
     * checks if the email and password match, authenticates the user and directs them to
     * ListActivity page.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_page);
        setup();

        this.signUpButton.setOnClickListener((v) -> {
            Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(i);
        });

        this.forgotPassword.setOnClickListener((v) -> {
            Intent i = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(i);
        });

        this.signInButton.setOnClickListener((v) -> {
            if (!getData()) {
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(newLogin).matches()) {
                // if login provided is a username
                DocumentReference userRef = db.collection("users").document(newLogin);
                userRef.get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        auth(doc.get("email").toString(), newLogin, newPassword);
                    } else {
                        shakeError(emailUsernameField, "User doesn't exist");
                    }
                });
            } else {
                // if login provided is an email
                db.collection("users").
                        whereEqualTo("email", newLogin).
                        get().
                        addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                    String username = document.getId();
                                    auth(newLogin, username, newPassword);
                                }
                            }
                });
            }
        });
    }

    /**
     * This tries to authenticates a user using Firestore Authentication
     * @param email
     *      email to be used to authenticate a user
     * @param username
     *      username to be used to authenticate a user
     * @param password
     *      password to be used to authenticate a user
     */
    protected void auth(String email, String username, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SIGNIN STATUS", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent i = new Intent(SignInActivity.this, ListActivity.class);
                            AppGlobals.getInstance().setOwnerName(username);
                            startActivity(i);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGNIN STATUS", "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            shakeError(passwordInField, "Incorrect Password");
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
        this.emailUsernameField = findViewById(R.id.email_username_field);
        this.passwordInField = findViewById(R.id.password_field);
        this.signInButton = findViewById(R.id.sign_in_button);
        this.forgotPassword = findViewById(R.id.forgot_password_text);
        this.signUpButton = findViewById(R.id.sign_up_text);
    }
    /**
     * This retrieves data from all interactive fields and checks data validity
     * @return true if all data is valid, false otherwise
     */
    protected Boolean getData() {
        newLogin = emailUsernameField.getText().toString();
        newPassword = passwordInField.getText().toString();
        boolean valid = true;
        if (newLogin.isEmpty()) {
            shakeError(emailUsernameField, "This field cannot be blank");
            valid = false;
        }
        if (newPassword.isEmpty()) {
            shakeError(passwordInField, "This field cannot be blank");
            valid = false;
        }
        return valid;
    }
}
