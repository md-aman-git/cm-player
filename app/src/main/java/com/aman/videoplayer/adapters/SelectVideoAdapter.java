package com.aman.videoplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

public class SelectVideoAdapter extends RecyclerView.Adapter<SelectVideoAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<ShareFiles> mSenderFiles;
    private String PATH_OF_SELECTED_FILE_PERF = "PATH_PREF_NAME";
    private String PATH_PREF_NAME = "HereFileStored";

    public SelectVideoAdapter(Context mContext, ArrayList<ShareFiles> mSenderFiles) {
        this.mContext = mContext;
        this.mSenderFiles = mSenderFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.file_name.setText(mSenderFiles.get(position).getFileName());
        int durationFile = mSenderFiles.get(position).getDuration();
        if (durationFile != 0) {
            holder.video_duration.setText(
                    formattedTime(durationFile / 1000));
        }
        else
        {
            holder.video_duration.setText("0:0");
        }
        Glide.with(mContext)
                .load(Uri.fromFile(new File(mSenderFiles.get(position).getPath())))
                .placeholder(R.drawable.black_transparent)
                .into(holder.img_icon);

        holder.music_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SenderActivity.class);
                ArrayList<ShareFiles> myList = new ArrayList<>();
                myList.add(mSenderFiles.get(position));
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
        return mSenderFiles.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name, video_duration;
        ImageView img_icon, selected;
        RelativeLayout music_item;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            img_icon = itemView.findViewById(R.id.img_music);
            music_item = itemView.findViewById(R.id.music_item);
            selected = itemView.findViewById(R.id.menuMore);
            video_duration = itemView.findViewById(R.id.video_duration);
        }
    }
    private String formattedTime(int mCurrentPosition) {

        String totalout;
        String totaloutNew;
        String seconds = String.valueOf((mCurrentPosition % 60));
        String minutes = String.valueOf(mCurrentPosition / 60);
        String hours = String.valueOf(mCurrentPosition / 3600);
        if (hours.equals("0")) {
            totalout = minutes + ":" + seconds;
            totaloutNew = minutes + ":" + "0" + seconds;
        }
        else
        {
            minutes = String.valueOf((mCurrentPosition - (3600 * Integer.parseInt(hours))) / 60);
            totalout = hours + ":" + minutes + ":" + seconds;
            totaloutNew = hours + ":" + minutes + ":" + "0" + seconds;
        }
        if (seconds.length() == 1) {
            return totaloutNew;
        } else {
            return totalout;
        }
    }
}
