package com.aman.videoplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.VideoFile;
import com.aman.videoplayer.activities.VideosFolderActivity;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<String> videoFilesFolder;
    private ArrayList<VideoFile> videoFiles;
    View view;
    public FolderAdapter(Context mContext, ArrayList<VideoFile> videoFiles,
                         ArrayList<String> videoFilesFolder) {
        this.mContext = mContext;
        this.videoFiles = videoFiles;
        this.videoFilesFolder = videoFilesFolder;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.folder_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.file_name.setText(videoFilesFolder.get(position)
                .substring(videoFilesFolder
                        .get(position).lastIndexOf("/") + 1));
        holder.total_files.setText(String.valueOf(NumberOfFiles(videoFilesFolder.get(position))));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, VideosFolderActivity.class);
            intent.putExtra("folderName", videoFilesFolder.get(position));
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoFilesFolder.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name, total_files;
        ImageView img_icon;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            img_icon = itemView.findViewById(R.id.img_music);
            total_files = itemView.findViewById(R.id.total_files);
        }
    }
    int NumberOfFiles(String folder)
    {
        int count = 0;
        for (VideoFile videoFile : videoFiles)
        {
            if (videoFile.getPath()
                    .substring(0, videoFile.getPath().lastIndexOf("/"))
                    .endsWith(folder))
            {
                count++;
            }
        }
        return count;
    }
}
