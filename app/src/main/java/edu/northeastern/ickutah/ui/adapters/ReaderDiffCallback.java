package edu.northeastern.ickutah.ui.adapters;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;
import edu.northeastern.ickutah.models.Reader;

public class ReaderDiffCallback extends DiffUtil.Callback {
    private final List<Reader> oldList;
    private final List<Reader> newList;

    public ReaderDiffCallback(List<Reader> oldList, List<Reader> newList) {
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
        return oldList.get(oldItemPosition).getReaderId().equals(newList.get(newItemPosition).getReaderId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}