package com.brighteyes.app.ui.training;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import com.brighteyes.app.databinding.ActivityMazeTrainingBinding;
import com.brighteyes.app.utils.BackgroundMusicManager;
import com.brighteyes.app.widget.GratingView;
import com.brighteyes.app.widget.MazeTrainingView;
import com.google.android.material.button.MaterialButton;

/**
 * 迷宫训练活动
 * 带有可自定义光栅覆盖，通过方向键控制角色走迷宫
 */
public class MazeTrainingActivity extends AppCompatActivity {

    private ActivityMazeTrainingBinding binding;
    private int score = 0;
    private int level = 1;

    // 光栅设置
    private int currentPattern = GratingView.PATTERN_STRIPES;
    private int currentColor = GratingView.COLOR_RED_BLUE;
    private int currentAnim = GratingView.ANIM_SCROLL;
    private int currentSize = 40;
    private long currentSpeed = 1000;
    private int currentAlpha = 128;
    private boolean isVertical = true;
    private boolean isReverse = false;
    private boolean isGratingPanelOpen = false;
    private boolean isGratingEnabled = true;

    // 按钮数组
    private MaterialButton[] patternButtons;
    private MaterialButton[] colorButtons;
    private MaterialButton[] animButtons;
    private MaterialButton[] musicButtons;

    // 音乐管理
    private BackgroundMusicManager musicManager;
    private int currentMusic = BackgroundMusicManager.MUSIC_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMazeTrainingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hideSystemUI();

        // 初始化音乐管理器（必须在initMusicControls之前）
        musicManager = BackgroundMusicManager.getInstance(this);
        Log.d("MazeTraining", "musicManager initialized: " + (musicManager != null));

        initButtonArrays();
        initListeners();
        initGratingSettings();
        initMusicControls();
        applyGratingSettings();
        updateModeDisplay();
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

    private void initListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnExitGame.setOnClickListener(v -> finish());

        // 难度选择
        binding.btnEasy.setOnClickListener(v -> selectDifficulty(1));
        binding.btnMedium.setOnClickListener(v -> selectDifficulty(2));
        binding.btnHard.setOnClickListener(v -> selectDifficulty(3));

        // 预设置界面的光栅开关（保留旧逻辑兼容）
        binding.switchGrating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isGratingEnabled = isChecked;
            binding.layoutGratingSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        binding.switchScrolling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 旧逻辑不再使用，现在由GratingView处理动画
        });

        binding.seekStripeWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSize = progress + 10;
                binding.tvStripeWidth.setText(currentSize + "px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.seekAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentAlpha = (int) (25 + progress * 2.3f); // 约 25-185
                int percent = (int) ((25 + progress * 2.3f) / 255f * 100);
                binding.tvAlpha.setText(percent + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 开始游戏
        binding.btnStart.setOnClickListener(v -> startGame());

        // 方向控制按钮
        binding.btnUp.setOnClickListener(v -> binding.mazeView.moveUp());
        binding.btnDown.setOnClickListener(v -> binding.mazeView.moveDown());
        binding.btnLeft.setOnClickListener(v -> binding.mazeView.moveLeft());
        binding.btnRight.setOnClickListener(v -> binding.mazeView.moveRight());

        // 游戏中光栅开关
        binding.switchGratingGame.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isGratingEnabled = isChecked;
            if (isChecked) {
                binding.gratingOverlay.setVisibility(View.VISIBLE);
                binding.gratingOverlay.startAnimation();
            } else {
                binding.gratingOverlay.stopAnimation();
                binding.gratingOverlay.setVisibility(View.GONE);
            }
        });

        // 光栅设置按钮
        binding.btnGratingSettings.setOnClickListener(v -> openGratingPanel());
        binding.btnCloseGratingPanel.setOnClickListener(v -> closeGratingPanel());

        // 游戏事件监听
        binding.mazeView.setOnMazeEventListener(new MazeTrainingView.OnMazeEventListener() {
            @Override
            public void onMazeComplete(long timeMs, int moves) {
                int timeScore = Math.max(0, 1000 - (int)(timeMs / 100));
                int moveScore = Math.max(0, 500 - moves * 5);
                int levelBonus = level * 100;
                int roundScore = timeScore + moveScore + levelBonus;
                score += roundScore;

                showComplete(timeMs, moves, roundScore);
            }

            @Override
            public void onMove() {
                // 可以添加移动音效
            }

            @Override
            public void onGameOver() {
                showGameOver();
            }
        });

        // 游戏结束界面按钮
        binding.btnRetryGame.setOnClickListener(v -> {
            binding.layoutGameOver.setVisibility(View.GONE);
            binding.layoutGame.setVisibility(View.VISIBLE);
            startGame();
        });

        binding.btnBackToSettings.setOnClickListener(v -> {
            binding.layoutGameOver.setVisibility(View.GONE);
            binding.layoutSettings.setVisibility(View.VISIBLE);
            level = 1;
            score = 0;
        });

        // 完成界面按钮
        binding.btnNextLevel.setOnClickListener(v -> {
            level++;
            updateMazeSize();
            binding.layoutComplete.setVisibility(View.GONE);
            binding.layoutGame.setVisibility(View.VISIBLE);
            startGame();
        });

        binding.btnRestart.setOnClickListener(v -> {
            binding.layoutComplete.setVisibility(View.GONE);
            binding.layoutSettings.setVisibility(View.VISIBLE);
            level = 1;
            score = 0;
        });

        binding.btnFinish.setOnClickListener(v -> finish());
    }

    private void initGratingSettings() {
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
        binding.seekGratingAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentAlpha = progress;
                int percent = (int) (progress / 255f * 100);
                binding.tvGratingAlpha.setText(percent + "%");
                binding.gratingOverlay.setAlpha(progress / 255f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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

    private void initMusicControls() {
        Log.d("MazeTraining", "initMusicControls() called");
        // 音乐选择按钮
        binding.btnMusicNone.setOnClickListener(v -> {
            Log.d("MazeTraining", "btnMusicNone clicked!");
            setMusic(BackgroundMusicManager.MUSIC_NONE);
        });
        binding.btnMusicJingle.setOnClickListener(v -> {
            Log.d("MazeTraining", "btnMusicJingle clicked!");
            setMusic(BackgroundMusicManager.MUSIC_JINGLE_BELLS);
        });
        binding.btnMusicABC.setOnClickListener(v -> {
            Log.d("MazeTraining", "btnMusicABC clicked!");
            setMusic(BackgroundMusicManager.MUSIC_ABC);
        });
        binding.btnMusicEdelweiss.setOnClickListener(v -> {
            Log.d("MazeTraining", "btnMusicEdelweiss clicked!");
            setMusic(BackgroundMusicManager.MUSIC_EDELWEISS);
        });
        binding.btnMusicGoat.setOnClickListener(v -> {
            Log.d("MazeTraining", "btnMusicGoat clicked!");
            setMusic(BackgroundMusicManager.MUSIC_PLEASANT_GOAT);
        });

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
        Log.d("MazeTraining", "setMusic called with type: " + musicType);
        currentMusic = musicType;
        updateButtonSelection(musicButtons, musicType);
        if (musicManager != null) {
            Log.d("MazeTraining", "musicManager is not null, calling playMusic");
            musicManager.playMusic(musicType);
            Toast.makeText(this, "播放: " + BackgroundMusicManager.getMusicName(musicType), Toast.LENGTH_SHORT).show();
        } else {
            Log.e("MazeTraining", "musicManager is NULL!");
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

    private void applyGratingSettings() {
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
        binding.seekGratingAlpha.setProgress(currentAlpha);
        binding.switchVertical.setChecked(isVertical);
        binding.switchReverse.setChecked(isReverse);
    }

    private void updateModeDisplay() {
        String patternName = GratingView.getPatternTypeName(currentPattern);
        String colorName = GratingView.getColorModeName(currentColor);
        binding.tvCurrentMode.setText(patternName + " · " + colorName);
    }

    private void openGratingPanel() {
        isGratingPanelOpen = true;
        float panelWidth = binding.layoutGratingPanel.getWidth();
        if (panelWidth <= 0) {
            panelWidth = getResources().getDisplayMetrics().density * 300; // 300dp to px
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                binding.layoutGratingPanel, "translationX", panelWidth, 0f);
        animator.setDuration(250);
        animator.start();
    }

    private void closeGratingPanel() {
        isGratingPanelOpen = false;
        float panelWidth = binding.layoutGratingPanel.getWidth();
        if (panelWidth <= 0) {
            panelWidth = getResources().getDisplayMetrics().density * 300; // 300dp to px
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                binding.layoutGratingPanel, "translationX", 0f, panelWidth);
        animator.setDuration(250);
        animator.start();
    }

    private void selectDifficulty(int diff) {
        binding.btnEasy.setAlpha(diff == 1 ? 1.0f : 0.5f);
        binding.btnMedium.setAlpha(diff == 2 ? 1.0f : 0.5f);
        binding.btnHard.setAlpha(diff == 3 ? 1.0f : 0.5f);

        level = diff;
        updateMazeSize();
    }

    private void updateMazeSize() {
        int width, height;
        switch (level) {
            case 1:
                width = 17;   // 简单 - 更大一些
                height = 11;
                break;
            case 2:
                width = 23;   // 中等 - 复杂
                height = 13;
                break;
            case 3:
            default:
                width = 29;   // 困难 - 很复杂
                height = 15;
                break;
        }
        width = Math.min(31, width + (level - 1) * 2);
        height = Math.min(21, height + (level - 1));

        binding.mazeView.setMazeSize(width, height);
    }

    private void startGame() {
        binding.layoutSettings.setVisibility(View.GONE);
        binding.layoutGame.setVisibility(View.VISIBLE);
        binding.layoutComplete.setVisibility(View.GONE);

        // 关闭迷宫视图的内置光栅
        binding.mazeView.setShowGrating(false);

        updateMazeSize();
        binding.mazeView.startGame();
        updateScore();

        // 启动GratingView光栅动画
        if (isGratingEnabled) {
            binding.gratingOverlay.setVisibility(View.VISIBLE);
            binding.gratingOverlay.startAnimation();
            binding.switchGratingGame.setChecked(true);
        } else {
            binding.gratingOverlay.setVisibility(View.GONE);
            binding.switchGratingGame.setChecked(false);
        }

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
            Log.d("MazeTraining", "Auto-playing music: " + BackgroundMusicManager.getMusicName(randomMusic));
        }
    }

    private void updateScore() {
        binding.tvScore.setText("得分: " + score);
        binding.tvLevel.setText("关卡: " + level);
    }

    private void showComplete(long timeMs, int moves, int roundScore) {
        binding.gratingOverlay.stopAnimation();

        binding.layoutGame.setVisibility(View.GONE);
        binding.layoutComplete.setVisibility(View.VISIBLE);

        int seconds = (int) (timeMs / 1000);
        binding.tvCompleteTime.setText("用时: " + seconds + "秒");
        binding.tvCompleteMoves.setText("步数: " + moves);
        binding.tvRoundScore.setText("本关得分: +" + roundScore);
        binding.tvTotalScore.setText("总得分: " + score);

        String evaluation;
        if (seconds < 30 && moves < 50) {
            evaluation = "太棒了！你是迷宫大师！";
        } else if (seconds < 60) {
            evaluation = "表现不错，继续加油！";
        } else {
            evaluation = "完成了！多练习会更快哦";
        }
        binding.tvEvaluation.setText(evaluation);
    }

    private void showGameOver() {
        binding.gratingOverlay.stopAnimation();
        if (musicManager != null) {
            musicManager.pauseMusic();
        }

        binding.layoutGame.setVisibility(View.GONE);
        binding.layoutGameOver.setVisibility(View.VISIBLE);

        long timeMs = binding.mazeView.getElapsedTime();
        int moves = binding.mazeView.getMoveCount();
        int seconds = (int) (timeMs / 1000);

        binding.tvGameOverTime.setText("坚持了: " + seconds + "秒");
        binding.tvGameOverMoves.setText("走了: " + moves + "步");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (binding.mazeView.isActive()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_W:
                    binding.mazeView.moveUp();
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_S:
                    binding.mazeView.moveDown();
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_A:
                    binding.mazeView.moveLeft();
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_D:
                    binding.mazeView.moveRight();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (isGratingPanelOpen) {
            closeGratingPanel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.gratingOverlay.pauseAnimation();
        binding.mazeView.pauseBugs();
        if (musicManager != null) {
            musicManager.pauseMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (binding.mazeView.isActive()) {
            if (isGratingEnabled) {
                binding.gratingOverlay.resumeAnimation();
            }
            binding.mazeView.resumeBugs();
        }
        if (musicManager != null && currentMusic != BackgroundMusicManager.MUSIC_NONE) {
            musicManager.resumeMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.gratingOverlay.stopAnimation();
        if (musicManager != null) {
            musicManager.stopMusic();
        }
    }
}
