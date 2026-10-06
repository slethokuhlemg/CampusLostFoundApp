package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminMessagesActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ListenerRegistration messagesListener;

    private LinearLayout messagesContainer;

    private TextView emptyText;

    private final Map<String, ConversationSummary> conversations =
            new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_messages
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

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
                            AdminMessagesActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();

            return;
        }

        TextView backButton =
                findViewById(
                        R.id.backButton
                );

        messagesContainer =
                findViewById(
                        R.id.messagesContainer
                );

        emptyText =
                findViewById(
                        R.id.emptyText
                );

        /*
         * The message input area is no longer needed
         * on the main admin inbox page.
         *
         * Replies are sent from AdminConversationActivity.
         */
        View messageEditText =
                findViewById(
                        R.id.messageEditText
                );

        View sendButton =
                findViewById(
                        R.id.sendButton
                );

        if (messageEditText != null) {
            messageEditText.setVisibility(View.GONE);
        }

        if (sendButton != null) {
            sendButton.setVisibility(View.GONE);
        }

        backButton.setOnClickListener(
                v -> finish()
        );

        listenForMessages();
    }

    private void listenForMessages() {

        if (messagesListener != null) {

            messagesListener.remove();

            messagesListener = null;
        }

        messagesListener =
                firestore
                        .collection("messages")
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Could not load messages: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    conversations.clear();

                                    if (snapshot == null) {

                                        displayConversations();

                                        return;
                                    }

                                    for (DocumentSnapshot document :
                                            snapshot.getDocuments()) {

                                        addMessageToConversation(
                                                document
                                        );
                                    }

                                    displayConversations();
                                }
                        );
    }

    private void addMessageToConversation(
            DocumentSnapshot document) {

        String senderRole =
                getStringValue(
                        document,
                        "senderRole"
                );

        String receiverRole =
                getStringValue(
                        document,
                        "receiverRole"
                );

        String senderUid =
                getStringValue(
                        document,
                        "senderUid"
                );

        String receiverUid =
                getStringValue(
                        document,
                        "receiverUid"
                );

        boolean isStudentMessage =
                senderRole.equalsIgnoreCase("user")
                        && receiverRole.equalsIgnoreCase("admin");

        boolean isAdminMessage =
                senderRole.equalsIgnoreCase("admin")
                        && receiverRole.equalsIgnoreCase("user");

        if (!isStudentMessage && !isAdminMessage) {
            return;
        }

        String studentUid;

        if (isStudentMessage) {

            studentUid = senderUid;

        } else {

            studentUid = receiverUid;
        }

        if (studentUid == null
                || studentUid.trim().isEmpty()) {

            return;
        }

        ConversationSummary summary =
                conversations.get(studentUid);

        if (summary == null) {

            summary =
                    new ConversationSummary();

            summary.studentUid =
                    studentUid;

            conversations.put(
                    studentUid,
                    summary
            );
        }

        long timestamp =
                getTimestamp(document);

        if (isStudentMessage) {

            String senderName =
                    getStringValue(
                            document,
                            "senderName"
                    );

            String senderEmail =
                    getStringValue(
                            document,
                            "senderEmail"
                    );

            if (!senderName.isEmpty()) {

                summary.studentName =
                        senderName;
            }

            if (!senderEmail.isEmpty()) {

                summary.studentEmail =
                        senderEmail;
            }

            Boolean read =
                    document.getBoolean(
                            "read"
                    );

            boolean isRead =
                    read != null && read;

            if (!isRead) {

                summary.unreadCount++;
            }
        }

        if (summary.latestMessage == null
                || timestamp >
                summary.latestTimestamp) {

            summary.latestMessage =
                    document;

            summary.latestTimestamp =
                    timestamp;
        }
    }

    private void displayConversations() {

        messagesContainer.removeAllViews();

        if (conversations.isEmpty()) {

            emptyText.setVisibility(
                    View.VISIBLE
            );

            emptyText.setText(
                    "No messages from students yet."
            );

            return;
        }

        emptyText.setVisibility(
                View.GONE
        );

        List<ConversationSummary> conversationList =
                new ArrayList<>(
                        conversations.values()
                );

        Collections.sort(
                conversationList,
                (first, second) ->
                        Long.compare(
                                second.latestTimestamp,
                                first.latestTimestamp
                        )
        );

        for (ConversationSummary conversation :
                conversationList) {

            addConversationView(
                    conversation
            );
        }
    }

    private void addConversationView(
            ConversationSummary conversation) {

        String studentName =
                conversation.studentName;

        if (studentName == null
                || studentName.trim().isEmpty()) {

            studentName =
                    "Student";
        }

        /*
         * IMPORTANT:
         *
         * studentName is modified above, so Java does
         * not consider it effectively final.
         *
         * We create a final copy for the lambda below.
         */
        final String finalStudentName =
                studentName;

        String email =
                conversation.studentEmail;

        if (email == null) {
            email = "";
        }

        /*
         * Same idea for email because it can also be
         * modified above.
         */
        final String finalStudentEmail =
                email;

        final String finalStudentUid =
                conversation.studentUid;

        String latestMessage =
                "";

        if (conversation.latestMessage != null) {

            latestMessage =
                    getStringValue(
                            conversation.latestMessage,
                            "message"
                    );
        }

        if (latestMessage.isEmpty()) {

            latestMessage =
                    "No message preview available.";
        }

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        int padding =
                dpToPixels(14);

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
                dpToPixels(10)
        );

        card.setLayoutParams(
                cardParams
        );

        /*
         * Student icon.
         */
        TextView avatar =
                new TextView(this);

        avatar.setText(
                getInitial(
                        finalStudentName
                )
        );

        avatar.setTextSize(
                18
        );

        avatar.setTypeface(
                null,
                Typeface.BOLD
        );

        avatar.setGravity(
                Gravity.CENTER
        );

        avatar.setTextColor(
                getColor(
                        R.color.brand_blue_dark
                )
        );

        avatar.setBackgroundResource(
                R.drawable.bg_circle_icon
        );

        LinearLayout.LayoutParams avatarParams =
                new LinearLayout.LayoutParams(
                        dpToPixels(52),
                        dpToPixels(52)
                );

        avatar.setLayoutParams(
                avatarParams
        );

        card.addView(
                avatar
        );

        /*
         * Middle content.
         */
        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams contentParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        contentParams.setMargins(
                dpToPixels(12),
                0,
                dpToPixels(8),
                0
        );

        content.setLayoutParams(
                contentParams
        );

        TextView nameText =
                new TextView(this);

        nameText.setText(
                finalStudentName
        );

        nameText.setTextSize(
                15
        );

        nameText.setTypeface(
                null,
                Typeface.BOLD
        );

        nameText.setTextColor(
                getColor(
                        R.color.text_primary
                )
        );

        content.addView(
                nameText
        );

        if (!finalStudentEmail.isEmpty()) {

            TextView emailText =
                    new TextView(this);

            emailText.setText(
                    finalStudentEmail
            );

            emailText.setTextSize(
                    11
            );

            emailText.setTextColor(
                    getColor(
                            R.color.text_secondary
                    )
            );

            LinearLayout.LayoutParams emailParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            emailParams.setMargins(
                    0,
                    dpToPixels(2),
                    0,
                    0
            );

            emailText.setLayoutParams(
                    emailParams
            );

            content.addView(
                    emailText
            );
        }

        TextView previewText =
                new TextView(this);

        previewText.setText(
                latestMessage
        );

        previewText.setTextSize(
                13
        );

        previewText.setTextColor(
                getColor(
                        R.color.text_secondary
                )
        );

        previewText.setMaxLines(
                1
        );

        previewText.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams previewParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        previewParams.setMargins(
                0,
                dpToPixels(5),
                0,
                0
        );

        previewText.setLayoutParams(
                previewParams
        );

        content.addView(
                previewText
        );

        card.addView(
                content
        );

        /*
         * Right side.
         */
        LinearLayout rightSide =
                new LinearLayout(this);

        rightSide.setOrientation(
                LinearLayout.VERTICAL
        );

        rightSide.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        TextView timeText =
                new TextView(this);

        timeText.setText(
                formatTime(
                        conversation.latestTimestamp
                )
        );

        timeText.setTextSize(
                10
        );

        timeText.setTextColor(
                getColor(
                        R.color.text_secondary
                )
        );

        rightSide.addView(
                timeText
        );

        if (conversation.unreadCount > 0) {

            TextView unreadText =
                    new TextView(this);

            String unread =
                    conversation.unreadCount > 99
                            ? "99+"
                            : String.valueOf(
                            conversation.unreadCount
                    );

            unreadText.setText(
                    unread
            );

            unreadText.setTextSize(
                    11
            );

            unreadText.setTypeface(
                    null,
                    Typeface.BOLD
            );

            unreadText.setGravity(
                    Gravity.CENTER
            );

            unreadText.setTextColor(
                    android.graphics.Color.WHITE
            );

            unreadText.setBackgroundResource(
                    R.drawable.bg_circle_icon
            );

            LinearLayout.LayoutParams unreadParams =
                    new LinearLayout.LayoutParams(
                            dpToPixels(26),
                            dpToPixels(26)
                    );

            unreadParams.setMargins(
                    0,
                    dpToPixels(7),
                    0,
                    0
            );

            unreadText.setLayoutParams(
                    unreadParams
            );

            rightSide.addView(
                    unreadText
            );
        }

        card.addView(
                rightSide
        );

        /*
         * Open the student's full conversation.
         *
         * IMPORTANT:
         * All values used here are final variables.
         */
        View.OnClickListener openConversation =
                v -> {

                    Intent intent =
                            new Intent(
                                    AdminMessagesActivity.this,
                                    AdminConversationActivity.class
                            );

                    intent.putExtra(
                            "studentUid",
                            finalStudentUid
                    );

                    intent.putExtra(
                            "studentName",
                            finalStudentName
                    );

                    intent.putExtra(
                            "studentEmail",
                            finalStudentEmail
                    );

                    startActivity(
                            intent
                    );
                };

        card.setOnClickListener(
                openConversation
        );

        avatar.setOnClickListener(
                openConversation
        );

        messagesContainer.addView(
                card
        );
    }

    private String getInitial(
            String name) {

        if (name == null
                || name.trim().isEmpty()) {

            return "S";
        }

        return name
                .trim()
                .substring(
                        0,
                        1
                )
                .toUpperCase(
                        Locale.getDefault()
                );
    }

    private String formatTime(
            long timestamp) {

        if (timestamp <= 0) {

            return "";
        }

        long now =
                System.currentTimeMillis();

        long difference =
                now - timestamp;

        if (difference <
                24L * 60L * 60L * 1000L) {

            return new SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
            ).format(
                    new Date(timestamp)
            );
        }

        return new SimpleDateFormat(
                "dd MMM",
                Locale.getDefault()
        ).format(
                new Date(timestamp)
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

        if (messagesListener != null) {

            messagesListener.remove();

            messagesListener = null;
        }
    }

    private static class ConversationSummary {

        String studentUid = "";

        String studentName = "";

        String studentEmail = "";

        DocumentSnapshot latestMessage;

        long latestTimestamp = 0;

        int unreadCount = 0;
    }
}