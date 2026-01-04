package com.brighteyes.app.ui.training;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.brighteyes.app.databinding.ActivityReactionTrainingBinding;
import com.brighteyes.app.utils.BackgroundMusicManager;
import com.brighteyes.app.widget.ReactionTrainingView;

import java.util.List;
import java.util.Random;

/**
 * 视觉注意力与反应时间训练活动
 */
public class ReactionTrainingActivity extends AppCompatActivity {

    private ActivityReactionTrainingBinding binding;
    private int score = 0;
    private BackgroundMusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReactionTrainingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        musicManager = BackgroundMusicManager.getInstance(this);

        hideSystemUI();
        initListeners();
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

        // 难度选择
        binding.btnEasy.setOnClickListener(v -> {
            selectDifficulty(1);
        });
        binding.btnMedium.setOnClickListener(v -> {
            selectDifficulty(2);
        });
        binding.btnHard.setOnClickListener(v -> {
            selectDifficulty(3);
        });

        // 呈现时间调节
        binding.seekDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration = 500 + progress * 50; // 500ms - 3000ms
                binding.tvDurationValue.setText(duration + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 开始训练
        binding.btnStart.setOnClickListener(v -> startTraining());

        // 训练事件监听
        binding.reactionView.setOnReactionEventListener(new ReactionTrainingView.OnReactionEventListener() {
            @Override
            public void onCorrectResponse(long reactionTimeMs) {
                // 根据反应时间给分
                int points;
                if (reactionTimeMs < 300) {
                    points = 30; // 极快
                } else if (reactionTimeMs < 500) {
                    points = 20; // 快
                } else if (reactionTimeMs < 800) {
                    points = 15; // 中等
                } else {
                    points = 10; // 慢
                }
                score += points;
                updateScore();
            }

            @Override
            public void onWrongResponse() {
                score = Math.max(0, score - 10);
                updateScore();
            }

            @Override
            public void onMissed() {
                score = Math.max(0, score - 5);
                updateScore();
            }

            @Override
            public void onRoundComplete(int round, float avgReactionTime) {
                showComplete(avgReactionTime);
            }
        });

        // 暂停/继续
        binding.btnPause.setOnClickListener(v -> {
            if (binding.btnPause.getText().equals("暂停")) {
                binding.reactionView.pauseTraining();
                binding.btnPause.setText("继续");
            } else {
                binding.reactionView.resumeTraining();
                binding.btnPause.setText("暂停");
            }
        });

        // 结束
        binding.btnStop.setOnClickListener(v -> {
            binding.reactionView.stopTraining();
            showComplete(binding.reactionView.getAverageReactionTime());
        });

        // 完成界面按钮
        binding.btnRestart.setOnClickListener(v -> {
            binding.layoutComplete.setVisibility(View.GONE);
            binding.layoutSettings.setVisibility(View.VISIBLE);
        });

        binding.btnFinish.setOnClickListener(v -> finish());
    }

    private void selectDifficulty(int difficulty) {
        binding.btnEasy.setAlpha(difficulty == 1 ? 1.0f : 0.5f);
        binding.btnMedium.setAlpha(difficulty == 2 ? 1.0f : 0.5f);
        binding.btnHard.setAlpha(difficulty == 3 ? 1.0f : 0.5f);

        binding.reactionView.setDifficulty(difficulty);

        String desc;
        switch (difficulty) {
            case 1:
                desc = "找到指定颜色的图形";
                break;
            case 2:
                desc = "找到指定形状的图形";
                break;
            default:
                desc = "找到指定颜色+形状的图形";
                break;
        }
        binding.tvDifficultyDesc.setText(desc);
    }

    private void startTraining() {
        score = 0;
        updateScore();

        // 设置呈现时间
        int duration = 500 + binding.seekDuration.getProgress() * 50;
        binding.reactionView.setStimulusDuration(duration);

        binding.layoutSettings.setVisibility(View.GONE);
        binding.layoutTraining.setVisibility(View.VISIBLE);
        binding.layoutComplete.setVisibility(View.GONE);

        binding.btnPause.setText("暂停");
        binding.reactionView.startTraining();

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

    private void updateScore() {
        binding.tvScore.setText("得分: " + score);
    }

    private void showComplete(float avgReactionTime) {
        binding.layoutTraining.setVisibility(View.GONE);
        binding.layoutComplete.setVisibility(View.VISIBLE);

        binding.tvFinalScore.setText("最终得分: " + score);

        int correct = binding.reactionView.getCorrectCount();
        int wrong = binding.reactionView.getWrongCount();
        int missed = binding.reactionView.getMissedCount();
        int total = correct + wrong + missed;

        binding.tvStats.setText(String.format(
                "正确: %d  错误: %d  漏过: %d\n正确率: %.0f%%",
                correct, wrong, missed,
                total > 0 ? correct * 100f / total : 0
        ));

        if (avgReactionTime > 0) {
            binding.tvAvgReaction.setText(String.format("平均反应时间: %.0fms", avgReactionTime));

            // 评价
            String evaluation;
            if (avgReactionTime < 400) {
                evaluation = "反应极快！视觉处理能力优秀！";
            } else if (avgReactionTime < 600) {
                evaluation = "反应较快，继续保持！";
            } else if (avgReactionTime < 800) {
                evaluation = "反应正常，仍有提升空间";
            } else {
                evaluation = "需要更多练习来提高反应速度";
            }
            binding.tvEvaluation.setText(evaluation);
        } else {
            binding.tvAvgReaction.setText("平均反应时间: --");
            binding.tvEvaluation.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.reactionView.pauseTraining();
        binding.btnPause.setText("继续");
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
        binding.reactionView.stopTraining();
        if (musicManager != null) {
            musicManager.stopMusic();
        }
    }

    @Override
    public void onBackPressed() {
        binding.reactionView.stopTraining();
        super.onBackPressed();
    }
}
