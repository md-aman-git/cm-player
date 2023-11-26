package com.aman.videoplayer.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.aman.videoplayer.modals.AudioLanguages;
import com.aman.videoplayer.CustomDialogClass;
import com.aman.videoplayer.GetSetLanguage;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.VideoFiles;
import com.aman.videoplayer.adapters.LanguageAudioAdapter;
import com.aman.videoplayer.utils.OrientationManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import static com.aman.videoplayer.adapters.VideoAdapter.mFiles;
import static com.aman.videoplayer.adapters.VideoFolderAdapter.mFilesOfFolder;

public class PlayerActivity extends AppCompatActivity implements OrientationManager.OrientationListener {

    private int position;
    private ProgressBar customProgressVolume, customProgressBright;
    private PlayerView playerView;
    ImageView btFullscreen, audioTrackBtn, lockUnlock,
            backArrow, audioImageUpDown, brightnessImageUpDown,
            subtitleBtn, lockedNow, nextBtn, previousBtn, btnResizeMode;
    private SimpleExoPlayer simpleExoPlayer;
    private boolean flag = false;
    private ArrayList<VideoFiles> myFiles = new ArrayList<>();
    private AudioManager audioManager;
    private int count = 1;
    private int brightness;
    private ContentResolver cResolver;
    private Window window;
    private WindowManager.LayoutParams layoutpars = null;
    private int maxVol = 15, screen, height, width, SEEK = 1;
    private boolean upDown = false, leftRight = false, once = true;
    private TextView seekDuration, filePlaying, brightnessLevel, audioLevel;
    private boolean orientationLandScape = false, screenLocked = false;
    private RelativeLayout customController, topLayout, bottomLayout;
    private CustomDialogClass dialogClass;
    private Intent intent;
    private DefaultTrackSelector trackSelector;
    static ArrayList<AudioLanguages> audioLanguages = new ArrayList<>();
    private DefaultTrackSelector.Parameters newParameters;
    private GetSetLanguage getSetLanguage;
    private LanguageAudioAdapter languageAudioAdapter;
    private int MY_POSITION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        OrientationManager orientationManager =
                new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this);
        orientationManager.enable();
        initialize();
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("fromFolderFrag")) {
            myFiles = mFilesOfFolder;
        } else if(sender != null && sender.equals("fromFileFrag")) {
            myFiles = mFiles;
        }
        position = getIntent().getIntExtra("position", 0);
        createExoPlayer(position);
        languageAudioAdapter =
                new LanguageAudioAdapter(PlayerActivity.this,
                        audioLanguages);
        btFullscreen.setOnClickListener(v -> {
            if (flag) {
                //btFullscreen.setImageResource(R.drawable.ic_fullscreen);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                orientationLandScape = false;
                flag = false;
                screen = width;
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams)
                                customController.getLayoutParams();
                ViewGroup.MarginLayoutParams paramsBright =
                        (ViewGroup.MarginLayoutParams)
                                customProgressBright.getLayoutParams();
                if (!orientationLandScape) {
                    params.bottomMargin = 90;
                    params.rightMargin = 0;
                    paramsBright.rightMargin = 40;
                } else {
                    params.bottomMargin = 0;
                    params.rightMargin = 90;
                    paramsBright.rightMargin = 140;
                }
            } else {
                //btFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                orientationLandScape = true;
                flag = true;
                screen = height;
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams)
                                customController.getLayoutParams();
                ViewGroup.MarginLayoutParams paramsBright =
                        (ViewGroup.MarginLayoutParams)
                                customProgressBright.getLayoutParams();
                if (!orientationLandScape) {
                    params.bottomMargin = 90;
                    params.rightMargin = 0;
                    params.rightMargin = 40;
                } else {
                    params.bottomMargin = 0;
                    params.rightMargin = 90;
                    paramsBright.rightMargin = 140;
                }
            }
        });
        getSetLanguage = new GetSetLanguage() {

            @Override
            public void myLanguageListener(int position) {
                audioLanguages.clear();
                MY_POSITION = position;
                for (int i = 0; i < simpleExoPlayer.getCurrentTrackGroups().length; i++) {
                    String format = simpleExoPlayer.getCurrentTrackGroups()
                            .get(i).getFormat(0).sampleMimeType;
                    String lang = simpleExoPlayer.getCurrentTrackGroups()
                            .get(i).getFormat(0).language;
                    String id = simpleExoPlayer.getCurrentTrackGroups()
                            .get(i).getFormat(0).id;
                    if (format != null && format.contains("audio") && id != null && lang != null) {
                        AudioLanguages audio;
                        if (position == (i - 1)) {
                            audio = new AudioLanguages(format, lang,
                                    id, true);
                        } else {
                            audio = new AudioLanguages(format, lang,
                                    id, false);
                        }
                        audioLanguages.add(audio);
                    }
                }
            }

            @Override
            public void caller(LanguageAudioAdapter languageAudioAdapter) {
                languageAudioAdapter.setCallback(this);
            }

            @Override
            public void changeAudio(int position) {
                simpleExoPlayer.setPlayWhenReady(false);
                DefaultTrackSelector.Parameters currentParameters = trackSelector.getParameters();
                newParameters = currentParameters
                        .buildUpon()
                        .setMaxVideoSizeSd()
                        .setPreferredAudioLanguage(audioLanguages.get(position).getAudioLanguage())
                        .build();
                // Set the new parameters.
                trackSelector.setParameters(newParameters);
                simpleExoPlayer.setPlayWhenReady(true);
                if (dialogClass != null) {
                    dialogClass.dismiss();
                    dialogClass = null;
                }
                Toast.makeText(PlayerActivity.this,
                        "Audio Changed", Toast.LENGTH_SHORT).show();
            }
        };
        getSetLanguage.caller(languageAudioAdapter);
        audioTrackBtn.setOnClickListener(view -> {
            getSetLanguage.myLanguageListener(MY_POSITION);
            dialogClass =
                    new CustomDialogClass(PlayerActivity.this, languageAudioAdapter);
            dialogClass.show();
        });
        backArrow.setOnClickListener(view -> onBackPressed());
        lockUnlock.setOnClickListener(view -> {
            screenLocked = true;
            topLayout.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.GONE);
            lockedNow.setVisibility(View.VISIBLE);
            playerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    {
                        if (playerView.isControllerVisible())
                            playerView.hideController();
                        else
                            playerView.showController();
                    }
                    return true;
                }
            });
            FullScreenCall();
        });
        lockedNow.setOnClickListener(view -> {
            screenLocked = false;
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams)
                            customController.getLayoutParams();
            params.topMargin = 0;
            lockedNow.setVisibility(View.GONE);
            topLayout.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.VISIBLE);
            if (!playerView.isControllerVisible())
                playerView.showController();
            gesturePlayerView();
        });
        subtitleBtn.setOnClickListener(view -> Toast.makeText(this,
                "This Feature In Next Update", Toast.LENGTH_SHORT).show());
    }

    private void createExoPlayer(int position) {
        //openAppMethodWork();
        setOrientationMethod();
        if (myFiles.get(position).getFilename() != null) {
            filePlaying.setText(myFiles.get(position).getFilename());
        }
        String path = myFiles.get(position).getPath();
        Log.e("Path", path + "");
        Uri uri = Uri.parse(Uri.encode(path));
        simpleExoPlayer = new SimpleExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        DataSource.Factory factory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "CM Player"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ProgressiveMediaSource
                .Factory(factory, extractorsFactory).createMediaSource(uri);
        playerView.setPlayer(simpleExoPlayer);
        playerView.setKeepScreenOn(true);
        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
        FullScreenCall();
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady,
                                             int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    nextBtnClicked();
                    if (seekDuration.getVisibility() == View.VISIBLE)
                        seekDuration.setVisibility(View.GONE);
                }
            }
        });
        btnResizeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (playerView.getResizeMode()) {
                    case AspectRatioFrameLayout.RESIZE_MODE_FIT:
                        playerView.setResizeMode(
                                AspectRatioFrameLayout.RESIZE_MODE_FILL);
                        simpleExoPlayer.setVideoScalingMode(C
                                .VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        btnResizeMode.setImageResource(R.drawable.ic_zoom_screen);
                        break;
                    case AspectRatioFrameLayout.RESIZE_MODE_FILL:
                        playerView.setResizeMode(
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                        btnResizeMode.setImageResource(R.drawable.ic_fullscreen);
                        break;
                    case AspectRatioFrameLayout.RESIZE_MODE_ZOOM:
                        playerView.setResizeMode(
                                AspectRatioFrameLayout.RESIZE_MODE_FIT);
                        btnResizeMode.setImageResource(R.drawable.ic_crop_screen);
                        break;
                }
            }
        });
    }

    private void gesturePlayerView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSystemWritePermission()) {
                handelSystemSettings();
                if (!screenLocked)
                {
                    playerView.setOnTouchListener(new View.OnTouchListener() {
                        float y1 = 0, x1 = 0;
                        long positionInitial = 0, positionFinal = 0;

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            float y = event.getY();
                            float x = event.getX();
                            float y2;
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                y1 = y;
                                x1 = x;
                                positionInitial = simpleExoPlayer.getCurrentPosition();
                            }
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (count < 2) {
                                    if (playerView.isControllerVisible()) {
                                        playerView.hideController();
                                        FullScreenCall();
                                        ViewGroup.MarginLayoutParams params =
                                                (ViewGroup.MarginLayoutParams)
                                                        customController.getLayoutParams();
                                        ViewGroup.MarginLayoutParams paramsBright =
                                                (ViewGroup.MarginLayoutParams)
                                                        customProgressBright.getLayoutParams();
                                        params.bottomMargin = 0;
                                        params.topMargin = 0;
                                        paramsBright.rightMargin = 40;
                                    } else {
                                        playerView.showController();
                                        showNavButton();
                                        ViewGroup.MarginLayoutParams params =
                                                (ViewGroup.MarginLayoutParams)
                                                        customController.getLayoutParams();
                                        ViewGroup.MarginLayoutParams paramsBright =
                                                (ViewGroup.MarginLayoutParams)
                                                        customProgressBright.getLayoutParams();
                                        if (!orientationLandScape) {
                                            params.bottomMargin = 90;
                                            params.topMargin = 50;
                                            params.rightMargin = 0;
                                            paramsBright.rightMargin = 40;
                                        } else {
                                            params.bottomMargin = 0;
                                            params.topMargin = 50;
                                            params.rightMargin = 90;
                                            paramsBright.rightMargin = 140;
                                        }
                                    }
                                }
                                count = 1;
                                SEEK = 1;
                                customProgressVolume.setVisibility(View.GONE);
                                customProgressBright.setVisibility(View.GONE);
                                audioImageUpDown.setVisibility(View.GONE);
                                brightnessImageUpDown.setVisibility(View.GONE);
                                audioLevel.setVisibility(View.GONE);
                                brightnessLevel.setVisibility(View.GONE);
                                seekDuration.setVisibility(View.GONE);
                                upDown = false;
                                leftRight = false;
                                once = true;
                                positionInitial = 0;
                                positionFinal = 0;
                            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                y2 = y;
                                if (10 * Math.abs((y1 - y)) > 10 * Math.abs(x - x1)) {
                                    if (once) {
                                        upDown = true;
                                        leftRight = false;
                                        once = false;
                                    }
                                }
                                else if (10 * Math.abs((y1 - y)) < 10 * Math.abs(x - x1)) {
                                    if (once) {
                                        upDown = false;
                                        leftRight = true;
                                        once = false;
                                    }
                                }
                                if (!leftRight) {
                                    if (x1 > screen / 2.0) {
                                        if ((y2 - y1) > 0) {
                                            //down
                                            if ((y2 - y1) > 20 * count) {
                                                audioLevel.setVisibility(View.VISIBLE);
                                                audioImageUpDown.setVisibility(View.VISIBLE);
                                                customProgressVolume.setVisibility(View.VISIBLE);
                                                count++;
                                                int volume_level = audioManager
                                                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                                                customProgressVolume
                                                        .setProgress(volume_level);
                                                audioManager.adjustVolume(AudioManager.ADJUST_LOWER,
                                                        AudioManager.FLAG_PLAY_SOUND);
                                                audioLevel
                                                        .setText(String.valueOf(volume_level));
                                            }
                                        } else if ((y2 - y1) < 0) {
                                            //up
                                            if ((y1 - y2) > 20 * count) {
                                                audioLevel.setVisibility(View.VISIBLE);
                                                audioImageUpDown.setVisibility(View.VISIBLE);
                                                customProgressVolume.setVisibility(View.VISIBLE);
                                                count++;
                                                int volume_level = audioManager
                                                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                                                customProgressVolume.setProgress(volume_level);
                                                audioManager.adjustVolume(AudioManager.ADJUST_RAISE,
                                                        AudioManager.FLAG_PLAY_SOUND);
                                                audioLevel
                                                        .setText(String.valueOf(volume_level));
                                            }
                                        }
                                    } else {
                                        if ((y2 - y1) > 0) {
                                            //down
                                            if ((y2 - y1) > 20 * count) {
                                                brightnessLevel.setVisibility(View.VISIBLE);
                                                brightnessImageUpDown.setVisibility(View.VISIBLE);
                                                customProgressBright.setVisibility(View.VISIBLE);
                                                count++;
                                                if (brightness < 256 && brightness > 17) {
                                                    //Set the brightness of this window
                                                    brightness -= 17;
                                                    layoutpars.screenBrightness = brightness / (float) 255;
                                                    //Apply attribute changes to this window
                                                }
                                                if (brightness <= 17) {
                                                    brightness = 0;
                                                    layoutpars.screenBrightness = brightness / (float) 255;
                                                }
                                                brightnessLevel
                                                        .setText(String.valueOf(brightness / 17));
                                                customProgressBright.setProgress(brightness);
                                                window.setAttributes(layoutpars);
                                            }
                                        } else if ((y2 - y1) < 0) {
                                            //up
                                            if ((y1 - y2) > 20 * count) {
                                                brightnessLevel.setVisibility(View.VISIBLE);
                                                brightnessImageUpDown.setVisibility(View.VISIBLE);
                                                customProgressBright.setVisibility(View.VISIBLE);
                                                count++;
                                                if (brightness < 256) {
                                                    if (brightness < 239) {
                                                        brightness += 17;
                                                    } else {
                                                        brightness = 255;
                                                    }
                                                    brightnessLevel
                                                            .setText(String.valueOf(brightness / 17));
                                                    //Set the brightness of this window
                                                    layoutpars.screenBrightness = brightness / (float) 255;
                                                    //Apply attribute changes to this window
                                                }
                                                customProgressBright.setProgress(brightness);
                                                window.setAttributes(layoutpars);
                                            }
                                        }
                                    }
                                }
                                if (!upDown) {
                                    if ((x - x1) > 80 * SEEK) {
                                        if ((x1 - x) < 0) {
                                            //right
                                            SEEK++;
                                            count = 2;
                                            if (simpleExoPlayer.getCurrentPosition() < simpleExoPlayer.getDuration())
                                                simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + 500 * SEEK);
                                            positionFinal = simpleExoPlayer.getCurrentPosition() + 500 * SEEK;
                                            seekDuration.setVisibility(View.VISIBLE);
                                            seekDuration.setText(formattedTime((positionFinal - positionInitial) / 1000));
                                        }
                                    } else if ((x1 - x) > 80 * SEEK) {
                                        //left
                                        if ((x1 - x) > 0) {
                                            //left
                                            SEEK++;
                                            count = 2;
                                            if (simpleExoPlayer.getCurrentPosition() > 0)
                                                simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() - 500 * SEEK);
                                            positionFinal = simpleExoPlayer.getCurrentPosition() - 500 * SEEK;
                                            seekDuration.setVisibility(View.VISIBLE);
                                            seekDuration.setText(formattedTime((positionFinal - positionInitial) / 1000));
                                        }
                                    }
                                }
                            }
                            return true;
                        }

                    });
                }
            } else {
                openAndroidPermissionsMenu();
                dialogClass = new CustomDialogClass(this, intent);
                dialogClass.setCancelable(false);
                dialogClass.show();
            }
        } else {
            handelSystemSettings();
            if (!screenLocked)
            {
                playerView.setOnTouchListener(new View.OnTouchListener() {
                    float y1 = 0, x1 = 0;
                    long positionInitial = 0, positionFinal = 0;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        float y = event.getY();
                        float x = event.getX();
                        float y2;
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            y1 = y;
                            x1 = x;
                            positionInitial = simpleExoPlayer.getCurrentPosition();
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (count < 2) {
                                if (playerView.isControllerVisible()) {
                                    playerView.hideController();
                                    FullScreenCall();
                                    ViewGroup.MarginLayoutParams params =
                                            (ViewGroup.MarginLayoutParams)
                                                    customController.getLayoutParams();
                                    ViewGroup.MarginLayoutParams paramsBright =
                                            (ViewGroup.MarginLayoutParams)
                                                    customProgressBright.getLayoutParams();
                                    params.bottomMargin = 0;
                                    params.topMargin = 0;
                                    paramsBright.rightMargin = 40;
                                } else {
                                    playerView.showController();
                                    showNavButton();
                                    ViewGroup.MarginLayoutParams params =
                                            (ViewGroup.MarginLayoutParams)
                                                    customController.getLayoutParams();
                                    ViewGroup.MarginLayoutParams paramsBright =
                                            (ViewGroup.MarginLayoutParams)
                                                    customProgressBright.getLayoutParams();
                                    if (!orientationLandScape) {
                                        params.bottomMargin = 90;
                                        params.topMargin = 50;
                                        params.rightMargin = 0;
                                        paramsBright.rightMargin = 40;
                                    } else {
                                        params.bottomMargin = 0;
                                        params.topMargin = 50;
                                        params.rightMargin = 90;
                                        paramsBright.rightMargin = 140;
                                    }
                                }
                            }
                            count = 1;
                            SEEK = 1;
                            customProgressVolume.setVisibility(View.GONE);
                            customProgressBright.setVisibility(View.GONE);
                            audioImageUpDown.setVisibility(View.GONE);
                            brightnessImageUpDown.setVisibility(View.GONE);
                            audioLevel.setVisibility(View.GONE);
                            brightnessLevel.setVisibility(View.GONE);
                            seekDuration.setVisibility(View.GONE);
                            upDown = false;
                            leftRight = false;
                            once = true;
                            positionInitial = 0;
                            positionFinal = 0;
                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            y2 = y;
                            if (10 * Math.abs((y1 - y)) > 10 * Math.abs(x - x1)) {
                                if (once) {
                                    upDown = true;
                                    leftRight = false;
                                    once = false;
                                }
                            }
                            else if (10 * Math.abs((y1 - y)) < 10 * Math.abs(x - x1)) {
                                if (once) {
                                    upDown = false;
                                    leftRight = true;
                                    once = false;
                                }
                            }
                            if (!leftRight) {
                                if (x1 > screen / 2.0) {
                                    if ((y2 - y1) > 0) {
                                        //down
                                        if ((y2 - y1) > 20 * count) {
                                            audioLevel.setVisibility(View.VISIBLE);
                                            audioImageUpDown.setVisibility(View.VISIBLE);
                                            customProgressVolume.setVisibility(View.VISIBLE);
                                            count++;
                                            int volume_level = audioManager
                                                    .getStreamVolume(AudioManager.STREAM_MUSIC);
                                            customProgressVolume
                                                    .setProgress(volume_level);
                                            audioManager.adjustVolume(AudioManager.ADJUST_LOWER,
                                                    AudioManager.FLAG_PLAY_SOUND);
                                            audioLevel
                                                    .setText(String.valueOf(volume_level));
                                        }
                                    } else if ((y2 - y1) < 0) {
                                        //up
                                        if ((y1 - y2) > 20 * count) {
                                            audioLevel.setVisibility(View.VISIBLE);
                                            audioImageUpDown.setVisibility(View.VISIBLE);
                                            customProgressVolume.setVisibility(View.VISIBLE);
                                            count++;
                                            int volume_level = audioManager
                                                    .getStreamVolume(AudioManager.STREAM_MUSIC);
                                            customProgressVolume.setProgress(volume_level);
                                            audioManager.adjustVolume(AudioManager.ADJUST_RAISE,
                                                    AudioManager.FLAG_PLAY_SOUND);
                                            audioLevel
                                                    .setText(String.valueOf(volume_level));
                                        }
                                    }
                                } else {
                                    if ((y2 - y1) > 0) {
                                        //down
                                        if ((y2 - y1) > 20 * count) {
                                            brightnessLevel.setVisibility(View.VISIBLE);
                                            brightnessImageUpDown.setVisibility(View.VISIBLE);
                                            customProgressBright.setVisibility(View.VISIBLE);
                                            count++;
                                            if (brightness < 256 && brightness > 17) {
                                                //Set the brightness of this window
                                                brightness -= 17;
                                                layoutpars.screenBrightness = brightness / (float) 255;
                                                //Apply attribute changes to this window
                                            }
                                            if (brightness <= 17) {
                                                brightness = 0;
                                                layoutpars.screenBrightness = brightness / (float) 255;
                                            }
                                            brightnessLevel
                                                    .setText(String.valueOf(brightness / 17));
                                            customProgressBright.setProgress(brightness);
                                            window.setAttributes(layoutpars);
                                        }
                                    } else if ((y2 - y1) < 0) {
                                        //up
                                        if ((y1 - y2) > 20 * count) {
                                            brightnessLevel.setVisibility(View.VISIBLE);
                                            brightnessImageUpDown.setVisibility(View.VISIBLE);
                                            customProgressBright.setVisibility(View.VISIBLE);
                                            count++;
                                            if (brightness < 256) {
                                                if (brightness < 239) {
                                                    brightness += 17;
                                                } else {
                                                    brightness = 255;
                                                }
                                                brightnessLevel
                                                        .setText(String.valueOf(brightness / 17));
                                                //Set the brightness of this window
                                                layoutpars.screenBrightness = brightness / (float) 255;
                                                //Apply attribute changes to this window
                                            }
                                            customProgressBright.setProgress(brightness);
                                            window.setAttributes(layoutpars);
                                        }
                                    }
                                }
                            }
                            if (!upDown) {
                                if ((x - x1) > 80 * SEEK) {
                                    if ((x1 - x) < 0) {
                                        //right
                                        SEEK++;
                                        count = 2;
                                        if (simpleExoPlayer.getCurrentPosition() < simpleExoPlayer.getDuration())
                                            simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + 500 * SEEK);
                                        positionFinal = simpleExoPlayer.getCurrentPosition() + 500 * SEEK;
                                        seekDuration.setVisibility(View.VISIBLE);
                                        seekDuration.setText(formattedTime((positionFinal - positionInitial) / 1000));
                                    }
                                } else if ((x1 - x) > 80 * SEEK) {
                                    //left
                                    if ((x1 - x) > 0) {
                                        //left
                                        SEEK++;
                                        count = 2;
                                        if (simpleExoPlayer.getCurrentPosition() > 0)
                                            simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() - 500 * SEEK);
                                        positionFinal = simpleExoPlayer.getCurrentPosition() - 500 * SEEK;
                                        seekDuration.setVisibility(View.VISIBLE);
                                        seekDuration.setText(formattedTime((positionFinal - positionInitial) / 1000));
                                    }
                                }
                            }
                        }
                        return true;
                    }

                });
            }
        }
    }

    private void initialize() {
        btFullscreen = findViewById(R.id.bt_fullscreen);
        playerView = findViewById(R.id.exoplayer_movie_view);
        customProgressVolume = findViewById(R.id.progressBarVol);
        customProgressBright = findViewById(R.id.progressBarBright);
        seekDuration = findViewById(R.id.DurationSeek);
        customController = findViewById(R.id.custom_controller);
        audioTrackBtn = findViewById(R.id.audio_track);
        filePlaying = findViewById(R.id.fileNamePlaying);
        lockUnlock = findViewById(R.id.bt_lock);
        lockedNow = findViewById(R.id.locker);
        topLayout = findViewById(R.id.topLayout);
        bottomLayout = findViewById(R.id.bottomLayout);
        subtitleBtn = findViewById(R.id.audio_subtitle);
        backArrow = findViewById(R.id.bt_back_arrow);
        brightnessLevel = findViewById(R.id.brightText);
        audioLevel = findViewById(R.id.volumeText);
        brightnessImageUpDown = findViewById(R.id.progressBrightCount);
        audioImageUpDown = findViewById(R.id.progressVolCount);
        nextBtn = findViewById(R.id.next_btn);
        previousBtn = findViewById(R.id.previous_btn);
        btnResizeMode = findViewById(R.id.btn_resize_mode);
        customProgressVolume.setMax(maxVol);
        customProgressBright.setMax(255);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        cResolver = getContentResolver();
        window = getWindow();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(this, trackSelectionFactory);
        // Build on the current parameters.
        DefaultTrackSelector.Parameters currentParameters =
                trackSelector.getParameters();
        newParameters = currentParameters
                .buildUpon()
                .setMaxVideoSizeSd()
                .setPreferredAudioLanguage("en")
                .build();
        // Set the new parameters.
        trackSelector.setParameters(newParameters);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gesturePlayerView();
        nextBtnMethod();
        previousBtnMethod();
    }

    private void previousBtnClicked() {
        simpleExoPlayer.stop();
        simpleExoPlayer.release();
        position = position == 0 ? myFiles.size() - 1 : --position;
        createExoPlayer(position);
    }

    private void previousBtnMethod() {
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousBtnClicked();
            }
        });
    }

    private void nextBtnMethod() {
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextBtnClicked();
            }
        });
    }

    private void nextBtnClicked() {
        simpleExoPlayer.stop();
        simpleExoPlayer.release();
        position = position == myFiles.size() - 1 ? 0 : ++position;
        createExoPlayer(position);
    }

    private void handelSystemSettings() {
        try {
            Settings.System.putInt(cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //Set the system brightness using the brightness variable value
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        //Get the current window attributes
        layoutpars = window.getAttributes();
        audioManager = (AudioManager) getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
    }

    private void setOrientationMethod() {
        String wid = myFiles.get(position).getWidth();
        String heig = myFiles.get(position).getHeight();
        String rotation = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        FileInputStream inputStream = null;
        try {
            retriever = new MediaMetadataRetriever();
            inputStream = new FileInputStream(myFiles.get(position).getPath());
            retriever.setDataSource(inputStream.getFD());
            rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (wid != null && heig != null) {
            if (Integer.parseInt(wid) > Integer.parseInt(heig)) {
                //btFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                orientationLandScape = true;
                flag = true;
                screen = height;
            } else {
                //btFullscreen.setImageResource(R.drawable.ic_fullscreen);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                orientationLandScape = false;
                flag = false;
                screen = width;
            }
        } else {
            if ((rotation != null && rotation.equals("270"))
                    || (rotation != null && rotation.equals("90"))) {
                //btFullscreen.setImageResource(R.drawable.ic_fullscreen);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                orientationLandScape = false;
                flag = false;
                screen = width;
            } else {
                //btFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                orientationLandScape = true;
                flag = true;
                screen = height;
            }
        }
        if (customController != null && customProgressBright != null) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams)
                            customController.getLayoutParams();
            ViewGroup.MarginLayoutParams paramsBright =
                    (ViewGroup.MarginLayoutParams)
                            customProgressBright.getLayoutParams();
            if (!orientationLandScape) {
                params.bottomMargin = 0;
                params.rightMargin = 0;
                params.topMargin = 0;
                params.leftMargin = 0;
            } else {
                params.bottomMargin = 0;
                params.rightMargin = 0;
                params.topMargin = 0;
                params.leftMargin = 0;
                paramsBright.rightMargin = 50;
            }
        }
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void FullScreenCall() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void showNavButton() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
    }

    private boolean checkSystemWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this);
        }
        return false;
    }

    private void openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
        }
    }

    private String formattedTime(long mCurrentPosition) {

        String totalout;
        String totaloutNew;
        String seconds = String.valueOf((mCurrentPosition % 60));
        String minutes = String.valueOf(mCurrentPosition / 60);
        String hours = String.valueOf(mCurrentPosition / 3600);
        if (hours.equals("0")) {
            totalout = minutes + ":" + seconds;
            totaloutNew = minutes + ":" + "0" + seconds;
        } else {
            minutes = String.valueOf((mCurrentPosition - (3600 * Integer.parseInt(hours))) / 60);
            totalout = hours + ":" + minutes + ":" + seconds;
            totaloutNew = hours + ":" + minutes + ":" + "0" + seconds;
        }
        if (seconds.length() == 1) {
            return totaloutNew;
        } else {
            return totalout;
        }
    }

    private void setTheme() {
        String MY_PREFS_THEME = "ThemesChanger";
        SharedPreferences preferences = getSharedPreferences(MY_PREFS_THEME, MODE_PRIVATE);
        if (preferences.getString("themes", "dark").equals("dark")) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
    }

    private void openAppMethodWork() {
        Uri uri = getIntent().getData();
        String id = null;
        //myFiles = getAllAudioFromDevice(this, id);
    }
    public ArrayList<VideoFiles>
    getAllAudioFromDevice(final Context context, String pathFinder) {

        final ArrayList<VideoFiles> tempAudioList = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
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
        String selection = android.provider.MediaStore.Video.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + pathFinder + "%"};
        Log.e("Uri In Video", uri + "");
        Cursor c = context.getContentResolver().query(uri, projection,
                selection, selectionArgs, null);
        if (c != null) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                String artist = c.getString(1);
                String title = c.getString(2);
                String path = c.getString(3);
                String file_name = c.getString(4);
                String duration = c.getString(5);
                String album = c.getString(6);
                String size = c.getString(7);
                String date_added = c.getString(8);
                String date_modified = c.getString(9);
                String width = c.getString(10);
                String height = c.getString(11);
                String resolution = c.getString(12);
                VideoFiles videoFiles = new VideoFiles(id, title, path,
                        album, date_added, date_modified,
                        artist, duration, size, file_name, width, height, resolution);
                tempAudioList.add(videoFiles);
            }
            c.close();
        }
        return tempAudioList;
    }

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch(screenOrientation){
            case PORTRAIT:
            case REVERSED_PORTRAIT:
                break;
            case REVERSED_LANDSCAPE:
                if (orientationLandScape)
                    this.setRequestedOrientation(ActivityInfo
                            .SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case LANDSCAPE:
                if (orientationLandScape)
                    this.setRequestedOrientation(ActivityInfo
                            .SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }
}