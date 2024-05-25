package com.ztech.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterAudioItem extends RecyclerView.Adapter<AdapterAudioItem.ViewHolder>  {
    private ObjectAudio[] objectAudio;
    private OnClickListener onClickListener;

    private Context context;
    public interface OnClickListener{
        void onCheck(int position, boolean isChecked);
    }

    AdapterAudioItem(ObjectAudio[] objectAudio, OnClickListener onClickListener, Context context) {
        this.objectAudio = objectAudio;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio_list, parent, false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (objectAudio !=null && objectAudio[position] != null) {
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_music));
            holder.checkBox.setChecked(objectAudio[position].isChecked());
            holder.textView.setText(getNameFromPath(objectAudio[position].getLocation()));
        }
    }

    @Override
    public int getItemCount() {
        if (objectAudio !=null)
            return objectAudio.length;
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView imageView;
        final CheckBox checkBox;
        final TextView textView;

        ViewHolder(@NonNull View view, final OnClickListener onClickListener) {
            super(view);
            imageView = view.findViewById(R.id.item_audio_imageView);
            checkBox = view.findViewById(R.id.item_audio_checkBox);
            textView = view.findViewById(R.id.item_audio_textView);
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

    private String getNameFromPath(String path){
        String[] temp = path.split("/");
        if (temp.length>1)
            return temp[temp.length-1];
        return "";
    }
}
