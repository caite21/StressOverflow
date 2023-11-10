package com.example.StressOverflow;

import androidx.appcompat.app.AppCompatActivity;

//import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        // If you signed in and want to sign out (log out button not yet implemented), then
        // wipe data from your emulator and launch the app again
//         if (false) {         // UNCOMMENT TO DEBUG, COMMENT LINE BELOW
        if (user != null) {
            Intent i = new Intent(MainActivity.this, ListActivity.class);
            startActivity(i);
        } else {
            Intent i = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(i);
        }
    }
}