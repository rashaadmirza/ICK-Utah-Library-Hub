package edu.northeastern.ickutah.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.models.BookIssue;
import edu.northeastern.ickutah.utils.StringUtils;
import edu.northeastern.ickutah.utils.UiUtils;

public class BookDetailsFragment extends Fragment {
    private static final String ARG_BOOK = "book";
    private Book book;

    public static BookDetailsFragment newInstance(Book book) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_details, container, false);

        // Initialize UI Elements
        TextView bookIdView = view.findViewById(R.id.book_id);
        TextView titleView = view.findViewById(R.id.book_title);
        TextView authorView = view.findViewById(R.id.book_author);
        TextView publisherView = view.findViewById(R.id.book_publisher);
        TextView yearView = view.findViewById(R.id.book_year);
        TextView totalCopiesView = view.findViewById(R.id.book_total_copies);
        TextView availableCopiesView = view.findViewById(R.id.book_available_copies);

        // Retrieve Data from Bundle
        Bundle args = getArguments();
        if (args != null) {
            book = (Book) args.getSerializable(ARG_BOOK);

            if (book != null) {
                bookIdView.setText(String.format("Book ID: %s", book.getBookId()));
                titleView.setText(book.getTitle());
                authorView.setText(String.format("Author: %s", book.getAuthor()));
                publisherView.setText(String.format("Publisher: %s", book.getPublisher()));
                yearView.setText(String.format("Year: %s", book.getYear()));
                totalCopiesView.setText(MessageFormat.format("Total copies: {0}", book.getTotalCopies()));
                availableCopiesView.setText(MessageFormat.format("Available copies: {0}", book.getAvailableCopies()));
            }
        }

        // Handle Issue Book Button
        MaterialButton issueButton = view.findViewById(R.id.btn_issue_book);
        issueButton.setOnClickListener(v -> showIssueBookDialog(book));

        // Handle Buttons
        MaterialButton backButton = view.findViewById(R.id.btn_back);
        MaterialButton editButton = view.findViewById(R.id.btn_edit);
        MaterialButton deleteButton = view.findViewById(R.id.btn_delete);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        editButton.setOnClickListener(v -> showEditBookDialog(book));
        deleteButton.setOnClickListener(v -> deleteBook(book));

        return view;
    }

    @SuppressLint("DefaultLocale")
    private void displayBookDetails(Book book) {
        if (book == null) return;

        new Thread(() -> {
            int issuedCopies = LibraryDatabase.getInstance(requireContext())
                    .bookIssueDao()
                    .getIssuedCopiesCount(book.getBookId());

            int availableCopies = Math.max(0, book.getTotalCopies() - issuedCopies);

            requireActivity().runOnUiThread(() -> {
                // Find Views
                TextView bookIdView = requireView().findViewById(R.id.book_id);
                TextView titleView = requireView().findViewById(R.id.book_title);
                TextView authorView = requireView().findViewById(R.id.book_author);
                TextView publisherView = requireView().findViewById(R.id.book_publisher);
                TextView yearView = requireView().findViewById(R.id.book_year);
                TextView totalCopiesView = requireView().findViewById(R.id.book_total_copies);
                TextView availableCopiesView = requireView().findViewById(R.id.book_available_copies);

                // Update UI
                bookIdView.setText(String.format("Book ID: %s", book.getBookId()));
                titleView.setText(book.getTitle());
                authorView.setText(String.format("Author: %s", book.getAuthor()));
                publisherView.setText(String.format("Publisher: %s", book.getPublisher()));
                yearView.setText(String.format("Year: %s", book.getYear()));
                totalCopiesView.setText(String.format("Total copies: %d", book.getTotalCopies()));
                availableCopiesView.setText(String.format("Available copies: %d", availableCopies));
            });
        }).start();
    }

    private void showIssueBookDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Issue Book");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_issue_book, null);
        builder.setView(dialogView);

        EditText etReaderId = dialogView.findViewById(R.id.et_reader_id);

        // Automatically show keyboard
        etReaderId.requestFocus();
        etReaderId.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etReaderId, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200); // Small delay to ensure the keyboard appears

        builder.setPositiveButton("Issue", (dialog, which) -> {
            String readerId = etReaderId.getText().toString().trim().toUpperCase();
            if (readerId.isEmpty()) {
                UiUtils.showToastS(requireContext(), "Reader ID is required!");
                return;
            }

            if (readerId.length() < 10) {
                UiUtils.showToastS(requireContext(), "Reader ID must be at least 10 characters long!");
                return;
            }

            new Thread(() -> {
                LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

                // Check if the reader exists
                if (db.readerDao().getReaderById(readerId) == null) {
                    requireActivity().runOnUiThread(() ->
                            UiUtils.showToastL(requireContext(), "Reader not found!")
                    );
                    return;
                }

                // Issue the book
                BookIssue newIssue = new BookIssue(
                        "ISSUE" + System.currentTimeMillis(),
                        book.getBookId(),
                        readerId,
                        new Date(),
                        new Date(System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000)), // 14 days loan
                        null,
                        false
                );

                db.bookIssueDao().insert(newIssue);

                // Update available copies
                book.setAvailableCopies(book.getAvailableCopies() - 1);
                db.bookDao().update(book);

                // Fetch updated book data
                Book updatedBook = db.bookDao().getBookById(book.getBookId());

                requireActivity().runOnUiThread(() -> {
                    UiUtils.showToastL(requireContext(), "Book Issued!");

                    // Update UI with fresh data
                    displayBookDetails(updatedBook);
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditBookDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Book Details");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_book, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etAuthor = dialogView.findViewById(R.id.et_author);
        EditText etPublisher = dialogView.findViewById(R.id.et_publisher);
        EditText etYear = dialogView.findViewById(R.id.et_year);
        EditText etCopies = dialogView.findViewById(R.id.et_copies);

        etTitle.setText(book.getTitle());
        etAuthor.setText(book.getAuthor());
        etPublisher.setText(book.getPublisher());
        etYear.setText(book.getYear());
        etCopies.setText(String.valueOf(book.getTotalCopies()));

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newAuthor = etAuthor.getText().toString().trim();
            String newPublisher = etPublisher.getText().toString().trim();
            String newYear = etYear.getText().toString().trim();
            String newCopiesStr = etCopies.getText().toString().trim();

            if (newTitle.isEmpty() || newAuthor.isEmpty() || newPublisher.isEmpty() || newYear.isEmpty() || newCopiesStr.isEmpty()) {
                UiUtils.showToastS(requireContext(), "All fields are required!");
                return;
            }

            // Check if year is in the future
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            try {
                int yearInt = Integer.parseInt(newYear);
                if (yearInt > currentYear) {
                    UiUtils.showToastS(requireContext(), "Year cannot be in the future!");
                    return;
                }
            } catch (NumberFormatException e) {
                UiUtils.showToastS(requireContext(), "Invalid year!");
                return;
            }

            int newTotalCopies;
            try {
                newTotalCopies = Integer.parseInt(newCopiesStr);
                if (newTotalCopies <= 0) {
                    UiUtils.showToastS(requireContext(), "Total copies must be at least 1!");
                    return;
                }
            } catch (NumberFormatException e) {
                UiUtils.showToastS(requireContext(), "Invalid number of copies!");
                return;
            }

            // Convert to Title Case before querying
            String formattedNewTitle = StringUtils.toTitleCase(newTitle);
            String formattedNewAuthor = StringUtils.toTitleCase(newAuthor);
            String formattedNewPublisher = StringUtils.toTitleCase(newPublisher);

            new Thread(() -> {
                LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

                // Check if book with same title & author exists
                Book existingBook = db.bookDao().getBookByTitleAndAuthor(formattedNewTitle, formattedNewAuthor);

                // Calculate issued copies and available copies
                int issuedCopies = db.bookIssueDao().getIssuedCopiesCount(book.getBookId());
                int newAvailableCopies = Math.max(0, newTotalCopies - issuedCopies);

                requireActivity().runOnUiThread(() -> {
                    if (existingBook != null && !existingBook.getBookId().equals(book.getBookId())) {
                        UiUtils.showToastS(requireContext(), "Book already exists!");
                    } else if (newTotalCopies < issuedCopies) {
                        UiUtils.showToastS(requireContext(), "Total copies cannot be less than issued copies!");
                    } else {
                        new Thread(() -> {
                            // Update book details in background thread
                            book.setTitle(formattedNewTitle);
                            book.setAuthor(formattedNewAuthor);
                            book.setPublisher(formattedNewPublisher);
                            book.setYear(newYear);
                            book.setTotalCopies(newTotalCopies);
                            book.setAvailableCopies(newAvailableCopies);
                            db.bookDao().update(book);

                            requireActivity().runOnUiThread(() -> {
                                UiUtils.showToastS(requireContext(), "Book Updated!");
                                displayBookDetails(book);
                            });
                        }).start();
                    }
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteBook(Book book) {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

            // Fetch latest checkout count
            int currentCheckouts = db.bookIssueDao().getIssuedCopiesCount(book.getBookId());

            requireActivity().runOnUiThread(() -> {
                if (currentCheckouts > 0) {
                    // Prevent deletion
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Cannot Delete Book")
                            .setMessage("This book has unreturned copies. Please return all copies before deletion.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    // Proceed with delete confirmation
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Book")
                            .setMessage("Are you sure you want to delete this book?")
                            .setPositiveButton("Delete", (dialog, which) -> new Thread(() -> {
                                db.bookDao().delete(book);
                                requireActivity().runOnUiThread(() -> {
                                    UiUtils.showToastS(requireContext(), "Book Deleted!");
                                    requireActivity().getSupportFragmentManager().popBackStack(); // Go back to previous screen
                                });
                            }).start())
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }).start();
    }
}