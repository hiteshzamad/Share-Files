package com.ztech.share;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.net.URLConnection;

public class AdapterSelectedItem extends RecyclerView.Adapter<AdapterSelectedItem.ViewHolder> {

    private ObjectSelected[] objectSelected;
    private OnClickListener onClickListener;
    private Context context;
    private PackageManager packageManager;

    public interface OnClickListener{
        void onClick(int position);
    }

    AdapterSelectedItem(ObjectSelected[] objectSelected, OnClickListener onClickListener,Context context) {
        this.objectSelected = objectSelected;
        this.onClickListener = onClickListener;
        this.context = context;
        packageManager = context.getPackageManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (objectSelected !=null && objectSelected[position] != null) {
            holder.imageView.setImageDrawable(objectSelected[position].getImage());
            holder.textView01.setText(objectSelected[position].getName());
            String path = objectSelected[position].getPath();
            if (isImageFile(path)) {
                Glide.with(context).load(path)
                        .error(context.getDrawable(R.drawable.ic_image))
                        .override(250, 250)
                        .centerCrop()
                        .into(holder.imageView);
            }
            else if (isVideoFile(path)){
                Glide.with(context).load(path)
                        .error(context.getDrawable(R.drawable.ic_video))
                        .override(250, 250)
                        .centerCrop()
                        .into(holder.imageView);
            }
            else if (isAudioFile(path)){
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_music));
            }
            else if (isTextFile(path)){
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_text));
            }
            else if (isPdfFile(path)){
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_pdf));
            }
            else if (isZipFile(path)){
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_zip));
            }
            else if (isApkFile(path)){
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, 0);
                if (packageInfo!=null) {
                    packageInfo.applicationInfo.sourceDir = path;
                    packageInfo.applicationInfo.publicSourceDir = path;
                    Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                    holder.imageView.setImageDrawable(icon);
                }
            }
        }
    }


    private static boolean isZipFile(String path) {
        try{
            String mimeType = URLConnection.guessContentTypeFromName(path);
            if(mimeType != null)
                return mimeType.startsWith("application/zip") ||  mimeType.startsWith("application/octet-stream") ||
                    mimeType.startsWith("application/x-zip-compressed") ||  mimeType.startsWith("multipart/x-zip");
        }catch (Exception ignored){
        }
        return false;
    }

    private static boolean isApkFile(String path) {
        try{
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("application/vnd.android.package-archive");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isAudioFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("audio");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isPdfFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            if(mimeType != null)
                return  mimeType.startsWith("application/pdf") ;
        }catch (Exception ignored){
        }
        return false;
    }

    private static boolean isImageFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("image");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isTextFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("text");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isVideoFile(String path) {
        try{
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("video");
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public int getItemCount() {
        if (objectSelected !=null)
            return objectSelected.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textView01;
        final ImageView imageView;
        final Button button;
        ViewHolder(@NonNull View view, final OnClickListener onClickListener) {
            super(view);
            textView01 = view.findViewById(R.id.item_selected_textView01);
            imageView = view.findViewById(R.id.item_selected_imageView01);
            button = view.findViewById(R.id.item_selected_button01);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
