package com.example.campuslostfound;

public class UserModel {

    private String uid;
    private String name;
    private String email;
    private String studentNumber;
    private String phone;
    private String role;

    public UserModel(
            String uid,
            String name,
            String email,
            String studentNumber,
            String phone,
            String role) {

        this.uid = uid;
        this.name = name;
        this.email = email;
        this.studentNumber = studentNumber;
        this.phone = phone;
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}