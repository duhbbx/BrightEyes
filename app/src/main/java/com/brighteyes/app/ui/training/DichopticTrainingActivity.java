package com.brighteyes.app.ui.training;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.brighteyes.app.R;
import com.brighteyes.app.databinding.ActivityDichopticTrainingBinding;
import com.brighteyes.app.utils.BackgroundMusicManager;
import com.brighteyes.app.widget.DichopticChaseView;
import com.brighteyes.app.widget.DichopticFusionView;
import com.brighteyes.app.widget.DichopticPuzzleView;

import java.util.Locale;
import java.util.Random;

/**
 * 双眼视功能训练活动
 * 包含拼图、追逐、融合三种训练模式
 */
public class DichopticTrainingActivity extends AppCompatActivity {

    private ActivityDichopticTrainingBinding binding;

    private static final int MODE_PUZZLE = 0;
    private static final int MODE_CHASE = 1;
    private static final int MODE_FUSION = 2;

    private int currentMode = -1;
    private int score = 0;
    private float strongEyeContrast = 0.3f;
    private CountDownTimer timer;
    private long trainingDurationMs = 5 * 60 * 1000;
    private boolean isPaused = false;
    private long remainingTime = trainingDurationMs;
    private BackgroundMusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDichopticTrainingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        musicManager = BackgroundMusicManager.getInstance(this);

        hideSystemUI();
        initListeners();
        initGameListeners();
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

    private void initListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackFromTraining.setOnClickListener(v -> stopTraining());

        // 模式选择
        binding.cardPuzzle.setOnClickListener(v -> startMode(MODE_PUZZLE));
        binding.cardChase.setOnClickListener(v -> startMode(MODE_CHASE));
        binding.cardFusion.setOnClickListener(v -> startMode(MODE_FUSION));

        // 对比度调节
        binding.seekStrongEyeContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                strongEyeContrast = progress / 100f;
                binding.tvContrastHint.setText(
                        String.format("当前: %d%% (健眼对比度越低，训练越困难)", progress));
                updateContrast();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 暂停/继续
        binding.btnPause.setOnClickListener(v -> {
            if (isPaused) {
                resumeTraining();
                binding.btnPause.setText("暂停");
            } else {
                pauseTraining();
                binding.btnPause.setText("继续");
            }
        });

        // 结束训练
        binding.btnStop.setOnClickListener(v -> stopTraining());

        // 完成
        binding.btnFinish.setOnClickListener(v -> finish());
    }

    private void initGameListeners() {
        // 拼图游戏监听
        binding.puzzleView.setOnPuzzleCompleteListener(new DichopticPuzzleView.OnPuzzleCompleteListener() {
            @Override
            public void onPuzzleComplete() {
                score += 50;
                updateScore();
            }

            @Override
            public void onPiecePlaced() {
                score += 10;
                updateScore();
            }
        });

        // 追逐游戏监听
        binding.chaseView.setOnGameEventListener(new DichopticChaseView.OnGameEventListener() {
            @Override
            public void onTargetCaught() {
                score += 10;
                updateScore();
            }

            @Override
            public void onObstacleHit() {
                score = Math.max(0, score - 5);
                updateScore();
            }

            @Override
            public void onGameOver(int gameScore) {
                // 游戏结束，重新开始
                binding.chaseView.postDelayed(() -> {
                    if (currentMode == MODE_CHASE && !isPaused) {
                        binding.chaseView.startGame();
                    }
                }, 2000);
            }
        });

        // 融合训练监听
        binding.fusionView.setOnFusionEventListener(new DichopticFusionView.OnFusionEventListener() {
            @Override
            public void onCorrectAnswer() {
                score += 20;
                updateScore();
            }

            @Override
            public void onWrongAnswer() {
                score = Math.max(0, score - 5);
                updateScore();
            }
        });
    }

    private void startMode(int mode) {
        currentMode = mode;
        score = 0;
        remainingTime = trainingDurationMs;

        binding.layoutModeSelect.setVisibility(View.GONE);
        binding.layoutTraining.setVisibility(View.VISIBLE);
        binding.layoutComplete.setVisibility(View.GONE);

        // 隐藏所有训练视图
        binding.puzzleView.setVisibility(View.GONE);
        binding.chaseView.setVisibility(View.GONE);
        binding.fusionView.setVisibility(View.GONE);

        // 设置标题和显示对应视图
        switch (mode) {
            case MODE_PUZZLE:
                binding.tvTrainingTitle.setText("拼图训练");
                binding.puzzleView.setVisibility(View.VISIBLE);
                binding.puzzleView.setStrongEyeContrast(strongEyeContrast);
                binding.puzzleView.startTraining();
                break;
            case MODE_CHASE:
                binding.tvTrainingTitle.setText("追逐游戏");
                binding.chaseView.setVisibility(View.VISIBLE);
                binding.chaseView.setStrongEyeContrast(strongEyeContrast);
                binding.chaseView.startGame();
                break;
            case MODE_FUSION:
                binding.tvTrainingTitle.setText("融合训练");
                binding.fusionView.setVisibility(View.VISIBLE);
                binding.fusionView.setStrongEyeContrast(strongEyeContrast);
                binding.fusionView.startTraining();
                break;
        }

        updateScore();
        startTimer();

        // 自动播放随机背景音乐
        autoPlayMusic();
    }

    private void autoPlayMusic() {
        if (musicManager != null) {
            int[] musicOptions = {
                BackgroundMusicManager.MUSIC_JINGLE_BELLS,
                BackgroundMusicManager.MUSIC_ABC,
                BackgroundMusicManager.MUSIC_EDELWEISS,
                BackgroundMusicManager.MUSIC_PLEASANT_GOAT
            };
            int randomIndex = new Random().nextInt(musicOptions.length);
            musicManager.playMusic(musicOptions[randomIndex]);
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                binding.tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                completeTraining();
            }
        }.start();
    }

    private void pauseTraining() {
        isPaused = true;
        if (timer != null) {
            timer.cancel();
        }

        switch (currentMode) {
            case MODE_PUZZLE:
                binding.puzzleView.pauseTraining();
                break;
            case MODE_CHASE:
                binding.chaseView.pauseGame();
                break;
            case MODE_FUSION:
                binding.fusionView.pauseTraining();
                break;
        }
    }

    private void resumeTraining() {
        isPaused = false;
        startTimer();

        switch (currentMode) {
            case MODE_PUZZLE:
                binding.puzzleView.resumeTraining();
                break;
            case MODE_CHASE:
                binding.chaseView.resumeGame();
                break;
            case MODE_FUSION:
                binding.fusionView.resumeTraining();
                break;
        }
    }

    private void stopTraining() {
        if (timer != null) {
            timer.cancel();
        }

        switch (currentMode) {
            case MODE_PUZZLE:
                binding.puzzleView.stopTraining();
                break;
            case MODE_CHASE:
                binding.chaseView.stopGame();
                break;
            case MODE_FUSION:
                binding.fusionView.stopTraining();
                break;
        }

        binding.layoutTraining.setVisibility(View.GONE);
        binding.layoutModeSelect.setVisibility(View.VISIBLE);
        currentMode = -1;
    }

    private void completeTraining() {
        if (timer != null) {
            timer.cancel();
        }

        switch (currentMode) {
            case MODE_PUZZLE:
                binding.puzzleView.stopTraining();
                break;
            case MODE_CHASE:
                binding.chaseView.stopGame();
                break;
            case MODE_FUSION:
                binding.fusionView.stopTraining();
                break;
        }

        binding.layoutTraining.setVisibility(View.GONE);
        binding.layoutComplete.setVisibility(View.VISIBLE);
        binding.tvFinalScore.setText("最终得分: " + score);
    }

    private void updateScore() {
        binding.tvScore.setText("得分: " + score);
    }

    private void updateContrast() {
        binding.puzzleView.setStrongEyeContrast(strongEyeContrast);
        binding.chaseView.setStrongEyeContrast(strongEyeContrast);
        binding.fusionView.setStrongEyeContrast(strongEyeContrast);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentMode != -1 && !isPaused) {
            pauseTraining();
            binding.btnPause.setText("继续");
        }
        if (musicManager != null) {
            musicManager.pauseMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (musicManager != null) {
            musicManager.stopMusic();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentMode != -1) {
            stopTraining();
        } else {
            super.onBackPressed();
        }
    }
}
