package com.aman.videoplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.activities.PlayerActivity;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.VideoFiles;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private Context mContext;
    public static ArrayList<VideoFiles> mFiles;

    public VideoAdapter(Context mContext, ArrayList<VideoFiles> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.file_name.setText(mFiles.get(position).getFilename());
        String durationFile = mFiles.get(position).getDuration();
        if (durationFile != null) {
            holder.video_duration.setText(
                    formattedTime(Integer.parseInt(durationFile) / 1000));
        }
        else
        {
            holder.video_duration.setText("0:0");
        }
        Glide.with(mContext)
                    .load(Uri.fromFile(new File(mFiles.get(position).getPath())))
                    .placeholder(R.drawable.black_transparent)
                    .into(holder.img_icon);
        holder.music_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("sender", "fromFileFrag");
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
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

    public void updateList(ArrayList<VideoFiles> videoFiles)
    {
        mFiles = new ArrayList<>();
        mFiles.addAll(videoFiles);
        notifyDataSetChanged();
    }
    private String formattedTime(int mCurrentPosition) {

        String totalout = "";
        String totaloutNew = "";
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
