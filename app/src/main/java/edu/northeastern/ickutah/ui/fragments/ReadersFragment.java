package edu.northeastern.ickutah.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.Reader;
import edu.northeastern.ickutah.ui.adapters.ReadersAdapter;
import edu.northeastern.ickutah.utils.StringUtils;
import edu.northeastern.ickutah.utils.UiUtils;

public class ReadersFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReadersAdapter readersAdapter;
    private List<Reader> readerList;
    private int scrollPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_readers, container, false);

        recyclerView = view.findViewById(R.id.recycler_readers);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the adapter with an empty list to prevent "No adapter attached" error
        readersAdapter = new ReadersAdapter(requireContext(), new ArrayList<>(), this::onReaderClicked);
        recyclerView.setAdapter(readersAdapter);

        loadReaders(); // Load readers initially

        // Initialize the Add Reader FAB
        FloatingActionButton fabAddReader = view.findViewById(R.id.fab_add_reader);
        fabAddReader.setOnClickListener(v -> showAddReaderDialog());

        // Listen for updates from ReaderDetailsFragment
        getParentFragmentManager().setFragmentResultListener("reader_update", this, (requestKey, bundle) -> {
            loadReaders(); // Reload readers when notified
        });

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

    private void loadReaders() {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());
            readerList = db.readerDao().getAllReaders();

            requireActivity().runOnUiThread(() -> {
                if (readersAdapter != null) {
                    readersAdapter.updateList(readerList);
                } else {
                    readersAdapter = new ReadersAdapter(requireContext(), readerList, this::onReaderClicked);
                    recyclerView.setAdapter(readersAdapter);
                }
            });
        }).start();
    }

    private void onReaderClicked(Reader reader) {
        // Save scroll position before navigating to ReaderDetailsFragment
        recyclerView.post(() -> scrollPosition = ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition());

        Fragment readerDetailsFragment = ReaderDetailsFragment.newInstance(reader);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, readerDetailsFragment)
                .addToBackStack(null)
                .commit();
    }

    public void showAddReaderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Register New Reader");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_reader, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_reader_name);
        EditText etEmail = dialogView.findViewById(R.id.et_reader_email);
        EditText etPhone = dialogView.findViewById(R.id.et_reader_phone);

        builder.setPositiveButton("Register", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                UiUtils.showToastS(requireContext(), "All fields are required!");
                return;
            }

            // Validate email and phone
            if (StringUtils.isInvalidEmail(email)) {
                UiUtils.showToastS(requireContext(), "Invalid email format!");
                return;
            }

            if (StringUtils.isInvalidPhone(phone)) {
                UiUtils.showToastS(requireContext(), "Invalid phone number! Must be 10 digits.");
                return;
            }

            // Convert case before querying
            String formattedName = StringUtils.toTitleCase(name);
            String formattedEmail = StringUtils.toLowerCase(email);

            // Check for duplicates before inserting
            new Thread(() -> {
                LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

                // Check if reader with same phone number exists
                Reader existingReader = db.readerDao().getReaderByPhone(phone);

                requireActivity().runOnUiThread(() -> {
                    if (existingReader != null) {
                        UiUtils.showToastS(requireContext(), "Reader with this phone number already exists!");
                    } else {
                        new Thread(() -> {
                            Reader newReader = new Reader(phone, formattedName, formattedEmail, phone, 0);
                            db.readerDao().insert(newReader);

                            // Fetch updated readerList
                            List<Reader> updatedReaders = db.readerDao().getAllReaders();

                            requireActivity().runOnUiThread(() -> {
                                UiUtils.showToastS(requireContext(), "Reader Registered!");
                                readersAdapter.updateList(updatedReaders);
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