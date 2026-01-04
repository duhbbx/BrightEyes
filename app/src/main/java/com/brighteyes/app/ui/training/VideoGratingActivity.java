package com.brighteyes.app.ui.training;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.brighteyes.app.R;
import com.brighteyes.app.databinding.ActivityVideoGratingBinding;
import com.brighteyes.app.utils.BackgroundMusicManager;
import com.brighteyes.app.widget.GratingView;
import com.google.android.material.button.MaterialButton;

import java.util.Random;

/**
 * 视频光栅训练活动
 * 在视频上叠加可自定义的光栅效果
 */
public class VideoGratingActivity extends AppCompatActivity {

    private ActivityVideoGratingBinding binding;
    private boolean isPlaying = false;
    private boolean isGratingOn = false;
    private boolean isSettingsPanelOpen = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;

    // 当前光栅设置
    private int currentPattern = GratingView.PATTERN_STRIPES;
    private int currentColor = GratingView.COLOR_BLACK_WHITE;
    private int currentAnim = GratingView.ANIM_SCROLL;
    private int currentSize = 40;
    private long currentSpeed = 1000;
    private int currentAlpha = 180;
    private boolean isVertical = true;
    private boolean isReverse = false;

    // 按钮数组
    private MaterialButton[] patternButtons;
    private MaterialButton[] colorButtons;
    private MaterialButton[] animButtons;
    private MaterialButton[] musicButtons;

    // 音乐管理
    private BackgroundMusicManager musicManager;
    private int currentMusic = BackgroundMusicManager.MUSIC_NONE;

    // 手势检测
    private float touchStartX;
    private static final float SWIPE_THRESHOLD = 100;

    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoGratingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hideSystemUI();
        initButtonArrays();
        initVideoPickerLauncher();
        initListeners();
        initSeekBarListeners();
        initMusicControls();
        applySettings();
        updateModeDisplay();

        // 初始化音乐管理器
        musicManager = BackgroundMusicManager.getInstance(this);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void initButtonArrays() {
        patternButtons = new MaterialButton[]{
                binding.btnPatternStripes,
                binding.btnPatternBlocks,
                binding.btnPatternCircles,
                binding.btnPatternRadial,
                binding.btnPatternWave
        };

        colorButtons = new MaterialButton[]{
                binding.btnColorBW,
                binding.btnColorRB,
                binding.btnColorRC,
                binding.btnColorRW,
                binding.btnColorBlW,
                binding.btnColorRWB,
                binding.btnColorRainbow,
                binding.btnColorGM,
                binding.btnColorYB,
                binding.btnColorRedT,
                binding.btnColorBlueT,
                binding.btnColorBlackT
        };

        animButtons = new MaterialButton[]{
                binding.btnAnimScroll,
                binding.btnAnimZoom,
                binding.btnAnimRotate,
                binding.btnAnimPulse,
                binding.btnAnimRandom
        };

        musicButtons = new MaterialButton[]{
                binding.btnMusicNone,
                binding.btnMusicJingle,
                binding.btnMusicABC,
                binding.btnMusicEdelweiss,
                binding.btnMusicGoat
        };
    }

    private void initVideoPickerLauncher() {
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        if (videoUri != null) {
                            playVideo(videoUri);
                        }
                    }
                }
        );
    }

    private void initListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());

        // 选择视频
        binding.btnSelectVideo.setOnClickListener(v -> openVideoPicker());

        // 播放/暂停
        binding.btnPlayPause.setOnClickListener(v -> togglePlayPause());

        // 开关光栅
        binding.btnToggleGrating.setOnClickListener(v -> toggleGrating());

        // 切换方向
        binding.btnToggleDirection.setOnClickListener(v -> {
            binding.gratingOverlay.toggleOrientation();
            isVertical = binding.gratingOverlay.isVertical();
            binding.switchVertical.setChecked(isVertical);
            updateModeDisplay();
        });

        // 设置按钮
        binding.btnSettings.setOnClickListener(v -> toggleSettingsPanel());

        // 关闭设置
        binding.btnCloseSettings.setOnClickListener(v -> closeSettingsPanel());

        // 视频进度条
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    binding.videoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 视频播放完成监听
        binding.videoView.setOnCompletionListener(mp -> {
            isPlaying = false;
            binding.btnPlayPause.setIconResource(R.drawable.ic_play);
        });

        // 视频准备好监听
        binding.videoView.setOnPreparedListener(mp -> {
            binding.seekBar.setMax(binding.videoView.getDuration());
            binding.layoutSelectHint.setVisibility(View.GONE);
            startSeekBarUpdate();
        });

        // 点击视频区域显示/隐藏控制栏
        binding.videoView.setOnClickListener(v -> toggleControlBars());

        // 手势检测 - 左滑打开设置
        binding.videoView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    return false;
                case MotionEvent.ACTION_UP:
                    float deltaX = event.getX() - touchStartX;
                    if (deltaX < -SWIPE_THRESHOLD && !isSettingsPanelOpen) {
                        openSettingsPanel();
                        return true;
                    }
                    return false;
            }
            return false;
        });

        // 图案类型按钮
        binding.btnPatternStripes.setOnClickListener(v -> setPattern(GratingView.PATTERN_STRIPES));
        binding.btnPatternBlocks.setOnClickListener(v -> setPattern(GratingView.PATTERN_BLOCKS));
        binding.btnPatternCircles.setOnClickListener(v -> setPattern(GratingView.PATTERN_CIRCLES));
        binding.btnPatternRadial.setOnClickListener(v -> setPattern(GratingView.PATTERN_RADIAL));
        binding.btnPatternWave.setOnClickListener(v -> setPattern(GratingView.PATTERN_SINE_WAVE));

        // 颜色模式按钮
        binding.btnColorBW.setOnClickListener(v -> setColor(GratingView.COLOR_BLACK_WHITE));
        binding.btnColorRB.setOnClickListener(v -> setColor(GratingView.COLOR_RED_BLUE));
        binding.btnColorRC.setOnClickListener(v -> setColor(GratingView.COLOR_RED_CYAN));
        binding.btnColorRW.setOnClickListener(v -> setColor(GratingView.COLOR_RED_WHITE));
        binding.btnColorBlW.setOnClickListener(v -> setColor(GratingView.COLOR_BLUE_WHITE));
        binding.btnColorRWB.setOnClickListener(v -> setColor(GratingView.COLOR_RED_WHITE_BLACK));
        binding.btnColorRainbow.setOnClickListener(v -> setColor(GratingView.COLOR_RAINBOW));
        binding.btnColorGM.setOnClickListener(v -> setColor(GratingView.COLOR_GREEN_MAGENTA));
        binding.btnColorYB.setOnClickListener(v -> setColor(GratingView.COLOR_YELLOW_BLUE));
        binding.btnColorRedT.setOnClickListener(v -> setColor(GratingView.COLOR_RED_TRANSPARENT));
        binding.btnColorBlueT.setOnClickListener(v -> setColor(GratingView.COLOR_BLUE_TRANSPARENT));
        binding.btnColorBlackT.setOnClickListener(v -> setColor(GratingView.COLOR_BLACK_TRANSPARENT));

        // 动画效果按钮
        binding.btnAnimScroll.setOnClickListener(v -> setAnimation(GratingView.ANIM_SCROLL));
        binding.btnAnimZoom.setOnClickListener(v -> setAnimation(GratingView.ANIM_ZOOM));
        binding.btnAnimRotate.setOnClickListener(v -> setAnimation(GratingView.ANIM_ROTATE));
        binding.btnAnimPulse.setOnClickListener(v -> setAnimation(GratingView.ANIM_PULSE));
        binding.btnAnimRandom.setOnClickListener(v -> setAnimation(GratingView.ANIM_RANDOM));

        // 方向开关
        binding.switchVertical.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVertical = isChecked;
            binding.gratingOverlay.setVertical(isVertical);
            updateModeDisplay();
        });

        binding.switchReverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isReverse = isChecked;
            binding.gratingOverlay.setReverseDirection(isReverse);
        });
    }

    private void initSeekBarListeners() {
        // 大小调节
        binding.seekSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSize = progress + 10;
                binding.tvSize.setText(String.valueOf(currentSize));
                binding.gratingOverlay.setStripeWidth(currentSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 速度调节
        binding.seekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress: 0-100, 对应 3000ms-200ms (慢到快)
                currentSpeed = 3000 - (progress * 28);
                String speedText;
                if (progress < 30) speedText = "慢";
                else if (progress < 70) speedText = "中";
                else speedText = "快";
                binding.tvSpeed.setText(speedText);
                binding.gratingOverlay.setAnimationSpeed(currentSpeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 透明度调节
        binding.seekAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentAlpha = progress;
                int percent = (int) (progress / 255f * 100);
                binding.tvAlpha.setText(percent + "%");
                binding.gratingOverlay.setAlpha(progress / 255f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initMusicControls() {
        // 音乐选择按钮
        binding.btnMusicNone.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_NONE));
        binding.btnMusicJingle.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_JINGLE_BELLS));
        binding.btnMusicABC.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_ABC));
        binding.btnMusicEdelweiss.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_EDELWEISS));
        binding.btnMusicGoat.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_PLEASANT_GOAT));

        // 音量调节
        binding.seekMusicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                binding.tvMusicVolume.setText(progress + "%");
                if (musicManager != null) {
                    musicManager.setVolume(volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 初始状态
        updateButtonSelection(musicButtons, currentMusic);
    }

    private void setMusic(int musicType) {
        currentMusic = musicType;
        updateButtonSelection(musicButtons, musicType);
        if (musicManager != null) {
            musicManager.playMusic(musicType);
        }
    }

    private void setPattern(int pattern) {
        currentPattern = pattern;
        updateButtonSelection(patternButtons, pattern);
        binding.gratingOverlay.setPatternType(pattern);
        updateModeDisplay();
    }

    private void setColor(int color) {
        currentColor = color;
        updateButtonSelection(colorButtons, color);
        binding.gratingOverlay.setColorMode(color);
        updateModeDisplay();
    }

    private void setAnimation(int anim) {
        currentAnim = anim;
        updateButtonSelection(animButtons, anim);
        binding.gratingOverlay.setAnimationType(anim);
    }

    private void updateButtonSelection(MaterialButton[] buttons, int selectedIndex) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setAlpha(i == selectedIndex ? 1.0f : 0.5f);
        }
    }

    private void applySettings() {
        binding.gratingOverlay.setPatternType(currentPattern);
        binding.gratingOverlay.setColorMode(currentColor);
        binding.gratingOverlay.setAnimationType(currentAnim);
        binding.gratingOverlay.setStripeWidth(currentSize);
        binding.gratingOverlay.setAnimationSpeed(currentSpeed);
        binding.gratingOverlay.setVertical(isVertical);
        binding.gratingOverlay.setReverseDirection(isReverse);
        binding.gratingOverlay.setAlpha(currentAlpha / 255f);

        // 设置初始按钮状态
        updateButtonSelection(patternButtons, currentPattern);
        updateButtonSelection(colorButtons, currentColor);
        updateButtonSelection(animButtons, currentAnim);

        // 设置初始SeekBar值
        binding.seekSize.setProgress(currentSize - 10);
        binding.seekSpeed.setProgress(50);
        binding.seekAlpha.setProgress(currentAlpha);
        binding.switchVertical.setChecked(isVertical);
        binding.switchReverse.setChecked(isReverse);
    }

    private void updateModeDisplay() {
        String patternName = GratingView.getPatternTypeName(currentPattern);
        String colorName = GratingView.getColorModeName(currentColor);
        binding.tvCurrentMode.setText(patternName + " · " + colorName);
    }

    private void toggleSettingsPanel() {
        if (isSettingsPanelOpen) {
            closeSettingsPanel();
        } else {
            openSettingsPanel();
        }
    }

    private void openSettingsPanel() {
        isSettingsPanelOpen = true;
        float panelWidth = binding.layoutSettingsPanel.getWidth();
        if (panelWidth <= 0) {
            panelWidth = getResources().getDisplayMetrics().density * 320; // 320dp to px
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                binding.layoutSettingsPanel, "translationX", panelWidth, 0f);
        animator.setDuration(250);
        animator.start();
        binding.tvBottomHint.setVisibility(View.GONE);
    }

    private void closeSettingsPanel() {
        isSettingsPanelOpen = false;
        float panelWidth = binding.layoutSettingsPanel.getWidth();
        if (panelWidth <= 0) {
            panelWidth = getResources().getDisplayMetrics().density * 320; // 320dp to px
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                binding.layoutSettingsPanel, "translationX", 0f, panelWidth);
        animator.setDuration(250);
        animator.start();
        binding.tvBottomHint.setVisibility(View.VISIBLE);
    }

    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        videoPickerLauncher.launch(intent);
    }

    private void playVideo(Uri videoUri) {
        try {
            // 获取持久化权限
            getContentResolver().takePersistableUriPermission(
                    videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            // 忽略权限错误
        }

        binding.videoView.setVideoURI(videoUri);
        binding.videoView.start();
        isPlaying = true;
        binding.btnPlayPause.setIconResource(R.drawable.ic_pause);

        // 自动播放随机背景音乐
        autoPlayMusic();
    }

    private void autoPlayMusic() {
        if (musicManager != null && currentMusic == BackgroundMusicManager.MUSIC_NONE) {
            // 随机选择一首音乐 (1-4)
            int[] musicOptions = {
                BackgroundMusicManager.MUSIC_JINGLE_BELLS,
                BackgroundMusicManager.MUSIC_ABC,
                BackgroundMusicManager.MUSIC_EDELWEISS,
                BackgroundMusicManager.MUSIC_PLEASANT_GOAT
            };
            int randomIndex = new Random().nextInt(musicOptions.length);
            int randomMusic = musicOptions[randomIndex];
            setMusic(randomMusic);
        }
    }

    private void togglePlayPause() {
        if (binding.videoView.isPlaying()) {
            binding.videoView.pause();
            isPlaying = false;
            binding.btnPlayPause.setIconResource(R.drawable.ic_play);
            if (isGratingOn) {
                binding.gratingOverlay.pauseAnimation();
            }
        } else {
            binding.videoView.start();
            isPlaying = true;
            binding.btnPlayPause.setIconResource(R.drawable.ic_pause);
            if (isGratingOn) {
                binding.gratingOverlay.resumeAnimation();
            }
        }
    }

    private void toggleGrating() {
        if (isGratingOn) {
            binding.gratingOverlay.stopAnimation();
            binding.gratingOverlay.setVisibility(View.GONE);
            binding.btnToggleGrating.setText("开启光栅");
            isGratingOn = false;
        } else {
            binding.gratingOverlay.setVisibility(View.VISIBLE);
            binding.gratingOverlay.startAnimation();
            binding.btnToggleGrating.setText("关闭光栅");
            isGratingOn = true;
        }
    }

    private void toggleControlBars() {
        if (binding.topBar.getVisibility() == View.VISIBLE) {
            binding.topBar.setVisibility(View.GONE);
            binding.bottomBar.setVisibility(View.GONE);
            binding.tvBottomHint.setVisibility(View.GONE);
        } else {
            binding.topBar.setVisibility(View.VISIBLE);
            binding.bottomBar.setVisibility(View.VISIBLE);
            if (!isSettingsPanelOpen) {
                binding.tvBottomHint.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startSeekBarUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (binding.videoView.isPlaying()) {
                    binding.seekBar.setProgress(binding.videoView.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateSeekBar);
    }

    @Override
    public void onBackPressed() {
        if (isSettingsPanelOpen) {
            closeSettingsPanel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding.videoView.isPlaying()) {
            binding.videoView.pause();
        }
        if (isGratingOn) {
            binding.gratingOverlay.pauseAnimation();
        }
        if (musicManager != null) {
            musicManager.pauseMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (musicManager != null && currentMusic != BackgroundMusicManager.MUSIC_NONE) {
            musicManager.resumeMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSeekBar);
        binding.gratingOverlay.stopAnimation();
        if (musicManager != null) {
            musicManager.stopMusic();
        }
    }
}
