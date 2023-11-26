package com.aman.videoplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.utils.FileTxThread;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.ShareFiles;

import java.util.ArrayList;

public class SharingFileAdapter extends RecyclerView.Adapter<SharingFileAdapter.ShareFileHolder> {
    private Context mContext;
    private ArrayList<ShareFiles> shareFiles;

    public SharingFileAdapter(Context mContext, ArrayList<ShareFiles> shareFiles) {
        this.mContext = mContext;
        this.shareFiles = shareFiles;
    }

    @NonNull
    @Override
    public ShareFileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sending_file_item,
                parent, false);
        return new ShareFileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareFileHolder holder, int position) {
        int size = Integer.parseInt(shareFiles.get(position).getSize()) / (1024 * 1024);
        holder.totalFileSize.setText(String.valueOf(size) + "MB");
        if (!shareFiles.get(position).getTitle().equals(""))
            holder.fileName.setText(shareFiles.get(position).getTitle());
        else
            holder.fileName.setText(shareFiles.get(position).getFileName());
        holder.customProgress.setMax(Integer.parseInt(shareFiles.get(position).getSize()));
        holder.currentFileSize.setText(String.valueOf(FileTxThread.currentSize / (1024 * 1024)) + "MB");
        holder.customProgress.setProgress((int)FileTxThread.currentSize);
        if (Integer.parseInt(shareFiles.get(position).getSize()) <= FileTxThread.currentSize)
        {
            holder.status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return shareFiles.size();
    }


    public static class ShareFileHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        public ImageView status;
        public TextView currentFileSize;
        TextView totalFileSize;
        TextView fileName;
        public ProgressBar customProgress;
        public ShareFileHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.sending_thumbnail);
            currentFileSize = itemView.findViewById(R.id.current_sending_file_size);
            totalFileSize = itemView.findViewById(R.id.total_sending_file_size);
            customProgress = itemView.findViewById(R.id.progressSentFile);
            fileName = itemView.findViewById(R.id.sending_file_name);
            status = itemView.findViewById(R.id.sending_status);
        }
    }

}
