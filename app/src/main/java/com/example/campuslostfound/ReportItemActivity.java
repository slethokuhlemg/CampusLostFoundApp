package com.example.campuslostfound;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportItemActivity extends AppCompatActivity {

    private Spinner typeSpinner;
    private Spinner categorySpinner;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText locationEditText;

    private TextView lostDateLabel;
    private Button lostDateButton;

    private String selectedLostDate = "";
    private String selectedLostTime = "";

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_item);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        typeSpinner = findViewById(R.id.typeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);

        lostDateLabel = findViewById(R.id.lostDateLabel);
        lostDateButton = findViewById(R.id.lostDateButton);

        setupSpinners();

        typeSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        boolean isLost =
                                "Lost".equalsIgnoreCase(
                                        typeSpinner
                                                .getSelectedItem()
                                                .toString()
                                );

                        lostDateLabel.setVisibility(
                                isLost
                                        ? android.view.View.VISIBLE
                                        : android.view.View.GONE
                        );

                        lostDateButton.setVisibility(
                                isLost
                                        ? android.view.View.VISIBLE
                                        : android.view.View.GONE
                        );

                        if (!isLost) {

                            selectedLostDate = "";
                            selectedLostTime = "";

                            lostDateButton.setText(
                                    "Select Date Lost"
                            );
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );

        lostDateButton.setOnClickListener(
                v -> showLostDatePicker()
        );

        Button submitButton =
                findViewById(R.id.submitButton);

        submitButton.setOnClickListener(
                v -> analyseAndSubmit()
        );
    }

    private void showLostDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        int currentYear =
                calendar.get(Calendar.YEAR);

        int currentMonth =
                calendar.get(Calendar.MONTH);

        int currentDay =
                calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            selectedLostDate =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );

                            showLostTimePicker();
                        },
                        currentYear,
                        currentMonth,
                        currentDay
                );

        datePickerDialog.show();
    }

    private void showLostTimePicker() {

        Calendar calendar =
                Calendar.getInstance();

        int currentHour =
                calendar.get(Calendar.HOUR_OF_DAY);

        int currentMinute =
                calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            Calendar selectedTime =
                                    Calendar.getInstance();

                            selectedTime.set(
                                    Calendar.HOUR_OF_DAY,
                                    hourOfDay
                            );

                            selectedTime.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            selectedLostTime =
                                    new SimpleDateFormat(
                                            "hh:mm a",
                                            Locale.getDefault()
                                    ).format(
                                            selectedTime.getTime()
                                    );

                            lostDateButton.setText(
                                    selectedLostDate +
                                            " · " +
                                            selectedLostTime
                            );
                        },
                        currentHour,
                        currentMinute,
                        false
                );

        timePickerDialog.show();
    }

    private void setupSpinners() {

        String[] types = {
                "Lost",
                "Found"
        };

        String[] categories = {
                "Laptop",
                "Cellphone",
                "Student Card",
                "Book",
                "Tablet",
                "Calculator",
                "Headphones",
                "Other"
        };

        typeSpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        types
                )
        );

        categorySpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories
                )
        );
    }

    private void analyseAndSubmit() {

        String category =
                categorySpinner
                        .getSelectedItem()
                        .toString();

        String type =
                typeSpinner
                        .getSelectedItem()
                        .toString();

        String title =
                titleEditText
                        .getText()
                        .toString()
                        .trim();

        String description =
                descriptionEditText
                        .getText()
                        .toString()
                        .trim();

        String location =
                locationEditText
                        .getText()
                        .toString()
                        .trim();

        if ("Lost".equalsIgnoreCase(type) &&
                selectedLostDate.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select the date the item was lost.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if ("Lost".equalsIgnoreCase(type) &&
                selectedLostTime.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select the time the item was lost.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (title.isEmpty() ||
                description.isEmpty() ||
                location.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter the title, description and location.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        PriorityResult result =
                AiPriorityClassifier.analyse(
                        category,
                        title,
                        description
                );

        String approval;

        if (result.autoApproved) {

            approval =
                    "AUTOMATICALLY APPROVED";

        } else {

            approval =
                    "SENT TO ADMIN FOR REVIEW";
        }

        String message =
                "AI Priority: " +
                        result.priority +
                        "\nScore: " +
                        result.score +
                        "/100" +
                        "\n\n" +
                        approval +
                        "\n\nReason:\n" +
                        result.reason;

        saveReportToFirebase(
                type,
                category,
                title,
                description,
                location,
                selectedLostDate,
                selectedLostTime,
                result
        );

        new AlertDialog.Builder(this)
                .setTitle("AI Analysis Complete")
                .setMessage(message)
                .setPositiveButton(
                        "OK",
                        (dialog, which) -> {

                            Toast.makeText(
                                    this,
                                    approval,
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        }
                )
                .show();
    }

    private void saveReportToFirebase(
            String type,
            String category,
            String title,
            String description,
            String location,
            String lostDate,
            String lostTime,
            PriorityResult result) {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please log in before reporting an item.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid =
                currentUser.getUid();

        final String reporterEmail;

        if (currentUser.getEmail() != null) {

            reporterEmail =
                    currentUser.getEmail();

        } else {

            reporterEmail =
                    "";
        }

        firestore
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            String reporterName =
                                    "Student";

                            String studentNumber =
                                    "";

                            if (documentSnapshot.exists()) {

                                String firestoreName =
                                        documentSnapshot.getString(
                                                "name"
                                        );

                                String firestoreStudentNumber =
                                        documentSnapshot.getString(
                                                "studentNumber"
                                        );

                                if (firestoreName != null &&
                                        !firestoreName.isEmpty()) {

                                    reporterName =
                                            firestoreName;
                                }

                                if (firestoreStudentNumber != null) {

                                    studentNumber =
                                            firestoreStudentNumber;
                                }
                            }

                            createFirebaseReport(
                                    uid,
                                    reporterName,
                                    studentNumber,
                                    reporterEmail,
                                    type,
                                    category,
                                    title,
                                    description,
                                    location,
                                    lostDate,
                                    lostTime,
                                    result
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

    private void createFirebaseReport(
            String uid,
            String reporterName,
            String studentNumber,
            String reporterEmail,
            String type,
            String category,
            String title,
            String description,
            String location,
            String lostDate,
            String lostTime,
            PriorityResult result) {

        long timestamp =
                System.currentTimeMillis();

        String dateDisplay =
                new SimpleDateFormat(
                        "dd MMM yyyy · hh:mm a",
                        Locale.getDefault()
                ).format(
                        new Date(timestamp)
                );

        String status;

        if (result.autoApproved) {

            status =
                    "Approved";

        } else {

            status =
                    "Pending";
        }

        DocumentReference reportReference =
                firestore
                        .collection("reports")
                        .document();

        String reportId =
                reportReference.getId();

        Map<String, Object> report =
                new HashMap<>();

        report.put(
                "id",
                reportId
        );

        report.put(
                "type",
                type
        );

        report.put(
                "category",
                category
        );

        report.put(
                "title",
                title
        );

        report.put(
                "description",
                description
        );

        report.put(
                "location",
                location
        );

        report.put(
                "lostDate",
                lostDate == null
                        ? ""
                        : lostDate
        );

        report.put(
                "lostTime",
                lostTime == null
                        ? ""
                        : lostTime
        );

        report.put(
                "foundDate",
                ""
        );

        report.put(
                "foundTime",
                ""
        );

        report.put(
                "foundTimestamp",
                0L
        );

        report.put(
                "photoUri",
                ""
        );

        report.put(
                "photoUrl",
                ""
        );

        report.put(
                "priority",
                result.priority
        );

        report.put(
                "score",
                result.score
        );

        report.put(
                "status",
                status
        );

        report.put(
                "reporterName",
                reporterName
        );

        report.put(
                "reporterEmail",
                reporterEmail
        );

        report.put(
                "reporterUid",
                uid
        );

        report.put(
                "studentNumber",
                studentNumber
        );

        report.put(
                "dateDisplay",
                dateDisplay
        );

        report.put(
                "timestamp",
                timestamp
        );

        report.put(
                "notification",
                ""
        );

        report.put(
                "notificationRead",
                true
        );

        reportReference
                .set(report)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Report saved successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "There was a problem saving your report: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }
}
