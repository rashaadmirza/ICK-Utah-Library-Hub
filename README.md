# ICK Utah Library Hub

The **ICK Utah Library Hub** is an Android application designed for the library administration at the [Islamic Center of Kuwait (ICK) in Utah](https://ickutah.org/). The app enables efficient management of books, readers, and issue tracking through an intuitive mobile interface. Developed using Java, Room (SQLite), and Material Design Components, the project aims to digitize and streamline library operations, ensuring data integrity and user-friendly management.

**[View Final Project Report](REPORT.md)**

---

## Features

- **Book Cataloging**: Add, update, and manage books with duplicate prevention and input validation.
- **Reader Registration**: Register readers with verified email and phone formats and track their activity.
- **Loan Management**: Issue and return books, with status tracking for active, overdue, and completed loans.
- **Search Functionality**: Unified search across books, readers, and issue records with filter options.
- **Import/Export via CSV**: Admins can import and export book and reader data using CSV files with built-in validation.
- **Visual Dashboard**: Home screen summarizes key metrics such as book counts, active issues, and overdue loans.
- **Admin Tools**: Includes options to clear past return records and manage datasets efficiently.
- **About & Support**: Provides project background and access to a feedback/support form.

---

## Technology Stack

- **Language**: Java
- **Database**: Room (SQLite)
- **UI Development**: XML, Material Components for Android
- **Architecture**: Fragment-based, MVVM-inspired organization
- **Tools Used**: Android Studio, Gradle

---

## Folder Structure

```
app/src/main/java/edu/northeastern/ickutah
│
├── dao                # Data Access Objects (BookDao, ReaderDao, BookIssueDao)
├── database           # Room Database setup and type converters (LibraryDatabase, DateConverter)
├── models             # Entity models for Room database (Book, Reader, BookIssue)
├── ui                 # UI components (Fragments, Adapters)
│   ├── adapters       # RecyclerView Adapters for Books, Readers, Book Issues, Search
│   └── fragments      # UI Fragments (Home, Library, Details, Readers, Search, Advanced, About)
├── utils              # Utility classes (UiUtils, StringUtils)
└── MainActivity.java  # App entry point and navigation host

app/src/main/res – App resources such as:
  ├── layout           # All XML files for screen layouts
  ├── drawable         # Vector icons, button styles, and custom shapes
  ├── values           # Centralized themes, colors, strings, and styles
  ├── menu             # Toolbar and drawer menu XMLs
  ├── mipmap           # Launcher icons for multiple screen densities

```

---

## Build Instructions

1. Clone the repository and open it in **Android Studio**.
2. Run the project on a physical Android device or emulator (Android 8 or above).
3. No cloud setup or Firebase integration is required. All data is stored locally using **Room**.
4. CSV imports use the Android file picker and require accessible `.csv` files with the appropriate structure.
5. To test CSV import, prepare sample files for:
   - `books.csv`: Title, Author, Publisher, Year, Total Copies
   - `readers.csv`: Name, Email, Phone

---

## Future Enhancements

- **Cloud Storage Integration** (e.g., Firebase or Firestore)
- **Book Cover Support** (image upload and display)
- **Book Location Tagging** (rack/shelf identifiers)
- **SMS Reminders** for due dates
- **User Roles & Authentication** (Admin/Reader separation)

---

## Developer

Developed by [Rashaad Mirza](https://www.linkedin.com/in/rashaadmirza/) as part of the CS 5520 Mobile App Development course at Northeastern University.
Please credit appropriately if you build upon this work.

---

## Acknowledgments

This project was developed under the guidance of [Dr. Gary Cantrell](https://www.khoury.northeastern.edu/people/gary-cantrell/) at Northeastern University and in coordination with **Sheikh Mohammed Al-Tigar** of ICK Utah. I am grateful for the feedback provided by the administrative staff and student testers, which significantly enhanced the app’s functionality and user experience.

---

## Feedback & Support

To report issues or suggest improvements, please submit a [Support Request Form](https://forms.gle/MdNb5M4xv7eJzRVy7).

---

