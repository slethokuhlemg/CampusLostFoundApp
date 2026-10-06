package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminReportsActivity extends AppCompatActivity {

    private RecyclerView reportsRecyclerView;

    private Spinner statusSpinner;

    private FirebaseFirestore firestore;

    private ListenerRegistration reportsListener;

    /*
     * allReports contains every report from Firebase.
     */
    private final List<ItemReport> allReports =
            new ArrayList<>();

    /*
     * displayedReports contains the reports
     * currently shown after applying the filter.
     */
    private final List<ItemReport> displayedReports =
            new ArrayList<>();

    private AdminReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_reports
        );

        /*
         * Check if the user is logged in
         * and has admin access.
         */

        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        boolean isAdmin =
                prefs.getBoolean(
                        "isAdmin",
                        false
                );

        boolean isLoggedIn =
                prefs.getBoolean(
                        "isLoggedIn",
                        false
                );

        if (!isAdmin || !isLoggedIn) {

            Toast.makeText(
                    this,
                    "Admin access required.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent =
                    new Intent(
                            AdminReportsActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();

            return;
        }

        /*
         * Initialize Firebase Firestore.
         */

        firestore =
                FirebaseFirestore.getInstance();

        /*
         * Find the RecyclerView.
         */

        reportsRecyclerView =
                findViewById(
                        R.id.adminReportsRecyclerView
                );

        reportsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        /*
         * Find the status filter.
         */

        statusSpinner =
                findViewById(
                        R.id.statusSpinner
                );

        /*
         * Create the status filter options.
         */

        String[] statusOptions = {
                "All Reports",
                "Pending",
                "Approved",
                "Rejected",
                "Resolved",
                "Claimed"
        };

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        statusOptions
                );

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        statusSpinner.setAdapter(
                spinnerAdapter
        );

        /*
         * Create the report adapter.
         *
         * We give it displayedReports because
         * this list changes when the admin selects
         * a different status.
         */

        adapter =
                new AdminReportAdapter(
                        displayedReports,
                        report -> {

                            Intent intent =
                                    new Intent(
                                            AdminReportsActivity.this,
                                            AdminReportDetailsActivity.class
                                    );

                            /*
                             * ItemReport currently uses
                             * a long ID.
                             *
                             * We temporarily use the
                             * timestamp as the ID.
                             */

                            intent.putExtra(
                                    "report_id",
                                    report.id
                            );

                            startActivity(
                                    intent
                            );
                        }
                );

        reportsRecyclerView.setAdapter(
                adapter
        );

        /*
         * When the admin changes the status filter,
         * update the list.
         */

        statusSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        String selectedStatus =
                                statusOptions[position];

                        filterReports(
                                selectedStatus
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {

                        filterReports(
                                "All Reports"
                        );
                    }
                }
        );

        /*
         * Start listening for reports.
         */

        listenForReports();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
         * Stop the Firestore listener when
         * the activity is destroyed.
         */

        if (reportsListener != null) {

            reportsListener.remove();

            reportsListener = null;
        }
    }

    private void listenForReports() {

        /*
         * Remove an old listener if one exists.
         */

        if (reportsListener != null) {

            reportsListener.remove();
        }

        /*
         * Listen to ALL reports.
         *
         * IMPORTANT:
         * There is intentionally NO status filter here.
         *
         * Admins need to see:
         *
         * Pending
         * Approved
         * Rejected
         * Resolved
         * Claimed
         */

        reportsListener =
                firestore
                        .collection("reports")
                        .addSnapshotListener(
                                (querySnapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Could not load reports: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    if (querySnapshot == null) {

                                        return;
                                    }

                                    allReports.clear();

                                    /*
                                     * Convert every Firestore
                                     * document into an ItemReport.
                                     */

                                    for (DocumentSnapshot document :
                                            querySnapshot.getDocuments()) {

                                        ItemReport report =
                                                convertDocumentToReport(
                                                        document
                                                );

                                        if (report != null) {

                                            allReports.add(
                                                    report
                                            );
                                        }
                                    }

                                    /*
                                     * Newest reports first.
                                     */

                                    Collections.sort(
                                            allReports,
                                            new Comparator<ItemReport>() {

                                                @Override
                                                public int compare(
                                                        ItemReport first,
                                                        ItemReport second) {

                                                    return Long.compare(
                                                            second.timestamp,
                                                            first.timestamp
                                                    );
                                                }
                                            }
                                    );

                                    /*
                                     * Apply the currently selected
                                     * status filter.
                                     */

                                    String selectedStatus =
                                            statusSpinner
                                                    .getSelectedItem()
                                                    .toString();

                                    filterReports(
                                            selectedStatus
                                    );
                                }
                        );
    }

    private ItemReport convertDocumentToReport(
            DocumentSnapshot document) {

        ItemReport report =
                new ItemReport();

        /*
         * timestamp
         */

        Long timestamp =
                document.getLong(
                        "timestamp"
                );

        if (timestamp != null) {

            report.timestamp =
                    timestamp;

            /*
             * ItemReport.id is currently a long.
             *
             * We temporarily use timestamp
             * for compatibility.
             */

            report.id =
                    timestamp;

        } else {

            report.timestamp =
                    0;

            report.id =
                    0;
        }

        /*
         * Report information.
         */

        report.type =
                getString(
                        document,
                        "type"
                );

        report.category =
                getString(
                        document,
                        "category"
                );

        report.title =
                getString(
                        document,
                        "title"
                );

        report.description =
                getString(
                        document,
                        "description"
                );

        report.location =
                getString(
                        document,
                        "location"
                );

        /*
         * Photo.
         */

        String photoUrl =
                getString(
                        document,
                        "photoUrl"
                );

        String photoUri =
                getString(
                        document,
                        "photoUri"
                );

        if (!photoUrl.isEmpty()) {

            report.photoUri =
                    photoUrl;

        } else if (!photoUri.isEmpty()) {

            report.photoUri =
                    photoUri;

        } else {

            report.photoUri =
                    null;
        }

        /*
         * AI information.
         */

        report.priority =
                getString(
                        document,
                        "priority"
                );

        report.score =
                getInt(
                        document,
                        "score"
                );

        /*
         * Report status.
         */

        report.status =
                getString(
                        document,
                        "status"
                );

        /*
         * Reporter information.
         */

        report.reporterName =
                getString(
                        document,
                        "reporterName"
                );

        report.reporterEmail =
                getString(
                        document,
                        "reporterEmail"
                );

        /*
         * Date.
         */

        report.dateDisplay =
                getString(
                        document,
                        "dateDisplay"
                );

        /*
         * Notification information.
         */

        report.notification =
                getString(
                        document,
                        "notification"
                );

        Boolean notificationRead =
                document.getBoolean(
                        "notificationRead"
                );

        if (notificationRead != null) {

            report.notificationRead =
                    notificationRead;

        } else {

            report.notificationRead =
                    true;
        }

        return report;
    }

    private void filterReports(
            String selectedStatus) {

        /*
         * Clear the currently displayed list.
         */

        displayedReports.clear();

        /*
         * Show every report when
         * "All Reports" is selected.
         */

        if (selectedStatus.equals(
                "All Reports")) {

            displayedReports.addAll(
                    allReports
            );

        } else {

            /*
             * Otherwise only display reports
             * matching the selected status.
             */

            for (ItemReport report :
                    allReports) {

                if (report.status != null
                        && report.status.equalsIgnoreCase(
                        selectedStatus)) {

                    displayedReports.add(
                            report
                    );
                }
            }
        }

        /*
         * Tell the RecyclerView that
         * the data has changed.
         */

        if (adapter != null) {

            adapter.notifyDataSetChanged();
        }
    }

    private String getString(
            DocumentSnapshot document,
            String field) {

        Object value =
                document.get(field);

        if (value == null) {

            return "";
        }

        return String.valueOf(value);
    }

    private int getInt(
            DocumentSnapshot document,
            String field) {

        Object value =
                document.get(field);

        if (value == null) {

            return 0;
        }

        if (value instanceof Number) {

            return ((Number) value).intValue();
        }

        try {

            return Integer.parseInt(
                    String.valueOf(value)
            );

        } catch (Exception e) {

            return 0;
        }
    }
}