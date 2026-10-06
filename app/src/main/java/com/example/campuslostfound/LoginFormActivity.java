package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFormActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_form);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        emailEditText =
                findViewById(R.id.emailEditText);

        passwordEditText =
                findViewById(R.id.passwordEditText);

        ImageButton backButton =
                findViewById(R.id.backButton);

        TextView forgotPasswordText =
                findViewById(R.id.forgotPasswordText);

        Button loginSubmitButton =
                findViewById(R.id.loginSubmitButton);

        backButton.setOnClickListener(
                v -> finish()
        );

        loginSubmitButton.setOnClickListener(
                v -> login()
        );

        forgotPasswordText.setOnClickListener(
                v -> openForgotPassword()
        );
    }

    private void login() {

        String email =
                emailEditText
                        .getText()
                        .toString()
                        .trim();

        String password =
                passwordEditText
                        .getText()
                        .toString()
                        .trim();

        if (email.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your email.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your password.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Show login message
        Toast.makeText(
                this,
                "Logging in...",
                Toast.LENGTH_SHORT
        ).show();

        /*
         * Firebase checks the email and password.
         */
        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser =
                                firebaseAuth.getCurrentUser();

                        if (firebaseUser == null) {

                            Toast.makeText(
                                    this,
                                    "Login failed. User information could not be loaded.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        String uid =
                                firebaseUser.getUid();

                        /*
                         * Get the user's profile from Firestore.
                         */
                        firestore
                                .collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(
                                        documentSnapshot ->
                                                loadUserProfile(
                                                        documentSnapshot,
                                                        email
                                                )
                                )
                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            this,
                                            "Login successful, but user profile could not be loaded.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        String errorMessage =
                                "Incorrect email or password.";

                        if (task.getException() != null) {

                            String firebaseMessage =
                                    task.getException()
                                            .getMessage();

                            if (firebaseMessage != null &&
                                    !firebaseMessage.isEmpty()) {

                                errorMessage =
                                        firebaseMessage;
                            }
                        }

                        Toast.makeText(
                                this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void loadUserProfile(
            DocumentSnapshot documentSnapshot,
            String email) {

        /*
         * Default values.
         */
        String name =
                "Student";

        String studentNumber =
                "";

        String phone =
                "";

        String role =
                "user";

        /*
         * Read the user's information from Firestore.
         */
        if (documentSnapshot.exists()) {

            String firestoreName =
                    documentSnapshot.getString("name");

            String firestoreStudentNumber =
                    documentSnapshot.getString(
                            "studentNumber"
                    );

            String firestorePhone =
                    documentSnapshot.getString("phone");

            String firestoreRole =
                    documentSnapshot.getString("role");

            if (firestoreName != null) {
                name = firestoreName;
            }

            if (firestoreStudentNumber != null) {
                studentNumber =
                        firestoreStudentNumber;
            }

            if (firestorePhone != null) {
                phone =
                        firestorePhone;
            }

            if (firestoreRole != null) {
                role =
                        firestoreRole;
            }
        }

        /*
         * Save the current user's information locally.
         *
         * Other existing parts of the app can still use
         * CampusPrefs while we migrate them to Firebase.
         */
        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        boolean isAdmin =
                role.equalsIgnoreCase("admin");

        prefs.edit()
                .putString(
                        "email",
                        email
                )
                .putString(
                        "studentName",
                        name
                )
                .putString(
                        "studentNumber",
                        studentNumber
                )
                .putString(
                        "phone",
                        phone
                )
                .putString(
                        "role",
                        role
                )
                .putBoolean(
                        "isAdmin",
                        isAdmin
                )
                .putBoolean(
                        "isLoggedIn",
                        true
                )
                .apply();

        Toast.makeText(
                this,
                "Login successful!",
                Toast.LENGTH_SHORT
        ).show();

        /*
         * Open HomeActivity.
         */
        Intent intent =
                new Intent(
                        LoginFormActivity.this,
                        HomeActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    private void openForgotPassword() {

        Intent intent =
                new Intent(
                        LoginFormActivity.this,
                        ForgotPasswordActivity.class
                );

        startActivity(intent);
    }
}