package com.brighteyes.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.brighteyes.app.BrightEyesApp;
import com.brighteyes.app.model.TrainingSession;
import com.brighteyes.app.model.UserProfile;
import com.brighteyes.app.repository.TrainingRepository;
import com.brighteyes.app.repository.UserRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final LiveData<UserProfile> userProfile;
    private final LiveData<List<TrainingSession>> todaySessions;
    private final LiveData<Integer> todayTotalDuration;
    private final LiveData<Integer> completedSessionCount;

    public MainViewModel(@NonNull Application application) {
        super(application);
        BrightEyesApp app = (BrightEyesApp) application;
        trainingRepository = app.getTrainingRepository();
        userRepository = app.getUserRepository();

        userProfile = userRepository.getUserProfile();
        todaySessions = trainingRepository.getTodaySessions();
        todayTotalDuration = trainingRepository.getTodayTotalDuration();
        completedSessionCount = trainingRepository.getCompletedSessionCount();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<TrainingSession>> getTodaySessions() {
        return todaySessions;
    }

    public LiveData<Integer> getTodayTotalDuration() {
        return todayTotalDuration;
    }

    public LiveData<Integer> getCompletedSessionCount() {
        return completedSessionCount;
    }

    public void saveUserProfile(UserProfile profile) {
        userRepository.saveUserProfile(profile);
    }
}
