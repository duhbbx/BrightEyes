package com.brighteyes.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey
    private int id = 1;

    private String name;
    private int age;
    private String affectedEye;
    private int dailyTrainingGoalMinutes;
    private int totalTrainingMinutes;
    private int consecutiveDays;
    private long lastTrainingDate;

    public UserProfile() {
        this.dailyTrainingGoalMinutes = 30;
        this.totalTrainingMinutes = 0;
        this.consecutiveDays = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAffectedEye() {
        return affectedEye;
    }

    public void setAffectedEye(String affectedEye) {
        this.affectedEye = affectedEye;
    }

    public int getDailyTrainingGoalMinutes() {
        return dailyTrainingGoalMinutes;
    }

    public void setDailyTrainingGoalMinutes(int dailyTrainingGoalMinutes) {
        this.dailyTrainingGoalMinutes = dailyTrainingGoalMinutes;
    }

    public int getTotalTrainingMinutes() {
        return totalTrainingMinutes;
    }

    public void setTotalTrainingMinutes(int totalTrainingMinutes) {
        this.totalTrainingMinutes = totalTrainingMinutes;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public long getLastTrainingDate() {
        return lastTrainingDate;
    }

    public void setLastTrainingDate(long lastTrainingDate) {
        this.lastTrainingDate = lastTrainingDate;
    }
}
