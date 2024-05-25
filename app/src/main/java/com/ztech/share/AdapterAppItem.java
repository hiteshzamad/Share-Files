package com.ztech.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterAppItem extends RecyclerView.Adapter<AdapterAppItem.ViewHolder> {

    private ObjectApp[] objectApps;
    private OnClickListener onClickListener;

    public interface OnClickListener{
        void onCheck(int position, boolean isChecked);
    }

    AdapterAppItem(ObjectApp[] objectApps, OnClickListener onClickListener) {
        this.objectApps = objectApps;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_list, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (objectApps !=null && objectApps[position] != null) {
            holder.imageView.setImageDrawable(objectApps[position].getIcon());
            holder.textView.setText(objectApps[position].getName());
            holder.checkBox.setChecked(objectApps[position].isChecked());
        }
    }

    @Override
    public int getItemCount() {
        if (objectApps !=null)
            return objectApps.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textView;
        final ImageView imageView;
        final CheckBox checkBox;
        ViewHolder(@NonNull View view, final OnClickListener onClickListener) {
            super(view);
            textView = view.findViewById(R.id.item_app_textView);
            imageView = view.findViewById(R.id.item_app_imageView);
            checkBox = view.findViewById(R.id.item_app_checkbox);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.toggle();
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onClickListener.onCheck(getAdapterPosition(),isChecked);
                }
            });
        }
    }
}
