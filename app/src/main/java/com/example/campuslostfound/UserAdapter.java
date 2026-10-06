package com.example.campuslostfound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<UserModel> userList;
    private OnRoleChangeListener onRoleChangeListener;

    public interface OnRoleChangeListener {
        void onRoleChange(UserModel user);
    }

    public UserAdapter(
            Context context,
            List<UserModel> userList,
            OnRoleChangeListener onRoleChangeListener) {

        this.context = context;
        this.userList = userList;
        this.onRoleChangeListener = onRoleChangeListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.user_item,
                                parent,
                                false
                        );

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UserViewHolder holder,
            int position) {

        UserModel user =
                userList.get(position);

        holder.userNameText.setText(
                user.getName()
        );

        holder.userEmailText.setText(
                "Email: " + user.getEmail()
        );

        holder.studentNumberText.setText(
                "Student Number: "
                        + user.getStudentNumber()
        );

        holder.phoneText.setText(
                "Phone: "
                        + user.getPhone()
        );

        holder.userRoleText.setText(
                "Role: "
                        + user.getRole()
        );

        if (user.getRole().equalsIgnoreCase("admin")) {

            holder.changeRoleButton.setText(
                    "Remove Admin"
            );

        } else {

            holder.changeRoleButton.setText(
                    "Approve as Admin"
            );
        }

        holder.changeRoleButton.setOnClickListener(
                v -> {

                    if (onRoleChangeListener != null) {

                        onRoleChangeListener.onRoleChange(
                                user
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {

        return userList.size();
    }

    public static class UserViewHolder
            extends RecyclerView.ViewHolder {

        TextView userNameText;
        TextView userEmailText;
        TextView studentNumberText;
        TextView phoneText;
        TextView userRoleText;

        Button changeRoleButton;

        public UserViewHolder(
                @NonNull View itemView) {

            super(itemView);

            userNameText =
                    itemView.findViewById(
                            R.id.userNameText
                    );

            userEmailText =
                    itemView.findViewById(
                            R.id.userEmailText
                    );

            studentNumberText =
                    itemView.findViewById(
                            R.id.studentNumberText
                    );

            phoneText =
                    itemView.findViewById(
                            R.id.phoneText
                    );

            userRoleText =
                    itemView.findViewById(
                            R.id.userRoleText
                    );

            changeRoleButton =
                    itemView.findViewById(
                            R.id.changeRoleButton
                    );
        }
    }
}