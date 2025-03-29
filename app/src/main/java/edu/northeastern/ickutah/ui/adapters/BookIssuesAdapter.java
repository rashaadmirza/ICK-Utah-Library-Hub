package edu.northeastern.ickutah.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.models.BookIssue;
import edu.northeastern.ickutah.utils.UiUtils;

public class BookIssuesAdapter extends RecyclerView.Adapter<BookIssuesAdapter.ViewHolder> {
    private List<BookIssue> bookIssueList;
    private final OnIssueClickListener issueClickListener;

    public interface OnIssueClickListener {
        void onIssueClick(BookIssue bookIssue);
    }

    public BookIssuesAdapter(Context ignoredContext, List<BookIssue> bookIssueList, OnIssueClickListener listener) {
        this.bookIssueList = bookIssueList;
        this.issueClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_issue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookIssue issue = bookIssueList.get(position);
//        holder.issueId.setText(String.format("Issue ID: %s", issue.getIssueId()));
        holder.bookId.setText(String.format("Book ID: %s", issue.getBookId()));
        holder.readerId.setText(String.format("Reader ID: %s", issue.getReaderId()));
        holder.dueDate.setText(String.format("Due: %s", issue.getDueDate()));

        if (issue.isReturned()) {
            holder.status.setText(R.string.returned);
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.successColor));
        } else {
            long currentTime = System.currentTimeMillis();
            if (issue.getDueDate().getTime() < currentTime) {
                // Book is overdue
                holder.status.setText(R.string.overdue);
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.errorColor));
            } else {
                holder.status.setText(R.string.not_returned);
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warningColor));
            }
        }

        // Adjust bottom margin dynamically
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (position == bookIssueList.size() - 1) {
            // Last item: Set bottom margin to 0dp
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, 0);
        } else {
            // Other items: Set bottom margin to 8dp
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, UiUtils.dpToPx(8, holder.itemView.getContext()));
        }

        holder.itemView.setOnClickListener(v -> issueClickListener.onIssueClick(issue));
    }

    @Override
    public int getItemCount() {
        return bookIssueList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<BookIssue> newIssueList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BookIssueDiffCallback(this.bookIssueList, newIssueList));
        this.bookIssueList = newIssueList;
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookId, readerId, dueDate, status;

        public ViewHolder(View itemView) {
            super(itemView);
//            issueId = itemView.findViewById(R.id.issue_id);
            bookId = itemView.findViewById(R.id.book_id);
            readerId = itemView.findViewById(R.id.reader_id);
            dueDate = itemView.findViewById(R.id.due_date);
            status = itemView.findViewById(R.id.issue_status);
        }
    }
}