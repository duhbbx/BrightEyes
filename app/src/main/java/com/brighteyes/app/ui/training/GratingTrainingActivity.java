package com.brighteyes.app.ui.training;

import android.animation.ObjectAnimator;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.brighteyes.app.R;
import com.brighteyes.app.databinding.ActivityGratingTrainingBinding;
import com.brighteyes.app.ui.base.BaseActivity;
import com.brighteyes.app.utils.BackgroundMusicManager;
import com.brighteyes.app.widget.GratingView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Random;

public class GratingTrainingActivity extends BaseActivity<ActivityGratingTrainingBinding> {

    // 当前设置
    private int currentPattern = GratingView.PATTERN_STRIPES;
    private int currentColor = GratingView.COLOR_BLACK_WHITE;
    private int currentAnim = GratingView.ANIM_SCROLL;
    private int currentSize = 40;
    private long currentSpeed = 1000;
    private boolean isVertical = true;
    private boolean isReverse = false;

    // 训练状态
    private long trainingDuration = 5 * 60 * 1000; // 默认5分钟
    private long remainingTime;
    private long startTime;
    private CountDownTimer countDownTimer;
    private boolean isPaused = false;
    private boolean isSettingsPanelOpen = false;
    private boolean isUnlimitedTime = false;

    // 音乐
    private BackgroundMusicManager musicManager;
    private int currentMusic = BackgroundMusicManager.MUSIC_NONE;
    private MaterialButton[] musicButtons;

    // 按钮数组
    private MaterialButton[] patternButtons;
    private MaterialButton[] colorButtons;
    private MaterialButton[] animButtons;
    private MaterialButton[] timeButtons;

    // 手势检测
    private float touchStartX;
    private static final float SWIPE_THRESHOLD = 100;

    @Override
    protected ActivityGratingTrainingBinding getViewBinding() {
        return ActivityGratingTrainingBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initView() {
        hideSystemUI();

        // 初始化按钮数组
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

        timeButtons = new MaterialButton[]{
                binding.btnTime3,
                binding.btnTime5,
                binding.btnTime10,
                binding.btnTimeUnlimited
        };

        musicButtons = new MaterialButton[]{
                binding.btnMusicNone,
                binding.btnMusicJingle,
                binding.btnMusicABC,
                binding.btnMusicEdelweiss,
                binding.btnMusicGoat
        };

        // 初始化音乐管理器
        musicManager = BackgroundMusicManager.getInstance(this);
    }

    @Override
    protected void initData() {
        remainingTime = trainingDuration;
        startTime = System.currentTimeMillis();

        // 立即开始训练
        applySettings();
        binding.gratingView.startAnimation();
        startCountDown();
        updateModeDisplay();

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

    @Override
    protected void initListener() {
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> confirmExit());

        // 暂停按钮
        binding.btnPause.setOnClickListener(v -> togglePause());

        // 设置按钮
        binding.btnSettings.setOnClickListener(v -> toggleSettingsPanel());
        binding.btnCloseSettings.setOnClickListener(v -> closeSettingsPanel());

        // 点击光栅视图切换方向
        binding.gratingView.setOnClickListener(v -> {
            if (!isSettingsPanelOpen) {
                binding.gratingView.toggleOrientation();
                isVertical = binding.gratingView.isVertical();
                binding.switchVertical.setChecked(isVertical);
                updateModeDisplay();
            }
        });

        // 手势检测 - 右滑打开设置
        binding.gratingView.setOnTouchListener((v, event) -> {
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

        // 大小滑块
        binding.seekSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSize = progress + 10;
                binding.tvSize.setText(String.valueOf(currentSize));
                binding.gratingView.setStripeWidth(currentSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 速度滑块
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
                binding.gratingView.setAnimationSpeed(currentSpeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 方向开关
        binding.switchVertical.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVertical = isChecked;
            binding.gratingView.setVertical(isVertical);
            updateModeDisplay();
        });

        binding.switchReverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isReverse = isChecked;
            binding.gratingView.setReverseDirection(isReverse);
        });

        // 时间按钮
        binding.btnTime3.setOnClickListener(v -> setTime(3 * 60 * 1000, 0));
        binding.btnTime5.setOnClickListener(v -> setTime(5 * 60 * 1000, 1));
        binding.btnTime10.setOnClickListener(v -> setTime(10 * 60 * 1000, 2));
        binding.btnTimeUnlimited.setOnClickListener(v -> setTime(-1, 3));

        // 音乐按钮
        binding.btnMusicNone.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_NONE));
        binding.btnMusicJingle.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_JINGLE_BELLS));
        binding.btnMusicABC.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_ABC));
        binding.btnMusicEdelweiss.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_EDELWEISS));
        binding.btnMusicGoat.setOnClickListener(v -> setMusic(BackgroundMusicManager.MUSIC_PLEASANT_GOAT));

        // 音量控制
        binding.seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                musicManager.setVolume(volume);
                binding.tvVolume.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 停止按钮
        binding.btnStop.setOnClickListener(v -> completeTraining());

        // 完成界面按钮
        binding.btnAgain.setOnClickListener(v -> restartTraining());
        binding.btnFinish.setOnClickListener(v -> finish());
    }

    private void setMusic(int musicType) {
        currentMusic = musicType;
        updateButtonSelection(musicButtons, musicType);
        musicManager.playMusic(musicType);
    }

    private void setPattern(int pattern) {
        currentPattern = pattern;
        updateButtonSelection(patternButtons, pattern);
        binding.gratingView.setPatternType(pattern);
        updateModeDisplay();
    }

    private void setColor(int color) {
        currentColor = color;
        updateButtonSelection(colorButtons, color);
        binding.gratingView.setColorMode(color);
        updateModeDisplay();
    }

    private void setAnimation(int anim) {
        currentAnim = anim;
        updateButtonSelection(animButtons, anim);
        binding.gratingView.setAnimationType(anim);
    }

    private void setTime(long duration, int buttonIndex) {
        if (duration < 0) {
            isUnlimitedTime = true;
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            binding.tvTimer.setText("∞");
        } else {
            isUnlimitedTime = false;
            trainingDuration = duration;
            remainingTime = duration;
            startCountDown();
        }
        updateButtonSelection(timeButtons, buttonIndex);
    }

    private void updateButtonSelection(MaterialButton[] buttons, int selectedIndex) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setAlpha(i == selectedIndex ? 1.0f : 0.5f);
        }
    }

    private void applySettings() {
        binding.gratingView.setPatternType(currentPattern);
        binding.gratingView.setColorMode(currentColor);
        binding.gratingView.setAnimationType(currentAnim);
        binding.gratingView.setStripeWidth(currentSize);
        binding.gratingView.setAnimationSpeed(currentSpeed);
        binding.gratingView.setVertical(isVertical);
        binding.gratingView.setReverseDirection(isReverse);
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
        binding.layoutBottomHint.setVisibility(View.GONE);
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
        binding.layoutBottomHint.setVisibility(View.VISIBLE);
    }

    private void startCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (isUnlimitedTime) {
            binding.tvTimer.setText("∞");
            return;
        }

        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                completeTraining();
            }
        }.start();
    }

    private void updateTimerDisplay() {
        long seconds = remainingTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        binding.tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void togglePause() {
        if (isPaused) {
            // 恢复
            binding.gratingView.resumeAnimation();
            if (!isUnlimitedTime) {
                startCountDown();
            }
            binding.btnPause.setImageResource(R.drawable.ic_pause);
            isPaused = false;
        } else {
            // 暂停
            binding.gratingView.pauseAnimation();
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            binding.btnPause.setImageResource(R.drawable.ic_play);
            isPaused = true;
        }
    }

    private void completeTraining() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding.gratingView.stopAnimation();

        // 计算实际训练时长
        long actualDuration = System.currentTimeMillis() - startTime;
        int minutes = (int) (actualDuration / 60000);
        int seconds = (int) ((actualDuration % 60000) / 1000);

        if (minutes > 0) {
            binding.tvCompleteDuration.setText("本次训练时长: " + minutes + "分" + seconds + "秒");
        } else {
            binding.tvCompleteDuration.setText("本次训练时长: " + seconds + "秒");
        }

        closeSettingsPanel();
        binding.layoutTopBar.setVisibility(View.GONE);
        binding.layoutBottomHint.setVisibility(View.GONE);
        binding.layoutComplete.setVisibility(View.VISIBLE);

        // 停止音乐
        musicManager.stopMusic();
    }

    private void restartTraining() {
        binding.layoutComplete.setVisibility(View.GONE);
        binding.layoutTopBar.setVisibility(View.VISIBLE);
        binding.layoutBottomHint.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
        remainingTime = trainingDuration;
        isPaused = false;
        binding.btnPause.setImageResource(R.drawable.ic_pause);

        binding.gratingView.startAnimation();
        if (!isUnlimitedTime) {
            startCountDown();
        } else {
            binding.tvTimer.setText("∞");
        }

        // 恢复音乐
        if (currentMusic != BackgroundMusicManager.MUSIC_NONE) {
            musicManager.playMusic(currentMusic);
        }
    }

    private void confirmExit() {
        // 直接退出，也可以添加确认对话框
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding.gratingView.stopAnimation();
        musicManager.stopMusic();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isSettingsPanelOpen) {
            closeSettingsPanel();
        } else if (binding.layoutComplete.getVisibility() == View.VISIBLE) {
            finish();
        } else {
            confirmExit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused && binding.layoutComplete.getVisibility() != View.VISIBLE) {
            togglePause();
        }
        musicManager.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPaused && currentMusic != BackgroundMusicManager.MUSIC_NONE) {
            musicManager.resumeMusic();
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding.gratingView.stopAnimation();
        musicManager.stopMusic();
        super.onDestroy();
    }
}
