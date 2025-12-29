package com.brighteyes.app.viewmodel;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.brighteyes.app.BrightEyesApp;
import com.brighteyes.app.model.TrainingSession;
import com.brighteyes.app.model.TrainingType;
import com.brighteyes.app.repository.TrainingRepository;

public class TrainingViewModel extends AndroidViewModel {

    private final TrainingRepository trainingRepository;

    private final MutableLiveData<TrainingType> currentTrainingType = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentLevel = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Long> remainingTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTraining = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isPaused = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isCompleted = new MutableLiveData<>(false);

    private TrainingSession currentSession;
    private CountDownTimer timer;
    private long trainingDurationMillis = 5 * 60 * 1000;
    private long pausedTimeRemaining;

    public TrainingViewModel(@NonNull Application application) {
        super(application);
        BrightEyesApp app = (BrightEyesApp) application;
        trainingRepository = app.getTrainingRepository();
    }

    public void setTrainingType(TrainingType type) {
        currentTrainingType.setValue(type);
    }

    public void setTrainingDuration(int minutes) {
        this.trainingDurationMillis = minutes * 60 * 1000L;
    }

    public void startTraining() {
        if (currentTrainingType.getValue() == null) return;

        currentSession = new TrainingSession(
                currentTrainingType.getValue().name(),
                System.currentTimeMillis()
        );
        currentSession.setLevel(currentLevel.getValue() != null ? currentLevel.getValue() : 1);

        isTraining.setValue(true);
        isPaused.setValue(false);
        isCompleted.setValue(false);
        score.setValue(0);

        startTimer(trainingDurationMillis);
    }

    public void pauseTraining() {
        if (timer != null) {
            timer.cancel();
            pausedTimeRemaining = remainingTime.getValue() != null ? remainingTime.getValue() : 0;
            isPaused.setValue(true);
        }
    }

    public void resumeTraining() {
        isPaused.setValue(false);
        startTimer(pausedTimeRemaining);
    }

    public void stopTraining() {
        if (timer != null) {
            timer.cancel();
        }
        completeSession();
    }

    public void addScore(int points) {
        Integer currentScore = score.getValue();
        score.setValue((currentScore != null ? currentScore : 0) + points);
    }

    public void levelUp() {
        Integer current = currentLevel.getValue();
        currentLevel.setValue((current != null ? current : 1) + 1);
    }

    private void startTimer(long durationMillis) {
        timer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime.postValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                remainingTime.postValue(0L);
                completeSession();
            }
        }.start();
    }

    private void completeSession() {
        isTraining.setValue(false);
        isCompleted.setValue(true);

        if (currentSession != null) {
            currentSession.setEndTime(System.currentTimeMillis());
            currentSession.setDurationSeconds(
                    (int) ((currentSession.getEndTime() - currentSession.getStartTime()) / 1000)
            );
            currentSession.setScore(score.getValue() != null ? score.getValue() : 0);
            currentSession.setLevel(currentLevel.getValue() != null ? currentLevel.getValue() : 1);
            currentSession.setCompleted(true);

            trainingRepository.insertSession(currentSession);
        }
    }

    public LiveData<TrainingType> getCurrentTrainingType() {
        return currentTrainingType;
    }

    public LiveData<Integer> getCurrentLevel() {
        return currentLevel;
    }

    public LiveData<Integer> getScore() {
        return score;
    }

    public LiveData<Long> getRemainingTime() {
        return remainingTime;
    }

    public LiveData<Boolean> getIsTraining() {
        return isTraining;
    }

    public LiveData<Boolean> getIsPaused() {
        return isPaused;
    }

    public LiveData<Boolean> getIsCompleted() {
        return isCompleted;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
        }
    }
}
