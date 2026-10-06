package com.example.campuslostfound;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        Button getStartedButton =
                findViewById(R.id.getStartedButton);

        Button loginButton =
                findViewById(R.id.loginButton);

        Button adminLoginButton =
                findViewById(R.id.adminLoginButton);

        // Force all welcome screen buttons to have a white background.
        // This prevents the Material theme from applying the blue tint.
        ColorStateList whiteTint =
                ColorStateList.valueOf(Color.WHITE);

        getStartedButton.setBackgroundTintList(whiteTint);
        loginButton.setBackgroundTintList(whiteTint);
        adminLoginButton.setBackgroundTintList(whiteTint);

        // Force all button text to blue.
        int blueTextColor =
                ContextCompat.getColor(
                        this,
                        R.color.brand_blue_dark
                );

        getStartedButton.setTextColor(blueTextColor);
        loginButton.setTextColor(blueTextColor);
        adminLoginButton.setTextColor(blueTextColor);

        // GET STARTED
        getStartedButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });

        // LOGIN
        loginButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    LoginFormActivity.class
            );

            startActivity(intent);
        });

        // LOGIN AS ADMIN
        adminLoginButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    AdminLoginActivity.class
            );

            startActivity(intent);
        });
    }
}