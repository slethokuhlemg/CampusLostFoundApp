package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyReportsActivity extends AppCompatActivity {

    private RecyclerView reportsRecyclerView;
    private TextView emptyText;

    private TextView tabLost;
    private TextView tabFound;
    private TextView tabClaimed;

    private ImageButton backButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final List<ItemReport> reportList =
            new ArrayList<>();

    private String currentType = "Lost";

    private boolean browseAll = false;

    private String currentUserEmail = "";

    private static final String TYPE_LOST = "Lost";
    private static final String TYPE_FOUND = "Found";
    private static final String TYPE_CLAIMED = "Claimed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_my_reports
        );

        reportsRecyclerView =
                findViewById(
                        R.id.reportsRecyclerView
                );

        emptyText =
                findViewById(
                        R.id.emptyText
                );

        tabLost =
                findViewById(
                        R.id.tabLost
                );

        tabFound =
                findViewById(
                        R.id.tabFound
                );

        tabClaimed =
                findViewById(
                        R.id.tabClaimed
                );

        backButton =
                findViewById(
                        R.id.backButton
                );

        db =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();

        reportsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        String mode =
                getIntent().getStringExtra(
                        "mode"
                );

        browseAll =
                "all".equalsIgnoreCase(
                        mode
                );

        getCurrentUserEmail();

        if (browseAll) {

            currentType =
                    TYPE_FOUND;

        } else {

            currentType =
                    TYPE_LOST;
        }

        backButton.setOnClickListener(
                v -> finish()
        );

        tabLost.setOnClickListener(
                v -> {

                    currentType =
                            TYPE_LOST;

                    selectTab(
                            TYPE_LOST
                    );

                    loadReports();
                }
        );

        tabFound.setOnClickListener(
                v -> {

                    currentType =
                            TYPE_FOUND;

                    selectTab(
                            TYPE_FOUND
                    );

                    loadReports();
                }
        );

        tabClaimed.setOnClickListener(
                v -> {

                    currentType =
                            TYPE_CLAIMED;

                    selectTab(
                            TYPE_CLAIMED
                    );

                    loadReports();
                }
        );

        selectTab(
                currentType
        );

        loadReports();
    }

    private void getCurrentUserEmail() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user != null
                && user.getEmail() != null
                && !user.getEmail().trim().isEmpty()) {

            currentUserEmail =
                    user.getEmail().trim();

            return;
        }

        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        currentUserEmail =
                prefs.getString(
                        "email",
                        ""
                );

        if (currentUserEmail == null) {

            currentUserEmail = "";
        }

        currentUserEmail =
                currentUserEmail.trim();
    }

    private void selectTab(
            String selectedType) {

        int selectedColor =
                Color.rgb(
                        30,
                        111,
                        217
                );

        int normalColor =
                Color.rgb(
                        91,
                        107,
                        133
                );

        if (TYPE_LOST.equals(
                selectedType
        )) {

            tabLost.setTextColor(
                    selectedColor
            );

            tabLost.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

        } else {

            tabLost.setTextColor(
                    normalColor
            );

            tabLost.setTypeface(
                    android.graphics.Typeface.DEFAULT
            );
        }

        if (TYPE_FOUND.equals(
                selectedType
        )) {

            tabFound.setTextColor(
                    selectedColor
            );

            tabFound.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

        } else {

            tabFound.setTextColor(
                    normalColor
            );

            tabFound.setTypeface(
                    android.graphics.Typeface.DEFAULT
            );
        }

        if (TYPE_CLAIMED.equals(
                selectedType
        )) {

            tabClaimed.setTextColor(
                    selectedColor
            );

            tabClaimed.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

        } else {

            tabClaimed.setTextColor(
                    normalColor
            );

            tabClaimed.setTypeface(
                    android.graphics.Typeface.DEFAULT
            );
        }
    }

    private void loadReports() {

        if (!browseAll
                && currentUserEmail.isEmpty()) {

            reportList.clear();

            showEmptyMessage();

            return;
        }

        db.collection("reports")
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            reportList.clear();

                            for (
                                    DocumentSnapshot document
                                    :
                                    querySnapshot.getDocuments()
                            ) {

                                String reporterEmail =
                                        document.getString(
                                                "reporterEmail"
                                        );

                                String type =
                                        document.getString(
                                                "type"
                                        );

                                String status =
                                        document.getString(
                                                "status"
                                        );

                                if (reporterEmail == null) {
                                    reporterEmail = "";
                                }

                                if (type == null) {
                                    type = "";
                                }

                                if (status == null) {
                                    status = "";
                                }

                                reporterEmail =
                                        reporterEmail.trim();

                                type =
                                        type.trim();

                                status =
                                        status.trim();

                                /*
                                 * MY REPORTS
                                 */
                                if (!browseAll) {

                                    if (!reporterEmail.equalsIgnoreCase(
                                            currentUserEmail
                                    )) {

                                        continue;
                                    }
                                }

                                /*
                                 * LOST TAB
                                 */
                                if (TYPE_LOST.equals(
                                        currentType
                                )) {

                                    if (!TYPE_LOST.equalsIgnoreCase(
                                            type
                                    )) {

                                        continue;
                                    }

                                    if (!"Approved".equalsIgnoreCase(
                                            status
                                    )) {

                                        continue;
                                    }
                                }

                                /*
                                 * FOUND TAB
                                 */
                                if (TYPE_FOUND.equals(
                                        currentType
                                )) {

                                    if (!TYPE_FOUND.equalsIgnoreCase(
                                            type
                                    )) {

                                        continue;
                                    }

                                    if (!"Approved".equalsIgnoreCase(
                                            status
                                    )) {

                                        continue;
                                    }
                                }

                                /*
                                 * CLAIMED HISTORY
                                 */
                                if (TYPE_CLAIMED.equals(
                                        currentType
                                )) {

                                    if (!"Claimed".equalsIgnoreCase(
                                            status
                                    )) {

                                        continue;
                                    }
                                }

                                ItemReport report =
                                        createReportFromDocument(
                                                document
                                        );

                                if (report != null) {

                                    reportList.add(
                                            report
                                    );
                                }
                            }

                            displayReports();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            reportList.clear();

                            Toast.makeText(
                                    MyReportsActivity.this,
                                    "Failed to load reports: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            displayReports();
                        }
                );
    }

    private ItemReport createReportFromDocument(
            DocumentSnapshot document) {

        try {

            ItemReport report =
                    new ItemReport();

            /*
             * FIRESTORE DOCUMENT ID
             */
            report.firestoreId =
                    document.getId();

            /*
             * TIMESTAMP / ID
             */
            Long timestamp =
                    document.getLong(
                            "timestamp"
                    );

            if (timestamp != null) {

                report.timestamp =
                        timestamp;

                report.id =
                        timestamp;

            } else {

                report.timestamp =
                        0L;

                report.id =
                        0L;
            }

            /*
             * BASIC INFORMATION
             */
            report.type =
                    document.getString(
                            "type"
                    );

            report.category =
                    document.getString(
                            "category"
                    );

            report.title =
                    document.getString(
                            "title"
                    );

            report.description =
                    document.getString(
                            "description"
                    );

            report.location =
                    document.getString(
                            "location"
                    );

            /*
             * PHOTO
             */
            String photoUrl =
                    document.getString(
                            "photoUrl"
                    );

            String photoUri =
                    document.getString(
                            "photoUri"
                    );

            if (photoUrl != null
                    && !photoUrl.trim().isEmpty()) {

                report.photoUri =
                        photoUrl;

            } else if (photoUri != null
                    && !photoUri.trim().isEmpty()) {

                report.photoUri =
                        photoUri;

            } else {

                report.photoUri =
                        "";
            }

            report.priority =
                    document.getString(
                            "priority"
                    );

            /*
             * SCORE
             */
            Long score =
                    document.getLong(
                            "score"
                    );

            if (score != null) {

                report.score =
                        score.intValue();

            } else {

                report.score =
                        0;
            }

            /*
             * STATUS
             */
            report.status =
                    document.getString(
                            "status"
                    );

            /*
             * REPORTER
             */
            report.reporterName =
                    document.getString(
                            "reporterName"
                    );

            report.reporterEmail =
                    document.getString(
                            "reporterEmail"
                    );

            /*
             * ORIGINAL REPORT DATE
             */
            report.dateDisplay =
                    document.getString(
                            "dateDisplay"
                    );

            /*
             * LOST DATE + TIME
             */
            report.lostDate =
                    document.getString(
                            "lostDate"
                    );

            report.lostTime =
                    document.getString(
                            "lostTime"
                    );

            /*
             * FOUND DATE + TIME
             */
            report.foundDate =
                    document.getString(
                            "foundDate"
                    );

            report.foundTime =
                    document.getString(
                            "foundTime"
                    );

            Long foundTimestamp =
                    document.getLong(
                            "foundTimestamp"
                    );

            if (foundTimestamp != null) {

                report.foundTimestamp =
                        foundTimestamp;

            } else {

                report.foundTimestamp =
                        0L;
            }

            /*
             * CLAIMED DATE + TIME
             */
            report.claimedDate =
                    document.getString(
                            "claimedDate"
                    );

            report.claimedTime =
                    document.getString(
                            "claimedTime"
                    );

            Long claimedTimestamp =
                    document.getLong(
                            "claimedTimestamp"
                    );

            if (claimedTimestamp != null) {

                report.claimedTimestamp =
                        claimedTimestamp;

            } else {

                report.claimedTimestamp =
                        0L;
            }

            /*
             * NOTIFICATION
             */
            report.notification =
                    document.getString(
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
                        false;
            }

            return report;

        } catch (Exception e) {

            return null;
        }
    }

    private void displayReports() {

        if (reportList.isEmpty()) {

            showEmptyMessage();

            return;
        }

        emptyText.setVisibility(
                View.GONE
        );

        reportsRecyclerView.setVisibility(
                View.VISIBLE
        );

        /*
         * Only the user's own LOST reports
         * can show the Mark as Found button.
         */
        boolean showRecoveryButton =
                !browseAll
                        && TYPE_LOST.equals(
                        currentType
                );

        /*
         * IMPORTANT:
         *
         * ReportAdapter constructor is:
         *
         * ReportAdapter(
         *     List<ItemReport>,
         *     OnReportClick,
         *     boolean,
         *     OnRecoveredClick
         * )
         *
         * There is NO "this" parameter.
         */
        ReportAdapter adapter =
                new ReportAdapter(
                        reportList,
                        report -> {

                            Intent intent =
                                    new Intent(
                                            MyReportsActivity.this,
                                            ItemDetailsActivity.class
                                    );

                            /*
                             * Timestamp fallback
                             */
                            intent.putExtra(
                                    "report_id",
                                    report.timestamp
                            );

                            /*
                             * Exact Firestore document
                             */
                            intent.putExtra(
                                    "firestore_id",
                                    report.firestoreId
                            );

                            startActivity(
                                    intent
                            );
                        },
                        showRecoveryButton,
                        report -> {

                            confirmItemRecovered(
                                    report
                            );
                        }
                );

        reportsRecyclerView.setAdapter(
                adapter
        );
    }

    private void showEmptyMessage() {

        reportsRecyclerView.setVisibility(
                View.GONE
        );

        emptyText.setVisibility(
                View.VISIBLE
        );

        if (TYPE_LOST.equals(
                currentType
        )) {

            if (browseAll) {

                emptyText.setText(
                        "No lost items found."
                );

            } else {

                emptyText.setText(
                        "You have no lost reports yet."
                );
            }

        } else if (TYPE_FOUND.equals(
                currentType
        )) {

            if (browseAll) {

                emptyText.setText(
                        "No found items available."
                );

            } else {

                emptyText.setText(
                        "You have no found reports yet."
                );
            }

        } else if (TYPE_CLAIMED.equals(
                currentType
        )) {

            if (browseAll) {

                emptyText.setText(
                        "No claimed history yet."
                );

            } else {

                emptyText.setText(
                        "You have no claimed items yet."
                );
            }
        }
    }

    private void confirmItemRecovered(
            ItemReport report) {

        new androidx.appcompat.app.AlertDialog.Builder(
                this
        )
                .setTitle(
                        "Mark as Found?"
                )
                .setMessage(
                        "Are you sure you want to mark this lost item as found?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Yes",
                        (dialog, which) ->
                                markItemAsRecovered(
                                        report
                                )
                )
                .show();
    }

    private void markItemAsRecovered(
            ItemReport report) {

        if (report == null) {

            Toast.makeText(
                    this,
                    "Unable to find this report.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (report.firestoreId == null
                || report.firestoreId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Unable to find the Firestore report.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!TYPE_LOST.equalsIgnoreCase(
                report.type
        )) {

            Toast.makeText(
                    this,
                    "Only lost items can be marked as found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!"Approved".equalsIgnoreCase(
                report.status
        )) {

            Toast.makeText(
                    this,
                    "Only approved reports can be marked as found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (currentUserEmail.isEmpty()
                || report.reporterEmail == null
                || !currentUserEmail.equalsIgnoreCase(
                report.reporterEmail.trim()
        )) {

            Toast.makeText(
                    this,
                    "You can only update your own report.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Calendar calendar =
                Calendar.getInstance();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                );

        String foundDate =
                dateFormat.format(
                        calendar.getTime()
                );

        String foundTime =
                timeFormat.format(
                        calendar.getTime()
                );

        long foundTimestamp =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        /*
         * MOVE LOST → FOUND
         */
        updates.put(
                "type",
                TYPE_FOUND
        );

        updates.put(
                "status",
                "Approved"
        );

        /*
         * PRESERVE ORIGINAL LOST DATE/TIME
         */
        updates.put(
                "lostDate",
                report.lostDate == null
                        ? ""
                        : report.lostDate
        );

        updates.put(
                "lostTime",
                report.lostTime == null
                        ? ""
                        : report.lostTime
        );

        /*
         * SAVE FOUND DATE/TIME
         */
        updates.put(
                "foundDate",
                foundDate
        );

        updates.put(
                "foundTime",
                foundTime
        );

        updates.put(
                "foundTimestamp",
                foundTimestamp
        );

        /*
         * CLEAR CLAIMED INFORMATION
         */
        updates.put(
                "claimedDate",
                ""
        );

        updates.put(
                "claimedTime",
                ""
        );

        updates.put(
                "claimedTimestamp",
                0L
        );

        /*
         * NOTIFICATION
         */
        updates.put(
                "notification",
                "Your lost item has been marked as found."
        );

        updates.put(
                "notificationRead",
                false
        );

        db.collection("reports")
                .document(
                        report.firestoreId
                )
                .update(
                        updates
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    MyReportsActivity.this,
                                    "Item moved to Found.",
                                    Toast.LENGTH_LONG
                            ).show();

                            loadReports();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    MyReportsActivity.this,
                                    "Failed to update item: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (db != null) {

            getCurrentUserEmail();

            loadReports();
        }
    }
}