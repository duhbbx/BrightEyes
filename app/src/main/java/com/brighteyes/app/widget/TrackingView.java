package com.brighteyes.app.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * 追踪训练视图
 * 追踪移动目标锻炼眼肌协调
 */
public class TrackingView extends View {

    public interface OnTargetClickListener {
        void onTargetClicked();
    }

    private Paint targetPaint;
    private Paint trailPaint;
    private ValueAnimator animatorX;
    private ValueAnimator animatorY;
    private boolean isAnimating = false;

    private float targetX = 0;
    private float targetY = 0;
    private float targetRadius = 50;
    private int targetColor = Color.YELLOW;

    private Random random = new Random();
    private OnTargetClickListener listener;

    public TrackingView(Context context) {
        super(context);
        init();
    }

    public TrackingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrackingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        targetPaint = new Paint();
        targetPaint.setAntiAlias(true);
        targetPaint.setStyle(Paint.Style.FILL);
        targetPaint.setColor(targetColor);

        trailPaint = new Paint();
        trailPaint.setAntiAlias(true);
        trailPaint.setStyle(Paint.Style.STROKE);
        trailPaint.setStrokeWidth(3);
        trailPaint.setColor(Color.argb(100, 255, 255, 0));

        setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        targetX = w / 2f;
        targetY = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制目标光晕
        targetPaint.setColor(Color.argb(50, 255, 255, 0));
        canvas.drawCircle(targetX, targetY, targetRadius * 1.5f, targetPaint);

        // 绘制目标
        targetPaint.setColor(targetColor);
        canvas.drawCircle(targetX, targetY, targetRadius, targetPaint);

        // 绘制中心点
        targetPaint.setColor(Color.RED);
        canvas.drawCircle(targetX, targetY, targetRadius * 0.3f, targetPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            // 检查是否点击到目标
            float distance = (float) Math.sqrt(
                    Math.pow(touchX - targetX, 2) + Math.pow(touchY - targetY, 2)
            );

            if (distance <= targetRadius * 1.5f) {
                if (listener != null) {
                    listener.onTargetClicked();
                }
                moveToRandomPosition();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    public void startAnimation() {
        if (isAnimating) return;

        isAnimating = true;
        moveToRandomPosition();
    }

    private void moveToRandomPosition() {
        if (!isAnimating) return;

        float newX = targetRadius + random.nextFloat() * (getWidth() - 2 * targetRadius);
        float newY = targetRadius + random.nextFloat() * (getHeight() - 2 * targetRadius);

        if (animatorX != null) animatorX.cancel();
        if (animatorY != null) animatorY.cancel();

        long duration = 1000 + random.nextInt(2000);

        animatorX = ValueAnimator.ofFloat(targetX, newX);
        animatorX.setDuration(duration);
        animatorX.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorX.addUpdateListener(animation -> {
            targetX = (float) animation.getAnimatedValue();
            invalidate();
        });

        animatorY = ValueAnimator.ofFloat(targetY, newY);
        animatorY.setDuration(duration);
        animatorY.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorY.addUpdateListener(animation -> {
            targetY = (float) animation.getAnimatedValue();
        });

        animatorX.start();
        animatorY.start();

        // 移动完成后继续移动
        postDelayed(this::moveToRandomPosition, duration + 500);
    }

    public void stopAnimation() {
        isAnimating = false;
        if (animatorX != null) {
            animatorX.cancel();
            animatorX = null;
        }
        if (animatorY != null) {
            animatorY.cancel();
            animatorY = null;
        }
        removeCallbacks(null);
    }

    public void pauseAnimation() {
        if (animatorX != null && animatorX.isRunning()) {
            animatorX.pause();
        }
        if (animatorY != null && animatorY.isRunning()) {
            animatorY.pause();
        }
    }

    public void resumeAnimation() {
        if (animatorX != null && animatorX.isPaused()) {
            animatorX.resume();
        }
        if (animatorY != null && animatorY.isPaused()) {
            animatorY.resume();
        }
    }

    public void setLevel(int level) {
        // 等级越高，目标越小，移动越快
        targetRadius = Math.max(20, 60 - (level * 4));
        invalidate();
    }

    public void setOnTargetClickListener(OnTargetClickListener listener) {
        this.listener = listener;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
