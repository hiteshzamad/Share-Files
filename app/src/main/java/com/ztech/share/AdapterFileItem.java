package com.ztech.share;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.net.URLConnection;

public class AdapterFileItem extends RecyclerView.Adapter<AdapterFileItem.ViewHolder> {

    public interface OnClickListener{
        void onClick(int position);
        void onCheck(int position,boolean isChecked);
    }

    private OnClickListener onClickListener;
    private ObjectFile[] objectFiles;
    private Context context;
    private String currentPath;

    AdapterFileItem(OnClickListener onClickListener, ObjectFile[] objectFiles, Context context, String currentPath) {
        this.onClickListener = onClickListener;
        this.objectFiles = objectFiles;
        this.context = context;
        this.currentPath = currentPath;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_list, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder ,int position) {
        if(objectFiles !=null && objectFiles[position]!=null) {
            holder.imageView.setImageDrawable(objectFiles[position].getDrawable());
            String path = currentPath + objectFiles[position].getFilename();
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
            holder.checkBox.setChecked(objectFiles[position].isChecked());
            holder.checkBox.setEnabled(objectFiles[position].isCheckable());
            holder.textView02.setText(objectFiles[position].getSize());
            holder.textView01.setText(objectFiles[position].getFilename());
        }
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
        if (objectFiles !=null)
            return objectFiles.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView01,textView02;
        final ImageView imageView;
        final CheckBox checkBox;

        ViewHolder(@NonNull View view, final OnClickListener onClickListener) {
            super(view);
            textView01 = view.findViewById(R.id.item_file_textView01);
            textView02 = view.findViewById(R.id.item_file_textView02);
            imageView = view.findViewById(R.id.item_file_image_view01);
            checkBox = view.findViewById(R.id.item_file_checkbox01);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onClickListener.onCheck(getAdapterPosition(),isChecked);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
