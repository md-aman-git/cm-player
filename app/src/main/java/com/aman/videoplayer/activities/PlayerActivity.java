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
import android.view.GestureDetector;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import java.math.BigDecimal;
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
    private TextView seekDuration, filePlaying, brightnessLevel,
            audioLevel, current_playback_speed, current_playback_speed_controller_value;

    private CardView current_playback_speed_controller, decrease_playback_speed, increase_playback_speed,
            fast_backward_card, double_tap_pause_card, double_tap_play_card, fast_forward_card;
    private boolean orientationLandScape = false, screenLocked = false, isDoubleTap = false;
    private RelativeLayout customController, topLayout, bottomLayout, otherControllerLayoutBottom, otherControllerLayoutTop;
    private CustomDialogClass dialogClass;
    private Intent intent;
    private DefaultTrackSelector trackSelector;
    static ArrayList<AudioLanguages> audioLanguages = new ArrayList<>();
    private DefaultTrackSelector.Parameters newParameters;
    private GetSetLanguage getSetLanguage;
    private LanguageAudioAdapter languageAudioAdapter;
    private int MY_POSITION = 0;
    private float currentPlaybackSpeed = 1.00f;
    private final BigDecimal PLAYBACK_SPEED_FACTOR = new BigDecimal("0.05");
    private BigDecimal currentSpeedBigDecimal = new BigDecimal("1.00");
    long positionInitial = 0, positionFinal = 0;

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
            otherControllerLayoutTop.setVisibility(View.GONE);
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
            otherControllerLayoutTop.setVisibility(View.VISIBLE);
            if (!playerView.isControllerFullyVisible())
                playerView.showController();
            gesturePlayerView();
        });
        subtitleBtn.setOnClickListener(view -> Toast.makeText(this,
                "This Feature In Next Update", Toast.LENGTH_SHORT).show());
        current_playback_speed_controller.setOnClickListener(view -> {
            topLayout.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.GONE);
            otherControllerLayoutTop.setVisibility(View.GONE);
            otherControllerLayoutBottom.setVisibility(View.VISIBLE);
            playerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (otherControllerLayoutBottom.getVisibility() == View.VISIBLE) {
                            ViewGroup.MarginLayoutParams params =
                                    (ViewGroup.MarginLayoutParams)
                                            customController.getLayoutParams();
                            params.topMargin = 0;
                            otherControllerLayoutBottom.setVisibility(View.GONE);
                            otherControllerLayoutTop.setVisibility(View.VISIBLE);
                            topLayout.setVisibility(View.VISIBLE);
                            bottomLayout.setVisibility(View.VISIBLE);
                            gesturePlayerView();
                        }
                        if(playerView.isControllerFullyVisible()) {
                            playerView.hideController();
                        }
                        else {
                            playerView.showController();
                        }
                    }
                    return true;
                }
            });
            FullScreenCall();
        });
        increase_playback_speed.setOnClickListener(view -> {
            setPlaybackSpeed(view.getId());
        });
        decrease_playback_speed.setOnClickListener(view -> {
            setPlaybackSpeed(view.getId());
        });
    }
    private void setPlaybackSpeed(int id) {
        if(id == increase_playback_speed.getId()) {
            currentSpeedBigDecimal = currentSpeedBigDecimal.add(PLAYBACK_SPEED_FACTOR);
        }
        else {
            currentSpeedBigDecimal = currentSpeedBigDecimal.subtract(PLAYBACK_SPEED_FACTOR);
        }
        currentPlaybackSpeed = currentSpeedBigDecimal.floatValue();
        simpleExoPlayer.setPlaybackParameters(simpleExoPlayer.getPlaybackParameters()
                .withSpeed(currentPlaybackSpeed));
        String PLAYBACK_SPEED_SYMBOL = "X";
        String currentPlaybackSpeedString = currentSpeedBigDecimal.toString() + PLAYBACK_SPEED_SYMBOL;
        current_playback_speed.setText(currentPlaybackSpeedString);
        current_playback_speed_controller_value.setText(currentPlaybackSpeedString);
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
                handleGestureSwipes();
            } else {
                openAndroidPermissionsMenu();
                dialogClass = new CustomDialogClass(this, intent);
                dialogClass.setCancelable(false);
                dialogClass.show();
            }
        } else {
            handleGestureSwipes();
        }
    }
    @OptIn(markerClass = UnstableApi.class)
    private void handleGestureSwipes() {
        handelSystemSettings();

        final GestureDetector gestureDetector = new GestureDetector(PlayerActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                float currentX = e.getX();
                long SEEK_FAST = 10000;//10 sec
                if (currentX > 2.0 * screen / 3.0) {
                    //fast forward
                    if (simpleExoPlayer.getCurrentPosition() + SEEK_FAST < simpleExoPlayer.getDuration()) {
                        fast_forward_card.setVisibility(View.VISIBLE);
                        simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + SEEK_FAST);
                    }
                } else if(currentX > screen / 3.0) {
                    //play-pause
                    if(simpleExoPlayer.getPlayWhenReady()) {
                        double_tap_play_card.setVisibility(View.VISIBLE);
                    }
                    else {
                        double_tap_pause_card.setVisibility(View.VISIBLE);
                    }
                    simpleExoPlayer.setPlayWhenReady(!simpleExoPlayer.getPlayWhenReady());
                }
                else {
                    //fast backward
                    fast_backward_card.setVisibility(View.VISIBLE);
                    if (simpleExoPlayer.getCurrentPosition() - SEEK_FAST >= 0) {
                        simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() - SEEK_FAST);
                    }
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(distanceX) > Math.abs(distanceY) && !upDown) {
                    leftRight = true;
                    if (diffX > 0) {
                        //right
                        if(Math.abs(diffX) > 40 * SEEK) {
                            SEEK++;
                            if (simpleExoPlayer.getCurrentPosition() < simpleExoPlayer.getDuration())
                                simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + 1000);
                            positionFinal = simpleExoPlayer.getCurrentPosition() + 1000;
                            seekDuration.setVisibility(View.VISIBLE);
                            seekDuration.setText(formattedTime((positionFinal - positionInitial) / 1000));
                        }
                    } else {
                        //left
                        if(Math.abs(diffX) > 40 * SEEK) {
                            SEEK++;
                            if (simpleExoPlayer.getCurrentPosition() < simpleExoPlayer.getDuration())
                                simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() - 1000);
                            positionFinal = simpleExoPlayer.getCurrentPosition() - 1000;
                            seekDuration.setVisibility(View.VISIBLE);
                            seekDuration.setText(formattedTime((positionFinal - positionInitial) / 1000));
                        }
                    }
                } else if(!leftRight) {
                    upDown = true;
                    if (diffY > 0) {
                        //down
                        if(screen / 2.0 < e1.getX()) {
                            //right down
                            if(Math.abs(diffY) > 20 * count) {
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
                        }
                        else {
                            //left down
                            if (Math.abs(diffY) > 20 * count) {
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
                        }
                    } else {
                        //up
                        if(screen / 2.0 < e1.getX()) {
                            //right up
                            if (Math.abs(diffY) > 20 * count) {
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
                        else {
                            //left up
                            if (Math.abs(diffY) > 20 * count) {
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
                return false;
            }
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
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
                return false;
            }
        });
        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    count = 1;
                    SEEK = 1;
                    positionInitial = 0;
                    positionFinal = 0;
                    leftRight = false;
                    upDown = false;
                    customProgressVolume.setVisibility(View.GONE);
                    customProgressBright.setVisibility(View.GONE);
                    audioImageUpDown.setVisibility(View.GONE);
                    brightnessImageUpDown.setVisibility(View.GONE);
                    audioLevel.setVisibility(View.GONE);
                    brightnessLevel.setVisibility(View.GONE);
                    seekDuration.setVisibility(View.GONE);
                    fast_backward_card.setVisibility(View.GONE);
                    fast_forward_card.setVisibility(View.GONE);
                    double_tap_pause_card.setVisibility(View.GONE);
                    double_tap_play_card.setVisibility(View.GONE);
                }
                return true;
            }
        });
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
        otherControllerLayoutBottom = findViewById(R.id.otherControllerLayoutBottom);
        otherControllerLayoutTop = findViewById(R.id.otherControllerLayoutTop);
        subtitleBtn = findViewById(R.id.audio_subtitle);
        backArrow = findViewById(R.id.bt_back_arrow);
        brightnessLevel = findViewById(R.id.brightText);
        audioLevel = findViewById(R.id.volumeText);
        brightnessImageUpDown = findViewById(R.id.progressBrightCount);
        audioImageUpDown = findViewById(R.id.progressVolCount);
        nextBtn = findViewById(R.id.next_btn);
        previousBtn = findViewById(R.id.previous_btn);
        btnResizeMode = findViewById(R.id.btn_resize_mode);
        current_playback_speed_controller_value = findViewById(R.id.current_playback_speed_controller_value);
        current_playback_speed = findViewById(R.id.current_playback_speed);
        current_playback_speed_controller = findViewById(R.id.current_playback_speed_controller);
        decrease_playback_speed = findViewById(R.id.decrease_playback_speed);
        increase_playback_speed = findViewById(R.id.increase_playback_speed);
        fast_backward_card = findViewById(R.id.fast_backward_card);
        double_tap_pause_card = findViewById(R.id.double_tap_pause_card);
        double_tap_play_card = findViewById(R.id.double_tap_play_card);
        fast_forward_card = findViewById(R.id.fast_forward_card);
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