package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_login);

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        emailEditText =
                findViewById(
                        R.id.adminEmailEditText
                );

        passwordEditText =
                findViewById(
                        R.id.adminPasswordEditText
                );

        Button loginButton =
                findViewById(
                        R.id.adminLoginSubmitButton
                );

        Button createAdminAccountButton =
                findViewById(
                        R.id.createAdminAccountButton
                );

        /*
         * ADMIN LOGIN BUTTON
         */
        loginButton.setOnClickListener(
                v -> loginAsAdmin()
        );


        /*
         * CREATE ADMIN ACCOUNT BUTTON
         */
        createAdminAccountButton.setOnClickListener(
                v -> {

                    Toast.makeText(
                            AdminLoginActivity.this,
                            "Opening Create Admin Account...",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent =
                            new Intent(
                                    AdminLoginActivity.this,
                                    AdminRegisterActivity.class
                            );

                    startActivity(intent);
                }
        );
    }


    private void loginAsAdmin() {

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


        Toast.makeText(
                this,
                "Logging in...",
                Toast.LENGTH_SHORT
        ).show();


        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                String errorMessage =
                                        "Incorrect admin email or password.";

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

                                return;
                            }


                            FirebaseUser firebaseUser =
                                    firebaseAuth.getCurrentUser();


                            if (firebaseUser == null) {

                                Toast.makeText(
                                        this,
                                        "Login failed. Administrator information could not be loaded.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            String uid =
                                    firebaseUser.getUid();


                            firestore
                                    .collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(
                                            documentSnapshot ->
                                                    checkAdminRole(
                                                            documentSnapshot,
                                                            email
                                                    )
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                Toast.makeText(
                                                        this,
                                                        "Could not verify administrator account: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();

                                                firebaseAuth.signOut();
                                            }
                                    );
                        }
                );
    }


    private void checkAdminRole(
            DocumentSnapshot documentSnapshot,
            String email) {

        if (!documentSnapshot.exists()) {

            Toast.makeText(
                    this,
                    "Administrator profile not found.",
                    Toast.LENGTH_LONG
            ).show();

            firebaseAuth.signOut();

            return;
        }


        String role =
                documentSnapshot.getString("role");


        /*
         * PENDING ADMIN
         */
        if (role != null &&
                role.equalsIgnoreCase("pending_admin")) {

            Toast.makeText(
                    this,
                    "Your admin account is waiting for approval from the main administrator.",
                    Toast.LENGTH_LONG
            ).show();

            firebaseAuth.signOut();

            return;
        }


        /*
         * NOT AN ADMIN
         */
        if (role == null ||
                !role.equalsIgnoreCase("admin")) {

            Toast.makeText(
                    this,
                    "This account is not an approved administrator account.",
                    Toast.LENGTH_LONG
            ).show();

            firebaseAuth.signOut();

            return;
        }


        /*
         * APPROVED ADMIN
         */
        String adminName =
                documentSnapshot.getString("name");


        if (adminName == null ||
                adminName.isEmpty()) {

            adminName =
                    "Administrator";
        }


        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );


        prefs.edit()
                .putBoolean(
                        "isLoggedIn",
                        true
                )
                .putBoolean(
                        "isAdmin",
                        true
                )
                .putString(
                        "role",
                        "admin"
                )
                .putString(
                        "adminName",
                        adminName
                )
                .putString(
                        "adminEmail",
                        email
                )
                .putString(
                        "email",
                        email
                )
                .apply();


        Toast.makeText(
                this,
                "Admin login successful!",
                Toast.LENGTH_SHORT
        ).show();


        Intent intent =
                new Intent(
                        AdminLoginActivity.this,
                        AdminHomeActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }
}