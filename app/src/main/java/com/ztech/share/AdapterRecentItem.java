package com.ztech.share;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URLConnection;

public class AdapterRecentItem extends RecyclerView.Adapter<AdapterRecentItem.ViewHolder> {

    private ObjectRecent[] objectRecent;
    private Context context;
    private OnClickListener onClickListener;

    public AdapterRecentItem(ObjectRecent[] objectRecent, Context context, OnClickListener onClickListener) {
        this.objectRecent = objectRecent;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    interface OnClickListener{
        void onClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_list, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(objectRecent ==null || objectRecent[position]==null)
            return;
        holder.textView.setText(objectRecent[position].getName());
        String path = objectRecent[position].getLocation();
        if (isImageFile(path)) {
            Glide.with(context).load(path)
                    .error(context.getDrawable(R.drawable.ic_image))
                    .override(250, 250)
                    .centerCrop()
                    .into(holder.imageView);
        }
        else if (isVideoFile(path)) {
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
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, 0);
            if (packageInfo!=null) {
                packageInfo.applicationInfo.sourceDir = path;
                packageInfo.applicationInfo.publicSourceDir = path;
                Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                holder.imageView.setImageDrawable(icon);
            }
        }
        else if (new File(path).isDirectory()){
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_folder_24dp ));
        }
        else
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_file));
    }

    private static boolean isZipFile(String path) {
        try{
            String mimeType = URLConnection.guessContentTypeFromName(path);
            if(mimeType != null) {
                return mimeType.startsWith("application/zip") || mimeType.startsWith("application/octet-stream") ||
                        mimeType.startsWith("application/x-zip-compressed") || mimeType.startsWith("multipart/x-zip");
            }
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
        if (objectRecent!=null)
            return objectRecent.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        ViewHolder(View view, final OnClickListener onClickListener) {
            super(view);
            textView = view.findViewById(R.id.item_recent_textView01);
            imageView = view.findViewById(R.id.item_recent_imageView01);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
