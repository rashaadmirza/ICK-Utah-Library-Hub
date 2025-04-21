package edu.northeastern.ickutah.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.BookIssue;
import edu.northeastern.ickutah.ui.adapters.BookIssuesAdapter;

public class BookIssuesFragment extends Fragment {
    private RecyclerView recyclerView;
    private BookIssuesAdapter bookIssuesAdapter;
    private List<BookIssue> filteredIssues;
    private int scrollPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_issues, container, false);

        recyclerView = view.findViewById(R.id.recycler_book_issues);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<BookIssue> bookIssueList = new ArrayList<>();
        bookIssuesAdapter = new BookIssuesAdapter(requireContext(), bookIssueList, this::onBookIssueClicked);
        recyclerView.setAdapter(bookIssuesAdapter);

        SwitchCompat switchShowReturned = view.findViewById(R.id.switch_show_returned);

        // Listen for switch toggle changes
        switchShowReturned.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadBookIssues(isChecked); // Reload list based on switch state
        });

        // Load list with "hide returned" as default
        loadBookIssues(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore scroll position after data is loaded
        recyclerView.post(() -> {
            if (scrollPosition != RecyclerView.NO_POSITION) {
                recyclerView.scrollToPosition(scrollPosition);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadBookIssues(boolean showReturned) {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

            // Get book issues based on switch state
            filteredIssues = db.bookIssueDao().getFilteredBookIssues(showReturned);

            requireActivity().runOnUiThread(() -> {
                if (bookIssuesAdapter != null) {
                    bookIssuesAdapter.updateList(filteredIssues); // Call updateList()
                } else {
                    bookIssuesAdapter = new BookIssuesAdapter(requireContext(), filteredIssues, this::onBookIssueClicked);
                    recyclerView.setAdapter(bookIssuesAdapter);
                }
            });


        }).start();
    }

    // Open Book Issue Details Page when clicked
    private void onBookIssueClicked(BookIssue issue) {
        // Save scroll position before navigating to ReaderDetailsFragment
        recyclerView.post(() -> scrollPosition = ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition());

        Fragment bookIssueDetailsFragment = BookIssueDetailsFragment.newInstance(issue);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookIssueDetailsFragment)
                .addToBackStack(null)
                .commit();
    }
}