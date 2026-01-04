package com.brighteyes.app.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.brighteyes.app.R;

/**
 * 背景音乐管理器
 * 用于训练时播放背景音乐
 */
public class BackgroundMusicManager {

    private static BackgroundMusicManager instance;
    private MediaPlayer mediaPlayer;
    private Context context;
    private int currentMusicId = -1;
    private boolean isPlaying = false;
    private float volume = 0.5f;

    // 音乐选项
    public static final int MUSIC_NONE = 0;
    public static final int MUSIC_JINGLE_BELLS = 1;      // 铃儿响叮当
    public static final int MUSIC_ABC = 2;               // ABC
    public static final int MUSIC_EDELWEISS = 3;         // 雪绒花
    public static final int MUSIC_PLEASANT_GOAT = 4;     // 喜羊羊

    private BackgroundMusicManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized BackgroundMusicManager getInstance(Context context) {
        if (instance == null) {
            instance = new BackgroundMusicManager(context);
        }
        return instance;
    }

    private static final String TAG = "BackgroundMusicManager";

    /**
     * 播放指定的背景音乐
     */
    public void playMusic(int musicType) {
        Log.d(TAG, "========= playMusic called =========");
        Log.d(TAG, "Music type: " + musicType + " (" + getMusicName(musicType) + ")");
        Log.d(TAG, "Context: " + (context != null ? context.getClass().getSimpleName() : "NULL"));

        if (musicType == MUSIC_NONE) {
            Log.d(TAG, "Music type is NONE, stopping music");
            stopMusic();
            return;
        }

        int resourceId = getMusicResource(musicType);
        Log.d(TAG, "Resource ID: " + resourceId + " (0x" + Integer.toHexString(resourceId) + ")");

        if (resourceId == 0) {
            Log.e(TAG, "Invalid resource ID for music type: " + musicType);
            return;
        }

        // 如果正在播放同一首歌，不重复播放
        if (currentMusicId == musicType && isPlaying) {
            Log.d(TAG, "Already playing this music, skipping");
            return;
        }

        // 停止当前音乐
        Log.d(TAG, "Stopping current music before playing new one");
        stopMusic();

        try {
            Log.d(TAG, "Creating MediaPlayer...");
            mediaPlayer = MediaPlayer.create(context, resourceId);
            if (mediaPlayer != null) {
                Log.d(TAG, "MediaPlayer created successfully");
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(volume, volume);
                Log.d(TAG, "Volume set to: " + volume);
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                    return false;
                });
                mediaPlayer.setOnPreparedListener(mp -> {
                    Log.d(TAG, "MediaPlayer prepared, duration: " + mp.getDuration() + "ms");
                });
                Log.d(TAG, "Starting playback...");
                mediaPlayer.start();
                currentMusicId = musicType;
                isPlaying = true;
                Log.d(TAG, "===== Music started playing successfully =====");
                Log.d(TAG, "isPlaying: " + mediaPlayer.isPlaying());
            } else {
                Log.e(TAG, "MediaPlayer.create returned null for resource: " + resourceId);
                Log.e(TAG, "This usually means the resource file is missing or corrupted");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception playing music: " + e.getMessage(), e);
        }
    }

    /**
     * 停止音乐
     */
    public void stopMusic() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
        isPlaying = false;
        currentMusicId = -1;
    }

    /**
     * 暂停音乐
     */
    public void pauseMusic() {
        if (mediaPlayer != null && isPlaying) {
            try {
                mediaPlayer.pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 恢复音乐
     */
    public void resumeMusic() {
        if (mediaPlayer != null && currentMusicId != -1) {
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置音量 (0.0 - 1.0)
     */
    public void setVolume(float vol) {
        this.volume = Math.max(0f, Math.min(1f, vol));
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setVolume(volume, volume);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public float getVolume() {
        return volume;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentMusicId() {
        return currentMusicId;
    }

    private int getMusicResource(int musicType) {
        switch (musicType) {
            case MUSIC_JINGLE_BELLS:
                return R.raw.jingle_bells;
            case MUSIC_ABC:
                return R.raw.abc_song;
            case MUSIC_EDELWEISS:
                return R.raw.edelweiss;
            case MUSIC_PLEASANT_GOAT:
                return R.raw.pleasant_goat;
            default:
                return 0;
        }
    }

    public static String getMusicName(int musicType) {
        switch (musicType) {
            case MUSIC_NONE:
                return "无";
            case MUSIC_JINGLE_BELLS:
                return "铃儿响叮当";
            case MUSIC_ABC:
                return "ABC";
            case MUSIC_EDELWEISS:
                return "雪绒花";
            case MUSIC_PLEASANT_GOAT:
                return "喜羊羊";
            default:
                return "未知";
        }
    }
}
