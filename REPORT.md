# Final Project Report - ICK Utah Library Hub

**Course**: CS 5520 - Mobile App Development  
**Instructor**: Dr. Gary Cantrell  
**Author**: Rashaad Mohammed Mirza  
**Email** [mirza.ra@northeastern.edu](mailto:mirza.ra@northeastern.edu)   
**Date**: April 21, 2025

**[View README](README.md)**

---

## 1. Project Reflection

This project was both a technical and personal milestone. I set out to create a mobile library management system for the [Islamic Center of Kuwait in Utah](https://ickutah.org/), a real-world need that had previously been proposed to me. Over the semester, I transformed that idea into a fully functional Android application using Java, Room, and Material Design components. 

Throughout the development process, I learned how to design and build a complete app from scratch; managing data persistence, building intuitive user interfaces, and handling real-world user scenarios like duplicate entry prevention and overdue tracking. I also gained valuable insight into structuring an app modularly, using fragments and adapters to keep the project maintainable.

If I were to do it again, I would start with a clearer architectural pattern (e.g., MVVM with ViewModels) and implement authentication and cloud sync earlier. Despite time constraints, I’m proud of how the app evolved and the attention to UI/UX polish. The app has been well received by testers and is already planned for pilot use by ICK Utah’s library team.

---

## 2. Incorporation of Class Concepts

The following concepts from the course and other resources were intentionally integrated into the app:

| Concept | Implementation |
|--------|----------------|
| **Fragments** | Used for modular navigation across screens like Home, Library, Readers, Book Details, Issue Records, Search, Advanced, and About. |
| **RecyclerView** | Displayed lists of books, readers, and issue records with custom item layouts and optimized ViewHolders. |
| **Adapters & DiffUtil** | `LibraryAdapter`, `ReadersAdapter`, `BookIssuesAdapter`, and `SearchAdapter` implemented efficient data handling. |
| **Custom Dialogs** | Used for adding/editing books and readers with `AlertDialog.Builder`. |
| **Data Persistence** | Room Database with DAOs was used for storing and managing data locally. |
| **Navigation** | Navigation through `MainActivity` using fragment transactions and drawer menu. |
| **Material Design** | Applied Material Components for buttons, cards, and dialogs to ensure consistency. |
| **File I/O** | CSV import/export functionality implemented using file picker and InputStream handling. |
| **Validation** | Input validation for books, readers, and import data with user-friendly messages. |
| **UX Enhancements** | Visual indicators for overdue/returned books, confirmation dialogs, dynamic card expansion, and search filtering. |

These components demonstrate a comprehensive application of the skills taught in class, with thoughtful design and feature planning.

---

## 3. Proposal Objective Breakdown

[View Project Proposal](https://docs.google.com/document/d/1MG8AiDJTvK4ksq_WfTxVjb_1Ox4cOdX9BtNQpzushRM/edit?usp=sharing) - This document outlines the initial objectives, motivation, and scope of the ICK Utah Library Hub project.

### Primary Objectives (Fully Met)
| Objective | Status | How It Was Met |
|-----------|--------|----------------|
| CSV book import | Fully Met | File picker-based import with validation and duplicate checking. |
| View all books | Fully Met | RecyclerView in `LibraryFragment`. |
| Add/edit/delete books | Fully Met | Full CRUD supported with validation. |
| Search books | Fully Met | SearchFragment with filters and clickable results. |
| UI using Android Studio | Fully Met | Clean layouts with Material Components. |
| RecyclerView & list features | Fully Met | All lists use optimized adapters and layouts. |

### Secondary Objectives (Fully Met)
| Objective | Status | Explanation |
|-----------|--------|-------------|
| Book lending (check-in/out) | Fully Met | Implemented via issue and return workflow. |
| Borrower tracking | Fully Met | Readers are registered and linked to issued books. |
| Entry validation | Fully Met | Strong input checks throughout forms and imports. |

### Tertiary Objectives (Fully Met)
| Objective | Status | Explanation |
|-----------|--------|-------------|
| Analytics dashboard | Fully Met | Home screen shows collection stats and reader activity. |
| SQLite storage | Fully Met | Implemented via Room ORM. |
| Barcode scanning | Not Met | Not attempted due to time/hardware.
| Admin auth | Not Met | Not implemented; scope limited to local use.

---

## 4. Grading

I believe this project meets and exceeds the expectations for an **A (92-100%)** based on the following:

| Category | Criteria | Achieved |
|----------|----------|----------|
| Technical Implementation | Robust CRUD features, issue tracking, search, CSV I/O | Yes |
| UI/UX Design | Consistent Material UI, responsive layouts, dashboard, and feedback | Yes |
| Code Organization | Modular fragments, adapters, DAOs, utility classes | Yes |
| Class Concepts | All major topics applied meaningfully | Yes |
| Documentation | Complete README, report, and proposal mapping | Yes |
| Originality & Scope | Real-world use case, stakeholder-aligned, fully functional | Yes |

This project has grown into a polished, multi-screen Android app with real-world relevance. I am confident it reflects strong effort, learning, and execution.

---
