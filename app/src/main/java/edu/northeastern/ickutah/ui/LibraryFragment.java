package edu.northeastern.ickutah.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.ui.adapters.LibraryAdapter;
import edu.northeastern.ickutah.utils.StringUtils;
import edu.northeastern.ickutah.utils.UiUtils;

public class LibraryFragment extends Fragment {
    private RecyclerView recyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Book> bookList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerView = view.findViewById(R.id.recycler_library);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter with an empty list to prevent "No adapter attached" error
        libraryAdapter = new LibraryAdapter(requireContext(), new ArrayList<>(), this::onBookClicked);
        recyclerView.setAdapter(libraryAdapter);

        loadBooks(); // Load bookList

        // Initialize the Add Book FAB
        FloatingActionButton fabAddBook = view.findViewById(R.id.fab_add_book);
        fabAddBook.setOnClickListener(v -> showAddBookDialog());

        return view;
    }

    private void loadBooks() {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());
            bookList = db.bookDao().getAllBooks();

            requireActivity().runOnUiThread(() -> {
                if (libraryAdapter != null) {
                    libraryAdapter.updateList(bookList);
                } else {
                    libraryAdapter = new LibraryAdapter(requireContext(), bookList, this::onBookClicked);
                    recyclerView.setAdapter(libraryAdapter);
                }
            });
        }).start();
    }

    private void onBookClicked(Book book) {
        Fragment bookDetailsFragment = BookDetailsFragment.newInstance(book);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookDetailsFragment)
                .addToBackStack(null)
                .commit();
    }

    public void showAddBookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Book");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_book, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etAuthor = dialogView.findViewById(R.id.et_author);
        EditText etPublisher = dialogView.findViewById(R.id.et_publisher);
        EditText etYear = dialogView.findViewById(R.id.et_year);
        EditText etCopies = dialogView.findViewById(R.id.et_copies);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String author = etAuthor.getText().toString().trim();
            String publisher = etPublisher.getText().toString().trim();
            String year = etYear.getText().toString().trim();
            String copiesStr = etCopies.getText().toString().trim();

            if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || year.isEmpty() || copiesStr.isEmpty()) {
                UiUtils.showToast(requireContext(), "All fields are required!");
                return;
            }

            // Check if year is in the future
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            try {
                int yearInt = Integer.parseInt(year);
                if (yearInt > currentYear) {
                    UiUtils.showToast(requireContext(), "Year cannot be in the future!");
                    return;
                    }
            } catch (NumberFormatException e) {
                UiUtils.showToast(requireContext(), "Invalid year!");
                return;
            }

            int totalCopies;
            try {
                totalCopies = Integer.parseInt(copiesStr);
                if (totalCopies <= 0) {
                    UiUtils.showToast(requireContext(), "Total copies must be at least 1!");
                    return;
                }
            } catch (NumberFormatException e) {
                UiUtils.showToast(requireContext(), "Invalid number of copies!");
                return;
            }

            // Convert to Title Case before querying
            String formattedTitle = StringUtils.toTitleCase(title);
            String formattedAuthor = StringUtils.toTitleCase(author);
            String formattedPublisher = StringUtils.toTitleCase(publisher);

            // Check for duplicates before inserting
            new Thread(() -> {
                LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

                // Check if book with same title & author exists
                Book existingBook = db.bookDao().getBookByTitleAndAuthor(formattedTitle, formattedAuthor);

                requireActivity().runOnUiThread(() -> {
                    if (existingBook != null) {
                        UiUtils.showToast(requireContext(), "Book already exists!");
                    } else {
                        new Thread(() -> {
                            // Insert new book
                            Book newBook = new Book(
                                    "BK" + System.currentTimeMillis(),
                                    formattedTitle,
                                    formattedAuthor,
                                    formattedPublisher,
                                    year,
                                    totalCopies,
                                    totalCopies
                            );
                            db.bookDao().insert(newBook);

                            // Fetch updated bookList
                            List<Book> updatedBooks = db.bookDao().getAllBooks();

                            requireActivity().runOnUiThread(() -> {
                                UiUtils.showToast(requireContext(), "Book Added!");
                                libraryAdapter.updateList(updatedBooks);
                            });
                        }).start();
                    }
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}