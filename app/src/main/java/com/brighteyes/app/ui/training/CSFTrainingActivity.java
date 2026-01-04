package com.brighteyes.app.ui.training;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.brighteyes.app.R;
import com.brighteyes.app.databinding.ActivityCsfTrainingBinding;
import com.brighteyes.app.utils.BackgroundMusicManager;
import com.brighteyes.app.widget.CSFTrainingView;

import java.util.Locale;
import java.util.Random;

/**
 * 对比敏感度训练活动
 *
 * 功能：
 * - 自适应CSF测试
 * - 不同空间频率+对比度组合
 * - 难度随正确率动态变化
 * - 生成CSF曲线
 */
public class CSFTrainingActivity extends AppCompatActivity {

    private ActivityCsfTrainingBinding binding;

    private CountDownTimer timer;
    private long trainingDurationMs = 10 * 60 * 1000; // 10分钟
    private long remainingTime = trainingDurationMs;
    private boolean isPaused = false;
    private int score = 0;
    private BackgroundMusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCsfTrainingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        musicManager = BackgroundMusicManager.getInstance(this);

        hideSystemUI();
        initListeners();
        startTraining();

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
        binding.btnBack.setOnClickListener(v -> finish());

        binding.csfView.setOnCSFEventListener(new CSFTrainingView.OnCSFEventListener() {
            @Override
            public void onCorrectAnswer(int frequency, float contrast) {
                score += 10;
                updateScore();
            }

            @Override
            public void onWrongAnswer(int frequency, float contrast) {
                score = Math.max(0, score - 5);
                updateScore();
            }

            @Override
            public void onTestComplete(float[] csfCurve) {
                // 测试完成，显示结果
                showComplete();
            }
        });

        binding.btnPause.setOnClickListener(v -> {
            if (isPaused) {
                resumeTraining();
                binding.btnPause.setText("暂停");
            } else {
                pauseTraining();
                binding.btnPause.setText("继续");
            }
        });

        binding.btnStop.setOnClickListener(v -> stopTraining());

        binding.btnShowCurve.setOnClickListener(v -> {
            binding.csfView.showCSFCurve();
        });

        binding.btnFinish.setOnClickListener(v -> finish());

        binding.btnRestart.setOnClickListener(v -> {
            binding.layoutComplete.setVisibility(View.GONE);
            binding.layoutTraining.setVisibility(View.VISIBLE);
            startTraining();
        });
    }

    private void startTraining() {
        score = 0;
        remainingTime = trainingDurationMs;
        isPaused = false;

        binding.layoutTraining.setVisibility(View.VISIBLE);
        binding.layoutComplete.setVisibility(View.GONE);

        updateScore();
        binding.csfView.startTraining();
        startTimer();
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
                showComplete();
            }
        }.start();
    }

    private void pauseTraining() {
        isPaused = true;
        if (timer != null) {
            timer.cancel();
        }
        binding.csfView.pauseTraining();
    }

    private void resumeTraining() {
        isPaused = false;
        startTimer();
        binding.csfView.resumeTraining();
    }

    private void stopTraining() {
        if (timer != null) {
            timer.cancel();
        }
        binding.csfView.stopTraining();
        showComplete();
    }

    private void showComplete() {
        if (timer != null) {
            timer.cancel();
        }

        binding.layoutTraining.setVisibility(View.GONE);
        binding.layoutComplete.setVisibility(View.VISIBLE);

        binding.tvFinalScore.setText("最终得分: " + score);

        int correct = binding.csfView.getTotalCorrect();
        int total = binding.csfView.getTotalTrials();
        String accuracy = total > 0 ? String.format("%.0f%%", correct * 100f / total) : "--";
        binding.tvAccuracy.setText("正确率: " + accuracy + " (" + correct + "/" + total + ")");
    }

    private void updateScore() {
        binding.tvScore.setText("得分: " + score);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused) {
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
        stopTraining();
        super.onBackPressed();
    }
}
