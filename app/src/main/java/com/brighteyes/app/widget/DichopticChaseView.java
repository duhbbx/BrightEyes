package com.brighteyes.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 双眼对抗追逐游戏
 * 玩家角色（红色-弱眼可见）追逐目标
 * 障碍物（青色-健眼可见）需要躲避
 * 必须双眼协作才能成功
 */
public class DichopticChaseView extends View {

    public interface OnGameEventListener {
        void onTargetCaught();
        void onObstacleHit();
        void onGameOver(int score);
    }

    private Paint weakEyePaint;      // 弱眼（红色）
    private Paint strongEyePaint;    // 健眼（青色）
    private Paint playerPaint;
    private Paint targetPaint;
    private Paint textPaint;

    // 对比度设置
    private float weakEyeContrast = 1.0f;
    private float strongEyeContrast = 0.4f;

    // 玩家
    private float playerX, playerY;
    private float playerSize = 60;

    // 目标（弱眼可见）
    private List<GameObject> targets = new ArrayList<>();

    // 障碍物（健眼可见）
    private List<GameObject> obstacles = new ArrayList<>();

    // 游戏状态
    private boolean isActive = false;
    private int score = 0;
    private int lives = 3;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private OnGameEventListener listener;

    private Runnable gameLoop;
    private long lastSpawnTime = 0;
    private long spawnInterval = 2000;

    public DichopticChaseView(Context context) {
        super(context);
        init();
    }

    public DichopticChaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DichopticChaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        weakEyePaint = new Paint();
        weakEyePaint.setAntiAlias(true);
        weakEyePaint.setStyle(Paint.Style.FILL);

        strongEyePaint = new Paint();
        strongEyePaint.setAntiAlias(true);
        strongEyePaint.setStyle(Paint.Style.FILL);

        playerPaint = new Paint();
        playerPaint.setAntiAlias(true);
        playerPaint.setStyle(Paint.Style.FILL);

        targetPaint = new Paint();
        targetPaint.setAntiAlias(true);
        targetPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(36);

        updateColors();
        setBackgroundColor(Color.BLACK);

        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isActive) {
                    updateGame();
                    invalidate();
                    handler.postDelayed(this, 33); // ~30 FPS
                }
            }
        };
    }

    private void updateColors() {
        int redValue = (int) (255 * weakEyeContrast);
        weakEyePaint.setColor(Color.rgb(redValue, 0, 0));
        playerPaint.setColor(Color.rgb(redValue, 0, 0));
        targetPaint.setColor(Color.rgb(redValue, (int)(50 * weakEyeContrast), 0));

        int cyanValue = (int) (255 * strongEyeContrast);
        strongEyePaint.setColor(Color.rgb(0, cyanValue, cyanValue));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        playerX = w / 2f;
        playerY = h - 150;
    }

    private void updateGame() {
        long currentTime = System.currentTimeMillis();

        // 生成新物体
        if (currentTime - lastSpawnTime > spawnInterval) {
            spawnObjects();
            lastSpawnTime = currentTime;
        }

        // 移动物体
        float speed = 5 + (score / 10);
        for (GameObject obj : targets) {
            obj.y += speed;
        }
        for (GameObject obj : obstacles) {
            obj.y += speed * 0.8f;
        }

        // 检测碰撞
        checkCollisions();

        // 移除超出屏幕的物体
        removeOffscreenObjects();
    }

    private void spawnObjects() {
        int width = getWidth();
        int height = getHeight();
        if (width < 150 || height < 150) return;

        // 生成目标（弱眼可见）
        if (random.nextFloat() < 0.6f) {
            int rangeX = Math.max(1, width - 80);
            float x = random.nextInt(rangeX) + 40;
            targets.add(new GameObject(x, -50, 40, true));
        }

        // 生成障碍物（健眼可见）
        if (random.nextFloat() < 0.4f) {
            int rangeX = Math.max(1, width - 100);
            float x = random.nextInt(rangeX) + 50;
            obstacles.add(new GameObject(x, -50, 50, false));
        }
    }

    private void checkCollisions() {
        RectF playerRect = new RectF(
                playerX - playerSize / 2, playerY - playerSize / 2,
                playerX + playerSize / 2, playerY + playerSize / 2
        );

        // 检测目标碰撞
        Iterator<GameObject> targetIt = targets.iterator();
        while (targetIt.hasNext()) {
            GameObject target = targetIt.next();
            if (playerRect.intersect(target.getRect())) {
                targetIt.remove();
                score += 10;
                if (listener != null) {
                    listener.onTargetCaught();
                }
            }
        }

        // 检测障碍物碰撞
        Iterator<GameObject> obstacleIt = obstacles.iterator();
        while (obstacleIt.hasNext()) {
            GameObject obstacle = obstacleIt.next();
            RectF obstacleRect = obstacle.getRect();
            if (RectF.intersects(playerRect, obstacleRect)) {
                obstacleIt.remove();
                lives--;
                if (listener != null) {
                    listener.onObstacleHit();
                }
                if (lives <= 0) {
                    gameOver();
                }
            }
        }
    }

    private void removeOffscreenObjects() {
        int height = getHeight();
        targets.removeIf(obj -> obj.y > height + 50);
        obstacles.removeIf(obj -> obj.y > height + 50);
    }

    private void gameOver() {
        isActive = false;
        if (listener != null) {
            listener.onGameOver(score);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制说明
        textPaint.setTextSize(32);
        canvas.drawText("红色角色(弱眼) 追逐红色星星 | 躲避青色障碍(健眼)",
                getWidth() / 2f, 40, textPaint);

        // 绘制目标（红色-弱眼可见）
        for (GameObject target : targets) {
            drawStar(canvas, target.x, target.y, target.size, targetPaint);
        }

        // 绘制障碍物（青色-健眼可见）
        for (GameObject obstacle : obstacles) {
            canvas.drawRect(obstacle.getRect(), strongEyePaint);
        }

        // 绘制玩家（红色-弱眼可见）
        canvas.drawCircle(playerX, playerY, playerSize / 2, playerPaint);
        // 绘制玩家眼睛
        Paint eyePaint = new Paint();
        eyePaint.setColor(Color.WHITE);
        canvas.drawCircle(playerX - 10, playerY - 5, 8, eyePaint);
        canvas.drawCircle(playerX + 10, playerY - 5, 8, eyePaint);
        eyePaint.setColor(Color.BLACK);
        canvas.drawCircle(playerX - 10, playerY - 5, 4, eyePaint);
        canvas.drawCircle(playerX + 10, playerY - 5, 4, eyePaint);

        // 绘制UI
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("得分: " + score, 30, getHeight() - 30, textPaint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("生命: " + lives, getWidth() - 30, getHeight() - 30, textPaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float size, Paint paint) {
        android.graphics.Path path = new android.graphics.Path();
        float innerRadius = size * 0.4f;
        double angle = -Math.PI / 2;
        double deltaAngle = Math.PI / 5;

        path.moveTo(cx + (float) (size * Math.cos(angle)),
                cy + (float) (size * Math.sin(angle)));

        for (int i = 0; i < 5; i++) {
            angle += deltaAngle;
            path.lineTo(cx + (float) (innerRadius * Math.cos(angle)),
                    cy + (float) (innerRadius * Math.sin(angle)));
            angle += deltaAngle;
            path.lineTo(cx + (float) (size * Math.cos(angle)),
                    cy + (float) (size * Math.sin(angle)));
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isActive) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // 玩家跟随触摸移动
                playerX = Math.max(playerSize / 2, Math.min(getWidth() - playerSize / 2, event.getX()));
                playerY = Math.max(playerSize / 2, Math.min(getHeight() - playerSize / 2, event.getY()));
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void startGame() {
        isActive = true;
        score = 0;
        lives = 3;
        targets.clear();
        obstacles.clear();
        lastSpawnTime = System.currentTimeMillis();
        handler.post(gameLoop);
    }

    public void stopGame() {
        isActive = false;
        handler.removeCallbacks(gameLoop);
    }

    public void pauseGame() {
        isActive = false;
        handler.removeCallbacks(gameLoop);
    }

    public void resumeGame() {
        if (!isActive) {
            isActive = true;
            handler.post(gameLoop);
        }
    }

    public void setWeakEyeContrast(float contrast) {
        this.weakEyeContrast = Math.max(0.1f, Math.min(1.0f, contrast));
        updateColors();
    }

    public void setStrongEyeContrast(float contrast) {
        this.strongEyeContrast = Math.max(0.0f, Math.min(1.0f, contrast));
        updateColors();
    }

    public void setLevel(int level) {
        spawnInterval = Math.max(500, 2500 - (level * 200));
        strongEyeContrast = Math.min(0.8f, 0.3f + (level * 0.05f));
        updateColors();
    }

    public void setOnGameEventListener(OnGameEventListener listener) {
        this.listener = listener;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getScore() {
        return score;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGame();
    }

    private static class GameObject {
        float x, y, size;
        boolean isTarget;

        GameObject(float x, float y, float size, boolean isTarget) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.isTarget = isTarget;
        }

        RectF getRect() {
            return new RectF(x - size, y - size, x + size, y + size);
        }
    }
}
