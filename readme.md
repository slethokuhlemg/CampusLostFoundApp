# Campus Lost & Found

## About the Project

**Campus Lost & Found** is an Android application designed to help students report, search for, and manage lost and found items on campus.

The application provides a central platform where students can report items they have lost or found and view reports from other students. Administrators can review reports, manage their status, and help students manage the recovery process.

## Features

- Student registration and login
- Student account management
- Report lost items
- Report found items
- Add descriptions and item details
- Select the date an item was lost
- Add item photographs
- Search for lost and found items
- Browse reported items
- View personal reports
- Track report status
- Receive report notifications
- Student-to-admin messaging
- Admin login and registration
- Admin dashboard
- Admin report management
- Approve or reject reports
- Mark items as resolved/recovered
- Delete reports when necessary
- Firebase Authentication
- Firebase Firestore database
- Cloud-based data storage

## Technologies Used

- **Java** – Main programming language
- **Android Studio** – Development environment
- **XML** – User interface design
- **Firebase Authentication** – User authentication
- **Firebase Firestore** – Cloud database
- **Material Design** – User interface components
- **RecyclerView** – Displaying lists of reports and items

## Application Structure

The project contains the following main sections:

### Student Side

Students can:

1. Create an account.
2. Log into the application.
3. Report lost items.
4. Report found items.
5. Search for reported items.
6. View their reports.
7. Track the status of their reports.
8. Receive notifications.
9. Communicate with administrators.

### Admin Side

Administrators can:

1. Log into the admin system.
2. View reported items.
3. Review report information.
4. View uploaded photographs.
5. Approve or reject reports.
6. Update report statuses.
7. Manage recovered or resolved items.
8. Delete reports when required.
9. Communicate with students.

## Firebase Backend

The application uses Firebase as its cloud backend.

### Firebase Authentication

Firebase Authentication is used to manage student and administrator accounts and provide secure login functionality.

### Firebase Firestore

Cloud Firestore is used to store application data such as:

- Lost item reports
- Found item reports
- Report status
- Student information
- Report dates
- Notifications
- Other application information

Because the application uses Firebase, information can be synchronized through the cloud instead of being stored only on one device.

## Project Requirements

To open and run the project, you will need:

- Android Studio
- Android SDK
- Java
- An Android phone or Android Emulator
- Internet connection
- Firebase project configuration

## Download the Application

The latest APK is available in the:

**`APK`** folder.

Download:

**`CampusLostFound.apk`**

You can install the APK on a compatible Android device for testing.

## Project Documentation

Complete project documentation is available in the:

**`Documentation`** folder.

The documentation contains additional information about the application, its features, design, backend, and development.

## Project Structure

```text
CampusLostFound
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── APK/
│   └── CampusLostFound.apk
│
├── Documentation/
│   └── CampusLostFound Project Report.pdf
│
├── gradle/
│
├── build.gradle
├── settings.gradle
└── README.md
```

## Purpose of the Application

The purpose of Campus Lost & Found is to make it easier for students to report and recover lost property on campus.

Instead of relying only on physical notices or word of mouth, students can use the application to report items and search for items that have been found by other students.

## Future Improvements

Possible future improvements include:

- Improved item matching
- Push notifications
- Advanced search filters
- Image-based item recognition
- Improved messaging
- Claim verification
- Location-based item searching
- Additional security features
- Improved administrator tools

## Developer

**Campus Lost & Found**

Android Application Project

Built using **Android Studio, Java, XML, and Firebase**.

## License

This project was created as an academic/student software development project.
