package edu.northeastern.ickutah.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.MessageFormat;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.Reader;
import edu.northeastern.ickutah.utils.StringUtils;
import edu.northeastern.ickutah.utils.UiUtils;

public class ReaderDetailsFragment extends Fragment {
    private static final String ARG_READER = "reader";
    private Reader reader;

    public static ReaderDetailsFragment newInstance(Reader reader) {
        ReaderDetailsFragment fragment = new ReaderDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_READER, reader);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader_details, container, false);

        // Initialize UI Elements
        TextView idView = view.findViewById(R.id.reader_id);
        TextView nameView = view.findViewById(R.id.reader_name);
        TextView emailView = view.findViewById(R.id.reader_email);
        TextView phoneView = view.findViewById(R.id.reader_phone);
        TextView statusView = view.findViewById(R.id.current_checkouts);

        // Retrieve Data from Bundle
        Bundle args = getArguments();
        if (args != null) {
            reader = (Reader) args.getSerializable(ARG_READER);

            if (reader != null) {
                idView.setText(reader.getReaderId());
                nameView.setText(String.format("Name: %s", reader.getName()));
                emailView.setText(String.format("Email: %s", reader.getEmail()));
                phoneView.setText(String.format("Phone: %s", reader.getPhone()));
                statusView.setText(MessageFormat.format("Current Checkouts: {0}", reader.getCurrentCheckouts()));
            }
        }

        // Handle Buttons
        ImageButton backButton = view.findViewById(R.id.btn_back);
        ImageButton editButton = view.findViewById(R.id.btn_edit);
        ImageButton deleteButton = view.findViewById(R.id.btn_delete);

        backButton.setOnClickListener(v -> {
            getParentFragmentManager().setFragmentResult("reader_update", new Bundle()); // Send update signal
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        editButton.setOnClickListener(v -> showEditReaderDialog(reader));
        deleteButton.setOnClickListener(v -> deleteReader(reader));

        return view;
    }

    @SuppressLint("DefaultLocale")
    private void displayReaderDetails(Reader reader) {
        if (reader == null || getView() == null) return;

        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

            // Fetch latest reader details from the database
            Reader updatedReader = db.readerDao().getReaderById(reader.getReaderId());

            // Fetch latest checkout count
            int currentCheckouts = db.bookIssueDao().getCurrentCheckouts(reader.getReaderId());

            requireActivity().runOnUiThread(() -> {
                if (updatedReader != null) {
                    // Find UI Elements
                    TextView idView = getView().findViewById(R.id.reader_id);
                    TextView nameView = getView().findViewById(R.id.reader_name);
                    TextView emailView = getView().findViewById(R.id.reader_email);
                    TextView phoneView = getView().findViewById(R.id.reader_phone);
                    TextView statusView = getView().findViewById(R.id.current_checkouts);

                    // Update UI with reader details
                    idView.setText(reader.getReaderId());
                    nameView.setText(String.format("Name: %s", reader.getName()));
                    emailView.setText(String.format("Email: %s", reader.getEmail()));
                    phoneView.setText(String.format("Phone: %s", reader.getPhone()));
                    statusView.setText(String.format("Current Checkouts: %d", currentCheckouts));
                }
            });
        }).start();
    }

    private void showEditReaderDialog(Reader reader) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Reader Details");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_reader, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_reader_name);
        EditText etEmail = dialogView.findViewById(R.id.et_reader_email);
        EditText etPhone = dialogView.findViewById(R.id.et_reader_phone);

        etName.setText(reader.getName());
        etEmail.setText(reader.getEmail());
        etPhone.setText(reader.getPhone());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                UiUtils.showToastS(requireContext(), "All fields are required!");
                return;
            }

            // Validate email and phone
            if (StringUtils.isInvalidEmail(newEmail)) {
                UiUtils.showToastS(requireContext(), "Invalid email format!");
                return;
            }

            if (StringUtils.isInvalidPhone(newPhone)) {
                UiUtils.showToastS(requireContext(), "Invalid phone number! Must be 10 digits.");
                return;
            }

            // Convert case before querying
            String formattedNewName = StringUtils.toTitleCase(newName);
            String formattedNewEmail = StringUtils.toLowerCase(newEmail);

            new Thread(() -> {
                LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

                // Check if reader with same phone exists
                Reader existingReader = db.readerDao().getReaderByPhone(newPhone);

                requireActivity().runOnUiThread(() -> {
                    if (existingReader != null && !existingReader.getReaderId().equals(reader.getReaderId())) {
                        UiUtils.showToastS(requireContext(), "Reader with this phone number already exists!");
                    } else {
                        new Thread(() -> {
                            // Update reader details in background thread
                            String oldReaderId = reader.getReaderId(); // Store old readerId before updating

                            reader.setReaderId(newPhone);
                            reader.setName(formattedNewName);
                            reader.setEmail(formattedNewEmail);
                            reader.setPhone(newPhone);
                            db.readerDao().update(reader);

                            // Update all book issues linked to the old readerId
                            db.bookIssueDao().updateReaderIdInBookIssues(oldReaderId, newPhone);

                            requireActivity().runOnUiThread(() -> {
                                UiUtils.showToastS(requireContext(), "Reader Updated!");
                                displayReaderDetails(reader);

                                // Notify ReadersFragment about the update
                                getParentFragmentManager().setFragmentResult("reader_update", new Bundle());
                            });
                        }).start();
                    }
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteReader(Reader reader) {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());

            // Fetch latest checkout count
            int currentCheckouts = db.bookIssueDao().getCurrentCheckouts(reader.getReaderId());

            requireActivity().runOnUiThread(() -> {
                if (currentCheckouts > 0) {
                    // Prevent deletion
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Cannot Delete Reader")
                            .setMessage("This reader has unreturned books. Please return all books before deletion.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    // Proceed with delete confirmation
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Reader")
                            .setMessage("Are you sure you want to delete this reader?")
                            .setPositiveButton("Delete", (dialog, which) -> new Thread(() -> {
                                db.readerDao().delete(reader);
                                requireActivity().runOnUiThread(() -> {
                                    UiUtils.showToastS(requireContext(), "Reader Deleted!");
                                    getParentFragmentManager().setFragmentResult("reader_update", new Bundle());
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                });
                            }).start())
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayReaderDetails(reader);
    }
}