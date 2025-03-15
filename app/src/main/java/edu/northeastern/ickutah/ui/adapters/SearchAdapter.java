package edu.northeastern.ickutah.ui.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    private List<Book> books;
    private List<Reader> readers;
    private List<BookIssue> bookIssues;
    private final OnSearchItemClickListener clickListener;

    public interface OnSearchItemClickListener {
        void onSearchItemClicked(Object item);
    }

    public SearchAdapter(List<Book> books, List<Reader> readers, List<BookIssue> bookIssues, OnSearchItemClickListener clickListener) {
        this.books = books;
        this.readers = readers;
        this.bookIssues = bookIssues;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < books.size()) {
            return VIEW_TYPE_BOOK;
        } else if (position < books.size() + readers.size()) {
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
            Book book = books.get(position);
            ((BookViewHolder) holder).bind(book, clickListener);
        } else if (holder instanceof ReaderViewHolder) {
            Reader reader = readers.get(position - books.size());
            ((ReaderViewHolder) holder).bind(reader, clickListener);
        } else {
            BookIssue issue = bookIssues.get(position - books.size() - readers.size());
            ((BookIssueViewHolder) holder).bind(issue, clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return books.size() + readers.size() + bookIssues.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateResults(List<Book> newBooks, List<Reader> newReaders, List<BookIssue> newBookIssues) {
        this.books = newBooks;
        this.readers = newReaders;
        this.bookIssues = newBookIssues;
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
        private final TextView issueDueDate;

        public BookIssueViewHolder(@NonNull View itemView) {
            super(itemView);
            bookId = itemView.findViewById(R.id.book_id);
            readerId = itemView.findViewById(R.id.reader_id);
            issueDueDate = itemView.findViewById(R.id.issue_due_date);
        }

        public void bind(BookIssue issue, OnSearchItemClickListener listener) {
            bookId.setText(issue.getBookId());
            readerId.setText(issue.getReaderId());
            issueDueDate.setText(issue.getDueDate().toString());
            itemView.setOnClickListener(v -> listener.onSearchItemClicked(issue));
        }
    }
}