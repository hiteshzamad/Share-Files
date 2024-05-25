package com.ztech.share;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterDeviceItem extends RecyclerView.Adapter<AdapterDeviceItem.ViewHolder> {

    private OnClickListener onClickListener;
    private ObjectDevice[] objectDevices;

    public interface OnClickListener{
        void onClick(int position);
    }

    AdapterDeviceItem(ObjectDevice[] objectDevices, OnClickListener onClickListener) {
        this.objectDevices = objectDevices;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (objectDevices !=null && objectDevices[position] != null){
            holder.textView.setText(objectDevices[position].getDeviceName());
            holder.textView02.setText(objectDevices[position].getDeviceName().substring(0,1).toUpperCase());
            holder.imageView.setColorFilter(Color.parseColor(objectDevices[position].getColorString()), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public int getItemCount() {
        if (objectDevices !=null)
            return objectDevices.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        final TextView textView,textView02;
        final ImageView imageView;

        ViewHolder(View view, final OnClickListener onClickListener) {
            super(view);
            textView = view.findViewById(R.id.device_textView01);
            textView02 = view.findViewById(R.id.device_textView02);
            imageView = view.findViewById(R.id.device_imageView);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
