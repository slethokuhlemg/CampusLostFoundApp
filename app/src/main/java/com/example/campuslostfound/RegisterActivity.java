package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText studentNumberEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        nameEditText =
                findViewById(R.id.nameEditText);

        studentNumberEditText =
                findViewById(R.id.studentNumberEditText);

        emailEditText =
                findViewById(R.id.registerEmailEditText);

        phoneEditText =
                findViewById(R.id.registerPhoneEditText);

        passwordEditText =
                findViewById(R.id.registerPasswordEditText);

        confirmPasswordEditText =
                findViewById(R.id.registerConfirmPasswordEditText);

        Button registerButton =
                findViewById(R.id.registerSubmitButton);

        registerButton.setOnClickListener(
                v -> registerStudent()
        );
    }

    private void registerStudent() {

        String name =
                nameEditText
                        .getText()
                        .toString()
                        .trim();

        String studentNumber =
                studentNumberEditText
                        .getText()
                        .toString()
                        .trim();

        String email =
                emailEditText
                        .getText()
                        .toString()
                        .trim();

        String phone =
                phoneEditText
                        .getText()
                        .toString()
                        .trim();

        String password =
                passwordEditText
                        .getText()
                        .toString()
                        .trim();

        String confirmPassword =
                confirmPasswordEditText
                        .getText()
                        .toString()
                        .trim();

        // Name validation
        if (name.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your full name.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Student number validation
        if (studentNumber.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your student number.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Email validation
        if (email.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your email.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(
                    this,
                    "Please enter a valid email address.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Phone validation
        if (phone.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your phone number.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (phone.length() < 10) {

            Toast.makeText(
                    this,
                    "Please enter a valid phone number.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Password validation
        if (password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter a password.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (password.length() < 6) {

            Toast.makeText(
                    this,
                    "Password must be at least 6 characters.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please confirm your password.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!password.equals(confirmPassword)) {

            Toast.makeText(
                    this,
                    "Passwords do not match.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Show message while Firebase creates the account
        Toast.makeText(
                this,
                "Creating your account...",
                Toast.LENGTH_SHORT
        ).show();

        /*
         * Create the account in Firebase Authentication.
         */
        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser =
                                firebaseAuth.getCurrentUser();

                        if (firebaseUser == null) {

                            Toast.makeText(
                                    this,
                                    "Account created, but user information could not be loaded.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        String uid =
                                firebaseUser.getUid();

                        /*
                         * Create the user's profile information
                         * in Cloud Firestore.
                         */
                        Map<String, Object> userData =
                                new HashMap<>();

                        userData.put(
                                "uid",
                                uid
                        );

                        userData.put(
                                "name",
                                name
                        );

                        userData.put(
                                "studentNumber",
                                studentNumber
                        );

                        userData.put(
                                "email",
                                email
                        );

                        userData.put(
                                "phone",
                                phone
                        );

                        userData.put(
                                "role",
                                "user"
                        );

                        firestore
                                .collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener(unused -> {

                                    /*
                                     * Keep basic information locally
                                     * so the existing app can still
                                     * use it until we migrate the
                                     * remaining activities to Firebase.
                                     */
                                    SharedPreferences prefs =
                                            getSharedPreferences(
                                                    "CampusPrefs",
                                                    MODE_PRIVATE
                                            );

                                    prefs.edit()
                                            .putString(
                                                    "studentName",
                                                    name
                                            )
                                            .putString(
                                                    "studentNumber",
                                                    studentNumber
                                            )
                                            .putString(
                                                    "email",
                                                    email
                                            )
                                            .putString(
                                                    "phone",
                                                    phone
                                            )
                                            .putBoolean(
                                                    "isLoggedIn",
                                                    true
                                            )
                                            .putBoolean(
                                                    "isAdmin",
                                                    false
                                            )
                                            .apply();

                                    Toast.makeText(
                                            this,
                                            "Account created successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    /*
                                     * Open the Home page.
                                     */
                                    Intent intent =
                                            new Intent(
                                                    RegisterActivity.this,
                                                    HomeActivity.class
                                            );

                                    intent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(intent);

                                    finish();

                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            this,
                                            "Account created, but profile could not be saved: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        String errorMessage =
                                "Registration failed.";

                        if (task.getException() != null) {

                            errorMessage =
                                    task.getException()
                                            .getMessage();
                        }

                        Toast.makeText(
                                this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}