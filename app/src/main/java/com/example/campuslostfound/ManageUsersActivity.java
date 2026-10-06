package com.example.campuslostfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    /*
     * UID of the MAIN ADMINISTRATOR
     */
    private static final String MAIN_ADMIN_UID =
            "HeQLdfT12rRMU9F0vFivwHDLxQ22";

    private RecyclerView usersRecyclerView;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private final List<UserModel> userList =
            new ArrayList<>();

    private UserAdapter userAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_manage_users
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        usersRecyclerView =
                findViewById(
                        R.id.usersRecyclerView
                );

        usersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        userAdapter =
                new UserAdapter(
                        this,
                        userList,
                        this::changeUserRole
                );

        usersRecyclerView.setAdapter(
                userAdapter
        );

        verifyMainAdminAccess();
    }


    /*
     * ==========================================
     * VERIFY MAIN ADMIN
     * ==========================================
     */

    private void verifyMainAdminAccess() {

        FirebaseUser firebaseUser =
                firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {

            denyAccess();

            return;
        }

        /*
         * Check the Firebase UID.
         *
         * Only the main administrator's
         * Firebase account can enter this screen.
         */
        if (!firebaseUser.getUid().equals(
                MAIN_ADMIN_UID
        )) {

            Toast.makeText(
                    this,
                    "Only the main administrator can manage users.",
                    Toast.LENGTH_LONG
            ).show();

            denyAccess();

            return;
        }

        /*
         * Also verify the Firestore role.
         */
        firestore
                .collection("users")
                .document(MAIN_ADMIN_UID)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                denyAccess();

                                return;
                            }

                            String role =
                                    documentSnapshot.getString(
                                            "role"
                                    );

                            if (role == null ||
                                    !role.equalsIgnoreCase("admin")) {

                                Toast.makeText(
                                        this,
                                        "Main administrator account is not configured correctly.",
                                        Toast.LENGTH_LONG
                                ).show();

                                denyAccess();

                                return;
                            }

                            loadUsers();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not verify main administrator access.",
                                    Toast.LENGTH_LONG
                            ).show();

                            denyAccess();
                        }
                );
    }


    /*
     * ==========================================
     * LOAD USERS FROM FIRESTORE
     * ==========================================
     */

    private void loadUsers() {

        firestore
                .collection("users")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            userList.clear();

                            for (
                                    QueryDocumentSnapshot document :
                                    queryDocumentSnapshots
                            ) {

                                String uid =
                                        document.getString("uid");

                                String name =
                                        document.getString("name");

                                String email =
                                        document.getString("email");

                                String studentNumber =
                                        document.getString(
                                                "studentNumber"
                                        );

                                String phone =
                                        document.getString("phone");

                                String role =
                                        document.getString("role");


                                if (uid == null) {

                                    uid =
                                            document.getId();
                                }


                                if (name == null ||
                                        name.isEmpty()) {

                                    name =
                                            "Unknown User";
                                }


                                if (email == null ||
                                        email.isEmpty()) {

                                    email =
                                            "No email";
                                }


                                if (studentNumber == null) {

                                    studentNumber =
                                            "";
                                }


                                if (phone == null) {

                                    phone =
                                            "";
                                }


                                if (role == null ||
                                        role.isEmpty()) {

                                    role =
                                            "user";
                                }


                                UserModel user =
                                        new UserModel(
                                                uid,
                                                name,
                                                email,
                                                studentNumber,
                                                phone,
                                                role
                                        );

                                userList.add(user);
                            }


                            userAdapter.notifyDataSetChanged();


                            if (userList.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "No users found.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Failed to load users: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    /*
     * ==========================================
     * CHANGE USER ROLE
     * ==========================================
     */

    private void changeUserRole(
            UserModel user) {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();


        if (currentUser == null) {

            denyAccess();

            return;
        }


        /*
         * Double-check that the person making
         * the change is the main administrator.
         */
        if (!currentUser.getUid().equals(
                MAIN_ADMIN_UID
        )) {

            Toast.makeText(
                    this,
                    "Only the main administrator can approve or remove administrators.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        /*
         * Never allow the main administrator
         * to remove their own admin role.
         */
        if (currentUser.getUid().equals(
                user.getUid()
        )) {

            Toast.makeText(
                    this,
                    "You cannot change your own admin role.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        String currentRole =
                user.getRole();


        /*
         * Only pending_admin accounts are
         * approved through this screen.
         */
        if (currentRole == null ||
                !currentRole.equalsIgnoreCase(
                        "pending_admin"
                )) {

            if (currentRole != null &&
                    currentRole.equalsIgnoreCase("admin")) {

                /*
                 * Remove an existing admin.
                 */
                firestore
                        .collection("users")
                        .document(user.getUid())
                        .update(
                                "role",
                                "user"
                        )
                        .addOnSuccessListener(
                                unused -> {

                                    user.setRole("user");

                                    userAdapter
                                            .notifyDataSetChanged();

                                    Toast.makeText(
                                            this,
                                            user.getName()
                                                    + " is no longer an administrator.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                        )
                        .addOnFailureListener(
                                e -> {

                                    Toast.makeText(
                                            this,
                                            "Could not remove admin role: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                        );

            } else {

                Toast.makeText(
                        this,
                        "Only pending administrator requests can be approved.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return;
        }


        /*
         * APPROVE PENDING ADMIN
         */
        firestore
                .collection("users")
                .document(user.getUid())
                .update(
                        "role",
                        "admin"
                )
                .addOnSuccessListener(
                        unused -> {

                            user.setRole("admin");

                            userAdapter
                                    .notifyDataSetChanged();

                            Toast.makeText(
                                    this,
                                    user.getName()
                                            + " has been approved as an administrator.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not approve administrator: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    /*
     * ==========================================
     * DENY ACCESS
     * ==========================================
     */

    private void denyAccess() {

        firebaseAuth.signOut();

        Intent intent =
                new Intent(
                        ManageUsersActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}