package com.aman.videoplayer.modals;

public class VideoFiles {
    private String id;
    private String title;
    private String path;
    private String album;
    private String dateAdded;
    private String lastModified;
    private String artist;
    private String duration;
    private String size;
    private String filename;
    private String width;
    private String height;
    private String resolution;

    public VideoFiles(String id, String title,
                      String path, String album, String dateAdded,
                      String lastModified, String artist,
                      String duration, String size, String filename,
                      String width, String height, String resolution) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.album = album;
        this.dateAdded = dateAdded;
        this.lastModified = lastModified;
        this.artist = artist;
        this.duration = duration;
        this.size = size;
        this.filename = filename;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
