package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileNameText;
    private TextView profileStudentNumberText;
    private TextView profileEmailText;
    private TextView profilePhoneText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        profileNameText =
                findViewById(R.id.profileNameText);

        profileStudentNumberText =
                findViewById(R.id.profileStudentNumberText);

        profileEmailText =
                findViewById(R.id.profileEmailText);

        profilePhoneText =
                findViewById(R.id.profilePhoneText);

        Button backButton =
                findViewById(R.id.profileBackButton);

        Button logoutButton =
                findViewById(R.id.profileLogoutButton);

        backButton.setOnClickListener(
                v -> finish()
        );

        logoutButton.setOnClickListener(
                v -> logout()
        );

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadProfile();
    }

    private void loadProfile() {

        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        String name =
                prefs.getString(
                        "studentName",
                        ""
                );

        String studentNumber =
                prefs.getString(
                        "studentNumber",
                        ""
                );

        String email =
                prefs.getString(
                        "email",
                        ""
                );

        String phone =
                prefs.getString(
                        "phone",
                        ""
                );

        /*
         * Display the saved account information.
         *
         * We do not redirect to LoginActivity here.
         * This prevents the Profile screen from sending
         * the user back to the login screen unnecessarily.
         */

        if (name.isEmpty()) {
            profileNameText.setText(
                    "Name not available"
            );
        } else {
            profileNameText.setText(
                    name
            );
        }

        if (studentNumber.isEmpty()) {
            profileStudentNumberText.setText(
                    "Student Number: Not available"
            );
        } else {
            profileStudentNumberText.setText(
                    "Student Number: " +
                            studentNumber
            );
        }

        if (email.isEmpty()) {
            profileEmailText.setText(
                    "Email: Not available"
            );
        } else {
            profileEmailText.setText(
                    "Email: " +
                            email
            );
        }

        if (phone.isEmpty()) {
            profilePhoneText.setText(
                    "Phone: Not available"
            );
        } else {
            profilePhoneText.setText(
                    "Phone: " +
                            phone
            );
        }
    }

    private void logout() {

        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        /*
         * IMPORTANT:
         *
         * We only log the user out.
         *
         * We do NOT delete:
         * - Name
         * - Student number
         * - Email
         * - Phone
         * - Password
         *
         * Therefore the account remains saved.
         */

        prefs.edit()
                .putBoolean(
                        "isLoggedIn",
                        false
                )
                .putBoolean(
                        "isAdmin",
                        false
                )
                .apply();

        Toast.makeText(
                this,
                "Logged out successfully.",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent =
                new Intent(
                        ProfileActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}