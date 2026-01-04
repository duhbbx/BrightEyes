package com.brighteyes.app.ui.main;

import android.content.Intent;
import android.view.LayoutInflater;

import androidx.lifecycle.ViewModelProvider;

import com.brighteyes.app.databinding.ActivityMainBinding;
import com.brighteyes.app.model.TrainingType;
import com.brighteyes.app.ui.base.BaseActivity;
import com.brighteyes.app.ui.settings.SettingsActivity;
import com.brighteyes.app.ui.training.CSFTrainingActivity;
import com.brighteyes.app.ui.training.DichopticTrainingActivity;
import com.brighteyes.app.ui.training.GratingTrainingActivity;
import com.brighteyes.app.ui.training.MazeTrainingActivity;
import com.brighteyes.app.ui.training.ReactionTrainingActivity;
import com.brighteyes.app.ui.training.TrainingActivity;
import com.brighteyes.app.ui.training.VideoGratingActivity;
import com.brighteyes.app.viewmodel.MainViewModel;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    private MainViewModel viewModel;

    @Override
    protected ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    protected void initData() {
        observeData();
    }

    @Override
    protected void initListener() {
        // 基础训练模块
        binding.cardRedFlash.setOnClickListener(v -> startTraining(TrainingType.RED_FLASH));
        binding.cardGrating.setOnClickListener(v -> {
            startActivity(new Intent(this, GratingTrainingActivity.class));
        });
        binding.cardTracking.setOnClickListener(v -> startTraining(TrainingType.TRACKING));
        binding.cardFocus.setOnClickListener(v -> startTraining(TrainingType.FOCUS));
        binding.cardColorRecognition.setOnClickListener(v -> startTraining(TrainingType.COLOR_RECOGNITION));
        binding.cardShapeMatching.setOnClickListener(v -> startTraining(TrainingType.SHAPE_MATCHING));

        // 高级训练模块
        binding.cardVideoGrating.setOnClickListener(v -> {
            startActivity(new Intent(this, VideoGratingActivity.class));
        });

        binding.cardDichoptic.setOnClickListener(v -> {
            startActivity(new Intent(this, DichopticTrainingActivity.class));
        });

        binding.cardCSF.setOnClickListener(v -> {
            startActivity(new Intent(this, CSFTrainingActivity.class));
        });

        binding.cardReaction.setOnClickListener(v -> {
            startActivity(new Intent(this, ReactionTrainingActivity.class));
        });

        binding.cardMaze.setOnClickListener(v -> {
            startActivity(new Intent(this, MazeTrainingActivity.class));
        });

        // 设置
        binding.btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void observeData() {
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                binding.tvWelcome.setText("你好，" + profile.getName());
                binding.tvConsecutiveDays.setText("连续训练 " + profile.getConsecutiveDays() + " 天");
            }
        });

        viewModel.getTodayTotalDuration().observe(this, duration -> {
            if (duration != null) {
                int minutes = duration / 60;
                binding.tvTodayDuration.setText("今日已训练 " + minutes + " 分钟");
                binding.progressToday.setProgress(minutes);
            }
        });

        viewModel.getCompletedSessionCount().observe(this, count -> {
            if (count != null) {
                binding.tvTotalSessions.setText("完成 " + count + " 次训练");
            }
        });
    }

    private void startTraining(TrainingType type) {
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_TRAINING_TYPE, type.name());
        startActivity(intent);
    }
}
