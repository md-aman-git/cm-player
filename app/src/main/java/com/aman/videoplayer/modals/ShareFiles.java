package com.aman.videoplayer.modals;

import android.graphics.drawable.Drawable;

public class ShareFiles {
    private String title;
    private String path;
    private String size;
    private int duration;
    private String fileName;
    private String id;
    private char type;

    public ShareFiles(String title, String path, String size,
                      int duration, String fileName, String id, char type) {
        this.title = title;
        this.path = path;
        this.size = size;
        this.duration = duration;
        this.fileName = fileName;
        this.id = id;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }
}
