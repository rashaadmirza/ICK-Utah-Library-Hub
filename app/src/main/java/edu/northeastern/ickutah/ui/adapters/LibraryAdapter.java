package edu.northeastern.ickutah.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.utils.UiUtils;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
    private final List<Book> bookList;
    private final OnBookClickListener bookClickListener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public LibraryAdapter(Context ignoredContext, List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.bookClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());

        // Adjust bottom margin dynamically
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (position == bookList.size() - 1) {
            // Last item: Set bottom margin to 0dp
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, 0);
        } else {
            // Other items: Set bottom margin to 8dp
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, UiUtils.dpToPx(8, holder.itemView.getContext()));
        }

        holder.itemView.setOnClickListener(v -> bookClickListener.onBookClick(book));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Book> newBookList) {
        bookList.clear();
        bookList.addAll(newBookList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, author;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
        }
    }
}