package com.ztech.share;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class AdapterVideoItem  extends RecyclerView.Adapter<AdapterVideoItem.ViewHolder>  {
    private ObjectVideo[] objectVideo;
    private OnClickListener onClickListener;

    private Context context;
    public interface OnClickListener{
        void onCheck(int position, boolean isChecked);
    }

    public AdapterVideoItem(ObjectVideo[] objectVideo, OnClickListener onClickListener, Context context) {
        this.objectVideo = objectVideo;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video_list, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (objectVideo !=null && objectVideo[position] != null) {
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, objectVideo[position].getId());
            Glide.with(context).load(contentUri)
                    .override(250, 250)
                    .error(context.getDrawable(R.drawable.ic_video))
                    .centerCrop()
                    .into(holder.imageView);
            holder.checkBox.setChecked(objectVideo[position].isChecked());
        }
    }

    @Override
    public int getItemCount() {
        if (objectVideo !=null)
            return objectVideo.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView imageView;
        final CheckBox checkBox;

        ViewHolder(@NonNull View view, final OnClickListener onClickListener) {
            super(view);
            imageView = view.findViewById(R.id.item_video_imageView);
            checkBox = view.findViewById(R.id.item_video_checkBox);
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

