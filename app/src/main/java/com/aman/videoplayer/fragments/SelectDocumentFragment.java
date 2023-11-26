package com.aman.videoplayer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.ShareFiles;
import com.aman.videoplayer.adapters.DocsAdapter;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SelectDocumentFragment extends Fragment {

    View view;
    ArrayList<ShareFiles> docsFiles;
    DocsAdapter docsAdapter;
    RecyclerView docsRV;
    public SelectDocumentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_select_documents, container, false);
        docsRV = view.findViewById(R.id.docsRV);
        docsFiles = new ArrayList<>();
        docsFiles = getAllDocsFiles(getContext());
        if (docsFiles != null && docsFiles.size() > 0)
        {
            docsAdapter = new DocsAdapter(getContext(), docsFiles);
            docsRV.setAdapter(docsAdapter);
            docsRV.setLayoutManager(new LinearLayoutManager(getContext(),
                    RecyclerView.VERTICAL, false));
        }
        return view;
    }
    public ArrayList<ShareFiles> getAllDocsFiles(final Context context) {

        String MY_PREFS_NAME = "SortOrder";
        SharedPreferences prefs = null;
        if (getContext() != null)
            prefs = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String order = null;
        if (prefs != null) {
            order = prefs.getString("sorting", "sortByName");
        }
        String sortOrder = null;
        assert order != null;
        switch (order) {
            case "sortByName":
                sortOrder = MediaStore.Files.FileColumns.DISPLAY_NAME + " ASC";
                break;
            case "sortByDate":
                sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
                break;
            case "sortBySize":
                sortOrder = MediaStore.Files.FileColumns.SIZE + " DESC";
                break;
        }
        final ArrayList<ShareFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.SIZE
        };
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        String[] selectionArgsPdf = new String[]{ mimeType };
        Cursor c = context.getContentResolver().query(uri, projection,
                selectionMimeType, selectionArgsPdf, sortOrder);
        if (c != null) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                String title = c.getString(1);
                String path = c.getString(2);
                String file_name = c.getString(3);
                int duration = c.getInt(4);
                String size = c.getString(5);
                ShareFiles shareFiles = new ShareFiles(title, path, size,
                        duration, file_name, id, 'd');
                Log.e("Path", path);
                tempAudioList.add(shareFiles);
            }
            c.close();
        }
        return tempAudioList;
    }
}