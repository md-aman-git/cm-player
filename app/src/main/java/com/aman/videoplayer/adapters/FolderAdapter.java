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
import com.aman.videoplayer.modals.VideoFiles;
import com.aman.videoplayer.activities.VideosFolderActivity;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<String> videoFilesFolder;
    private ArrayList<VideoFiles> videoFiles;
    View view;
    public FolderAdapter(Context mContext, ArrayList<VideoFiles> videoFiles,
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, VideosFolderActivity.class);
                intent.putExtra("folderName", videoFilesFolder.get(position));
                mContext.startActivity(intent);
            }
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
        for (VideoFiles videoFiles : videoFiles)
        {
            if (videoFiles.getPath()
                    .substring(0, videoFiles.getPath().lastIndexOf("/"))
                    .endsWith(folder))
            {
                count++;
            }
        }
        return count;
    }
}
