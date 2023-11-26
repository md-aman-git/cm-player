package com.aman.videoplayer;

import com.aman.videoplayer.adapters.LanguageAudioAdapter;

public interface GetSetLanguage {
    void myLanguageListener(int position);
    void caller(LanguageAudioAdapter languageAudioAdapter);
    void changeAudio(int position);
}
