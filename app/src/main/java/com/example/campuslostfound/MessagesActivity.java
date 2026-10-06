package com.example.campuslostfound;

import android.content.Intent;
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

public class MessagesActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ListenerRegistration messagesListener;

    private LinearLayout messagesContainer;
    private EditText messageEditText;
    private TextView emptyText;

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

        backButton.setOnClickListener(
                v -> finish()
        );

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please log in first.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent =
                    new Intent(
                            MessagesActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();

            return;
        }

        sendButton.setOnClickListener(
                v -> sendMessage()
        );

        listenForMessages();
    }

    private void sendMessage() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please log in first.",
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
                    "Please enter a message.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        firestore
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(
                        userDocument -> {

                            String name =
                                    getStringValue(
                                            userDocument,
                                            "name"
                                    );

                            if (name.isEmpty()) {

                                name =
                                        "Student";
                            }

                            String email;

                            if (user.getEmail() != null) {

                                email =
                                        user.getEmail();

                            } else {

                                email = "";
                            }

                            Map<String, Object>
                                    messageData =
                                    new HashMap<>();

                            messageData.put(
                                    "senderUid",
                                    user.getUid()
                            );

                            messageData.put(
                                    "senderEmail",
                                    email
                            );

                            messageData.put(
                                    "senderName",
                                    name
                            );

                            messageData.put(
                                    "senderRole",
                                    "user"
                            );

                            messageData.put(
                                    "receiverUid",
                                    ""
                            );

                            messageData.put(
                                    "receiverRole",
                                    "admin"
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
                                            documentReference -> {

                                                messageEditText
                                                        .setText("");

                                                Toast.makeText(
                                                        this,
                                                        "Message sent to administrator.",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                Toast.makeText(
                                                        this,
                                                        "Could not send message: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not load your profile: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void listenForMessages() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {

            return;
        }

        String uid =
                user.getUid();

        messagesListener =
                firestore
                        .collection("messages")
                        .whereEqualTo(
                                "senderUid",
                                uid
                        )
                        .addSnapshotListener(
                                (sentSnapshot, sentError) -> {

                                    if (sentError != null) {

                                        Toast.makeText(
                                                this,
                                                "Could not load your messages: "
                                                        + sentError.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    messages.clear();

                                    if (sentSnapshot != null) {

                                        for (DocumentSnapshot document :
                                                sentSnapshot.getDocuments()) {

                                            /*
                                             * Only include messages
                                             * that were sent to an admin.
                                             */
                                            String receiverRole =
                                                    getStringValue(
                                                            document,
                                                            "receiverRole"
                                                    );

                                            if (receiverRole.equalsIgnoreCase(
                                                    "admin"
                                            )) {

                                                messages.add(
                                                        document
                                                );
                                            }
                                        }
                                    }

                                    loadReceivedMessages(uid);
                                }
                        );
    }

    private void loadReceivedMessages(
            String uid) {

        firestore
                .collection("messages")
                .whereEqualTo(
                        "receiverUid",
                        uid
                )
                .get()
                .addOnSuccessListener(
                        receivedSnapshot -> {

                            for (DocumentSnapshot document :
                                    receivedSnapshot.getDocuments()) {

                                String senderRole =
                                        getStringValue(
                                                document,
                                                "senderRole"
                                        );

                                /*
                                 * Only include replies
                                 * from administrators.
                                 */
                                if (senderRole.equalsIgnoreCase(
                                        "admin"
                                )) {

                                    addOrReplaceMessage(
                                            document
                                    );

                                    Boolean read =
                                            document.getBoolean(
                                                    "read"
                                            );

                                    boolean isRead =
                                            read != null &&
                                                    read;

                                    if (!isRead) {

                                        document
                                                .getReference()
                                                .update(
                                                        "read",
                                                        true
                                                );
                                    }
                                }
                            }

                            sortAndDisplayMessages();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not load replies: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            sortAndDisplayMessages();
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
                    "No messages yet.\nSend a message to the administrator."
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

        /*
         * USER/STUDENT MESSAGE
         *
         * senderRole = user
         * This is MY message.
         */
        boolean isMyMessage =
                senderRole.equalsIgnoreCase(
                        "user"
                );

        /*
         * OUTER ROW
         *
         * My messages     = RIGHT
         * Admin messages  = LEFT
         */
        LinearLayout messageRow =
                new LinearLayout(this);

        messageRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        messageRow.setGravity(
                isMyMessage
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
         * Keep the bubble from
         * taking the entire screen.
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

        if (isMyMessage) {

            /*
             * MY MESSAGE
             *
             * Strong blue
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
             * ADMIN MESSAGE
             *
             * Light blue
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

        if (isMyMessage) {

            senderText.setText(
                    "You"
            );

            senderText.setTextColor(
                    Color.WHITE
            );

        } else {

            senderText.setText(
                    "Administrator"
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
                isMyMessage
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
         * MESSAGE TIME
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
                    isMyMessage
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
                    isMyMessage
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

        if (messagesListener != null) {

            messagesListener.remove();

            messagesListener = null;
        }
    }
}