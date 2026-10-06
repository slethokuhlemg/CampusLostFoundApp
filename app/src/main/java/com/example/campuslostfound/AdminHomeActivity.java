package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminHomeActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private TextView lostItemsCount;
    private TextView foundItemsCount;
    private TextView pendingReportsCount;
    private TextView pendingClaimsCount;
    private TextView returnedItemsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_home
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        initializeStatistics();

        verifyAdminAccess();
    }

    private void initializeStatistics() {

        lostItemsCount =
                findViewById(
                        R.id.lostItemsCount
                );

        foundItemsCount =
                findViewById(
                        R.id.foundItemsCount
                );

        pendingReportsCount =
                findViewById(
                        R.id.pendingReportsCount
                );

        pendingClaimsCount =
                findViewById(
                        R.id.pendingClaimsCount
                );

        returnedItemsCount =
                findViewById(
                        R.id.returnedItemsCount
                );
    }

    private void verifyAdminAccess() {

        FirebaseUser firebaseUser =
                firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {

            denyAdminAccess();

            return;
        }

        String uid =
                firebaseUser.getUid();

        firestore
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                denyAdminAccess();

                                return;
                            }

                            String role =
                                    documentSnapshot.getString(
                                            "role"
                                    );

                            if (role == null ||
                                    !role.equalsIgnoreCase("admin")) {

                                denyAdminAccess();

                                return;
                            }

                            loadAdminHome(
                                    documentSnapshot.getString(
                                            "name"
                                    )
                            );

                            loadStatistics();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not verify administrator access.",
                                    Toast.LENGTH_LONG
                            ).show();

                            denyAdminAccess();
                        }
                );
    }

    private void loadAdminHome(
            String adminName) {

        TextView welcomeText =
                findViewById(
                        R.id.adminWelcomeText
                );

        Button viewReportsButton =
                findViewById(
                        R.id.viewReportsButton
                );

        Button messagesButton =
                findViewById(
                        R.id.adminMessagesButton
                );

        Button manageUsersButton =
                findViewById(
                        R.id.manageUsersButton
                );

        Button logoutButton =
                findViewById(
                        R.id.adminLogoutButton
                );

        if (adminName == null ||
                adminName.isEmpty()) {

            adminName =
                    "Administrator";
        }

        welcomeText.setText(
                "Welcome, " + adminName
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
                .apply();

        viewReportsButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    AdminHomeActivity.this,
                                    AdminReportsActivity.class
                            );

                    startActivity(intent);
                }
        );

        messagesButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    AdminHomeActivity.this,
                                    AdminMessagesActivity.class
                            );

                    startActivity(intent);
                }
        );

        manageUsersButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    AdminHomeActivity.this,
                                    ManageUsersActivity.class
                            );

                    startActivity(intent);
                }
        );

        logoutButton.setOnClickListener(
                v -> logoutAdmin()
        );
    }

    /**
     * Loads all dashboard statistics from Firestore.
     */
    private void loadStatistics() {

        firestore
                .collection("reports")
                .get()
                .addOnSuccessListener(
                        this::calculateReportStatistics
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not load report statistics.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );

        loadPendingClaims();
    }

    /**
     * Calculates:
     * Lost Items
     * Found Items
     * Pending Reports
     * Returned Items
     */
    private void calculateReportStatistics(
            QuerySnapshot snapshot) {

        int lostItems = 0;
        int foundItems = 0;
        int pendingReports = 0;
        int returnedItems = 0;

        for (com.google.firebase.firestore.DocumentSnapshot document :
                snapshot.getDocuments()) {

            String type =
                    document.getString("type");

            String status =
                    document.getString("status");

            if (type != null) {

                if (type.equalsIgnoreCase("lost")) {

                    lostItems++;

                } else if (type.equalsIgnoreCase("found")) {

                    foundItems++;
                }
            }

            if (status != null) {

                if (status.equalsIgnoreCase("pending")) {

                    pendingReports++;

                } else if (status.equalsIgnoreCase("returned")) {

                    returnedItems++;
                }
            }
        }

        lostItemsCount.setText(
                String.valueOf(lostItems)
        );

        foundItemsCount.setText(
                String.valueOf(foundItems)
        );

        pendingReportsCount.setText(
                String.valueOf(pendingReports)
        );

        returnedItemsCount.setText(
                String.valueOf(returnedItems)
        );
    }

    /**
     * Loads pending claims from the Firestore "claims" collection.
     *
     * Expected structure:
     *
     * claims
     *    └── claim document
     *          └── status: "Pending"
     */
    private void loadPendingClaims() {

        firestore
                .collection("claims")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int pendingClaims = 0;

                            for (com.google.firebase.firestore.DocumentSnapshot document :
                                    snapshot.getDocuments()) {

                                String status =
                                        document.getString("status");

                                if (status != null &&
                                        status.equalsIgnoreCase("pending")) {

                                    pendingClaims++;
                                }
                            }

                            pendingClaimsCount.setText(
                                    String.valueOf(
                                            pendingClaims
                                    )
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            /*
                             * If the claims collection does not
                             * exist yet, display 0 instead of
                             * crashing the application.
                             */
                            pendingClaimsCount.setText(
                                    "0"
                            );
                        }
                );
    }

    /**
     * Refresh statistics whenever the admin returns
     * to the dashboard.
     */
    @Override
    protected void onResume() {

        super.onResume();

        if (firebaseAuth != null &&
                firebaseAuth.getCurrentUser() != null) {

            loadStatistics();
        }
    }

    private void denyAdminAccess() {

        prefs.edit()
                .putBoolean(
                        "isLoggedIn",
                        false
                )
                .putBoolean(
                        "isAdmin",
                        false
                )
                .remove("role")
                .remove("adminName")
                .remove("adminEmail")
                .apply();

        firebaseAuth.signOut();

        Toast.makeText(
                this,
                "Admin access required.",
                Toast.LENGTH_LONG
        ).show();

        Intent intent =
                new Intent(
                        AdminHomeActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    private void logoutAdmin() {

        firebaseAuth.signOut();

        prefs.edit()
                .putBoolean(
                        "isLoggedIn",
                        false
                )
                .putBoolean(
                        "isAdmin",
                        false
                )
                .remove("role")
                .remove("adminName")
                .remove("adminEmail")
                .apply();

        Toast.makeText(
                this,
                "Admin logged out.",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent =
                new Intent(
                        AdminHomeActivity.this,
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

