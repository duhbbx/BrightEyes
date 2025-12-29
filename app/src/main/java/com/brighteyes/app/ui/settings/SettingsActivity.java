package com.brighteyes.app.ui.settings;

import android.view.LayoutInflater;

import androidx.lifecycle.ViewModelProvider;

import com.brighteyes.app.databinding.ActivitySettingsBinding;
import com.brighteyes.app.model.UserProfile;
import com.brighteyes.app.ui.base.BaseActivity;
import com.brighteyes.app.viewmodel.MainViewModel;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {

    private MainViewModel viewModel;
    private UserProfile currentProfile;

    @Override
    protected ActivitySettingsBinding getViewBinding() {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
    }

    @Override
    protected void initData() {
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                currentProfile = profile;
                binding.etName.setText(profile.getName());
                binding.etAge.setText(String.valueOf(profile.getAge()));
                binding.etDailyGoal.setText(String.valueOf(profile.getDailyTrainingGoalMinutes()));

                if ("左眼".equals(profile.getAffectedEye())) {
                    binding.radioLeft.setChecked(true);
                } else if ("右眼".equals(profile.getAffectedEye())) {
                    binding.radioRight.setChecked(true);
                } else {
                    binding.radioBoth.setChecked(true);
                }
            } else {
                currentProfile = new UserProfile();
            }
        });
    }

    @Override
    protected void initListener() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String ageStr = binding.etAge.getText().toString().trim();
        String goalStr = binding.etDailyGoal.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("请输入姓名");
            return;
        }

        if (ageStr.isEmpty()) {
            binding.etAge.setError("请输入年龄");
            return;
        }

        currentProfile.setName(name);
        currentProfile.setAge(Integer.parseInt(ageStr));

        if (!goalStr.isEmpty()) {
            currentProfile.setDailyTrainingGoalMinutes(Integer.parseInt(goalStr));
        }

        if (binding.radioLeft.isChecked()) {
            currentProfile.setAffectedEye("左眼");
        } else if (binding.radioRight.isChecked()) {
            currentProfile.setAffectedEye("右眼");
        } else {
            currentProfile.setAffectedEye("双眼");
        }

        viewModel.saveUserProfile(currentProfile);
        showToast("保存成功");
        finish();
    }
}
