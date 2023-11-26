package com.aman.videoplayer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.adapters.LanguageAudioAdapter;

public class CustomDialogClass extends Dialog implements
        View.OnClickListener {

    public Context c;
    public Button yes, no;
    private Intent intent;
    RecyclerView recyclerViewLang;
    LanguageAudioAdapter languageAudioAdapter;

    public CustomDialogClass(Context a, Intent intent) {
        super(a);
        this.c = a;
        this.intent = intent;
    }
    public CustomDialogClass(Context a, LanguageAudioAdapter languageAudioAdapter) {
        super(a);
        this.c = a;
        this.languageAudioAdapter = languageAudioAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (intent != null) {
            setContentView(R.layout.custom_dialog);
            yes = findViewById(R.id.btn_yes);
            no = findViewById(R.id.btn_no);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);
        }
        else
        {
            setContentView(R.layout.language_option_dialog);
            recyclerViewLang = findViewById(R.id.languageAudioRV);
            recyclerViewLang.setAdapter(languageAudioAdapter);
            recyclerViewLang.setLayoutManager(new LinearLayoutManager(c,
                    RecyclerView.VERTICAL, false));
            recyclerViewLang.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                c.startActivity(intent);
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
