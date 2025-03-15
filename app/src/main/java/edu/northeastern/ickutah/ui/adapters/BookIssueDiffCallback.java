package edu.northeastern.ickutah.ui.adapters;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import edu.northeastern.ickutah.models.BookIssue;

public class BookIssueDiffCallback extends DiffUtil.Callback {
    private final List<BookIssue> oldList;
    private final List<BookIssue> newList;

    public BookIssueDiffCallback(List<BookIssue> oldList, List<BookIssue> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getIssueId().equals(newList.get(newItemPosition).getIssueId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
