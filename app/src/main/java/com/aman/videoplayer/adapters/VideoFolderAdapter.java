package com.aman.videoplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.activities.PlayerActivity;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.VideoFiles;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.MyViewHolder> {

    private Context mContext;
    public static ArrayList<VideoFiles> mFilesOfFolder;

    public VideoFolderAdapter(Context mContext, ArrayList<VideoFiles> mFilesOfFolder) {
        this.mContext = mContext;
        this.mFilesOfFolder = mFilesOfFolder;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_item,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.file_name.setText(mFilesOfFolder.get(position).getFilename());
        String duration = mFilesOfFolder.get(position).getDuration();
        if (duration != null)
            holder.video_duration.setText(formattedTime(Integer
                .parseInt(duration) / 1000));
        else
            holder.video_duration.setText(formattedTime(0));
        Glide.with(mContext)
                .asBitmap()
                .load(Uri.fromFile(new File(mFilesOfFolder.get(position).getPath())))
                .into(holder.img_icon);

        holder.music_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                //  now sending position of item to retrieve the path of that file to open in the new activity..
                intent.putExtra("position", position);
                intent.putExtra("sender", "fromFolderFrag");
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilesOfFolder.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name, video_duration;
        ImageView img_icon, menuMore;
        RelativeLayout music_item;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            img_icon = itemView.findViewById(R.id.img_music);
            music_item = itemView.findViewById(R.id.music_item);
            menuMore = itemView.findViewById(R.id.menuMore);
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
        } else {
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

