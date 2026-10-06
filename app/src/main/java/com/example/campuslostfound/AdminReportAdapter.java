package com.example.campuslostfound;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminReportAdapter
        extends RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(ItemReport report);
    }

    private final List<ItemReport> reports;
    private final OnReportClickListener listener;

    public AdminReportAdapter(
            List<ItemReport> reports,
            OnReportClickListener listener) {

        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_admin_report,
                                parent,
                                false
                        );

        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReportViewHolder holder,
            int position) {

        ItemReport report =
                reports.get(position);

        /*
         * Title
         */

        if (report.title != null
                && !report.title.isEmpty()) {

            holder.titleText.setText(
                    report.title
            );

        } else {

            holder.titleText.setText(
                    "Untitled Report"
            );
        }

        /*
         * Type
         */

        holder.typeText.setText(
                "Type: "
                        + safeText(report.type)
        );

        /*
         * Category
         */

        holder.categoryText.setText(
                "Category: "
                        + safeText(report.category)
        );

        /*
         * Location
         */

        holder.locationText.setText(
                "Location: "
                        + safeText(report.location)
        );

        /*
         * Status
         */

        String status =
                safeText(report.status);

        holder.statusText.setText(
                "Status: "
                        + status
        );

        /*
         * Make the status easier to identify.
         */

        if (status.equalsIgnoreCase(
                "Pending")) {

            holder.statusText.setTextColor(
                    Color.rgb(
                            245,
                            158,
                            11
                    )
            );

            holder.statusText.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    254,
                                    243,
                                    220
                            )
                    )
            );

        } else if (status.equalsIgnoreCase(
                "Approved")) {

            holder.statusText.setTextColor(
                    Color.rgb(
                            43,
                            166,
                            122
                    )
            );

            holder.statusText.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    230,
                                    246,
                                    241
                            )
                    )
            );

        } else if (status.equalsIgnoreCase(
                "Rejected")) {

            holder.statusText.setTextColor(
                    Color.rgb(
                            214,
                            69,
                            69
                    )
            );

            holder.statusText.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    251,
                                    233,
                                    233
                            )
                    )
            );

        } else if (status.equalsIgnoreCase(
                "Resolved")) {

            holder.statusText.setTextColor(
                    Color.rgb(
                            27,
                            96,
                            188
                    )
            );

        } else if (status.equalsIgnoreCase(
                "Claimed")) {

            holder.statusText.setTextColor(
                    Color.rgb(
                            30,
                            111,
                            217
                    )
            );

        } else {

            holder.statusText.setTextColor(
                    Color.DKGRAY
            );
        }

        /*
         * Open report details when
         * the admin taps the report.
         */

        holder.itemView.setOnClickListener(
                v -> listener.onReportClick(report)
        );
    }

    @Override
    public int getItemCount() {

        return reports.size();
    }

    private String safeText(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not provided";
        }

        return value;
    }

    static class ReportViewHolder
            extends RecyclerView.ViewHolder {

        TextView titleText;
        TextView typeText;
        TextView categoryText;
        TextView locationText;
        TextView statusText;

        public ReportViewHolder(
                @NonNull View itemView) {

            super(itemView);

            titleText =
                    itemView.findViewById(
                            R.id.adminReportTitle
                    );

            typeText =
                    itemView.findViewById(
                            R.id.adminReportType
                    );

            categoryText =
                    itemView.findViewById(
                            R.id.adminReportCategory
                    );

            locationText =
                    itemView.findViewById(
                            R.id.adminReportLocation
                    );

            statusText =
                    itemView.findViewById(
                            R.id.adminReportStatus
                    );
        }
    }
}