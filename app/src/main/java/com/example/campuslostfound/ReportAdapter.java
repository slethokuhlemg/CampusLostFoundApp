package com.example.campuslostfound;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    public interface OnReportClick {
        void onClick(ItemReport report);
    }

    public interface OnRecoveredClick {
        void onRecovered(ItemReport report);
    }

    private final List<ItemReport> reports;
    private final OnReportClick listener;

    private final OnRecoveredClick recoveredListener;
    private final boolean showRecoveryButton;

    public ReportAdapter(
            List<ItemReport> reports,
            OnReportClick listener) {

        this.reports = reports;
        this.listener = listener;
        this.recoveredListener = null;
        this.showRecoveryButton = false;
    }

    public ReportAdapter(
            List<ItemReport> reports,
            OnReportClick listener,
            boolean showRecoveryButton,
            OnRecoveredClick recoveredListener) {

        this.reports = reports;
        this.listener = listener;
        this.showRecoveryButton = showRecoveryButton;
        this.recoveredListener = recoveredListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_report_row,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        ItemReport report = reports.get(position);

        String title = report.title;

        if (title == null || title.trim().isEmpty()) {
            title = "Untitled Item";
        }

        holder.title.setText(title);

        String location = report.location;

        if (location == null || location.trim().isEmpty()) {
            location = "Unknown location";
        }

        String lostDate = report.lostDate;
        String lostTime = report.lostTime;

        String foundDate = report.foundDate;
        String foundTime = report.foundTime;

        String claimedDate = report.claimedDate;
        String claimedTime = report.claimedTime;

        boolean hasLostDate =
                lostDate != null
                        && !lostDate.trim().isEmpty();

        boolean hasLostTime =
                lostTime != null
                        && !lostTime.trim().isEmpty();

        boolean hasFoundDate =
                foundDate != null
                        && !foundDate.trim().isEmpty();

        boolean hasFoundTime =
                foundTime != null
                        && !foundTime.trim().isEmpty();

        boolean hasClaimedDate =
                claimedDate != null
                        && !claimedDate.trim().isEmpty();

        boolean hasClaimedTime =
                claimedTime != null
                        && !claimedTime.trim().isEmpty();

        StringBuilder dateText =
                new StringBuilder();

        /*
         * LOST DATE AND TIME
         */
        if (hasLostDate || hasLostTime) {

            dateText.append("Lost: ");

            if (hasLostDate) {
                dateText.append(lostDate);
            }

            if (hasLostDate && hasLostTime) {
                dateText.append(" · ");
            }

            if (hasLostTime) {
                dateText.append(lostTime);
            }

        } else {

            String date = report.dateDisplay;

            if (date != null && !date.trim().isEmpty()) {
                dateText.append(date);
            }
        }

        /*
         * FOUND DATE AND TIME
         */
        if (hasFoundDate || hasFoundTime) {

            if (dateText.length() > 0) {
                dateText.append("\n");
            }

            dateText.append("Found: ");

            if (hasFoundDate) {
                dateText.append(foundDate);
            }

            if (hasFoundDate && hasFoundTime) {
                dateText.append(" · ");
            }

            if (hasFoundTime) {
                dateText.append(foundTime);
            }
        }

        /*
         * CLAIMED DATE AND TIME
         */
        if (hasClaimedDate || hasClaimedTime) {

            if (dateText.length() > 0) {
                dateText.append("\n");
            }

            dateText.append("Claimed: ");

            if (hasClaimedDate) {
                dateText.append(claimedDate);
            }

            if (hasClaimedDate && hasClaimedTime) {
                dateText.append(" · ");
            }

            if (hasClaimedTime) {
                dateText.append(claimedTime);
            }
        }

        if (dateText.length() == 0) {

            holder.subtitle.setText(location);

        } else {

            holder.subtitle.setText(
                    location
                            + "\n"
                            + dateText
            );
        }

        /*
         * STATUS
         */
        String status = report.status;

        if (status == null || status.trim().isEmpty()) {
            status = "Unknown";
        }

        holder.status.setText(status);

        if ("Approved".equalsIgnoreCase(status)) {

            holder.status.setTextColor(
                    Color.rgb(
                            43,
                            166,
                            122
                    )
            );

            holder.status.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    230,
                                    246,
                                    241
                            )
                    )
            );

        } else if ("Rejected".equalsIgnoreCase(status)) {

            holder.status.setTextColor(
                    Color.rgb(
                            214,
                            69,
                            69
                    )
            );

            holder.status.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    251,
                                    233,
                                    233
                            )
                    )
            );

        } else if ("Pending".equalsIgnoreCase(status)) {

            holder.status.setTextColor(
                    Color.rgb(
                            245,
                            158,
                            11
                    )
            );

            holder.status.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    254,
                                    243,
                                    220
                            )
                    )
            );

        } else if (
                "Returned".equalsIgnoreCase(status)
                        || "Recovered".equalsIgnoreCase(status)
                        || "Resolved".equalsIgnoreCase(status)
                        || "Claimed".equalsIgnoreCase(status)
        ) {

            holder.status.setTextColor(
                    Color.rgb(
                            30,
                            111,
                            217
                    )
            );

            holder.status.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    234,
                                    243,
                                    255
                            )
                    )
            );

        } else {

            holder.status.setTextColor(
                    Color.DKGRAY
            );
        }

        /*
         * IMAGE
         */
        boolean imageLoaded = false;

        holder.image.setImageDrawable(null);

        if (report.photoUri != null
                && !report.photoUri.trim().isEmpty()) {

            try {

                Uri uri =
                        Uri.parse(
                                report.photoUri
                        );

                holder.image.setImageURI(uri);

                if (holder.image.getDrawable() != null) {

                    holder.image.setVisibility(
                            View.VISIBLE
                    );

                    holder.letter.setVisibility(
                            View.GONE
                    );

                    imageLoaded = true;
                }

            } catch (Exception ignored) {

                imageLoaded = false;
            }
        }

        /*
         * FALLBACK LETTER
         */
        if (!imageLoaded) {

            holder.image.setVisibility(
                    View.GONE
            );

            holder.letter.setVisibility(
                    View.VISIBLE
            );

            String category = report.category;

            if (category == null
                    || category.trim().isEmpty()) {

                category = "?";
            }

            holder.letter.setText(
                    category
                            .substring(
                                    0,
                                    1
                            )
                            .toUpperCase()
            );
        }

        /*
         * RECOVERY / MARK AS FOUND BUTTON
         */
        boolean canRecover =
                showRecoveryButton
                        && report.type != null
                        && report.type.equalsIgnoreCase(
                        "Lost"
                )
                        && report.status != null
                        && report.status.equalsIgnoreCase(
                        "Approved"
                )
                        && report.firestoreId != null
                        && !report.firestoreId.trim().isEmpty();

        if (canRecover) {

            holder.recoveredButton.setVisibility(
                    View.VISIBLE
            );

            holder.recoveredButton.setEnabled(
                    true
            );

            holder.recoveredButton.setOnClickListener(
                    v -> {

                        if (recoveredListener != null) {

                            recoveredListener.onRecovered(
                                    report
                            );
                        }
                    }
            );

        } else {

            holder.recoveredButton.setVisibility(
                    View.GONE
            );

            holder.recoveredButton.setOnClickListener(
                    null
            );
        }

        /*
         * OPEN REPORT DETAILS
         */
        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onClick(
                                report
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView image;
        TextView letter;
        TextView title;
        TextView subtitle;
        TextView status;
        Button recoveredButton;

        ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            image =
                    itemView.findViewById(
                            R.id.rowImage
                    );

            letter =
                    itemView.findViewById(
                            R.id.rowLetter
                    );

            title =
                    itemView.findViewById(
                            R.id.rowTitle
                    );

            subtitle =
                    itemView.findViewById(
                            R.id.rowSubtitle
                    );

            status =
                    itemView.findViewById(
                            R.id.rowStatus
                    );

            recoveredButton =
                    itemView.findViewById(
                            R.id.rowRecoveredButton
                    );
        }
    }
}