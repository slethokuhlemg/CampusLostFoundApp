package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminReportDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private DocumentReference reportReference;

    private DocumentSnapshot reportDocument;

    private long reportId;

    private TextView titleText;
    private TextView statusText;
    private TextView typeText;
    private TextView categoryText;
    private TextView descriptionText;
    private TextView locationText;
    private TextView priorityText;
    private TextView scoreText;
    private TextView reporterText;

    private ImageView reportImage;

    private Button approveButton;
    private Button rejectButton;
    private Button resolveButton;
    private Button claimButton;
    private Button deleteButton;

    private ListenerRegistration reportListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_report_details
        );

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
                            AdminReportDetailsActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();

            return;
        }

        firestore =
                FirebaseFirestore.getInstance();

        titleText =
                findViewById(
                        R.id.adminDetailTitle
                );

        statusText =
                findViewById(
                        R.id.adminDetailStatus
                );

        reportImage =
                findViewById(
                        R.id.adminReportImage
                );

        typeText =
                findViewById(
                        R.id.adminDetailType
                );

        categoryText =
                findViewById(
                        R.id.adminDetailCategory
                );

        descriptionText =
                findViewById(
                        R.id.adminDetailDescription
                );

        locationText =
                findViewById(
                        R.id.adminDetailLocation
                );

        priorityText =
                findViewById(
                        R.id.adminDetailPriority
                );

        scoreText =
                findViewById(
                        R.id.adminDetailScore
                );

        reporterText =
                findViewById(
                        R.id.adminDetailReporter
                );

        approveButton =
                findViewById(
                        R.id.approveButton
                );

        rejectButton =
                findViewById(
                        R.id.rejectButton
                );

        resolveButton =
                findViewById(
                        R.id.resolveButton
                );

        claimButton =
                findViewById(
                        R.id.claimButton
                );

        deleteButton =
                findViewById(
                        R.id.deleteButton
                );

        reportId =
                getIntent().getLongExtra(
                        "report_id",
                        -1
                );

        if (reportId == -1) {

            Toast.makeText(
                    this,
                    "Report could not be found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        loadReport();
    }

    private void loadReport() {

        firestore
                .collection("reports")
                .whereEqualTo(
                        "timestamp",
                        reportId
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            if (querySnapshot.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "Report could not be found in Firebase.",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();

                                return;
                            }

                            reportDocument =
                                    querySnapshot
                                            .getDocuments()
                                            .get(0);

                            reportReference =
                                    reportDocument
                                            .getReference();

                            displayReport(
                                    reportDocument
                            );

                            listenForReportChanges();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not load report: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void listenForReportChanges() {

        if (reportReference == null) {
            return;
        }

        reportListener =
                reportReference
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {
                                        return;
                                    }

                                    if (snapshot == null ||
                                            !snapshot.exists()) {

                                        Toast.makeText(
                                                this,
                                                "This report no longer exists.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        finish();

                                        return;
                                    }

                                    reportDocument =
                                            snapshot;

                                    displayReport(
                                            snapshot
                                    );
                                }
                        );
    }

    private void displayReport(
            DocumentSnapshot document) {

        String title =
                getStringValue(
                        document,
                        "title"
                );

        String status =
                getStringValue(
                        document,
                        "status"
                );

        String type =
                getStringValue(
                        document,
                        "type"
                );

        String category =
                getStringValue(
                        document,
                        "category"
                );

        String description =
                getStringValue(
                        document,
                        "description"
                );

        String location =
                getStringValue(
                        document,
                        "location"
                );

        String priority =
                getStringValue(
                        document,
                        "priority"
                );

        int score =
                getIntValue(
                        document,
                        "score"
                );

        String reporterName =
                getStringValue(
                        document,
                        "reporterName"
                );

        String reporterEmail =
                getStringValue(
                        document,
                        "reporterEmail"
                );

        String dateDisplay =
                getStringValue(
                        document,
                        "dateDisplay"
                );

        String lostDate =
                getStringValue(
                        document,
                        "lostDate"
                );

        String lostTime =
                getStringValue(
                        document,
                        "lostTime"
                );

        String foundDate =
                getStringValue(
                        document,
                        "foundDate"
                );

        String foundTime =
                getStringValue(
                        document,
                        "foundTime"
                );

        String claimedDate =
                getStringValue(
                        document,
                        "claimedDate"
                );

        String claimedTime =
                getStringValue(
                        document,
                        "claimedTime"
                );

        titleText.setText(
                title
        );

        statusText.setText(
                "Status: " +
                        status
        );

        if ("Approved".equalsIgnoreCase(status)) {

            statusText.setTextColor(
                    Color.rgb(
                            43,
                            166,
                            122
                    )
            );

        } else if ("Rejected".equalsIgnoreCase(status)) {

            statusText.setTextColor(
                    Color.rgb(
                            214,
                            69,
                            69
                    )
            );

        } else if ("Pending".equalsIgnoreCase(status)) {

            statusText.setTextColor(
                    Color.rgb(
                            245,
                            158,
                            11
                    )
            );

        } else {

            statusText.setTextColor(
                    Color.DKGRAY
            );
        }

        typeText.setText(
                "Type: " +
                        type
        );

        categoryText.setText(
                "Category: " +
                        category
        );

        descriptionText.setText(
                "Description: " +
                        description
        );

        locationText.setText(
                "Location: " +
                        location
        );

        priorityText.setText(
                "AI Priority: " +
                        priority
        );

        scoreText.setText(
                "AI Score: " +
                        score +
                        "/100"
        );

        String reporterInfo =
                "Reported by: " +
                        reporterName +
                        "\nEmail: " +
                        reporterEmail +
                        "\nDate Reported: " +
                        dateDisplay;

        if (!lostDate.isEmpty()) {

            reporterInfo +=
                    "\nDate Lost: " +
                            lostDate;
        }

        if (!lostTime.isEmpty()) {

            reporterInfo +=
                    "\nTime Lost: " +
                            lostTime;
        }

        if (!foundDate.isEmpty()) {

            reporterInfo +=
                    "\nDate Found: " +
                            foundDate;
        }

        if (!foundTime.isEmpty()) {

            reporterInfo +=
                    "\nTime Found: " +
                            foundTime;
        }

        if (!claimedDate.isEmpty()) {

            reporterInfo +=
                    "\nDate Claimed: " +
                            claimedDate;
        }

        if (!claimedTime.isEmpty()) {

            reporterInfo +=
                    "\nTime Claimed: " +
                            claimedTime;
        }

        reporterText.setText(
                reporterInfo
        );

        displayPhoto(
                document
        );

        approveButton.setVisibility(
                View.GONE
        );

        rejectButton.setVisibility(
                View.GONE
        );

        resolveButton.setVisibility(
                View.GONE
        );

        claimButton.setVisibility(
                View.GONE
        );

        if ("Pending".equalsIgnoreCase(status)) {

            approveButton.setVisibility(
                    View.VISIBLE
            );

            rejectButton.setVisibility(
                    View.VISIBLE
            );
        }

        if ("Approved".equalsIgnoreCase(status)) {

            rejectButton.setVisibility(
                    View.VISIBLE
            );

            if ("Lost".equalsIgnoreCase(type)) {

                resolveButton.setVisibility(
                        View.VISIBLE
                );
            }

            if ("Found".equalsIgnoreCase(type)) {

                claimButton.setVisibility(
                        View.VISIBLE
                );
            }
        }

        approveButton.setOnClickListener(
                v -> approveReport()
        );

        rejectButton.setOnClickListener(
                v -> rejectReport()
        );

        resolveButton.setOnClickListener(
                v -> confirmResolve()
        );

        claimButton.setOnClickListener(
                v -> confirmClaim()
        );

        deleteButton.setOnClickListener(
                v -> confirmDelete()
        );
    }

    private void displayPhoto(
            DocumentSnapshot document) {

        String photoUrl =
                getStringValue(
                        document,
                        "photoUrl"
                );

        String photoUri =
                getStringValue(
                        document,
                        "photoUri"
                );

        String photo =
                !photoUrl.isEmpty()
                        ? photoUrl
                        : photoUri;

        if (photo.isEmpty()) {

            reportImage.setVisibility(
                    View.GONE
            );

            return;
        }

        if (photo.startsWith("content://") ||
                photo.startsWith("file://")) {

            try {

                Uri imageUri =
                        Uri.parse(
                                photo
                        );

                reportImage.setImageURI(
                        imageUri
                );

                reportImage.setVisibility(
                        View.VISIBLE
                );

                return;

            } catch (Exception ignored) {
            }
        }

        reportImage.setVisibility(
                View.GONE
        );
    }

    private void approveReport() {

        if (reportReference == null) {

            Toast.makeText(
                    this,
                    "Report is not ready.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Approve Report"
                )
                .setMessage(
                        "Are you sure you want to approve this report?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Approve",
                        (dialog, which) -> {

                            reportReference
                                    .update(
                                            "status",
                                            "Approved",
                                            "notification",
                                            "Your reported item has been approved and is now visible to users.",
                                            "notificationRead",
                                            false
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                Toast.makeText(
                                                        this,
                                                        "Report approved successfully.",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                finish();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                Toast.makeText(
                                                        this,
                                                        "Could not approve the report: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .show();
    }

    private void rejectReport() {

        if (reportReference == null) {

            Toast.makeText(
                    this,
                    "Report is not ready.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Reject Report"
                )
                .setMessage(
                        "Are you sure you want to reject this report?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Reject",
                        (dialog, which) -> {

                            reportReference
                                    .update(
                                            "status",
                                            "Rejected",
                                            "notification",
                                            "Your reported item has been rejected by the administrator.",
                                            "notificationRead",
                                            false
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                Toast.makeText(
                                                        this,
                                                        "Report rejected.",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                finish();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                Toast.makeText(
                                                        this,
                                                        "Could not reject the report: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .show();
    }

    private void confirmResolve() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Mark as Resolved"
                )
                .setMessage(
                        "Are you sure you want to mark this lost item as resolved?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Resolve",
                        (dialog, which) ->
                                resolveReport()
                )
                .show();
    }

    private void resolveReport() {

        if (reportReference == null) {

            Toast.makeText(
                    this,
                    "Report is not ready.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        long foundTimestamp =
                System.currentTimeMillis();

        Date foundDateObject =
                new Date(
                        foundTimestamp
                );

        String foundDate =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                ).format(
                        foundDateObject
                );

        String foundTime =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(
                        foundDateObject
                );

        reportReference
                .update(
                        "status",
                        "Resolved",
                        "foundDate",
                        foundDate,
                        "foundTime",
                        foundTime,
                        "foundTimestamp",
                        foundTimestamp,
                        "notification",
                        "Your lost item report has been marked as resolved. The item was found on "
                                + foundDate
                                + " at "
                                + foundTime
                                + ".",
                        "notificationRead",
                        false
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Report marked as resolved.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not resolve the report: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void confirmClaim() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Mark as Claimed"
                )
                .setMessage(
                        "Are you sure you want to mark this found item as claimed?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Claim",
                        (dialog, which) ->
                                claimReport()
                )
                .show();
    }

    private void claimReport() {

        if (reportReference == null) {

            Toast.makeText(
                    this,
                    "Report is not ready.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        long claimedTimestamp =
                System.currentTimeMillis();

        Date claimedDateObject =
                new Date(
                        claimedTimestamp
                );

        String claimedDate =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                ).format(
                        claimedDateObject
                );

        String claimedTime =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(
                        claimedDateObject
                );

        reportReference
                .update(
                        "status",
                        "Claimed",
                        "claimedDate",
                        claimedDate,
                        "claimedTime",
                        claimedTime,
                        "claimedTimestamp",
                        claimedTimestamp,
                        "notification",
                        "Your found item report has been marked as claimed on "
                                + claimedDate
                                + " at "
                                + claimedTime
                                + ".",
                        "notificationRead",
                        false
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Report marked as claimed.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not mark the report as claimed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void confirmDelete() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Delete Report"
                )
                .setMessage(
                        "Are you sure you want to permanently delete this report?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteReport()
                )
                .show();
    }

    private void deleteReport() {

        if (reportReference == null) {

            Toast.makeText(
                    this,
                    "Report is not ready.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        reportReference
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Report deleted successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not delete the report: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private String getStringValue(
            DocumentSnapshot document,
            String field) {

        Object value =
                document.get(field);

        if (value == null) {
            return "";
        }

        return String.valueOf(
                value
        );
    }

    private int getIntValue(
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
                    String.valueOf(
                            value
                    )
            );

        } catch (Exception e) {

            return 0;
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (reportListener != null) {

            reportListener.remove();

            reportListener = null;
        }
    }
}