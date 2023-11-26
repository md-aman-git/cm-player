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

import com.aman.videoplayer.utils.ClientRxThread;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.ReceiveFilesModal;

import java.util.ArrayList;

public class ReceivingFileAdapter extends RecyclerView.Adapter<ReceivingFileAdapter.ReceiveFileHolder> {
    private Context mContext;
    private ArrayList<ReceiveFilesModal> receiveFiles;

    public ReceivingFileAdapter(Context mContext, ArrayList<ReceiveFilesModal> receiveFiles) {
        this.mContext = mContext;
        this.receiveFiles = receiveFiles;
    }

    @NonNull
    @Override
    public ReceiveFileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sending_file_item,
                parent, false);
        return new ReceiveFileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveFileHolder holder, int position) {
        holder.totalFileSize.setText(receiveFiles.get(position).getFileSize() / (1024 * 1024) + "MB");
        holder.fileName.setText(receiveFiles.get(position).getFileName());
        holder.currentFileSize.setText(
                String.valueOf(ClientRxThread.currentFileReceived / (1024*1024)) + "MB");
        holder.customProgress.setMax(receiveFiles.get(position).getFileSize());
        holder.customProgress.setProgress((int)ClientRxThread.currentFileReceived);
        if (receiveFiles.get(position).getFileSize() <= ClientRxThread.currentFileReceived)
        {
            holder.status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return receiveFiles.size();
    }


    public static class ReceiveFileHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        public ImageView status;
        public TextView currentFileSize;
        TextView totalFileSize;
        TextView fileName;
        public ProgressBar customProgress;
        public ReceiveFileHolder(@NonNull View itemView) {
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
