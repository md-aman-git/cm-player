package com.aman.videoplayer.activities;

import static com.aman.videoplayer.adapters.VideoAdapter.mFiles;
import static com.aman.videoplayer.adapters.VideoFolderAdapter.mFilesOfFolder;

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
import android.provider.MediaStore;
import android.provider.Settings;
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

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.aman.videoplayer.CustomDialogClass;
import com.aman.videoplayer.GetSetLanguage;
import com.aman.videoplayer.R;
import com.aman.videoplayer.adapters.LanguageAudioAdapter;
import com.aman.videoplayer.modals.AudioLanguages;
import com.aman.videoplayer.modals.VideoFile;
import com.aman.videoplayer.utils.MediaStoreUtils;
import com.aman.videoplayer.utils.OrientationManager;
import com.google.common.base.Strings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity implements OrientationManager.OrientationListener {

    private int position;
    private ProgressBar customProgressVolume, customProgressBright;
    private PlayerView playerView;
    ImageView btFullscreen, audioTrackBtn, lockUnlock,
            backArrow, audioImageUpDown, brightnessImageUpDown,
            subtitleBtn, lockedNow, nextBtn, previousBtn, btnResizeMode;
    private ExoPlayer simpleExoPlayer;
    private boolean flag = false;
    private ArrayList<VideoFile> myFiles = new ArrayList<>();
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

    @OptIn(markerClass = UnstableApi.class)
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
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            contentFetchFromExternalApp(type, intent);
        }
        else {
            String sender = intent.getStringExtra("sender");
            if (sender != null && sender.equals("fromFolderFrag")) {
                myFiles = mFilesOfFolder;
            } else if (sender != null && sender.equals("fromFileFrag")) {
                myFiles = mFiles;
            }
            position = intent.getIntExtra("position", 0);
        }
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
                for (int i = 0; i < simpleExoPlayer.getCurrentTracks().getGroups().size(); i++) {
                    String format = simpleExoPlayer.getCurrentTracks().getGroups()
                            .get(i).getTrackFormat(0).sampleMimeType;
                    String lang = simpleExoPlayer.getCurrentTracks().getGroups()
                            .get(i).getTrackFormat(0).language;
                    String id = simpleExoPlayer.getCurrentTracks().getGroups()
                            .get(i).getTrackFormat(0).id;
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
                simpleExoPlayer.setTrackSelectionParameters(simpleExoPlayer
                        .getTrackSelectionParameters()
                        .buildUpon()
                        .setMaxVideoSizeSd()
                        .setPreferredAudioLanguage(audioLanguages.get(position).getAudioLanguage())
                        .build());
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
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (playerView.isControllerFullyVisible())
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
            if (!playerView.isControllerFullyVisible())
                playerView.showController();
            gesturePlayerView();
        });
        subtitleBtn.setOnClickListener(view -> Toast.makeText(this,
                "This Feature In Next Update", Toast.LENGTH_SHORT).show());
    }

    private void contentFetchFromExternalApp(String type, Intent intent) {
        if (type.startsWith("video/")) {
            Uri uriContent = intent.getData();
            Cursor cursor = null;
            if(uriContent != null) {
                cursor = this.getContentResolver().query(uriContent,
                        null, null, null, null);
            }

            String contentDisplayName = null, contentSize = null;
            if(cursor != null) {
                cursor.moveToFirst();
                int indexContentDisplayName = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                if(indexContentDisplayName > -1) {
                    contentDisplayName = cursor.getString(indexContentDisplayName);
                }
                int indexContentSize = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                if(indexContentSize > -1) {
                    contentSize = cursor.getString(indexContentSize);
                }

                cursor.close();
            }
            if(contentDisplayName != null) {
                StringBuilder selectionBuilder = new StringBuilder(MediaStore.Video.Media.DATA + " like?");
                if(!Strings.isNullOrEmpty(contentSize)) {
                    selectionBuilder.append(" and " + MediaStore.Video.Media.SIZE + " = ?");
                }
                String selection = selectionBuilder.toString();
                String[] selectionArgs = new String[]{"%" + contentDisplayName + "%", contentSize};
                myFiles = MediaStoreUtils.getVideoFiles(this, selection, selectionArgs);
                if(!myFiles.isEmpty()) {
                    String selectionFolder = MediaStore.Video.Media.DATA + " like?";
                    String contentPath = myFiles.get(0).getPath();
                    String contentFolderPath = contentPath.substring(0, contentPath.lastIndexOf('/'));
                    String contentFolder = contentFolderPath.substring(contentFolderPath.lastIndexOf('/'));
                    String[] selectionArgsFolder = new String[]{"%" + contentFolder + "%"};
                    myFiles = MediaStoreUtils.getVideoFiles(this, selectionFolder, selectionArgsFolder);
                    for(int i = 0; i < myFiles.size(); i++) {
                        if(myFiles.get(i).getPath().equals(contentPath)) {
                            position = i;
                            break;
                        }
                    }
                }
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void createExoPlayer(int position) {
        //openAppMethodWork();
        setOrientationMethod();
        if (myFiles.get(position).getFilename() != null) {
            filePlaying.setText(myFiles.get(position).getFilename());
        }
        String path = myFiles.get(position).getPath();
        Log.e("Path", path);
        Uri uri = Uri.parse(Uri.encode(path));
        MediaItem mediaItem = new MediaItem.Builder().setUri(uri).build();
        simpleExoPlayer = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        playerView.setPlayer(simpleExoPlayer);
        playerView.setKeepScreenOn(true);
        simpleExoPlayer.setMediaItem(mediaItem);
        simpleExoPlayer.prepare();
        simpleExoPlayer.setPlayWhenReady(true);
        FullScreenCall();
        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
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

    @OptIn(markerClass = UnstableApi.class)
    private void gesturePlayerView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSystemWritePermission()) {
                handelSystemSettings();
                if (!screenLocked) {
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
                                    if (playerView.isControllerFullyVisible()) {
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
                                } else if (10 * Math.abs((y1 - y)) < 10 * Math.abs(x - x1)) {
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
            if (!screenLocked) {
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
                                if (playerView.isControllerFullyVisible()) {
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
                            } else if (10 * Math.abs((y1 - y)) < 10 * Math.abs(x - x1)) {
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

    @OptIn(markerClass = UnstableApi.class)
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
        trackSelector = new DefaultTrackSelector(this);
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
            retriever.release();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        } finally {
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

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch (screenOrientation) {
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