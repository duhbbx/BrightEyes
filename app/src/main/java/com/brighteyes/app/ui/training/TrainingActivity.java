package com.brighteyes.app.ui.training;

import android.view.LayoutInflater;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.brighteyes.app.R;
import com.brighteyes.app.databinding.ActivityTrainingBinding;
import com.brighteyes.app.model.TrainingType;
import com.brighteyes.app.ui.base.BaseActivity;
import com.brighteyes.app.viewmodel.TrainingViewModel;
import com.brighteyes.app.widget.ColorRecognitionView;
import com.brighteyes.app.widget.FocusView;
import com.brighteyes.app.widget.GratingView;
import com.brighteyes.app.widget.RedFlashView;
import com.brighteyes.app.widget.ShapeMatchingView;
import com.brighteyes.app.widget.TrackingView;

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

        setupTrainingViewListeners();
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
            startTrainingView();
        });

        binding.btnPause.setOnClickListener(v -> {
            Boolean isPaused = viewModel.getIsPaused().getValue();
            if (isPaused != null && isPaused) {
                viewModel.resumeTraining();
                resumeTrainingView();
                binding.btnPause.setText(R.string.pause);
            } else {
                viewModel.pauseTraining();
                pauseTrainingView();
                binding.btnPause.setText(R.string.resume);
            }
        });

        binding.btnStop.setOnClickListener(v -> {
            stopTrainingView();
            viewModel.stopTraining();
        });

        binding.btnFinish.setOnClickListener(v -> finish());

        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupTrainingViewListeners() {
        // 追踪训练点击监听
        binding.trackingView.setOnTargetClickListener(() -> {
            viewModel.addScore(10);
        });

        // 聚焦训练点击监听
        binding.focusView.setOnFocusCompleteListener(() -> {
            viewModel.addScore(10);
        });

        // 色彩识别监听
        binding.colorRecognitionView.setOnColorMatchListener(new ColorRecognitionView.OnColorMatchListener() {
            @Override
            public void onCorrectMatch() {
                viewModel.addScore(20);
            }

            @Override
            public void onWrongMatch() {
                // 可以扣分或不处理
            }
        });

        // 形状配对监听
        binding.shapeMatchingView.setOnShapeMatchListener(new ShapeMatchingView.OnShapeMatchListener() {
            @Override
            public void onCorrectMatch() {
                viewModel.addScore(20);
            }

            @Override
            public void onWrongMatch() {
                // 可以扣分或不处理
            }
        });

        // 红光闪烁点击得分
        binding.redFlashView.setOnClickListener(v -> {
            Boolean isTraining = viewModel.getIsTraining().getValue();
            if (isTraining != null && isTraining) {
                viewModel.addScore(10);
            }
        });

        // 光栅训练点击切换方向
        binding.gratingView.setOnClickListener(v -> {
            Boolean isTraining = viewModel.getIsTraining().getValue();
            if (isTraining != null && isTraining) {
                binding.gratingView.toggleOrientation();
                viewModel.addScore(5);
            }
        });
    }

    private void startTrainingView() {
        // 隐藏所有训练视图
        hideAllTrainingViews();

        // 根据训练类型显示对应视图
        switch (trainingType) {
            case RED_FLASH:
                binding.redFlashView.setVisibility(View.VISIBLE);
                binding.redFlashView.setLevel(1);
                binding.redFlashView.startFlashing();
                break;

            case GRATING:
                binding.gratingView.setVisibility(View.VISIBLE);
                binding.gratingView.setLevel(1);
                binding.gratingView.startAnimation();
                break;

            case TRACKING:
                binding.trackingView.setVisibility(View.VISIBLE);
                binding.trackingView.setLevel(1);
                binding.trackingView.startAnimation();
                break;

            case FOCUS:
                binding.focusView.setVisibility(View.VISIBLE);
                binding.focusView.setLevel(1);
                binding.focusView.startAnimation();
                break;

            case COLOR_RECOGNITION:
                binding.colorRecognitionView.setVisibility(View.VISIBLE);
                binding.colorRecognitionView.setLevel(1);
                binding.colorRecognitionView.startTraining();
                break;

            case SHAPE_MATCHING:
                binding.shapeMatchingView.setVisibility(View.VISIBLE);
                binding.shapeMatchingView.setLevel(1);
                binding.shapeMatchingView.startTraining();
                break;
        }
    }

    private void hideAllTrainingViews() {
        binding.redFlashView.setVisibility(View.GONE);
        binding.gratingView.setVisibility(View.GONE);
        binding.trackingView.setVisibility(View.GONE);
        binding.focusView.setVisibility(View.GONE);
        binding.colorRecognitionView.setVisibility(View.GONE);
        binding.shapeMatchingView.setVisibility(View.GONE);
        binding.trainingCanvas.setVisibility(View.GONE);
    }

    private void pauseTrainingView() {
        switch (trainingType) {
            case RED_FLASH:
                binding.redFlashView.pauseFlashing();
                break;
            case GRATING:
                binding.gratingView.pauseAnimation();
                break;
            case TRACKING:
                binding.trackingView.pauseAnimation();
                break;
            case FOCUS:
                binding.focusView.pauseAnimation();
                break;
            case COLOR_RECOGNITION:
                binding.colorRecognitionView.pauseTraining();
                break;
            case SHAPE_MATCHING:
                binding.shapeMatchingView.pauseTraining();
                break;
        }
    }

    private void resumeTrainingView() {
        switch (trainingType) {
            case RED_FLASH:
                binding.redFlashView.resumeFlashing();
                break;
            case GRATING:
                binding.gratingView.resumeAnimation();
                break;
            case TRACKING:
                binding.trackingView.resumeAnimation();
                break;
            case FOCUS:
                binding.focusView.resumeAnimation();
                break;
            case COLOR_RECOGNITION:
                binding.colorRecognitionView.resumeTraining();
                break;
            case SHAPE_MATCHING:
                binding.shapeMatchingView.resumeTraining();
                break;
        }
    }

    private void stopTrainingView() {
        switch (trainingType) {
            case RED_FLASH:
                binding.redFlashView.stopFlashing();
                break;
            case GRATING:
                binding.gratingView.stopAnimation();
                break;
            case TRACKING:
                binding.trackingView.stopAnimation();
                break;
            case FOCUS:
                binding.focusView.stopAnimation();
                break;
            case COLOR_RECOGNITION:
                binding.colorRecognitionView.stopTraining();
                break;
            case SHAPE_MATCHING:
                binding.shapeMatchingView.stopTraining();
                break;
        }
    }

    private void updateTrainingLevel(int level) {
        switch (trainingType) {
            case RED_FLASH:
                if (binding.redFlashView.isFlashing()) {
                    binding.redFlashView.setLevel(level);
                }
                break;
            case GRATING:
                if (binding.gratingView.isAnimating()) {
                    binding.gratingView.setLevel(level);
                }
                break;
            case TRACKING:
                if (binding.trackingView.isAnimating()) {
                    binding.trackingView.setLevel(level);
                }
                break;
            case FOCUS:
                if (binding.focusView.isAnimating()) {
                    binding.focusView.setLevel(level);
                }
                break;
            case COLOR_RECOGNITION:
                if (binding.colorRecognitionView.isActive()) {
                    binding.colorRecognitionView.setLevel(level);
                }
                break;
            case SHAPE_MATCHING:
                if (binding.shapeMatchingView.isActive()) {
                    binding.shapeMatchingView.setLevel(level);
                }
                break;
        }
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
                // 每100分升一级
                int newLevel = (score / 100) + 1;
                Integer currentLevel = viewModel.getCurrentLevel().getValue();
                if (currentLevel == null || newLevel > currentLevel) {
                    viewModel.levelUp();
                }
            }
        });

        viewModel.getCurrentLevel().observe(this, level -> {
            if (level != null) {
                binding.tvLevel.setText("等级: " + level);
                updateTrainingLevel(level);
            }
        });

        viewModel.getIsCompleted().observe(this, completed -> {
            if (completed != null && completed) {
                showCompletionScreen();
            }
        });
    }

    private void showCompletionScreen() {
        stopTrainingView();
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
            pauseTrainingView();
            binding.btnPause.setText(R.string.resume);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        stopTrainingView();
        super.onDestroy();
    }
}
