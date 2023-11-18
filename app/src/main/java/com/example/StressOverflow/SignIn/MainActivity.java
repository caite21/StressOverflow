package com.example.StressOverflow.SignIn;
import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.Item.ListActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Checks Firebase session to figure out if the user has logged in recently.
 * If not, then directs the user to sign-in page, otherwise open ListActivity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    /**
     * Called upon creation of activity. Directs the user to the appropriate
     * activity.
     */
    // NEEDS REFACTORING (SAGI)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth instance = FirebaseAuth.getInstance();
        FirebaseUser user = instance.getCurrentUser();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();;

        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser updatedUser = mAuth.getCurrentUser();
                    if (updatedUser != null) {
                        db.collection("users").
                                whereEqualTo("email", user.getEmail()).
                                get().
                                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                String username = document.getId();
                                                Intent i = new Intent(MainActivity.this, ListActivity.class);
                                                AppGlobals.getInstance().setOwnerName(username);
                                                startActivity(i);
                                            }
                                        }
                                    }
                                });

                    } else {
                        Intent i = new Intent(MainActivity.this, SignInActivity.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(i);
                }
            });

        } else {
            Intent i = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(i);
        }
    }
}
