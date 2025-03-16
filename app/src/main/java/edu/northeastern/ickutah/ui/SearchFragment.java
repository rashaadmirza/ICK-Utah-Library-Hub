package edu.northeastern.ickutah.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.models.BookIssue;
import edu.northeastern.ickutah.models.Reader;
import edu.northeastern.ickutah.ui.adapters.SearchAdapter;

public class SearchFragment extends Fragment {
    private EditText searchInput;
    private SearchAdapter searchAdapter;
    private CheckBox filterBooks, filterReaders, filterIssues;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchInput = view.findViewById(R.id.search_input);
        RecyclerView recyclerView = view.findViewById(R.id.search_results);
        filterBooks = view.findViewById(R.id.filter_books);
        filterReaders = view.findViewById(R.id.filter_readers);
        filterIssues = view.findViewById(R.id.filter_book_issues);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchAdapter = new SearchAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), this::onSearchItemClicked);
        recyclerView.setAdapter(searchAdapter);

        // Clear initial results
        searchAdapter.updateResults(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        // Search dynamically when typing
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Handle checkbox toggle dynamically
        View.OnClickListener filterListener = v -> performSearch(searchInput.getText().toString().trim());
        filterBooks.setOnClickListener(filterListener);
        filterReaders.setOnClickListener(filterListener);
        filterIssues.setOnClickListener(filterListener);

        return view;
    }

    private void performSearch(String query) {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

            List<Book> filteredBooks = filterBooks.isChecked() ? db.bookDao().searchBooks(query) : new ArrayList<>();
            List<Reader> filteredReaders = filterReaders.isChecked() ? db.readerDao().searchReaders(query) : new ArrayList<>();
            List<BookIssue> filteredIssues = filterIssues.isChecked() ? db.bookIssueDao().searchBookIssues(query) : new ArrayList<>();

            if (!query.isEmpty()) {
                requireActivity().runOnUiThread(() -> searchAdapter.updateResults(filteredBooks, filteredReaders, filteredIssues));
            } else {
                requireActivity().runOnUiThread(() -> searchAdapter.updateResults(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
            }
        }).start();
    }

    private void onSearchItemClicked(Object item) {
        Fragment fragment;
        if (item instanceof Book) {
            fragment = BookDetailsFragment.newInstance((Book) item);
        } else if (item instanceof Reader) {
            fragment = ReaderDetailsFragment.newInstance((Reader) item);
        } else if (item instanceof BookIssue) {
            fragment = BookIssueDetailsFragment.newInstance((BookIssue) item);
        } else {
            return;
        }

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}