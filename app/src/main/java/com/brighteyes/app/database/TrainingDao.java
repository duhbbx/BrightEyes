package com.brighteyes.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.brighteyes.app.model.TrainingSession;

import java.util.List;

@Dao
public interface TrainingDao {

    @Insert
    long insert(TrainingSession session);

    @Update
    void update(TrainingSession session);

    @Query("SELECT * FROM training_sessions ORDER BY startTime DESC")
    LiveData<List<TrainingSession>> getAllSessions();

    @Query("SELECT * FROM training_sessions WHERE startTime >= :startOfDay AND startTime < :endOfDay")
    LiveData<List<TrainingSession>> getSessionsForDay(long startOfDay, long endOfDay);

    @Query("SELECT SUM(durationSeconds) FROM training_sessions WHERE startTime >= :startOfDay AND startTime < :endOfDay")
    LiveData<Integer> getTotalDurationForDay(long startOfDay, long endOfDay);

    @Query("SELECT * FROM training_sessions WHERE trainingType = :type ORDER BY startTime DESC LIMIT 10")
    LiveData<List<TrainingSession>> getRecentSessionsByType(String type);

    @Query("SELECT COUNT(*) FROM training_sessions WHERE completed = 1")
    LiveData<Integer> getCompletedSessionCount();
}
