package com.example.campuslostfound;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        emailEditText =
                findViewById(R.id.forgotEmailEditText);

        Button resetButton =
                findViewById(R.id.resetPasswordButton);

        Button backButton =
                findViewById(R.id.backToLoginButton);

        // Connect to Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(
                v -> resetPassword()
        );

        backButton.setOnClickListener(
                v -> finish()
        );
    }

    private void resetPassword() {

        String email =
                emailEditText
                        .getText()
                        .toString()
                        .trim();

        // Check if email is empty
        if (email.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your email.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Check if email is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(
                    this,
                    "Please enter a valid email address.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Send password reset email using Firebase
        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Password reset email sent. Please check your email.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Unable to send password reset email. Please check your email and try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
