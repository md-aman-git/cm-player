package com.aman.videoplayer;

import android.graphics.drawable.Drawable;

public class ApkModal {
    private String title;
    private String path;
    private String size;
    private String fileName;
    private Drawable icon;
    private char type;

    public ApkModal(String title, String path, String size,
                    String fileName, Drawable icon, char type) {
        this.title = title;
        this.path = path;
        this.size = size;
        this.fileName = fileName;
        this.icon = icon;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }
}
