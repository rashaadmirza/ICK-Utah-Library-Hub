package edu.northeastern.ickutah.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private boolean isExpanded = false;
    private TextView accordionContent, titleCount, authorCount, bookCount, readingCount, bookLoanedCount, overdueLoansCount;
    private ImageView accordionIcon;
    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI Elements
        titleCount = view.findViewById(R.id.title_count);
        authorCount = view.findViewById(R.id.author_count);
        bookCount = view.findViewById(R.id.book_count);
        readingCount = view.findViewById(R.id.reading_count);
        bookLoanedCount = view.findViewById(R.id.book_loaned_count);
        overdueLoansCount = view.findViewById(R.id.overdue_loans_count);

        // Initialize ExecutorService for Background Tasks
        executorService = Executors.newSingleThreadExecutor();

        // Load data from database
        loadDataFromDatabase();

        // Initialize Accordion UI Elements
        View accordionHeader = view.findViewById(R.id.accordion_header);
        accordionContent = view.findViewById(R.id.accordion_content);
        accordionIcon = view.findViewById(R.id.accordion_icon);

        // Handle Accordion Click
        accordionHeader.setOnClickListener(v -> toggleAccordion());

        return view;
    }

    private void loadDataFromDatabase() {
        executorService.execute(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

            // Get the current timestamp
            long currentTime = System.currentTimeMillis();

            // Fetch data from the database
            int totalBooks = db.bookDao().getTotalBookCount();
            int totalTitles = db.bookDao().getUniqueTitleCount();
            int totalAuthors = db.bookDao().getUniqueAuthorCount();
            int totalLoans = db.bookIssueDao().getTotalIssuedBooks();
            int totalReading = db.bookIssueDao().getCurrentlyReadingCount();
            int totalOverdue = db.bookIssueDao().getOverdueBooksCount(currentTime); // Pass current timestamp

            // Update UI on the Main Thread
            requireActivity().runOnUiThread(() -> {
                titleCount.setText(String.valueOf(totalTitles));
                authorCount.setText(String.valueOf(totalAuthors));
                bookCount.setText(String.valueOf(totalBooks));
                bookLoanedCount.setText(String.valueOf(totalLoans));
                readingCount.setText(String.valueOf(totalReading));
                overdueLoansCount.setText(String.valueOf(totalOverdue));
            });
        });
    }

    private void toggleAccordion() {
        if (isExpanded) {
            accordionContent.setVisibility(View.GONE);
            accordionIcon.setImageResource(R.drawable.ic_expand_more);
        } else {
            accordionContent.setVisibility(View.VISIBLE);
            accordionIcon.setImageResource(R.drawable.ic_expand_less);
        }
        isExpanded = !isExpanded;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromDatabase(); // Refresh Home Page when navigating back
    }
}