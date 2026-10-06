package com.example.campuslostfound;

import android.content.Intent;
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

public class AdminRegisterActivity extends AppCompatActivity {

    private EditText adminNameEditText;
    private EditText adminEmailEditText;
    private EditText adminPasswordEditText;
    private EditText adminConfirmPasswordEditText;

    private Button adminRegisterSubmitButton;
    private Button adminBackButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_register
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        adminNameEditText =
                findViewById(
                        R.id.adminNameEditText
                );

        adminEmailEditText =
                findViewById(
                        R.id.adminEmailEditText
                );

        adminPasswordEditText =
                findViewById(
                        R.id.adminPasswordEditText
                );

        adminConfirmPasswordEditText =
                findViewById(
                        R.id.adminConfirmPasswordEditText
                );

        adminRegisterSubmitButton =
                findViewById(
                        R.id.adminRegisterSubmitButton
                );

        adminBackButton =
                findViewById(
                        R.id.adminBackButton
                );

        adminRegisterSubmitButton.setOnClickListener(
                v -> registerAdminRequest()
        );

        adminBackButton.setOnClickListener(
                v -> finish()
        );
    }

    private void registerAdminRequest() {

        String name =
                adminNameEditText
                        .getText()
                        .toString()
                        .trim();

        String email =
                adminEmailEditText
                        .getText()
                        .toString()
                        .trim();

        String password =
                adminPasswordEditText
                        .getText()
                        .toString();

        String confirmPassword =
                adminConfirmPasswordEditText
                        .getText()
                        .toString();

        if (name.isEmpty()) {

            adminNameEditText.setError(
                    "Please enter your name."
            );

            adminNameEditText.requestFocus();

            return;
        }

        if (email.isEmpty()) {

            adminEmailEditText.setError(
                    "Please enter your email."
            );

            adminEmailEditText.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            adminEmailEditText.setError(
                    "Please enter a valid email address."
            );

            adminEmailEditText.requestFocus();

            return;
        }

        if (password.isEmpty()) {

            adminPasswordEditText.setError(
                    "Please enter a password."
            );

            adminPasswordEditText.requestFocus();

            return;
        }

        if (password.length() < 6) {

            adminPasswordEditText.setError(
                    "Password must be at least 6 characters."
            );

            adminPasswordEditText.requestFocus();

            return;
        }

        if (!password.equals(confirmPassword)) {

            adminConfirmPasswordEditText.setError(
                    "Passwords do not match."
            );

            adminConfirmPasswordEditText.requestFocus();

            return;
        }

        adminRegisterSubmitButton.setEnabled(false);

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                adminRegisterSubmitButton
                                        .setEnabled(true);

                                String message =
                                        task.getException() != null
                                                ? task.getException()
                                                .getMessage()
                                                : "Registration failed.";

                                Toast.makeText(
                                        this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            FirebaseUser firebaseUser =
                                    firebaseAuth.getCurrentUser();

                            if (firebaseUser == null) {

                                adminRegisterSubmitButton
                                        .setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Account was created, but the user could not be found.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            String uid =
                                    firebaseUser.getUid();

                            /*
                             * The account is NOT an admin yet.
                             *
                             * It starts as pending_admin.
                             *
                             * Only the main administrator can
                             * change this to admin.
                             */
                            Map<String, Object> adminData =
                                    new HashMap<>();

                            adminData.put(
                                    "uid",
                                    uid
                            );

                            adminData.put(
                                    "name",
                                    name
                            );

                            adminData.put(
                                    "studentNumber",
                                    ""
                            );

                            adminData.put(
                                    "email",
                                    email
                            );

                            adminData.put(
                                    "phone",
                                    ""
                            );

                            adminData.put(
                                    "role",
                                    "pending_admin"
                            );

                            firestore
                                    .collection("users")
                                    .document(uid)
                                    .set(adminData)
                                    .addOnSuccessListener(
                                            unused -> {

                                                /*
                                                 * Sign out immediately.
                                                 *
                                                 * This prevents the newly
                                                 * registered person from
                                                 * entering the admin area
                                                 * before approval.
                                                 */
                                                firebaseAuth.signOut();

                                                Toast.makeText(
                                                        this,
                                                        "Admin account request submitted. Please wait for the main administrator to approve your account.",
                                                        Toast.LENGTH_LONG
                                                ).show();

                                                Intent intent =
                                                        new Intent(
                                                                AdminRegisterActivity.this,
                                                                AdminLoginActivity.class
                                                        );

                                                startActivity(intent);

                                                finish();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                firebaseAuth.signOut();

                                                adminRegisterSubmitButton
                                                        .setEnabled(true);

                                                Toast.makeText(
                                                        this,
                                                        "Account created, but the approval request could not be saved: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                );
    }
}