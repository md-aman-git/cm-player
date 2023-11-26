package com.aman.videoplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.R;
import com.aman.videoplayer.activities.SenderActivity;
import com.aman.videoplayer.modals.ShareFiles;
import com.google.gson.Gson;
import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<ShareFiles> mFiles;
    private String PATH_OF_SELECTED_FILE_PERF = "PATH_PREF_NAME";
    private String PATH_PREF_NAME = "HereFileStored";

    public AudioAdapter(Context mContext, ArrayList<ShareFiles> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.file_name.setText(mFiles.get(position).getFileName());
        int size = Integer.parseInt(mFiles.get(position).getSize()) / 1024;
        if (size >= 1024)
            holder.audio_size.setText( size / 1024 + "MB");
        else
            holder.audio_size.setText(size + "KB");
        holder.audio_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SenderActivity.class);
                ArrayList<ShareFiles> myList = new ArrayList<>();
                myList.add(mFiles.get(position));
                SharedPreferences.Editor pathEditor =
                        mContext.getSharedPreferences(PATH_OF_SELECTED_FILE_PERF,
                                Context.MODE_PRIVATE).edit();
                Gson gson = new Gson();
                ArrayList<ShareFiles> textList = new ArrayList<>(myList);
                String jsonText = gson.toJson(textList);
                pathEditor.putString(PATH_PREF_NAME, jsonText);
                pathEditor.apply();
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name, audio_size;
        ImageView img_icon;
        RelativeLayout audio_item;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.audio_file_name);
            img_icon = itemView.findViewById(R.id.img_audio);
            audio_item = itemView.findViewById(R.id.audio_item);
            audio_size = itemView.findViewById(R.id.audio_size);
        }
    }

    void updateList(ArrayList<ShareFiles> videoFiles)
    {
        mFiles = new ArrayList<>();
        mFiles.addAll(videoFiles);
        notifyDataSetChanged();
    }
}
