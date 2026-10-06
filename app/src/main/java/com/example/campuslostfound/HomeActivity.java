package com.example.campuslostfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recentFoundRecyclerView;
    private RecyclerView recentReportedRecyclerView;

    private TextView emptyRecentText;
    private TextView emptyReportedText;
    private TextView messageBadge;

    private String[] activeCategoryFilter = null;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ListenerRegistration reportsListener;
    private ListenerRegistration unreadMessagesListener;

    private final List<ItemReport> allActiveReports =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_home
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        TextView welcomeText =
                findViewById(
                        R.id.welcomeText
                );

        TextView notificationBell =
                findViewById(
                        R.id.notificationBell
                );

        EditText searchEditText =
                findViewById(
                        R.id.searchEditText
                );

        TextView filterIcon =
                findViewById(
                        R.id.filterIcon
                );

        Button reportButton =
                findViewById(
                        R.id.reportButton
                );

        TextView viewAllText =
                findViewById(
                        R.id.viewAllText
                );

        recentFoundRecyclerView =
                findViewById(
                        R.id.recentFoundRecyclerView
                );

        recentReportedRecyclerView =
                findViewById(
                        R.id.recentReportedRecyclerView
                );

        emptyRecentText =
                findViewById(
                        R.id.emptyRecentText
                );

        emptyReportedText =
                findViewById(
                        R.id.emptyReportedText
                );

        messageBadge =
                findViewById(
                        R.id.messageBadge
                );

        recentFoundRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recentFoundRecyclerView.setNestedScrollingEnabled(
                false
        );

        recentReportedRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recentReportedRecyclerView.setNestedScrollingEnabled(
                false
        );

        SharedPreferences prefs =
                getSharedPreferences(
                        "CampusPrefs",
                        MODE_PRIVATE
                );

        String name =
                prefs.getString(
                        "studentName",
                        "Student"
                );

        welcomeText.setText(
                "Hello, " + name + " 👋"
        );

        notificationBell.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    HomeActivity.this,
                                    NotificationsActivity.class
                            );

                    startActivity(intent);
                }
        );

        filterIcon.setOnClickListener(
                v -> Toast.makeText(
                        HomeActivity.this,
                        "Choose a category below.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        reportButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    HomeActivity.this,
                                    ReportItemActivity.class
                            );

                    startActivity(intent);
                }
        );

        viewAllText.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    HomeActivity.this,
                                    MyReportsActivity.class
                            );

                    intent.putExtra(
                            "mode",
                            "all"
                    );

                    startActivity(intent);
                }
        );

        findViewById(R.id.catElectronics)
                .setOnClickListener(
                        v -> filterByCategory(
                                new String[]{
                                        "Laptop",
                                        "Cellphone",
                                        "Tablet",
                                        "Calculator",
                                        "Electronics"
                                }
                        )
                );

        findViewById(R.id.catBooks)
                .setOnClickListener(
                        v -> filterByCategory(
                                new String[]{
                                        "Book",
                                        "Books"
                                }
                        )
                );

        findViewById(R.id.catAccessories)
                .setOnClickListener(
                        v -> filterByCategory(
                                new String[]{
                                        "Headphones",
                                        "Student Card",
                                        "Keys",
                                        "Watch",
                                        "Accessories"
                                }
                        )
                );

        findViewById(R.id.catOthers)
                .setOnClickListener(
                        v -> filterByCategory(
                                new String[]{
                                        "Other",
                                        "Others"
                                }
                        )
                );

        searchEditText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        String query =
                                s.toString().trim();

                        activeCategoryFilter = null;

                        displayReports(query);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        findViewById(R.id.navReports)
                .setOnClickListener(
                        v -> {

                            Intent intent =
                                    new Intent(
                                            HomeActivity.this,
                                            MyReportsActivity.class
                                    );

                            intent.putExtra(
                                    "mode",
                                    "mine"
                            );

                            startActivity(intent);
                        }
                );

        findViewById(R.id.navReportFab)
                .setOnClickListener(
                        v -> {

                            Intent intent =
                                    new Intent(
                                            HomeActivity.this,
                                            ReportItemActivity.class
                                    );

                            startActivity(intent);
                        }
                );

        findViewById(R.id.navMessages)
                .setOnClickListener(
                        v -> {

                            Intent intent =
                                    new Intent(
                                            HomeActivity.this,
                                            MessagesActivity.class
                                    );

                            startActivity(intent);
                        }
                );

        findViewById(R.id.navProfile)
                .setOnClickListener(
                        v -> {

                            Intent intent =
                                    new Intent(
                                            HomeActivity.this,
                                            ProfileActivity.class
                                    );

                            startActivity(intent);
                        }
                );

        listenForApprovedReports();

        listenForUnreadMessages();
    }

    private void listenForUnreadMessages() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {

            messageBadge.setVisibility(
                    View.GONE
            );

            return;
        }

        String uid =
                user.getUid();

        if (unreadMessagesListener != null) {
            unreadMessagesListener.remove();
        }

        unreadMessagesListener =
                firestore
                        .collection("messages")
                        .whereEqualTo(
                                "receiverUid",
                                uid
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null
                                            || snapshot == null) {

                                        messageBadge.setVisibility(
                                                View.GONE
                                        );

                                        return;
                                    }

                                    int unreadCount = 0;

                                    for (DocumentSnapshot document :
                                            snapshot.getDocuments()) {

                                        String senderRole =
                                                getStringValue(
                                                        document,
                                                        "senderRole"
                                                );

                                        Boolean read =
                                                document.getBoolean(
                                                        "read"
                                                );

                                        boolean isRead =
                                                read != null
                                                        && read;

                                        if (senderRole
                                                .equalsIgnoreCase(
                                                        "admin"
                                                )
                                                && !isRead) {

                                            unreadCount++;
                                        }
                                    }

                                    if (unreadCount > 0) {

                                        messageBadge.setVisibility(
                                                View.VISIBLE
                                        );

                                        if (unreadCount > 99) {

                                            messageBadge.setText(
                                                    "99+"
                                            );

                                        } else {

                                            messageBadge.setText(
                                                    String.valueOf(
                                                            unreadCount
                                                    )
                                            );
                                        }

                                    } else {

                                        messageBadge.setVisibility(
                                                View.GONE
                                        );
                                    }
                                }
                        );
    }

    private void listenForApprovedReports() {

        if (reportsListener != null) {
            reportsListener.remove();
        }

        /*
         * We listen to the whole reports collection and filter
         * the active reports locally.
         *
         * This makes the Home screen robust when a report changes
         * from Lost -> Found or Found -> Claimed.
         */

        reportsListener =
                firestore
                        .collection("reports")
                        .addSnapshotListener(
                                (querySnapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                HomeActivity.this,
                                                "Could not load reports: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    if (querySnapshot == null) {
                                        return;
                                    }

                                    allActiveReports.clear();

                                    for (DocumentSnapshot document :
                                            querySnapshot.getDocuments()) {

                                        ItemReport report =
                                                convertDocumentToReport(
                                                        document
                                                );

                                        if (report == null) {
                                            continue;
                                        }

                                        /*
                                         * Only Approved reports are
                                         * active on the public Home page.
                                         *
                                         * Claimed, Rejected, Pending,
                                         * Resolved and other historical
                                         * statuses stay out of the
                                         * active Home lists.
                                         */

                                        if (report.status == null
                                                || !report.status.equalsIgnoreCase(
                                                "Approved"
                                        )) {

                                            continue;
                                        }

                                        if (report.type == null) {
                                            continue;
                                        }

                                        if (!report.type.equalsIgnoreCase(
                                                "Lost"
                                        )
                                                && !report.type.equalsIgnoreCase(
                                                "Found"
                                        )) {

                                            continue;
                                        }

                                        allActiveReports.add(
                                                report
                                        );
                                    }

                                    Collections.sort(
                                            allActiveReports,
                                            new Comparator<ItemReport>() {

                                                @Override
                                                public int compare(
                                                        ItemReport first,
                                                        ItemReport second) {

                                                    long firstTime =
                                                            getBestSortTimestamp(
                                                                    first
                                                            );

                                                    long secondTime =
                                                            getBestSortTimestamp(
                                                                    second
                                                            );

                                                    return Long.compare(
                                                            secondTime,
                                                            firstTime
                                                    );
                                                }
                                            }
                                    );

                                    EditText searchEditText =
                                            findViewById(
                                                    R.id.searchEditText
                                            );

                                    String query =
                                            searchEditText
                                                    .getText()
                                                    .toString()
                                                    .trim();

                                    displayReports(
                                            query
                                    );
                                }
                        );
    }

    private long getBestSortTimestamp(
            ItemReport report) {

        if (report == null) {
            return 0;
        }

        if (report.type != null
                && report.type.equalsIgnoreCase(
                "Found"
        )
                && report.foundTimestamp > 0) {

            return report.foundTimestamp;
        }

        return report.timestamp;
    }

    private ItemReport convertDocumentToReport(
            DocumentSnapshot document) {

        try {

            ItemReport report =
                    new ItemReport();

            report.firestoreId =
                    document.getId();

            Long timestamp =
                    document.getLong(
                            "timestamp"
                    );

            if (timestamp != null) {

                report.id =
                        timestamp;

                report.timestamp =
                        timestamp;

            } else {

                report.id = 0;
                report.timestamp = 0;
            }

            report.type =
                    getStringValue(
                            document,
                            "type"
                    );

            report.category =
                    getStringValue(
                            document,
                            "category"
                    );

            report.title =
                    getStringValue(
                            document,
                            "title"
                    );

            report.description =
                    getStringValue(
                            document,
                            "description"
                    );

            report.location =
                    getStringValue(
                            document,
                            "location"
                    );

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

            if (!photoUrl.isEmpty()) {

                report.photoUri =
                        photoUrl;

            } else if (!photoUri.isEmpty()) {

                report.photoUri =
                        photoUri;

            } else {

                report.photoUri = null;
            }

            report.priority =
                    getStringValue(
                            document,
                            "priority"
                    );

            Long score =
                    document.getLong(
                            "score"
                    );

            report.score =
                    score == null
                            ? 0
                            : score.intValue();

            report.status =
                    getStringValue(
                            document,
                            "status"
                    );

            report.reporterName =
                    getStringValue(
                            document,
                            "reporterName"
                    );

            report.reporterEmail =
                    getStringValue(
                            document,
                            "reporterEmail"
                    );

            report.dateDisplay =
                    getStringValue(
                            document,
                            "dateDisplay"
                    );

            report.lostDate =
                    getStringValue(
                            document,
                            "lostDate"
                    );

            report.lostTime =
                    getStringValue(
                            document,
                            "lostTime"
                    );

            report.foundDate =
                    getStringValue(
                            document,
                            "foundDate"
                    );

            report.foundTime =
                    getStringValue(
                            document,
                            "foundTime"
                    );

            report.foundTimestamp =
                    getLongValue(
                            document,
                            "foundTimestamp"
                    );

            report.claimedDate =
                    getStringValue(
                            document,
                            "claimedDate"
                    );

            report.claimedTime =
                    getStringValue(
                            document,
                            "claimedTime"
                    );

            report.claimedTimestamp =
                    getLongValue(
                            document,
                            "claimedTimestamp"
                    );

            report.notification =
                    getStringValue(
                            document,
                            "notification"
                    );

            Boolean notificationRead =
                    document.getBoolean(
                            "notificationRead"
                    );

            report.notificationRead =
                    notificationRead != null
                            && notificationRead;

            return report;

        } catch (Exception e) {

            return null;
        }
    }

    private long getLongValue(
            DocumentSnapshot document,
            String field) {

        Object value =
                document.get(field);

        if (value instanceof Number) {

            return ((Number) value).longValue();
        }

        if (value == null) {
            return 0;
        }

        try {

            return Long.parseLong(
                    String.valueOf(
                            value
                    )
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private String getStringValue(
            DocumentSnapshot document,
            String field) {

        String value =
                document.getString(
                        field
                );

        return value == null
                ? ""
                : value;
    }

    private void filterByCategory(
            String[] categories) {

        activeCategoryFilter =
                categories;

        displayReports("");
    }

    private void displayReports(
            String query) {

        List<ItemReport> results =
                new ArrayList<>();

        for (ItemReport report :
                allActiveReports) {

            if (activeCategoryFilter != null) {

                boolean categoryMatches =
                        false;

                for (String category :
                        activeCategoryFilter) {

                    if (report.category != null
                            && report.category.equalsIgnoreCase(
                            category
                    )) {

                        categoryMatches = true;

                        break;
                    }
                }

                if (!categoryMatches) {
                    continue;
                }
            }

            if (query != null
                    && !query.isEmpty()) {

                String lowerQuery =
                        query.toLowerCase(
                                Locale.getDefault()
                        );

                boolean matches =
                        contains(
                                report.title,
                                lowerQuery
                        )
                                || contains(
                                report.description,
                                lowerQuery
                        )
                                || contains(
                                report.category,
                                lowerQuery
                        )
                                || contains(
                                report.location,
                                lowerQuery
                        )
                                || contains(
                                report.type,
                                lowerQuery
                        )
                                || contains(
                                report.reporterName,
                                lowerQuery
                        );

                if (!matches) {
                    continue;
                }
            }

            results.add(
                    report
            );
        }

        renderLists(
                results,
                query
        );
    }

    private boolean contains(
            String value,
            String query) {

        if (value == null) {
            return false;
        }

        return value
                .toLowerCase(
                        Locale.getDefault()
                )
                .contains(query);
    }

    private void renderLists(
            List<ItemReport> results,
            String query) {

        List<ItemReport> foundItems =
                new ArrayList<>();

        List<ItemReport> reportedItems =
                new ArrayList<>();

        for (ItemReport report :
                results) {

            if (report.type != null
                    && report.type.equalsIgnoreCase(
                    "Found"
            )) {

                foundItems.add(
                        report
                );

            } else if (report.type != null
                    && report.type.equalsIgnoreCase(
                    "Lost"
            )) {

                reportedItems.add(
                        report
                );
            }
        }

        if ((query == null || query.isEmpty())
                && activeCategoryFilter == null) {

            if (foundItems.size() > 5) {

                foundItems =
                        new ArrayList<>(
                                foundItems.subList(
                                        0,
                                        5
                                )
                        );
            }

            if (reportedItems.size() > 5) {

                reportedItems =
                        new ArrayList<>(
                                reportedItems.subList(
                                        0,
                                        5
                                )
                        );
            }
        }

        if (foundItems.isEmpty()) {

            recentFoundRecyclerView.setVisibility(
                    View.GONE
            );

            emptyRecentText.setVisibility(
                    View.VISIBLE
            );

            emptyRecentText.setText(
                    "No recently found items."
            );

        } else {

            recentFoundRecyclerView.setVisibility(
                    View.VISIBLE
            );

            emptyRecentText.setVisibility(
                    View.GONE
            );

            setAdapter(
                    recentFoundRecyclerView,
                    foundItems
            );
        }

        if (reportedItems.isEmpty()) {

            recentReportedRecyclerView.setVisibility(
                    View.GONE
            );

            emptyReportedText.setVisibility(
                    View.VISIBLE
            );

            emptyReportedText.setText(
                    "No recently reported lost items."
            );

        } else {

            recentReportedRecyclerView.setVisibility(
                    View.VISIBLE
            );

            emptyReportedText.setVisibility(
                    View.GONE
            );

            setAdapter(
                    recentReportedRecyclerView,
                    reportedItems
            );
        }
    }

    private void setAdapter(
            RecyclerView recyclerView,
            List<ItemReport> reports) {

        recyclerView.setAdapter(
                new ReportAdapter(
                        reports,
                        report -> {

                            Intent intent =
                                    new Intent(
                                            HomeActivity.this,
                                            ItemDetailsActivity.class
                                    );

                            intent.putExtra(
                                    "report_id",
                                    report.id
                            );

                            intent.putExtra(
                                    "firestore_id",
                                    report.firestoreId
                            );

                            startActivity(intent);
                        }
                )
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (reportsListener != null) {

            reportsListener.remove();

            reportsListener = null;
        }

        if (unreadMessagesListener != null) {

            unreadMessagesListener.remove();

            unreadMessagesListener = null;
        }
    }
}