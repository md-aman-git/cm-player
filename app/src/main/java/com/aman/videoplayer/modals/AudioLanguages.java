package com.aman.videoplayer.modals;

public class AudioLanguages {
    private String audioType;
    private String audioLanguage;
    private String id;
    boolean selectedLanguage;

    public AudioLanguages(String audioType, String audioLanguage, String id,
                          boolean selectedLanguage) {
        this.audioType = audioType;
        this.audioLanguage = audioLanguage;
        this.id = id;
        this.selectedLanguage = selectedLanguage;
    }

    public String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public String getAudioLanguage() {
        return audioLanguage;
    }

    public void setAudioLanguage(String audioLanguage) {
        this.audioLanguage = audioLanguage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(boolean selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }
}
