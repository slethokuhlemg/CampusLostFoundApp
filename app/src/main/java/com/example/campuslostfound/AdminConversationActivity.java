package com.example.campuslostfound;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminConversationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ListenerRegistration studentMessagesListener;
    private ListenerRegistration adminMessagesListener;

    private LinearLayout messagesContainer;

    private EditText messageEditText;

    private TextView emptyText;

    private TextView titleText;

    private String studentUid = "";

    private String studentName = "Student";

    private String studentEmail = "";

    private final List<DocumentSnapshot> messages =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_messages
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        studentUid =
                getIntent().getStringExtra(
                        "studentUid"
                );

        String receivedName =
                getIntent().getStringExtra(
                        "studentName"
                );

        String receivedEmail =
                getIntent().getStringExtra(
                        "studentEmail"
                );

        if (receivedName != null
                && !receivedName.trim().isEmpty()) {

            studentName =
                    receivedName;
        }

        if (receivedEmail != null) {

            studentEmail =
                    receivedEmail;
        }

        if (studentUid == null) {

            studentUid = "";
        }

        TextView backButton =
                findViewById(
                        R.id.backButton
                );

        messagesContainer =
                findViewById(
                        R.id.messagesContainer
                );

        messageEditText =
                findViewById(
                        R.id.messageEditText
                );

        Button sendButton =
                findViewById(
                        R.id.sendButton
                );

        emptyText =
                findViewById(
                        R.id.emptyText
                );

        int titleId =
                getResources().getIdentifier(
                        "titleText",
                        "id",
                        getPackageName()
                );

        if (titleId != 0) {

            titleText =
                    findViewById(
                            titleId
                    );
        }

        if (titleText != null) {

            titleText.setText(
                    studentName
            );
        }

        backButton.setOnClickListener(
                v -> finish()
        );

        if (studentUid.isEmpty()) {

            Toast.makeText(
                    this,
                    "Student account could not be identified.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        sendButton.setOnClickListener(
                v -> sendReply()
        );

        listenForConversation();
    }

    private void listenForConversation() {

        if (studentMessagesListener != null) {

            studentMessagesListener.remove();

            studentMessagesListener = null;
        }

        studentMessagesListener =
                firestore
                        .collection("messages")
                        .whereEqualTo(
                                "senderUid",
                                studentUid
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Could not load student messages: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    messages.clear();

                                    if (snapshot != null) {

                                        for (DocumentSnapshot document :
                                                snapshot.getDocuments()) {

                                            String receiverRole =
                                                    getStringValue(
                                                            document,
                                                            "receiverRole"
                                                    );

                                            if (receiverRole.equalsIgnoreCase(
                                                    "admin"
                                            )) {

                                                addOrReplaceMessage(
                                                        document
                                                );

                                                markAsRead(
                                                        document
                                                );
                                            }
                                        }
                                    }

                                    loadAdminMessages();
                                }
                        );
    }

    private void loadAdminMessages() {

        if (adminMessagesListener != null) {

            adminMessagesListener.remove();

            adminMessagesListener = null;
        }

        adminMessagesListener =
                firestore
                        .collection("messages")
                        .whereEqualTo(
                                "receiverUid",
                                studentUid
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Could not load admin replies: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    if (snapshot != null) {

                                        for (DocumentSnapshot document :
                                                snapshot.getDocuments()) {

                                            String senderRole =
                                                    getStringValue(
                                                            document,
                                                            "senderRole"
                                                    );

                                            if (senderRole.equalsIgnoreCase(
                                                    "admin"
                                            )) {

                                                addOrReplaceMessage(
                                                        document
                                                );
                                            }
                                        }
                                    }

                                    sortAndDisplayMessages();
                                }
                        );
    }

    private void markAsRead(
            DocumentSnapshot document) {

        Boolean read =
                document.getBoolean(
                        "read"
                );

        boolean isRead =
                read != null && read;

        if (!isRead) {

            document
                    .getReference()
                    .update(
                            "read",
                            true
                    );
        }
    }

    private void sendReply() {

        FirebaseUser admin =
                firebaseAuth.getCurrentUser();

        if (admin == null) {

            Toast.makeText(
                    this,
                    "Admin account not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (studentUid.isEmpty()) {

            Toast.makeText(
                    this,
                    "Student account could not be identified.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String message =
                messageEditText
                        .getText()
                        .toString()
                        .trim();

        if (message.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please type a message.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String adminEmail =
                admin.getEmail() == null
                        ? ""
                        : admin.getEmail();

        Map<String, Object> messageData =
                new HashMap<>();

        messageData.put(
                "senderUid",
                admin.getUid()
        );

        messageData.put(
                "senderEmail",
                adminEmail
        );

        messageData.put(
                "senderName",
                "Administrator"
        );

        messageData.put(
                "senderRole",
                "admin"
        );

        messageData.put(
                "receiverUid",
                studentUid
        );

        messageData.put(
                "receiverRole",
                "user"
        );

        messageData.put(
                "message",
                message
        );

        messageData.put(
                "timestamp",
                System.currentTimeMillis()
        );

        messageData.put(
                "read",
                false
        );

        firestore
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(
                        unused -> {

                            messageEditText.setText("");

                            Toast.makeText(
                                    this,
                                    "Reply sent to "
                                            + studentName,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not send reply: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void addOrReplaceMessage(
            DocumentSnapshot document) {

        for (int i = 0;
             i < messages.size();
             i++) {

            if (messages
                    .get(i)
                    .getId()
                    .equals(
                            document.getId()
                    )) {

                messages.set(
                        i,
                        document
                );

                return;
            }
        }

        messages.add(
                document
        );
    }

    private void sortAndDisplayMessages() {

        Collections.sort(
                messages,
                new Comparator<DocumentSnapshot>() {

                    @Override
                    public int compare(
                            DocumentSnapshot first,
                            DocumentSnapshot second) {

                        return Long.compare(
                                getTimestamp(first),
                                getTimestamp(second)
                        );
                    }
                }
        );

        displayMessages();
    }

    private void displayMessages() {

        messagesContainer.removeAllViews();

        if (messages.isEmpty()) {

            emptyText.setVisibility(
                    View.VISIBLE
            );

            emptyText.setText(
                    "No messages in this conversation yet."
            );

            return;
        }

        emptyText.setVisibility(
                View.GONE
        );

        for (DocumentSnapshot document :
                messages) {

            addMessageView(
                    document
            );
        }
    }

    private void addMessageView(
            DocumentSnapshot document) {

        String senderRole =
                getStringValue(
                        document,
                        "senderRole"
                );

        String message =
                getStringValue(
                        document,
                        "message"
                );

        long timestamp =
                getTimestamp(
                        document
                );

        boolean isAdmin =
                senderRole.equalsIgnoreCase(
                        "admin"
                );

        /*
         * OUTER ROW
         *
         * Admin messages = RIGHT
         * Student messages = LEFT
         */
        LinearLayout messageRow =
                new LinearLayout(this);

        messageRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        messageRow.setGravity(
                isAdmin
                        ? Gravity.END
                        : Gravity.START
        );

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        rowParams.setMargins(
                dpToPixels(8),
                dpToPixels(4),
                dpToPixels(8),
                dpToPixels(4)
        );

        messageRow.setLayoutParams(
                rowParams
        );

        /*
         * MESSAGE BUBBLE
         */
        LinearLayout bubble =
                new LinearLayout(this);

        bubble.setOrientation(
                LinearLayout.VERTICAL
        );

        bubble.setPadding(
                dpToPixels(14),
                dpToPixels(10),
                dpToPixels(14),
                dpToPixels(8)
        );

        /*
         * Keep messages from becoming
         * full-screen width.
         */
        LinearLayout.LayoutParams bubbleParams =
                new LinearLayout.LayoutParams(
                        (int) (
                                getResources()
                                        .getDisplayMetrics()
                                        .widthPixels
                                        * 0.78f
                        ),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        bubble.setLayoutParams(
                bubbleParams
        );

        GradientDrawable bubbleBackground =
                new GradientDrawable();

        if (isAdmin) {

            /*
             * ADMIN MESSAGE
             *
             * Dark/strong blue
             * on the RIGHT.
             */
            bubbleBackground.setColor(
                    Color.rgb(
                            30,
                            111,
                            217
                    )
            );

            bubbleBackground.setCornerRadii(
                    new float[]{
                            dpToPixels(18),
                            dpToPixels(18),

                            dpToPixels(18),
                            dpToPixels(18),

                            dpToPixels(4),
                            dpToPixels(4),

                            dpToPixels(18),
                            dpToPixels(18)
                    }
            );

        } else {

            /*
             * STUDENT MESSAGE
             *
             * LIGHT BLUE
             * on the LEFT.
             */
            bubbleBackground.setColor(
                    Color.rgb(
                            225,
                            236,
                            250
                    )
            );

            bubbleBackground.setCornerRadii(
                    new float[]{
                            dpToPixels(18),
                            dpToPixels(18),

                            dpToPixels(18),
                            dpToPixels(18),

                            dpToPixels(18),
                            dpToPixels(18),

                            dpToPixels(4),
                            dpToPixels(4)
                    }
            );
        }

        bubble.setBackground(
                bubbleBackground
        );

        /*
         * SENDER NAME
         */
        TextView senderText =
                new TextView(this);

        if (isAdmin) {

            senderText.setText(
                    "You"
            );

            senderText.setTextColor(
                    Color.WHITE
            );

        } else {

            senderText.setText(
                    studentName
            );

            senderText.setTextColor(
                    Color.rgb(
                            30,
                            111,
                            217
                    )
            );
        }

        senderText.setTextSize(
                12
        );

        senderText.setTypeface(
                null,
                Typeface.BOLD
        );

        bubble.addView(
                senderText
        );

        /*
         * MESSAGE TEXT
         */
        TextView messageText =
                new TextView(this);

        messageText.setText(
                message
        );

        messageText.setTextSize(
                15
        );

        messageText.setTextColor(
                isAdmin
                        ? Color.WHITE
                        : getColor(
                        R.color.text_primary
                )
        );

        messageText.setPadding(
                0,
                dpToPixels(4),
                0,
                0
        );

        bubble.addView(
                messageText
        );

        /*
         * TIME
         */
        if (timestamp > 0) {

            TextView timeText =
                    new TextView(this);

            timeText.setText(
                    formatTime(
                            timestamp
                    )
            );

            timeText.setTextSize(
                    9
            );

            timeText.setTextColor(
                    isAdmin
                            ? Color.rgb(
                            225,
                            236,
                            250
                    )
                            : Color.rgb(
                            91,
                            107,
                            133
                    )
            );

            timeText.setGravity(
                    isAdmin
                            ? Gravity.END
                            : Gravity.START
            );

            LinearLayout.LayoutParams timeParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            timeParams.setMargins(
                    0,
                    dpToPixels(4),
                    0,
                    0
            );

            timeText.setLayoutParams(
                    timeParams
            );

            bubble.addView(
                    timeText
            );
        }

        messageRow.addView(
                bubble
        );

        messagesContainer.addView(
                messageRow
        );
    }

    private String formatTime(
            long timestamp) {

        return new SimpleDateFormat(
                "HH:mm",
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

        return String.valueOf(
                value
        );
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

        if (studentMessagesListener != null) {

            studentMessagesListener.remove();

            studentMessagesListener = null;
        }

        if (adminMessagesListener != null) {

            adminMessagesListener.remove();

            adminMessagesListener = null;
        }
    }
}