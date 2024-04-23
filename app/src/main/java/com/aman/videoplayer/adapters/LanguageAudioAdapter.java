package com.aman.videoplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.GetSetLanguage;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.AudioLanguages;

import java.util.ArrayList;

public class LanguageAudioAdapter extends RecyclerView.Adapter<LanguageAudioAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<AudioLanguages> audios;
    GetSetLanguage getSetLanguage;

    public LanguageAudioAdapter(Context mContext, ArrayList<AudioLanguages> audios) {
        this.mContext = mContext;
        this.audios = audios;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.language_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        String language = "Default Language";
        if (audios.size() > 0)
        {
            switch (audios.get(position).getAudioLanguage()) {
                case "hi":
                    language = "Hindi";
                    break;
                case "en":
                    language = "English";
                    break;
                case "ru":
                    language = "Russian";
                    break;
                case "ta":
                    language = "Tamil";
                    break;
                case "zh":
                    language = "Chinese";
                    break;
                case "und":
                    language = "Not Defined";
                    break;
                case "```":
                    language = "Recorded";
                    break;
                default:
                    language = audios.get(position).getAudioLanguage();
                    break;
            }
            if (audios.get(position).isSelectedLanguage()) {
                holder.language_name.setText(language);
                holder.radioButton.setImageResource(R.drawable.ic_radio_button_checked);
            }
            else {
                holder.language_name.setText(language);
                holder.radioButton.setImageResource(R.drawable.ic_radio_button_unchecked);
            }
        }
        else
        {
            holder.language_name.setText(language);
            holder.radioButton.setImageResource(R.drawable.ic_radio_button_checked);
        }
        holder.itemView.setOnClickListener(view -> {
            getSetLanguage.myLanguageListener(position);
            getSetLanguage.changeAudio(position);
        });
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    public void setCallback(GetSetLanguage getSetLanguage) {
        this.getSetLanguage = getSetLanguage;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView language_name;
        ImageView radioButton;
        RelativeLayout language_item;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            language_name = itemView.findViewById(R.id.language_name);
            radioButton = itemView.findViewById(R.id.radio_button);
            language_item = itemView.findViewById(R.id.language_item);
        }
    }
}

