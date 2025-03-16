package edu.northeastern.ickutah.ui.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.models.BookIssue;
import edu.northeastern.ickutah.models.Reader;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_BOOK = 0;
    private static final int VIEW_TYPE_READER = 1;
    private static final int VIEW_TYPE_BOOK_ISSUE = 2;

    private List<Book> bookList;
    private List<Reader> readerList;
    private List<BookIssue> bookIssueList;
    private final OnSearchItemClickListener searchItemClickListener;

    public interface OnSearchItemClickListener {
        void onSearchItemClicked(Object item);
    }

    public SearchAdapter(List<Book> books, List<Reader> readers, List<BookIssue> bookIssues, OnSearchItemClickListener listener) {
        this.bookList = books;
        this.readerList = readers;
        this.bookIssueList = bookIssues;
        this.searchItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < bookList.size()) {
            return VIEW_TYPE_BOOK;
        } else if (position < bookList.size() + readerList.size()) {
            return VIEW_TYPE_READER;
        } else {
            return VIEW_TYPE_BOOK_ISSUE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_BOOK) {
            view = inflater.inflate(R.layout.item_book, parent, false);
            return new BookViewHolder(view);
        } else if (viewType == VIEW_TYPE_READER) {
            view = inflater.inflate(R.layout.item_reader, parent, false);
            return new ReaderViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_book_issue, parent, false);
            return new BookIssueViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BookViewHolder) {
            Book book = bookList.get(position);
            ((BookViewHolder) holder).bind(book, searchItemClickListener);
        } else if (holder instanceof ReaderViewHolder) {
            Reader reader = readerList.get(position - bookList.size());
            ((ReaderViewHolder) holder).bind(reader, searchItemClickListener);
        } else {
            BookIssue issue = bookIssueList.get(position - bookList.size() - readerList.size());
            ((BookIssueViewHolder) holder).bind(issue, searchItemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size() + readerList.size() + bookIssueList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateResults(List<Book> newBooks, List<Reader> newReaders, List<BookIssue> newBookIssues) {
        this.bookList = newBooks;
        this.readerList = newReaders;
        this.bookIssueList = newBookIssues;
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookTitle;
        private final TextView bookAuthor;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
        }

        public void bind(Book book, OnSearchItemClickListener listener) {
            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());
            itemView.setOnClickListener(v -> listener.onSearchItemClicked(book));
        }
    }

    static class ReaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView readerName;
        private final TextView readerPhone;

        public ReaderViewHolder(@NonNull View itemView) {
            super(itemView);
            readerName = itemView.findViewById(R.id.reader_name);
            readerPhone = itemView.findViewById(R.id.reader_phone);
        }

        public void bind(Reader reader, OnSearchItemClickListener listener) {
            readerName.setText(reader.getName());
            readerPhone.setText(reader.getPhone());
            itemView.setOnClickListener(v -> listener.onSearchItemClicked(reader));
        }
    }

    static class BookIssueViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookId;
        private final TextView readerId;
        private final TextView dueDate;
        private final TextView issueStatus;

        public BookIssueViewHolder(@NonNull View itemView) {
            super(itemView);
            bookId = itemView.findViewById(R.id.book_id);
            readerId = itemView.findViewById(R.id.reader_id);
            dueDate = itemView.findViewById(R.id.due_date);
            issueStatus = itemView.findViewById(R.id.issue_status);
        }

        public void bind(BookIssue issue, OnSearchItemClickListener listener) {
            bookId.setText(String.format("Book ID: %s", issue.getBookId()));
            readerId.setText(String.format("Reader ID: %s", issue.getReaderId()));
            dueDate.setText(String.format("Due: %s", issue.getDueDate()));

            issueStatus.setText(issue.isReturned() ? "Returned" : "Not Returned");
            issueStatus.setTextColor(issue.isReturned() ? ContextCompat.getColor(itemView.getContext(), R.color.successColor) : ContextCompat.getColor(itemView.getContext(), R.color.errorColor));

            itemView.setOnClickListener(v -> listener.onSearchItemClicked(issue));
        }
    }
}