package com.aman.videoplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.aman.videoplayer.modals.VideoFile;

import java.util.ArrayList;
import java.util.List;

public class MediaStoreUtils {
    public static ArrayList<VideoFile> getVideoFiles(Context context, String selection,
                                         String[] selectionArgs) {
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.ALBUM,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.RESOLUTION
        };
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri,
                projection, selection, selectionArgs, null);
        ArrayList<VideoFile> videoFiles = new ArrayList<>();
        if(cursor != null) {
            while(cursor.moveToNext()) {

                String id = cursor.getString(0);
                String artist = cursor.getString(1);
                String title = cursor.getString(2);
                String path = cursor.getString(3);
                String file_name = cursor.getString(4);
                String duration = cursor.getString(5);
                String album = cursor.getString(6);
                String size = cursor.getString(7);
                String date_added = cursor.getString(8);
                String date_modified = cursor.getString(9);
                String width = cursor.getString(10);
                String height = cursor.getString(11);
                String resolution = cursor.getString(12);
                VideoFile videoFile = new VideoFile(id, title, path,
                        album, date_added, date_modified,
                        artist, duration, size, file_name, width, height, resolution);
                videoFiles.add(videoFile);
            }
            cursor.close();
        }
        return videoFiles;
    }
}
