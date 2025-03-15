package edu.northeastern.ickutah.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.models.BookIssue;
import edu.northeastern.ickutah.models.Reader;
import edu.northeastern.ickutah.utils.UiUtils;

public class BookIssueDetailsFragment extends Fragment {
    private static final String ARG_BOOK_ISSUE = "book_issue";
    private BookIssue bookIssue;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());

    public static BookIssueDetailsFragment newInstance(BookIssue bookIssue) {
        BookIssueDetailsFragment fragment = new BookIssueDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK_ISSUE, bookIssue);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_issue_details, container, false);

        // Initialize UI Elements
        TextView issueIdView = view.findViewById(R.id.issue_id);
        TextView bookIdView = view.findViewById(R.id.book_id);
        TextView readerIdView = view.findViewById(R.id.reader_id);
        TextView issueDateView = view.findViewById(R.id.issue_date);
        TextView dueDateView = view.findViewById(R.id.due_date);
        TextView returnDateView = view.findViewById(R.id.return_date);
        TextView statusView = view.findViewById(R.id.book_issue_status);
        MaterialButton returnButton = view.findViewById(R.id.btn_mark_as_returned);
        MaterialButton backButton = view.findViewById(R.id.btn_back);

        // Retrieve Data from Bundle
        Bundle args = getArguments();
        if (args != null) {
            bookIssue = (BookIssue) args.getSerializable(ARG_BOOK_ISSUE);
            if (bookIssue != null) {
                issueIdView.setText(String.format("Issue ID: %s", bookIssue.getIssueId()));
                bookIdView.setText(String.format("Book ID: %s", bookIssue.getBookId()));
                readerIdView.setText(String.format("Reader ID: %s", bookIssue.getReaderId()));
                issueDateView.setText(MessageFormat.format("Issued: {0}", dateFormat.format(bookIssue.getIssueDate())));
                dueDateView.setText(MessageFormat.format("Due: {0}", dateFormat.format(bookIssue.getDueDate())));

                if (bookIssue.isReturned()) {
                    returnDateView.setText(MessageFormat.format("Returned: {0}", dateFormat.format(bookIssue.getReturnDate())));
                    statusView.setText(R.string.returned);
                    statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.successColor));
                    returnButton.setVisibility(View.GONE); // Hide return button if book is already returned
                } else {
                    returnDateView.setText(R.string.returned_label);
                    statusView.setText(R.string.not_returned);
                    statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.errorColor));
                    returnButton.setVisibility(View.VISIBLE);
                }

                // Handle Book Return
                returnButton.setOnClickListener(v -> returnBook(bookIssue));
            }
        }

        // Handle Back Button
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void displayBookIssueDetails() {
        if (bookIssue == null || getView() == null) return;

        // Find UI Elements
        TextView issueIdView = getView().findViewById(R.id.issue_id);
        TextView bookIdView = getView().findViewById(R.id.book_id);
        TextView readerIdView = getView().findViewById(R.id.reader_id);
        TextView issueDateView = getView().findViewById(R.id.issue_date);
        TextView dueDateView = getView().findViewById(R.id.due_date);
        TextView returnDateView = getView().findViewById(R.id.return_date);
        TextView statusView = getView().findViewById(R.id.book_issue_status);
        MaterialButton markAsReturnedButton = getView().findViewById(R.id.btn_mark_as_returned);

        // Update UI with issue details
        issueIdView.setText(String.format("Issue ID: %s", bookIssue.getIssueId()));
        bookIdView.setText(String.format("Book ID: %s", bookIssue.getBookId()));
        readerIdView.setText(String.format("Reader ID: %s", bookIssue.getReaderId()));
        issueDateView.setText(String.format("Issued: %s", dateFormat.format(bookIssue.getIssueDate())));
        dueDateView.setText(String.format("Due: %s", dateFormat.format(bookIssue.getDueDate())));

        if (bookIssue.isReturned()) {
            returnDateView.setText(String.format("Returned: %s", dateFormat.format(bookIssue.getReturnDate())));
            statusView.setText(R.string.returned);
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.successColor));
            markAsReturnedButton.setVisibility(View.GONE); // Hide button if already returned
        } else {
            returnDateView.setText(R.string.returned_label);
            statusView.setText(R.string.not_returned);
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.errorColor));
            markAsReturnedButton.setVisibility(View.VISIBLE); // Show button if not returned
        }
    }

    private void returnBook(BookIssue bookIssue) {
        new Thread(() -> {
            // Mark the book as returned
            bookIssue.setReturned(true);
            bookIssue.setReturnDate(new Date());
            LibraryDatabase.getInstance(requireContext()).bookIssueDao().update(bookIssue);

            // Update book availability
            Book book = LibraryDatabase.getInstance(requireContext()).bookDao().getBookById(bookIssue.getBookId());
            if (book != null) {
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                LibraryDatabase.getInstance(requireContext()).bookDao().update(book);
            }

            // Fetch updated checkouts count
            int updatedCheckouts = LibraryDatabase.getInstance(requireContext())
                    .bookIssueDao()
                    .getCurrentCheckouts(bookIssue.getReaderId());

            // Update the reader's checkouts count
            Reader reader = LibraryDatabase.getInstance(requireContext()).readerDao().getReaderById(bookIssue.getReaderId());
            if (reader != null) {
                reader.setCurrentCheckouts(updatedCheckouts);
                LibraryDatabase.getInstance(requireContext()).readerDao().update(reader);
            }

            // Update UI on the Main Thread
            requireActivity().runOnUiThread(() -> {
                UiUtils.showToast(requireContext(), "Book Returned!");
                displayBookIssueDetails(); // Refresh the UI
            });
        }).start();
    }
}