package com.brighteyes.app;

import android.app.Application;

import com.brighteyes.app.repository.TrainingRepository;
import com.brighteyes.app.repository.UserRepository;

public class BrightEyesApp extends Application {

    private static BrightEyesApp instance;
    private TrainingRepository trainingRepository;
    private UserRepository userRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initRepositories();
    }

    private void initRepositories() {
        trainingRepository = new TrainingRepository(this);
        userRepository = new UserRepository(this);
    }

    public static BrightEyesApp getInstance() {
        return instance;
    }

    public TrainingRepository getTrainingRepository() {
        return trainingRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
