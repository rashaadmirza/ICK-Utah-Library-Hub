package edu.northeastern.ickutah.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.northeastern.ickutah.R;
import edu.northeastern.ickutah.models.Reader;
import edu.northeastern.ickutah.utils.UiUtils;

public class ReadersAdapter extends RecyclerView.Adapter<ReadersAdapter.ViewHolder> {
    private List<Reader> readerList;
    private final OnReaderClickListener readerClickListener;

    public interface OnReaderClickListener {
        void onReaderClick(Reader reader);
    }

    public ReadersAdapter(Context ignoredContext, List<Reader> readerList, OnReaderClickListener listener) {
        this.readerList = readerList;
        this.readerClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reader, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reader reader = readerList.get(position);
        holder.readerName.setText(reader.getName());
        holder.readerPhone.setText(reader.getPhone());

        // Adjust bottom margin dynamically
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (position == readerList.size() - 1) {
            // Last item: Set bottom margin to 0dp
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, 0);
        } else {
            // Other items: Set bottom margin to 8dp
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, UiUtils.dpToPx(8, holder.itemView.getContext()));
        }

        holder.itemView.setOnClickListener(v -> readerClickListener.onReaderClick(reader));
    }

    @Override
    public int getItemCount() {
        return readerList.size();
    }

    public void updateList(List<Reader> newReaderList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ReaderDiffCallback(this.readerList, newReaderList));
        this.readerList = newReaderList;
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView readerName, readerPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            readerName = itemView.findViewById(R.id.reader_name);
            readerPhone = itemView.findViewById(R.id.reader_phone);
        }
    }
}