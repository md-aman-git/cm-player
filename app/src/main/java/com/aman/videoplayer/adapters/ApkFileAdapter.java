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

import com.aman.videoplayer.ApkModal;
import com.aman.videoplayer.R;
import com.aman.videoplayer.activities.SenderActivity;
import com.aman.videoplayer.modals.ShareFiles;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ApkFileAdapter extends RecyclerView.Adapter<ApkFileAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<ApkModal> mFiles;
    private String PATH_OF_SELECTED_FILE_PERF = "PATH_PREF_NAME";
    private String PATH_PREF_NAME = "HereFileStored";

    public ApkFileAdapter(Context mContext, ArrayList<ApkModal> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.documents_item,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        Glide.with(mContext)
                .load(mFiles.get(position).getIcon())
                .into(holder.img_icon);
        int size = Integer.parseInt(mFiles.get(position).getSize()) / 1024;
        if (size >= 1024)
            holder.docs_size.setText( size / 1024 + "MB");
        else
            holder.docs_size.setText(size + "KB");
        holder.docs_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SenderActivity.class);
                ArrayList<ShareFiles> myList = new ArrayList<>();
                ShareFiles shareFiles =
                        new ShareFiles(mFiles.get(position).getTitle(),
                                mFiles.get(position).getPath(),
                                mFiles.get(position).getSize(),
                                0,
                                mFiles.get(position).getFileName(),
                                "0",
                                mFiles.get(position).getType());
                myList.add(shareFiles);
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
        TextView file_name, docs_size;
        ImageView img_icon;
        RelativeLayout docs_item;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.document_file_name);
            img_icon = itemView.findViewById(R.id.img_document);
            docs_item = itemView.findViewById(R.id.docs_item);
            docs_size = itemView.findViewById(R.id.document_size);
        }
    }

    void updateList(ArrayList<ApkModal> videoFiles)
    {
        mFiles = new ArrayList<>();
        mFiles.addAll(videoFiles);
        notifyDataSetChanged();
    }
}

