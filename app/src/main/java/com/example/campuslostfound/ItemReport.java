package com.example.campuslostfound;

public class ItemReport {

    // ==================================================
    // LOCAL / FIRESTORE IDENTIFICATION
    // ==================================================

    public long id;

    /*
     * Real Firestore document ID.
     */
    public String firestoreId;

    // ==================================================
    // REPORT INFORMATION
    // ==================================================

    public String type;
    public String category;
    public String title;
    public String description;
    public String location;

    // ==================================================
    // PHOTO
    // ==================================================

    public String photoUri;

    // ==================================================
    // PRIORITY / SCORE
    // ==================================================

    public String priority;
    public int score;

    // ==================================================
    // STATUS
    // ==================================================

    public String status;

    // ==================================================
    // REPORTER
    // ==================================================

    public String reporterName;
    public String reporterEmail;

    // ==================================================
    // DATE / TIME
    // ==================================================

    /*
     * Date and time when the report was created.
     */
    public String dateDisplay;

    /*
     * Date the item was reported as lost.
     *
     * Example:
     * 28/09/2026
     */
    public String lostDate;

    /*
     * Time the item was reported as lost.
     *
     * Example:
     * 02:30 PM
     */
    public String lostTime;

    /*
     * Date the item was found/recovered.
     *
     * Example:
     * 02/10/2026
     */
    public String foundDate;

    /*
     * Time the item was found/recovered.
     *
     * Example:
     * 01:45 AM
     */
    public String foundTime;

    /*
     * Date the item was claimed.
     *
     * Example:
     * 02/10/2026
     */
    public String claimedDate;

    /*
     * Time the item was claimed.
     *
     * Example:
     * 03:20 AM
     */
    public String claimedTime;

    // ==================================================
    // TIMESTAMPS
    // ==================================================

    /*
     * Timestamp for when the original report was created.
     */
    public long timestamp;

    /*
     * Timestamp for when the item was found/recovered.
     */
    public long foundTimestamp;

    /*
     * Timestamp for when the item was claimed.
     */
    public long claimedTimestamp;

    // ==================================================
    // NOTIFICATIONS
    // ==================================================

    public String notification;
    public boolean notificationRead;
}