package com.example.campuslostfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME =
            "campus_lost_found.db";

    private static final int DB_VERSION = 2;

    private static final String TABLE_REPORTS =
            "reports";

    public DbHelper(Context context) {
        super(
                context,
                DB_NAME,
                null,
                DB_VERSION
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable =
                "CREATE TABLE " +
                        TABLE_REPORTS +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        "type TEXT, " +

                        "category TEXT, " +

                        "title TEXT, " +

                        "description TEXT, " +

                        "location TEXT, " +

                        "photo_uri TEXT, " +

                        "priority TEXT, " +

                        "score INTEGER, " +

                        "status TEXT, " +

                        "reporter_name TEXT, " +

                        "reporter_email TEXT, " +

                        "date_display TEXT, " +

                        "timestamp INTEGER, " +

                        "notification TEXT, " +

                        "notification_read INTEGER DEFAULT 1" +

                        ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        db.execSQL(
                "DROP TABLE IF EXISTS " +
                        TABLE_REPORTS
        );

        onCreate(db);
    }

    public long insertReport(ItemReport report) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "type",
                report.type
        );

        values.put(
                "category",
                report.category
        );

        values.put(
                "title",
                report.title
        );

        values.put(
                "description",
                report.description
        );

        values.put(
                "location",
                report.location
        );

        if (report.photoUri != null) {

            values.put(
                    "photo_uri",
                    report.photoUri
            );

        } else {

            values.putNull(
                    "photo_uri"
            );
        }

        values.put(
                "priority",
                report.priority
        );

        values.put(
                "score",
                report.score
        );

        values.put(
                "status",
                report.status
        );

        values.put(
                "reporter_name",
                report.reporterName
        );

        values.put(
                "reporter_email",
                report.reporterEmail
        );

        values.put(
                "date_display",
                report.dateDisplay
        );

        values.put(
                "timestamp",
                report.timestamp
        );

        values.put(
                "notification",
                report.notification
        );

        values.put(
                "notification_read",
                report.notificationRead ? 1 : 0
        );

        return db.insert(
                TABLE_REPORTS,
                null,
                values
        );
    }

    public int updateStatus(
            long id,
            String newStatus) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "status",
                newStatus
        );

        return db.update(
                TABLE_REPORTS,
                values,
                "id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int approveReport(long id) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "status",
                "Approved"
        );

        values.put(
                "notification",
                "Your report has been approved and is now visible."
        );

        values.put(
                "notification_read",
                0
        );

        return db.update(
                TABLE_REPORTS,
                values,
                "id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int rejectReport(long id) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "status",
                "Rejected"
        );

        values.put(
                "notification",
                "Your report has been rejected by an administrator."
        );

        values.put(
                "notification_read",
                0
        );

        return db.update(
                TABLE_REPORTS,
                values,
                "id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int deleteReport(long id) {

        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_REPORTS,
                "id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public List<ItemReport> getReportsByUserAndType(
            String email,
            String type) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "reporter_email = ? AND type = ?",
                        new String[]{
                                email,
                                type
                        },
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public List<ItemReport> getAllReports() {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public List<ItemReport> getReportsByStatus(
            String status) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "status = ?",
                        new String[]{
                                status
                        },
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public List<ItemReport> getPendingReports() {

        return getReportsByStatus(
                "Pending"
        );
    }

    public List<ItemReport> getApprovedReports() {

        return getReportsByStatus(
                "Approved"
        );
    }

    public List<ItemReport> getRejectedReports() {

        return getReportsByStatus(
                "Rejected"
        );
    }

    public List<ItemReport> getReportsByType(
            String type) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "type = ?",
                        new String[]{
                                type
                        },
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public List<ItemReport> getRecentByType(
            String type,
            int limit) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "type = ?",
                        new String[]{
                                type
                        },
                        null,
                        null,
                        "timestamp DESC",
                        String.valueOf(limit)
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public List<ItemReport> getApprovedRecentByType(
            String type,
            int limit) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "type = ? AND status = ?",
                        new String[]{
                                type,
                                "Approved"
                        },
                        null,
                        null,
                        "timestamp DESC",
                        String.valueOf(limit)
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    /*
     * SEARCH BY TYPE
     *
     * Searches approved reports of one type.
     */
    public List<ItemReport> searchByType(
            String type,
            String query) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        String search =
                "%" + query + "%";

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "type = ? AND status = ? AND " +
                                "(title LIKE ? OR " +
                                "category LIKE ? OR " +
                                "description LIKE ? OR " +
                                "location LIKE ? OR " +
                                "reporter_name LIKE ? OR " +
                                "type LIKE ?)",
                        new String[]{
                                type,
                                "Approved",
                                search,
                                search,
                                search,
                                search,
                                search,
                                search
                        },
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    /*
     * SEARCH ALL APPROVED REPORTS
     *
     * Searches both Lost and Found reports.
     *
     * This method is used by HomeActivity.
     */
    public List<ItemReport> searchApprovedReports(
            String query) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        String search =
                "%" + query + "%";

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "status = ? AND " +
                                "(title LIKE ? OR " +
                                "category LIKE ? OR " +
                                "description LIKE ? OR " +
                                "location LIKE ? OR " +
                                "reporter_name LIKE ? OR " +
                                "type LIKE ?)",
                        new String[]{
                                "Approved",
                                search,
                                search,
                                search,
                                search,
                                search,
                                search
                        },
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public List<ItemReport> getByTypeAndCategories(
            String type,
            String[] categories) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        if (categories == null ||
                categories.length == 0) {

            return reports;
        }

        StringBuilder selection =
                new StringBuilder();

        selection.append(
                "type = ? AND status = ? AND ("
        );

        List<String> arguments =
                new ArrayList<>();

        arguments.add(type);
        arguments.add("Approved");

        for (int i = 0;
             i < categories.length;
             i++) {

            if (i > 0) {
                selection.append(" OR ");
            }

            selection.append(
                    "category = ?"
            );

            arguments.add(
                    categories[i]
            );
        }

        selection.append(")");

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        selection.toString(),
                        arguments.toArray(
                                new String[0]
                        ),
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public ItemReport getReportById(
            long id) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "id = ?",
                        new String[]{
                                String.valueOf(id)
                        },
                        null,
                        null,
                        null
                );

        ItemReport report = null;

        if (cursor.moveToFirst()) {

            report =
                    fromCursor(cursor);
        }

        cursor.close();

        return report;
    }

    public List<ItemReport> getUnreadNotifications(
            String email) {

        SQLiteDatabase db =
                getReadableDatabase();

        List<ItemReport> reports =
                new ArrayList<>();

        Cursor cursor =
                db.query(
                        TABLE_REPORTS,
                        null,
                        "reporter_email = ? " +
                                "AND notification_read = 0 " +
                                "AND notification IS NOT NULL " +
                                "AND notification != ''",
                        new String[]{
                                email
                        },
                        null,
                        null,
                        "timestamp DESC"
                );

        while (cursor.moveToNext()) {

            reports.add(
                    fromCursor(cursor)
            );
        }

        cursor.close();

        return reports;
    }

    public int markNotificationRead(
            long id) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "notification_read",
                1
        );

        return db.update(
                TABLE_REPORTS,
                values,
                "id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int countByType(
            String type) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE_REPORTS +
                                " WHERE type = ?",
                        new String[]{
                                type
                        }
                );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public int countTotal() {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE_REPORTS,
                        null
                );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public int countPending() {

        return countByStatus(
                "Pending"
        );
    }

    public int countApproved() {

        return countByStatus(
                "Approved"
        );
    }

    public int countRejected() {

        return countByStatus(
                "Rejected"
        );
    }

    private int countByStatus(
            String status) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE_REPORTS +
                                " WHERE status = ?",
                        new String[]{
                                status
                        }
                );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public int countResolved() {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE_REPORTS +
                                " WHERE status = ? " +
                                "OR status = ?",
                        new String[]{
                                "Resolved",
                                "Claimed"
                        }
                );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    private ItemReport fromCursor(
            Cursor cursor) {

        ItemReport report =
                new ItemReport();

        report.id =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                "id"
                        )
                );

        report.type =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "type"
                        )
                );

        report.category =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "category"
                        )
                );

        report.title =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "title"
                        )
                );

        report.description =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "description"
                        )
                );

        report.location =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "location"
                        )
                );

        report.photoUri =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "photo_uri"
                        )
                );

        report.priority =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "priority"
                        )
                );

        report.score =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                "score"
                        )
                );

        report.status =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "status"
                        )
                );

        report.reporterName =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "reporter_name"
                        )
                );

        report.reporterEmail =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "reporter_email"
                        )
                );

        report.dateDisplay =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "date_display"
                        )
                );

        report.timestamp =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                "timestamp"
                        )
                );

        report.notification =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "notification"
                        )
                );

        report.notificationRead =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                "notification_read"
                        )
                ) == 1;

        return report;
    }

    public void closeDatabase() {
        close();
    }
}