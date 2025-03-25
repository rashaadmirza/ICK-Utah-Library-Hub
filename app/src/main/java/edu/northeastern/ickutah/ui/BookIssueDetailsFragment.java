package edu.northeastern.ickutah.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

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
    private boolean isUndoPressed = false;

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
        MaterialButton markAsReturnedButton = view.findViewById(R.id.btn_mark_as_returned);
        ImageButton backButton = view.findViewById(R.id.btn_back);

        // Retrieve Data from Bundle
        Bundle args = getArguments();
        if (args != null) {
            bookIssue = (BookIssue) args.getSerializable(ARG_BOOK_ISSUE);
            if (bookIssue != null) {
                issueIdView.setText(String.format("Issue ID: %s", bookIssue.getIssueId()));
                bookIdView.setText(String.format("Book ID: %s", bookIssue.getBookId()));
                readerIdView.setText(String.format("Reader ID: %s", bookIssue.getReaderId()));
                issueDateView.setText(String.format("Issued: %s", dateFormat.format(bookIssue.getIssueDate())));
                dueDateView.setText(String.format("Due: %s", dateFormat.format(bookIssue.getDueDate())));

                setReturnStatus(view);

                // Handle Book Return
                markAsReturnedButton.setOnClickListener(v -> confirmReturnBook(bookIssue));
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

        // Update UI with issue details
        issueIdView.setText(String.format("Issue ID: %s", bookIssue.getIssueId()));
        bookIdView.setText(String.format("Book ID: %s", bookIssue.getBookId()));
        readerIdView.setText(String.format("Reader ID: %s", bookIssue.getReaderId()));
        issueDateView.setText(String.format("Issued: %s", dateFormat.format(bookIssue.getIssueDate())));
        dueDateView.setText(String.format("Due: %s", dateFormat.format(bookIssue.getDueDate())));

        setReturnStatus(getView());
    }

    private void setReturnStatus(View rootView) {
        if (bookIssue == null || rootView == null) return;

        TextView returnDateView = rootView.findViewById(R.id.return_date);
        TextView statusView = rootView.findViewById(R.id.issue_status);
        MaterialButton markAsReturnedButton = rootView.findViewById(R.id.btn_mark_as_returned);

        if (bookIssue.isReturned()) {
            returnDateView.setVisibility(View.VISIBLE);
            returnDateView.setText(String.format("Returned: %s", dateFormat.format(bookIssue.getReturnDate())));
            statusView.setText(R.string.returned);
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.successColor));
            markAsReturnedButton.setVisibility(View.GONE); // Hide button if already returned
        } else {
            returnDateView.setVisibility(View.GONE); // Hide return date if not returned
            statusView.setText(R.string.not_returned);
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.errorColor));
            markAsReturnedButton.setVisibility(View.VISIBLE); // Show button if not returned
        }
    }

    private void confirmReturnBook(BookIssue bookIssue) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Return")
                .setMessage("Are you sure you want to mark this book as returned?")
                .setPositiveButton("Yes, Return", (dialog, which) -> snackbackReturn(bookIssue))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void snackbackReturn(BookIssue bookIssue) {
        Date returnTime = new Date(); // Get current time
        isUndoPressed = false; // Reset flag at the start

        // Create an overlay to darken the screen
        View overlay = new View(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black)); // Dark color
        overlay.setAlpha(0.5f); // Adjust transparency (0.0 - fully transparent, 1.0 - fully opaque)

        // Add overlay to the root view
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        rootView.addView(overlay);

        // Block Background Interaction (Prevent Back Navigation & Clicks But Keep Snackbar Interactive)
        overlay.setClickable(true);  // This prevents touches from passing through to the background
        overlay.setFocusable(true);

        // Show Snackbar for Undo Option
        Snackbar snackbar = Snackbar.make(rootView, "Finalizing return...", Snackbar.LENGTH_LONG)
                .setAction("Cancel", v -> cancelReturn()) // Undo action
                .setDuration(2000); // Delay before finalizing

        // Get Snackbar View
        View snackbarView = snackbar.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();

        // Adjust Gravity (Move Snackbar higher)
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = 195; // Same as UiUtils.showToast
        snackbarView.setLayoutParams(params);

        snackbar.show();

        // Re-enable User Interaction after Snackbar disappears
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                rootView.removeView(overlay); // Remove overlay after Snackbar disappears
            }
        });

        // Handler to finalize return after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isUndoPressed) {
                finalizeReturn(bookIssue, returnTime);
            }
        }, 2500);
    }
    
    private void finalizeReturn(BookIssue bookIssue, Date returnTime) {
        new Thread(() -> {
            bookIssue.setReturned(true);
            bookIssue.setReturnDate(returnTime);

            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());
            db.bookIssueDao().update(bookIssue);

            // Update book availability
            Book book = db.bookDao().getBookById(bookIssue.getBookId());
            if (book != null) {
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                db.bookDao().update(book);
            }

            // Fetch updated checkouts count
            int updatedCheckouts = db
                    .bookIssueDao()
                    .getCurrentCheckouts(bookIssue.getReaderId());

            // Update the reader's checkouts count
            Reader reader = db.readerDao().getReaderById(bookIssue.getReaderId());
            if (reader != null) {
                reader.setCurrentCheckouts(updatedCheckouts);
                db.readerDao().update(reader);
            }

            // Update UI
            requireActivity().runOnUiThread(() -> {
                if (bookIssue.isReturned()) {
                    UiUtils.showToastS(requireContext(), "Book Returned!");
                }
                displayBookIssueDetails(); // Refresh UI
            });
        }).start();
    }

    private void cancelReturn() {
        isUndoPressed = true; // Mark return as canceled

        requireActivity().runOnUiThread(() -> {
            UiUtils.showToastS(requireContext(), "Return canceled! Book is still checked out.");
            displayBookIssueDetails(); // Refresh UI
        });
    }
}