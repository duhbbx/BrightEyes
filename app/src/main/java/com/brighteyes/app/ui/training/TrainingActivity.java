package com.brighteyes.app.ui.training;

import android.view.LayoutInflater;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.brighteyes.app.R;
import com.brighteyes.app.databinding.ActivityTrainingBinding;
import com.brighteyes.app.model.TrainingType;
import com.brighteyes.app.ui.base.BaseActivity;
import com.brighteyes.app.viewmodel.TrainingViewModel;

import java.util.Locale;

public class TrainingActivity extends BaseActivity<ActivityTrainingBinding> {

    public static final String EXTRA_TRAINING_TYPE = "training_type";

    private TrainingViewModel viewModel;
    private TrainingType trainingType;

    @Override
    protected ActivityTrainingBinding getViewBinding() {
        return ActivityTrainingBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initView() {
        hideSystemUI();
        viewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

        String typeName = getIntent().getStringExtra(EXTRA_TRAINING_TYPE);
        if (typeName != null) {
            trainingType = TrainingType.valueOf(typeName);
            viewModel.setTrainingType(trainingType);
            binding.tvTrainingName.setText(trainingType.getDisplayName());
            binding.tvTrainingDesc.setText(trainingType.getDescription());
        }
    }

    @Override
    protected void initData() {
        observeData();
    }

    @Override
    protected void initListener() {
        binding.btnStart.setOnClickListener(v -> {
            viewModel.startTraining();
            binding.layoutPreStart.setVisibility(View.GONE);
            binding.layoutTraining.setVisibility(View.VISIBLE);
        });

        binding.btnPause.setOnClickListener(v -> {
            Boolean isPaused = viewModel.getIsPaused().getValue();
            if (isPaused != null && isPaused) {
                viewModel.resumeTraining();
                binding.btnPause.setText(R.string.pause);
            } else {
                viewModel.pauseTraining();
                binding.btnPause.setText(R.string.resume);
            }
        });

        binding.btnStop.setOnClickListener(v -> viewModel.stopTraining());

        binding.btnFinish.setOnClickListener(v -> finish());

        binding.btnBack.setOnClickListener(v -> onBackPressed());

        binding.trainingCanvas.setOnClickListener(v -> {
            Boolean isTraining = viewModel.getIsTraining().getValue();
            if (isTraining != null && isTraining) {
                viewModel.addScore(10);
            }
        });
    }

    private void observeData() {
        viewModel.getRemainingTime().observe(this, time -> {
            if (time != null) {
                long seconds = time / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                binding.tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }
        });

        viewModel.getScore().observe(this, score -> {
            if (score != null) {
                binding.tvScore.setText("得分: " + score);
            }
        });

        viewModel.getCurrentLevel().observe(this, level -> {
            if (level != null) {
                binding.tvLevel.setText("等级: " + level);
            }
        });

        viewModel.getIsCompleted().observe(this, completed -> {
            if (completed != null && completed) {
                showCompletionScreen();
            }
        });
    }

    private void showCompletionScreen() {
        binding.layoutTraining.setVisibility(View.GONE);
        binding.layoutComplete.setVisibility(View.VISIBLE);

        Integer finalScore = viewModel.getScore().getValue();
        Integer finalLevel = viewModel.getCurrentLevel().getValue();
        binding.tvFinalScore.setText("最终得分: " + (finalScore != null ? finalScore : 0));
        binding.tvFinalLevel.setText("达到等级: " + (finalLevel != null ? finalLevel : 1));
    }

    @Override
    public void onBackPressed() {
        Boolean isTraining = viewModel.getIsTraining().getValue();
        if (isTraining != null && isTraining) {
            viewModel.pauseTraining();
            binding.btnPause.setText(R.string.resume);
        } else {
            super.onBackPressed();
        }
    }
}
