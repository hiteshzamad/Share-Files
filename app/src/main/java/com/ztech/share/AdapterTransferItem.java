package com.ztech.share;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterTransferItem extends RecyclerView.Adapter<AdapterTransferItem.ViewHolder> {

    private ObjectTransfer[] objectTransfers;
    private OnClickListener onClickListener;
    private boolean isReceiver;
    private boolean isCompleted;

    public interface OnClickListener{
        void onClick(int position);
    }

    AdapterTransferItem(ObjectTransfer[] objectTransfers, OnClickListener onClickListener,boolean isReceiver, boolean isCompleted) {
        this.objectTransfers = objectTransfers;
        this.onClickListener = onClickListener;
        this.isReceiver = isReceiver;
        this.isCompleted = isCompleted;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transfer, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (objectTransfers != null && objectTransfers[position] != null) {
            holder.textView01.setText(objectTransfers[position].getFileName());
            holder.textView02.setText(objectTransfers[position].getStatus());
            holder.progressBar.setProgress(objectTransfers[position].getPercent());
            String name = objectTransfers[position].getFileName();
            int l = name.length();
            if (l > 4 && name.substring(l-4).compareToIgnoreCase(".apk")==0 &&
                    objectTransfers[position].getPercent() == 100 && isReceiver &&
                    isCompleted&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                holder.button.setVisibility(View.VISIBLE);
        }
    }

    public void setCompleted() {
        isCompleted = true;
    }

    @Override
    public int getItemCount() {
        if (objectTransfers !=null)
            return objectTransfers.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        final TextView textView01;
        final TextView textView02;
        final ProgressBar progressBar;
        final Button button;
        ViewHolder(@NonNull View view, final OnClickListener onClickListener) {
            super(view);
            textView01 = view.findViewById(R.id.item_transfer_textView01);
            textView02 = view.findViewById(R.id.item_transfer_textView02);
            progressBar = view.findViewById(R.id.item_transfer_progressBar01);
            button = view.findViewById(R.id.item_transfer_button01);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
