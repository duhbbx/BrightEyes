package com.brighteyes.app.ui.settings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.lifecycle.ViewModelProvider;

import com.brighteyes.app.R;
import com.brighteyes.app.databinding.ActivitySettingsBinding;
import com.brighteyes.app.model.UserProfile;
import com.brighteyes.app.ui.base.BaseActivity;
import com.brighteyes.app.ui.main.MainActivity;
import com.brighteyes.app.utils.LocaleHelper;
import com.brighteyes.app.viewmodel.MainViewModel;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {

    private MainViewModel viewModel;
    private UserProfile currentProfile;
    private boolean isLanguageInitializing = true;

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
            getSupportActionBar().setTitle(R.string.settings);
        }

        // 设置语言选择器
        setupLanguageSpinner();
    }

    private void setupLanguageSpinner() {
        String[] languages = LocaleHelper.getSupportedLanguages();
        String[] displayNames = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            displayNames[i] = LocaleHelper.getLanguageDisplayName(this, languages[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, displayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(adapter);

        // 设置当前选中的语言
        int currentIndex = LocaleHelper.getLanguageIndex(this);
        binding.spinnerLanguage.setSelection(currentIndex);

        // 设置选择监听
        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLanguageInitializing) {
                    isLanguageInitializing = false;
                    return;
                }

                String selectedLang = LocaleHelper.getLanguageByIndex(position);
                String currentLang = LocaleHelper.getLanguage(SettingsActivity.this);

                if (!selectedLang.equals(currentLang)) {
                    LocaleHelper.setLanguage(SettingsActivity.this, selectedLang);
                    // 重启应用以应用新语言
                    restartApp();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void initData() {
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                currentProfile = profile;
                binding.etName.setText(profile.getName());
                binding.etAge.setText(String.valueOf(profile.getAge()));
                binding.etDailyGoal.setText(String.valueOf(profile.getDailyTrainingGoalMinutes()));

                String affectedEye = profile.getAffectedEye();
                if ("left".equals(affectedEye) || "左眼".equals(affectedEye)) {
                    binding.radioLeft.setChecked(true);
                } else if ("right".equals(affectedEye) || "右眼".equals(affectedEye)) {
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
            binding.etName.setError(getString(R.string.error_enter_name));
            return;
        }

        if (ageStr.isEmpty()) {
            binding.etAge.setError(getString(R.string.error_enter_age));
            return;
        }

        currentProfile.setName(name);
        currentProfile.setAge(Integer.parseInt(ageStr));

        if (!goalStr.isEmpty()) {
            currentProfile.setDailyTrainingGoalMinutes(Integer.parseInt(goalStr));
        }

        // 使用语言无关的值存储
        if (binding.radioLeft.isChecked()) {
            currentProfile.setAffectedEye("left");
        } else if (binding.radioRight.isChecked()) {
            currentProfile.setAffectedEye("right");
        } else {
            currentProfile.setAffectedEye("both");
        }

        viewModel.saveUserProfile(currentProfile);
        showToast(getString(R.string.save_success));
        finish();
    }
}
