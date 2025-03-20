package edu.northeastern.ickutah.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.database.LibraryDatabase;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.models.BookIssue;
import edu.northeastern.ickutah.models.Reader;
import edu.northeastern.ickutah.utils.StringUtils;
import edu.northeastern.ickutah.utils.UiUtils;

public class AdvancedFragment extends Fragment {

    private Context context;
    private ActivityResultLauncher<Intent> filePickerLauncherBooks, filePickerLauncherReaders;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filePickerLauncherBooks = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            importBooks(uri);
                        }
                    }
                }
        );

        filePickerLauncherReaders = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            importReaders(fileUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advanced, container, false);
        context = requireContext();

        // Button References
        MaterialButton btnImportBooks = view.findViewById(R.id.btn_import_books);
        MaterialButton btnExportBooks = view.findViewById(R.id.btn_export_books);
        MaterialButton btnImportReaders = view.findViewById(R.id.btn_import_readers);
        MaterialButton btnExportReaders = view.findViewById(R.id.btn_export_readers);
        MaterialButton btnExportIssues = view.findViewById(R.id.btn_export_issues);
        MaterialButton btnClearPastReturns = view.findViewById(R.id.btn_clear_past_returns);

        // Click Listeners
        btnImportBooks.setOnClickListener(v -> openFilePickerForBooks());
        btnExportBooks.setOnClickListener(v -> exportBooks());
        btnImportReaders.setOnClickListener(v -> openFilePickerForReaders());
        btnExportReaders.setOnClickListener(v -> exportReaders());
        btnExportIssues.setOnClickListener(v -> exportBookIssues());
        btnClearPastReturns.setOnClickListener(v -> clearPastReturns());

        return view;
    }

    /**
     * Import Books from CSV
     */

    private void openFilePickerForBooks() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncherBooks.launch(intent);
    }

    private void importBooks(Uri uri) {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());
            List<Book> booksToInsert = new ArrayList<>();
            int skippedCount = 0;
            int addedCount = 0;

            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                boolean isFirstLine = true;
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                while ((line = reader.readLine()) != null) {
                    // Skip header line
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] columns = line.split(",");
                    if (columns.length < 5) continue; // Skip invalid rows

                    String title = columns[0].trim();
                    String author = columns[1].trim();
                    String publisher = columns[2].trim();
                    String yearStr = columns[3].trim();
                    String copiesStr = columns[4].trim();

                    // **Validation Checks**
                    if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || yearStr.isEmpty() || copiesStr.isEmpty()) {
                        skippedCount++;
                        continue; // Skip rows with missing data
                    }

                    // Validate Year
                    int yearInt;
                    try {
                        yearInt = Integer.parseInt(yearStr);
                        if (yearInt > currentYear) {
                            skippedCount++;
                            continue; // Skip future years
                        }
                    } catch (NumberFormatException e) {
                        skippedCount++;
                        continue; // Skip invalid year format
                    }

                    // Validate Copies
                    int totalCopies;
                    try {
                        totalCopies = Integer.parseInt(copiesStr);
                        if (totalCopies <= 0) {
                            skippedCount++;
                            continue; // Skip invalid copies
                        }
                    } catch (NumberFormatException e) {
                        skippedCount++;
                        continue; // Skip invalid number format
                    }

                    // Convert to Title Case
                    String formattedTitle = StringUtils.toTitleCase(title);
                    String formattedAuthor = StringUtils.toTitleCase(author);
                    String formattedPublisher = StringUtils.toTitleCase(publisher);

                    // Check if book exists
                    Book existingBook = db.bookDao().getBookByTitleAndAuthor(title, author);
                    if (existingBook == null) {
                        booksToInsert.add(new Book(
                                "BK" + System.currentTimeMillis(),
                                formattedTitle,
                                formattedAuthor,
                                formattedPublisher,
                                yearStr,
                                totalCopies,
                                totalCopies
                        ));
                        addedCount++;
                    } else {
                        skippedCount++;
                    }
                }

                // Insert new books
                db.bookDao().insertAll(booksToInsert);

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        UiUtils.showToastS(requireContext(), "Error importing books!")
                );
                Log.e("ImportBooks", "Error importing books: " + e.getMessage());
                return;
            }

            // Show success message
            int finalAddedCount = addedCount;
            int finalSkippedCount = skippedCount;
            requireActivity().runOnUiThread(() ->
                    UiUtils.showToastL(requireContext(), "Imported: " + finalAddedCount + ", Skipped: " + finalSkippedCount)
            );

        }).start();
    }

    /**
     * Export Books to CSV
     */
    @SuppressLint("DefaultLocale")
    private void exportBooks() {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(context);
            List<Book> books = db.bookDao().getAllBooks();

            if (books.isEmpty()) {
                requireActivity().runOnUiThread(() -> UiUtils.showToastS(context, "No books to export!"));
                return;
            }

            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "books.csv");
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Book ID,Title,Author,Publisher,Year,Total Copies,Available Copies\n");
                for (Book book : books) {
                    writer.append(String.format("%s,%s,%s,%s,%s,%d,%d\n",
                            book.getBookId(), book.getTitle(), book.getAuthor(),
                            book.getPublisher(), book.getYear(),
                            book.getTotalCopies(), book.getAvailableCopies()));
                }
                requireActivity().runOnUiThread(() ->
                        UiUtils.showToastL(context, "Books exported to " + file.getAbsolutePath()));
            } catch (IOException e) {
                Log.e("ExportBooks", "Error exporting books: " + e.getMessage());
                requireActivity().runOnUiThread(() -> UiUtils.showToastS(context, "Export failed!"));
            }
        }).start();
    }

    /**
     * Import Readers from CSV
     */
    private void openFilePickerForReaders() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncherReaders.launch(intent);
    }

    private void importReaders(Uri uri) {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(requireContext());
            List<Reader> readersToInsert = new ArrayList<>();
            int skippedCount = 0;
            int addedCount = 0;

            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    // Skip header line
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] columns = line.split(",");
                    if (columns.length < 3) {
                        skippedCount++; // Skip invalid rows
                        continue;
                    }

                    String name = columns[0].trim();
                    String email = columns[1].trim();
                    String phone = columns[2].trim();

                    // **Validation Checks**
                    if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                        skippedCount++;
                        continue; // Skip rows with missing data
                    }

                    // Validate Email
                    if (StringUtils.isInvalidEmail(email)) {
                        skippedCount++;
                        continue; // Skip invalid email format
                    }

                    // Validate Phone Number (Must be 10 digits)
                    if (StringUtils.isInvalidPhone(phone)) {
                        skippedCount++;
                        continue; // Skip invalid phone format
                    }

                    // **Convert to Proper Case**
                    String formattedName = StringUtils.toTitleCase(name);
                    String formattedEmail = StringUtils.toLowerCase(email);

                    // **Check if Reader Already Exists**
                    Reader existingReader = db.readerDao().getReaderByPhone(phone);
                    if (existingReader == null) {
                        readersToInsert.add(new Reader(phone, formattedName, formattedEmail, phone, 0));
                        addedCount++;
                    } else {
                        skippedCount++;
                    }
                }

                // Insert new readers
                db.readerDao().insertAll(readersToInsert);

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        UiUtils.showToastS(requireContext(), "Error importing readers!")
                );
                Log.e("ImportReaders", "Error importing readers: " + e.getMessage());
                return;
            }

            // Show success message
            int finalAddedCount = addedCount;
            int finalSkippedCount = skippedCount;
            requireActivity().runOnUiThread(() ->
                    UiUtils.showToastL(requireContext(), "Imported: " + finalAddedCount + ", Skipped: " + finalSkippedCount)
            );

        }).start();
    }

    /**
     * Export Readers to CSV
     */
    @SuppressLint("DefaultLocale")
    private void exportReaders() {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(context);
            List<Reader> readers = db.readerDao().getAllReaders();

            if (readers.isEmpty()) {
                requireActivity().runOnUiThread(() -> UiUtils.showToastS(context, "No readers to export!"));
                return;
            }

            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "readers.csv");
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Reader ID,Name,Email,Phone,Current Checkouts\n");
                for (Reader reader : readers) {
                    writer.append(String.format("%s,%s,%s,%s,%d\n",
                            reader.getReaderId(), reader.getName(), reader.getEmail(),
                            reader.getPhone(), reader.getCurrentCheckouts()));
                }
                requireActivity().runOnUiThread(() ->
                        UiUtils.showToastL(context, "Readers exported to " + file.getAbsolutePath()));
            } catch (IOException e) {
                Log.e("ExportReaders", "Error exporting readers: " + e.getMessage());
                requireActivity().runOnUiThread(() -> UiUtils.showToastS(context, "Export failed!"));
            }
        }).start();
    }

    /**
     * Export Book Issues to CSV
     */
    private void exportBookIssues() {
        new Thread(() -> {
            LibraryDatabase db = LibraryDatabase.getInstance(context);
            List<BookIssue> issues = db.bookIssueDao().getAllBookIssues();

            if (issues.isEmpty()) {
                requireActivity().runOnUiThread(() -> UiUtils.showToastS(context, "No book issues to export!"));
                return;
            }

            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "book_issues.csv");
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Issue ID,Book ID,Reader ID,Issue Date,Due Date,Returned\n");
                for (BookIssue issue : issues) {
                    writer.append(String.format("%s,%s,%s,%s,%s,%s\n",
                            issue.getIssueId(), issue.getBookId(), issue.getReaderId(),
                            issue.getIssueDate().toString(), issue.getDueDate().toString(),
                            issue.isReturned() ? "Yes" : "No"));
                }
                requireActivity().runOnUiThread(() ->
                        UiUtils.showToastL(context, "Book issues exported to " + file.getAbsolutePath()));
            } catch (IOException e) {
                Log.e("ExportBookIssues", "Error exporting book issues: " + e.getMessage());
                requireActivity().runOnUiThread(() -> UiUtils.showToastS(context, "Export failed!"));
            }
        }).start();
    }

    /**
     * Clear Past Returns (Deletes all records where books are returned)
     */
    private void clearPastReturns() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("This action cannot be undone. Are you sure you want to clear all past returns?")
                .setPositiveButton("Yes, Clear", (dialog, which) -> {
                    // Proceed with clearing past returns
                    new Thread(() -> {
                        LibraryDatabase db = LibraryDatabase.getInstance(context);
                        int deletedRows = db.bookIssueDao().deleteReturnedIssues();

                        requireActivity().runOnUiThread(() -> {
                            if (deletedRows > 0) {
                                UiUtils.showToastS(context, "Cleared " + deletedRows + " past returns!");
                            } else {
                                UiUtils.showToastS(context, "No past returns to clear.");
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.warning_24px) // Warning icon
                .show();
    }

}
