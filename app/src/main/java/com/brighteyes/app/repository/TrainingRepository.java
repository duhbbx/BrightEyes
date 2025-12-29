package com.brighteyes.app.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.brighteyes.app.database.AppDatabase;
import com.brighteyes.app.database.TrainingDao;
import com.brighteyes.app.model.TrainingSession;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrainingRepository {

    private final TrainingDao trainingDao;
    private final ExecutorService executor;

    public TrainingRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        trainingDao = db.trainingDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insertSession(TrainingSession session) {
        executor.execute(() -> trainingDao.insert(session));
    }

    public void updateSession(TrainingSession session) {
        executor.execute(() -> trainingDao.update(session));
    }

    public LiveData<List<TrainingSession>> getAllSessions() {
        return trainingDao.getAllSessions();
    }

    public LiveData<List<TrainingSession>> getTodaySessions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        return trainingDao.getSessionsForDay(startOfDay, endOfDay);
    }

    public LiveData<Integer> getTodayTotalDuration() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        return trainingDao.getTotalDurationForDay(startOfDay, endOfDay);
    }

    public LiveData<List<TrainingSession>> getRecentSessionsByType(String type) {
        return trainingDao.getRecentSessionsByType(type);
    }

    public LiveData<Integer> getCompletedSessionCount() {
        return trainingDao.getCompletedSessionCount();
    }
}
