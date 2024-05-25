package com.ztech.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Stack;

public class AdapterFilePointer extends RecyclerView.Adapter<AdapterFilePointer.ViewHolder> {
    private Stack<String> stringStack;
    private OnClickListener onClickListener;

    public interface OnClickListener{
        void onClickDestination(int position);
    }

    AdapterFilePointer(Stack<String> stringStack, OnClickListener onClickListener) {
        this.stringStack = stringStack;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterFilePointer.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_destination, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (stringStack!=null && stringStack.elementAt(position)!=null)
            holder.textView.setText(stringStack.elementAt(position));
    }

    @Override
    public int getItemCount() {
        if (stringStack!=null)
            return stringStack.size();
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textView;
        ViewHolder(@NonNull View view, final OnClickListener onClickListener) {
            super(view);
            textView = view.findViewById(R.id.item_file_destination_textView01);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClickDestination(getAdapterPosition());
                }
            });
        }
    }
}
