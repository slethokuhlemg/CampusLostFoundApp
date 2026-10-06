package com.example.campuslostfound;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ListenerRegistration notificationsListener;

    private LinearLayout notificationsContainer;

    private TextView emptyText;

    private final List<DocumentSnapshot> notificationDocuments =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_notifications
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        TextView backButton =
                findViewById(
                        R.id.backButton
                );

        notificationsContainer =
                findViewById(
                        R.id.notificationsContainer
                );

        emptyText =
                findViewById(
                        R.id.emptyText
                );

        backButton.setOnClickListener(
                v -> finish()
        );

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please log in first.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent =
                    new Intent(
                            this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();

            return;
        }

        listenForNotifications();
    }

    private void listenForNotifications() {

        String email =
                firebaseAuth
                        .getCurrentUser()
                        .getEmail();

        if (email == null ||
                email.trim().isEmpty()) {

            emptyText.setVisibility(
                    View.VISIBLE
            );

            emptyText.setText(
                    "Could not find your account email."
            );

            return;
        }

        notificationsListener =
                firestore
                        .collection("reports")
                        .whereEqualTo(
                                "reporterEmail",
                                email
                        )
                        .addSnapshotListener(
                                (querySnapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Could not load notifications: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    if (querySnapshot == null) {
                                        return;
                                    }

                                    notificationDocuments.clear();

                                    for (DocumentSnapshot document :
                                            querySnapshot.getDocuments()) {

                                        String notification =
                                                getStringValue(
                                                        document,
                                                        "notification"
                                                );

                                        if (!notification.isEmpty()) {

                                            notificationDocuments.add(
                                                    document
                                            );
                                        }
                                    }

                                    Collections.sort(
                                            notificationDocuments,
                                            new Comparator<DocumentSnapshot>() {

                                                @Override
                                                public int compare(
                                                        DocumentSnapshot first,
                                                        DocumentSnapshot second) {

                                                    return Long.compare(
                                                            getTimestamp(second),
                                                            getTimestamp(first)
                                                    );
                                                }
                                            }
                                    );

                                    displayNotifications();
                                }
                        );
    }

    private void displayNotifications() {

        notificationsContainer.removeAllViews();

        if (notificationDocuments.isEmpty()) {

            emptyText.setVisibility(
                    View.VISIBLE
            );

            emptyText.setText(
                    "You have no notifications."
            );

            return;
        }

        emptyText.setVisibility(
                View.GONE
        );

        for (DocumentSnapshot document :
                notificationDocuments) {

            addNotificationCard(
                    document
            );
        }
    }

    private void addNotificationCard(
            DocumentSnapshot document) {

        String notification =
                getStringValue(
                        document,
                        "notification"
                );

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

        String date =
                getStringValue(
                        document,
                        "dateDisplay"
                );

        Boolean readValue =
                document.getBoolean(
                        "notificationRead"
                );

        boolean isRead =
                readValue != null &&
                        readValue;

        LinearLayout card =
                new LinearLayout(
                        this
                );

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding =
                dpToPixels(16);

        card.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dpToPixels(12)
        );

        card.setLayoutParams(
                cardParams
        );

        if (isRead) {

            card.setBackgroundResource(
                    R.drawable.bg_notification_read
            );

        } else {

            card.setBackgroundResource(
                    R.drawable.bg_notification_new
            );
        }

        if (!isRead) {

            TextView unreadText =
                    new TextView(
                            this
                    );

            unreadText.setText(
                    "NEW"
            );

            unreadText.setTextSize(
                    11
            );

            unreadText.setTextColor(
                    android.graphics.Color.WHITE
            );

            unreadText.setGravity(
                    Gravity.CENTER
            );

            unreadText.setTypeface(
                    null,
                    Typeface.BOLD
            );

            unreadText.setPadding(
                    dpToPixels(8),
                    dpToPixels(4),
                    dpToPixels(8),
                    dpToPixels(4)
            );

            unreadText.setBackgroundResource(
                    R.drawable.bg_notification_badge
            );

            LinearLayout.LayoutParams badgeParams =
                    new LinearLayout.LayoutParams(
                            dpToPixels(50),
                            dpToPixels(28)
                    );

            badgeParams.setMargins(
                    0,
                    0,
                    0,
                    dpToPixels(8)
            );

            unreadText.setLayoutParams(
                    badgeParams
            );

            card.addView(
                    unreadText
            );
        }

        TextView titleText =
                new TextView(
                        this
                );

        titleText.setText(
                title.isEmpty()
                        ? "Report Update"
                        : title
        );

        titleText.setTextSize(
                17
        );

        titleText.setTextColor(
                getResources().getColor(
                        R.color.text_primary
                )
        );

        titleText.setTypeface(
                null,
                Typeface.BOLD
        );

        TextView statusText =
                new TextView(
                        this
                );

        statusText.setText(
                "Status: " +
                        (
                                status.isEmpty()
                                        ? "Updated"
                                        : status
                        )
        );

        statusText.setTextSize(
                13
        );

        statusText.setTextColor(
                getResources().getColor(
                        R.color.brand_green_dark
                )
        );

        statusText.setPadding(
                0,
                dpToPixels(5),
                0,
                dpToPixels(5)
        );

        TextView messageText =
                new TextView(
                        this
                );

        messageText.setText(
                notification
        );

        messageText.setTextSize(
                14
        );

        messageText.setTextColor(
                getResources().getColor(
                        R.color.text_primary
                )
        );

        TextView dateText =
                new TextView(
                        this
                );

        dateText.setText(
                date
        );

        dateText.setTextSize(
                12
        );

        dateText.setTextColor(
                getResources().getColor(
                        R.color.text_secondary
                )
        );

        dateText.setPadding(
                0,
                dpToPixels(8),
                0,
                0
        );

        card.addView(
                titleText
        );

        card.addView(
                statusText
        );

        card.addView(
                messageText
        );

        if (!date.isEmpty()) {
            card.addView(
                    dateText
            );
        }

        card.setOnClickListener(
                v -> {

                    document
                            .getReference()
                            .update(
                                    "notificationRead",
                                    true
                            );

                    Long timestamp =
                            document.getLong(
                                    "timestamp"
                            );

                    if (timestamp != null) {

                        Intent intent =
                                new Intent(
                                        NotificationsActivity.this,
                                        ItemDetailsActivity.class
                                );

                        intent.putExtra(
                                "report_id",
                                timestamp
                        );

                        startActivity(
                                intent
                        );
                    }
                }
        );

        notificationsContainer.addView(
                card
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

        return String.valueOf(value);
    }

    private long getTimestamp(
            DocumentSnapshot document) {

        Long timestamp =
                document.getLong(
                        "timestamp"
                );

        if (timestamp == null) {
            return 0;
        }

        return timestamp;
    }

    private int dpToPixels(
            int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (notificationsListener != null) {

            notificationsListener.remove();

            notificationsListener = null;
        }
    }
}