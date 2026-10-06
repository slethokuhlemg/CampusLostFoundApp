package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private ItemReport report;

    private String firestoreDocumentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_details);

        // --------------------------------------------------
        // FIREBASE
        // --------------------------------------------------

        firestore =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();

        // --------------------------------------------------
        // BACK BUTTON
        // --------------------------------------------------

        ImageButton backButton =
                findViewById(R.id.backButton);

        backButton.setOnClickListener(
                v -> finish()
        );

        // --------------------------------------------------
        // GET FIRESTORE DOCUMENT ID
        // --------------------------------------------------

        String passedFirestoreId =
                getIntent().getStringExtra(
                        "firestore_id"
                );

        if (passedFirestoreId != null) {

            firestoreDocumentId =
                    passedFirestoreId.trim();
        }

        // --------------------------------------------------
        // GET REPORT TIMESTAMP
        // --------------------------------------------------

        long reportId =
                getIntent().getLongExtra(
                        "report_id",
                        -1
                );

        /*
         * We prefer the Firestore document ID.
         *
         * If it was not passed, we fall back to the
         * timestamp used by the older version of the app.
         */
        if (!firestoreDocumentId.isEmpty()) {

            loadReportByFirestoreId(
                    firestoreDocumentId
            );

        } else if (reportId != -1) {

            loadReportByTimestamp(
                    reportId
            );

        } else {

            Toast.makeText(
                    this,
                    "Report ID is missing.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    // ======================================================
    // LOAD REPORT BY FIRESTORE DOCUMENT ID
    // ======================================================

    private void loadReportByFirestoreId(
            String documentId) {

        firestore
                .collection("reports")
                .document(documentId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (document == null
                                    || !document.exists()) {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "This report could not be found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                                return;
                            }

                            report =
                                    convertDocumentToReport(
                                            document
                                    );

                            if (report == null) {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "Could not read this report.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                                return;
                            }

                            /*
                             * Keep the exact Firestore ID.
                             */
                            report.firestoreId =
                                    document.getId();

                            displayReport();
                        }
                )
                .addOnFailureListener(
                        error -> {

                            Toast.makeText(
                                    ItemDetailsActivity.this,
                                    "Could not load report: "
                                            + error.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ======================================================
    // LOAD REPORT BY TIMESTAMP
    // ======================================================

    private void loadReportByTimestamp(
            long reportId) {

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

                            if (querySnapshot == null
                                    || querySnapshot.isEmpty()) {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "This report could not be found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                                return;
                            }

                            DocumentSnapshot document =
                                    querySnapshot
                                            .getDocuments()
                                            .get(0);

                            /*
                             * Save the Firestore document ID so all
                             * future updates use the exact document.
                             */
                            firestoreDocumentId =
                                    document.getId();

                            report =
                                    convertDocumentToReport(
                                            document
                                    );

                            if (report == null) {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "Could not read this report.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                                return;
                            }

                            report.firestoreId =
                                    document.getId();

                            displayReport();
                        }
                )
                .addOnFailureListener(
                        error -> {

                            Toast.makeText(
                                    ItemDetailsActivity.this,
                                    "Could not load report: "
                                            + error.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ======================================================
    // CONVERT FIRESTORE DOCUMENT
    // ======================================================

    private ItemReport convertDocumentToReport(
            DocumentSnapshot document) {

        try {

            ItemReport item =
                    new ItemReport();

            // --------------------------------------------------
            // FIRESTORE ID
            // --------------------------------------------------

            item.firestoreId =
                    document.getId();

            // --------------------------------------------------
            // TIMESTAMP / ID
            // --------------------------------------------------

            Long timestamp =
                    document.getLong(
                            "timestamp"
                    );

            if (timestamp != null) {

                item.id =
                        timestamp;

                item.timestamp =
                        timestamp;

            } else {

                item.id =
                        0;

                item.timestamp =
                        0;
            }

            // --------------------------------------------------
            // REPORT INFORMATION
            // --------------------------------------------------

            item.type =
                    getString(
                            document,
                            "type"
                    );

            item.category =
                    getString(
                            document,
                            "category"
                    );

            item.title =
                    getString(
                            document,
                            "title"
                    );

            item.description =
                    getString(
                            document,
                            "description"
                    );

            item.location =
                    getString(
                            document,
                            "location"
                    );

            item.status =
                    getString(
                            document,
                            "status"
                    );

            // --------------------------------------------------
            // REPORTER
            // --------------------------------------------------

            item.reporterName =
                    getString(
                            document,
                            "reporterName"
                    );

            item.reporterEmail =
                    getString(
                            document,
                            "reporterEmail"
                    );

            // --------------------------------------------------
            // DATE AND TIME INFORMATION
            // --------------------------------------------------

            item.dateDisplay =
                    getString(
                            document,
                            "dateDisplay"
                    );

            item.lostDate =
                    getString(
                            document,
                            "lostDate"
                    );

            item.lostTime =
                    getString(
                            document,
                            "lostTime"
                    );

            item.foundDate =
                    getString(
                            document,
                            "foundDate"
                    );

            item.foundTime =
                    getString(
                            document,
                            "foundTime"
                    );

            Long foundTimestamp =
                    document.getLong(
                            "foundTimestamp"
                    );

            if (foundTimestamp != null) {

                item.foundTimestamp =
                        foundTimestamp;

            } else {

                item.foundTimestamp =
                        0;
            }

            item.claimedDate =
                    getString(
                            document,
                            "claimedDate"
                    );

            item.claimedTime =
                    getString(
                            document,
                            "claimedTime"
                    );

            Long claimedTimestamp =
                    document.getLong(
                            "claimedTimestamp"
                    );

            if (claimedTimestamp != null) {

                item.claimedTimestamp =
                        claimedTimestamp;

            } else {

                item.claimedTimestamp =
                        0;
            }

            // --------------------------------------------------
            // PHOTO
            // --------------------------------------------------

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

                item.photoUri =
                        photoUrl;

            } else if (!photoUri.isEmpty()) {

                item.photoUri =
                        photoUri;

            } else {

                item.photoUri =
                        "";
            }

            // --------------------------------------------------
            // PRIORITY
            // --------------------------------------------------

            item.priority =
                    getString(
                            document,
                            "priority"
                    );

            Long score =
                    document.getLong(
                            "score"
                    );

            if (score != null) {

                item.score =
                        score.intValue();

            } else {

                item.score =
                        0;
            }

            // --------------------------------------------------
            // NOTIFICATION
            // --------------------------------------------------

            item.notification =
                    getString(
                            document,
                            "notification"
                    );

            Boolean notificationRead =
                    document.getBoolean(
                            "notificationRead"
                    );

            item.notificationRead =
                    notificationRead != null
                            && notificationRead;

            return item;

        } catch (Exception e) {

            return null;
        }
    }

    // ======================================================
    // GET STRING
    // ======================================================

    private String getString(
            DocumentSnapshot document,
            String field) {

        String value =
                document.getString(
                        field
                );

        if (value == null) {

            return "";
        }

        return value;
    }

    // ======================================================
    // DISPLAY REPORT
    // ======================================================

    private void displayReport() {

        ImageView image =
                findViewById(
                        R.id.detailImage
                );

        TextView letter =
                findViewById(
                        R.id.detailLetter
                );

        TextView statusBadge =
                findViewById(
                        R.id.detailStatusBadge
                );

        TextView title =
                findViewById(
                        R.id.detailTitle
                );

        TextView subtitle =
                findViewById(
                        R.id.detailSubtitle
                );

        TextView description =
                findViewById(
                        R.id.detailDescription
                );

        TextView reporterLabel =
                findViewById(
                        R.id.detailReporterLabel
                );

        TextView reporterName =
                findViewById(
                        R.id.detailReporterName
                );

        Button contactButton =
                findViewById(
                        R.id.contactButton
                );

        Button resolveButton =
                findViewById(
                        R.id.resolveButton
                );

        // --------------------------------------------------
        // TITLE
        // --------------------------------------------------

        String titleText =
                report.title;

        if (titleText == null
                || titleText.trim().isEmpty()) {

            titleText =
                    "Untitled Item";
        }

        title.setText(
                titleText
        );

        // --------------------------------------------------
        // STATUS
        // --------------------------------------------------

        String status =
                report.status;

        if (status == null
                || status.trim().isEmpty()) {

            status =
                    "Unknown";
        }

        statusBadge.setText(
                status
        );

        // --------------------------------------------------
        // TYPE
        // --------------------------------------------------

        String type =
                report.type;

        if (type == null
                || type.trim().isEmpty()) {

            type =
                    "Item";
        }

        // --------------------------------------------------
        // LOCATION
        // --------------------------------------------------

        String location =
                report.location;

        if (location == null
                || location.trim().isEmpty()) {

            location =
                    "Unknown location";
        }

        // --------------------------------------------------
        // BUILD DATE / TIME
        // --------------------------------------------------

        StringBuilder subtitleText =
                new StringBuilder();

        subtitleText.append(
                type
        );

        subtitleText.append(
                " in "
        );

        subtitleText.append(
                location
        );

        // --------------------------------------------------
        // DATE REPORTED
        // --------------------------------------------------

        if (report.dateDisplay != null
                && !report.dateDisplay.trim().isEmpty()) {

            subtitleText.append(
                    "\nReported: "
            );

            subtitleText.append(
                    report.dateDisplay
            );
        }

        // --------------------------------------------------
        // LOST DATE / TIME
        // --------------------------------------------------

        if (report.lostDate != null
                && !report.lostDate.trim().isEmpty()) {

            subtitleText.append(
                    "\nLost: "
            );

            subtitleText.append(
                    report.lostDate
            );

            if (report.lostTime != null
                    && !report.lostTime.trim().isEmpty()) {

                subtitleText.append(
                        " · "
                );

                subtitleText.append(
                        report.lostTime
                );
            }
        }

        // --------------------------------------------------
        // FOUND DATE / TIME
        // --------------------------------------------------

        if (report.foundDate != null
                && !report.foundDate.trim().isEmpty()) {

            subtitleText.append(
                    "\nFound: "
            );

            subtitleText.append(
                    report.foundDate
            );

            if (report.foundTime != null
                    && !report.foundTime.trim().isEmpty()) {

                subtitleText.append(
                        " · "
                );

                subtitleText.append(
                        report.foundTime
                );
            }
        }

        // --------------------------------------------------
        // CLAIMED DATE / TIME
        // --------------------------------------------------

        if (report.claimedDate != null
                && !report.claimedDate.trim().isEmpty()) {

            subtitleText.append(
                    "\nClaimed: "
            );

            subtitleText.append(
                    report.claimedDate
            );

            if (report.claimedTime != null
                    && !report.claimedTime.trim().isEmpty()) {

                subtitleText.append(
                        " · "
                );

                subtitleText.append(
                        report.claimedTime
                );
            }
        }

        subtitle.setText(
                subtitleText.toString()
        );

        // --------------------------------------------------
        // STATUS COLOUR
        // --------------------------------------------------

        setStatusColour(
                statusBadge,
                status
        );

        // --------------------------------------------------
        // DESCRIPTION
        // --------------------------------------------------

        if (report.description != null
                && !report.description.trim().isEmpty()) {

            description.setText(
                    report.description
            );

            description.setVisibility(
                    View.VISIBLE
            );

        } else {

            description.setText(
                    "No description provided."
            );

            description.setVisibility(
                    View.VISIBLE
            );
        }

        // --------------------------------------------------
        // REPORTER
        // --------------------------------------------------

        if (type.equalsIgnoreCase(
                "Found"
        )) {

            reporterLabel.setText(
                    "Found by"
            );

        } else {

            reporterLabel.setText(
                    "Reported by"
            );
        }

        String reporter =
                report.reporterName;

        if (reporter == null
                || reporter.trim().isEmpty()) {

            reporter =
                    "Unknown user";
        }

        reporterName.setText(
                reporter
        );

        // --------------------------------------------------
        // IMAGE
        // --------------------------------------------------

        displayImage(
                image,
                letter
        );

        // --------------------------------------------------
        // CONTACT BUTTON
        // --------------------------------------------------

        contactButton.setOnClickListener(
                v -> contactReporter()
        );

        // --------------------------------------------------
        // OWNER
        // --------------------------------------------------

        String currentEmail =
                getCurrentUserEmail();

        boolean isOwner =
                !currentEmail.isEmpty()
                        &&
                        report.reporterEmail != null
                        &&
                        currentEmail.equalsIgnoreCase(
                                report.reporterEmail.trim()
                        );

        // --------------------------------------------------
        // CHECK IF ALREADY FINISHED
        // --------------------------------------------------

        boolean alreadyFinished =
                report.status != null
                        &&
                        (
                                report.status.equalsIgnoreCase(
                                        "Resolved"
                                )
                                        ||
                                        report.status.equalsIgnoreCase(
                                                "Claimed"
                                        )
                        );

        // --------------------------------------------------
        // RESOLVE / CLAIM BUTTON
        // --------------------------------------------------

        if (isOwner
                && !alreadyFinished
                && "Approved".equalsIgnoreCase(
                report.status
        )) {

            resolveButton.setVisibility(
                    View.VISIBLE
            );

            if ("Found".equalsIgnoreCase(
                    type
            )) {

                resolveButton.setText(
                        "Mark as Claimed"
                );

            } else {

                resolveButton.setText(
                        "Mark as Found"
                );
            }

            resolveButton.setOnClickListener(
                    v -> updateReportStatus()
            );

        } else {

            resolveButton.setVisibility(
                    View.GONE
            );
        }
    }

    // ======================================================
    // GET CURRENT USER EMAIL
    // ======================================================

    private String getCurrentUserEmail() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user != null
                && user.getEmail() != null
                && !user.getEmail().trim().isEmpty()) {

            return user.getEmail().trim();
        }

        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        String email =
                prefs.getString(
                        "email",
                        ""
                );

        if (email == null) {

            return "";
        }

        return email.trim();
    }

    // ======================================================
    // SET STATUS COLOUR
    // ======================================================

    private void setStatusColour(
            TextView statusBadge,
            String status) {

        if ("Approved".equalsIgnoreCase(
                status
        )) {

            statusBadge.setTextColor(
                    Color.rgb(
                            43,
                            166,
                            122
                    )
            );

            statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    230,
                                    246,
                                    241
                            )
                    )
            );

        } else if ("Rejected".equalsIgnoreCase(
                status
        )) {

            statusBadge.setTextColor(
                    Color.rgb(
                            214,
                            69,
                            69
                    )
            );

            statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    251,
                                    233,
                                    233
                            )
                    )
            );

        } else if ("Pending".equalsIgnoreCase(
                status
        )) {

            statusBadge.setTextColor(
                    Color.rgb(
                            245,
                            158,
                            11
                    )
            );

            statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    254,
                                    243,
                                    220
                            )
                    )
            );

        } else if ("Resolved".equalsIgnoreCase(
                status
        )
                || "Claimed".equalsIgnoreCase(
                status
        )) {

            statusBadge.setTextColor(
                    Color.rgb(
                            30,
                            111,
                            217
                    )
            );

            statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    234,
                                    243,
                                    255
                            )
                    )
            );

        } else {

            statusBadge.setTextColor(
                    Color.DKGRAY
            );
        }
    }

    // ======================================================
    // DISPLAY IMAGE
    // ======================================================

    private void displayImage(
            ImageView image,
            TextView letter) {

        /*
         * Local content/file URI.
         */
        if (report.photoUri != null
                && !report.photoUri.trim().isEmpty()
                &&
                (
                        report.photoUri.startsWith(
                                "content://"
                        )
                                ||
                                report.photoUri.startsWith(
                                        "file://"
                                )
                )) {

            try {

                image.setImageURI(
                        Uri.parse(
                                report.photoUri
                        )
                );

                if (image.getDrawable() != null) {

                    image.setVisibility(
                            View.VISIBLE
                    );

                    letter.setVisibility(
                            View.GONE
                    );

                    return;
                }

            } catch (Exception ignored) {
            }
        }

        /*
         * Fallback letter.
         */
        image.setVisibility(
                View.GONE
        );

        letter.setVisibility(
                View.VISIBLE
        );

        String category =
                report.category;

        if (category == null
                || category.trim().isEmpty()) {

            category =
                    "?";
        }

        letter.setText(
                category
                        .substring(
                                0,
                                1
                        )
                        .toUpperCase(
                                Locale.getDefault()
                        )
        );
    }

    // ======================================================
    // CONTACT REPORTER
    // ======================================================

    private void contactReporter() {

        if (report == null) {

            return;
        }

        if (report.reporterEmail == null
                || report.reporterEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "No email address available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        Intent.ACTION_SENDTO
                );

        intent.setData(
                Uri.parse(
                        "mailto:"
                                + report.reporterEmail
                )
        );

        intent.putExtra(
                Intent.EXTRA_SUBJECT,
                "Regarding your "
                        + report.type
                        + " item: "
                        + report.title
        );

        try {

            startActivity(
                    intent
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "No email app available.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ======================================================
    // UPDATE REPORT STATUS
    // ======================================================

    private void updateReportStatus() {

        if (report == null) {

            return;
        }

        if (firestoreDocumentId == null
                || firestoreDocumentId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Report document ID is missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String currentEmail =
                getCurrentUserEmail();

        /*
         * Security check.
         */
        if (currentEmail.isEmpty()
                || report.reporterEmail == null
                || !currentEmail.equalsIgnoreCase(
                report.reporterEmail.trim()
        )) {

            Toast.makeText(
                    this,
                    "You can only update your own report.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Only Approved reports can be changed.
         */
        if (!"Approved".equalsIgnoreCase(
                report.status
        )) {

            Toast.makeText(
                    this,
                    "Only approved reports can be updated.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // --------------------------------------------------
        // CURRENT DATE / TIME
        // --------------------------------------------------

        long currentTimestamp =
                System.currentTimeMillis();

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

        String currentDate =
                dateFormat.format(
                        new Date(
                                currentTimestamp
                        )
                );

        String currentTime =
                timeFormat.format(
                        new Date(
                                currentTimestamp
                        )
                );

        // ==================================================
        // LOST -> FOUND
        // ==================================================

        if ("Lost".equalsIgnoreCase(
                report.type
        )) {

            /*
             * Keep the original Lost date/time.
             *
             * Change only the type and add Found information.
             */
            firestore
                    .collection("reports")
                    .document(
                            firestoreDocumentId
                    )
                    .update(
                            "type",
                            "Found",
                            "status",
                            "Approved",
                            "foundDate",
                            currentDate,
                            "foundTime",
                            currentTime,
                            "foundTimestamp",
                            currentTimestamp,
                            "notification",
                            "Your lost item was marked as found on "
                                    + currentDate
                                    + " at "
                                    + currentTime
                                    + ".",
                            "notificationRead",
                            false
                    )
                    .addOnSuccessListener(
                            unused -> {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "Item moved to Found.",
                                        Toast.LENGTH_LONG
                                ).show();

                                /*
                                 * Return to the previous screen.
                                 *
                                 * MyReportsActivity will reload
                                 * and the item will now appear
                                 * under Found.
                                 */
                                finish();
                            }
                    )
                    .addOnFailureListener(
                            error -> {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "Could not update report: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                    );

            return;
        }

        // ==================================================
        // FOUND -> CLAIMED
        // ==================================================

        if ("Found".equalsIgnoreCase(
                report.type
        )) {

            /*
             * Keep the Found information.
             *
             * Change status to Claimed.
             *
             * Save the exact claim date/time.
             */
            firestore
                    .collection("reports")
                    .document(
                            firestoreDocumentId
                    )
                    .update(
                            "status",
                            "Claimed",
                            "claimedDate",
                            currentDate,
                            "claimedTime",
                            currentTime,
                            "claimedTimestamp",
                            currentTimestamp,
                            "notification",
                            "This item was marked as claimed on "
                                    + currentDate
                                    + " at "
                                    + currentTime
                                    + ".",
                            "notificationRead",
                            false
                    )
                    .addOnSuccessListener(
                            unused -> {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "Item marked as Claimed.",
                                        Toast.LENGTH_LONG
                                ).show();

                                /*
                                 * Returning to My Reports will
                                 * allow the Claimed History tab
                                 * to display this item.
                                 */
                                finish();
                            }
                    )
                    .addOnFailureListener(
                            error -> {

                                Toast.makeText(
                                        ItemDetailsActivity.this,
                                        "Could not update report: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                    );

            return;
        }

        Toast.makeText(
                this,
                "This report cannot be updated.",
                Toast.LENGTH_SHORT
        ).show();
    }
}