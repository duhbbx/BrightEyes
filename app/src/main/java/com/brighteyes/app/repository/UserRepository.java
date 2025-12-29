package com.brighteyes.app.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.brighteyes.app.database.AppDatabase;
import com.brighteyes.app.database.UserDao;
import com.brighteyes.app.model.UserProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executor;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void saveUserProfile(UserProfile profile) {
        executor.execute(() -> userDao.insert(profile));
    }

    public void updateUserProfile(UserProfile profile) {
        executor.execute(() -> userDao.update(profile));
    }

    public LiveData<UserProfile> getUserProfile() {
        return userDao.getUserProfile();
    }
}
