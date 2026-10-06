package com.example.campuslostfound;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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

public class AdminPanelActivity extends AppCompatActivity {

    private static final String TAG = "AdminPanelActivity";

    private FirebaseFirestore firestore;

    private ListenerRegistration reportsListener;

    private RecyclerView recyclerView;
    private TextView emptyText;

    private TextView statTotal;
    private TextView statLost;
    private TextView statFound;
    private TextView statResolved;

    private final List<ItemReport> allReports =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_panel);

        firestore =
                FirebaseFirestore.getInstance();

        ImageButton backButton =
                findViewById(R.id.backButton);

        statTotal =
                findViewById(R.id.statTotal);

        statLost =
                findViewById(R.id.statLost);

        statFound =
                findViewById(R.id.statFound);

        statResolved =
                findViewById(R.id.statResolved);

        recyclerView =
                findViewById(R.id.adminRecyclerView);

        emptyText =
                findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        backButton.setOnClickListener(
                v -> finish()
        );

        listenForReports();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (reportsListener != null) {

            reportsListener.remove();

            reportsListener = null;
        }
    }

    private void listenForReports() {

        if (reportsListener != null) {

            reportsListener.remove();
        }

        Log.d(
                TAG,
                "Starting Firestore reports listener..."
        );

        reportsListener =
                firestore
                        .collection("reports")
                        .addSnapshotListener(
                                (querySnapshot, error) -> {

                                    if (error != null) {

                                        Log.e(
                                                TAG,
                                                "Firestore error: "
                                                        + error.getMessage(),
                                                error
                                        );

                                        recyclerView.setVisibility(
                                                View.GONE
                                        );

                                        emptyText.setVisibility(
                                                View.VISIBLE
                                        );

                                        emptyText.setText(
                                                "Could not load reports.\n\n"
                                                        + error.getMessage()
                                        );

                                        Toast.makeText(
                                                this,
                                                "Firestore error: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    if (querySnapshot == null) {

                                        Log.d(
                                                TAG,
                                                "Firestore returned null snapshot."
                                        );

                                        recyclerView.setVisibility(
                                                View.GONE
                                        );

                                        emptyText.setVisibility(
                                                View.VISIBLE
                                        );

                                        emptyText.setText(
                                                "No reports available."
                                        );

                                        return;
                                    }

                                    Log.d(
                                            TAG,
                                            "Firestore documents received: "
                                                    + querySnapshot.size()
                                    );

                                    allReports.clear();

                                    for (DocumentSnapshot document :
                                            querySnapshot.getDocuments()) {

                                        Log.d(
                                                TAG,
                                                "Reading report document: "
                                                        + document.getId()
                                        );

                                        ItemReport report =
                                                convertDocumentToReport(
                                                        document
                                                );

                                        if (report != null) {

                                            allReports.add(
                                                    report
                                            );

                                            Log.d(
                                                    TAG,
                                                    "Report loaded: "
                                                            + report.title
                                            );
                                        }
                                    }

                                    /*
                                     * Sort newest reports first.
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

                                    Log.d(
                                            TAG,
                                            "Reports successfully converted: "
                                                    + allReports.size()
                                    );

                                    updateStatistics();

                                    displayReports();
                                }
                        );
    }

    private ItemReport convertDocumentToReport(
            DocumentSnapshot document) {

        ItemReport report =
                new ItemReport();

        /*
         * Firestore document ID
         *
         * ItemReport.id is currently a long,
         * so we cannot store the Firestore ID here.
         *
         * We temporarily use the timestamp to
         * remain compatible with the existing app.
         */

        Long timestamp =
                document.getLong("timestamp");

        if (timestamp != null) {

            report.id =
                    timestamp;

            report.timestamp =
                    timestamp;

        } else {

            report.id =
                    0;

            report.timestamp =
                    0;
        }

        /*
         * Basic report information.
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
         * Photo
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
         * Status
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

        report.lostDate =
                getString(
                        document,
                        "lostDate"
                );

        /*
         * Notification.
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

        if (value instanceof Long) {

            return ((Long) value).intValue();
        }

        if (value instanceof Integer) {

            return (Integer) value;
        }

        if (value instanceof Double) {

            return ((Double) value).intValue();
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

    private void updateStatistics() {

        int total =
                allReports.size();

        int lost =
                0;

        int found =
                0;

        int resolved =
                0;

        for (ItemReport report :
                allReports) {

            if (report.type != null &&
                    report.type.equalsIgnoreCase(
                            "Lost"
                    )) {

                lost++;
            }

            if (report.type != null &&
                    report.type.equalsIgnoreCase(
                            "Found"
                    )) {

                found++;
            }

            if (report.status != null &&
                    (
                            report.status.equalsIgnoreCase(
                                    "Resolved"
                            )
                                    ||
                                    report.status.equalsIgnoreCase(
                                            "Claimed"
                                    )
                    )) {

                resolved++;
            }
        }

        statTotal.setText(
                String.valueOf(total)
        );

        statLost.setText(
                String.valueOf(lost)
        );

        statFound.setText(
                String.valueOf(found)
        );

        statResolved.setText(
                String.valueOf(resolved)
        );
    }

    private void displayReports() {

        if (allReports.isEmpty()) {

            recyclerView.setVisibility(
                    View.GONE
            );

            emptyText.setVisibility(
                    View.VISIBLE
            );

            emptyText.setText(
                    "No reports available."
            );

            return;
        }

        recyclerView.setVisibility(
                View.VISIBLE
        );

        emptyText.setVisibility(
                View.GONE
        );

        recyclerView.setAdapter(
                new ReportAdapter(
                        allReports,
                        report -> {

                            Intent intent =
                                    new Intent(
                                            AdminPanelActivity.this,
                                            ItemDetailsActivity.class
                                    );

                            intent.putExtra(
                                    "report_id",
                                    report.id
                            );

                            startActivity(
                                    intent
                            );
                        }
                )
        );
    }
}