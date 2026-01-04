package com.brighteyes.app.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * 红光闪烁训练视图
 * 用于弱视训练中的红光刺激治疗
 */
public class RedFlashView extends View {

    private Paint paint;
    private ValueAnimator animator;
    private int currentAlpha = 255;
    private boolean isFlashing = false;

    // 闪烁频率 (毫秒)
    private long flashDuration = 500;

    // 红光颜色
    private int redColor = Color.RED;

    public RedFlashView(Context context) {
        super(context);
        init();
    }

    public RedFlashView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedFlashView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isFlashing) {
            paint.setColor(redColor);
            paint.setAlpha(currentAlpha);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
    }

    /**
     * 开始红光闪烁
     */
    public void startFlashing() {
        if (isFlashing) return;

        isFlashing = true;

        animator = ValueAnimator.ofInt(255, 0, 255);
        animator.setDuration(flashDuration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            currentAlpha = (int) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    /**
     * 停止红光闪烁
     */
    public void stopFlashing() {
        isFlashing = false;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        currentAlpha = 0;
        invalidate();
    }

    /**
     * 暂停闪烁
     */
    public void pauseFlashing() {
        if (animator != null && animator.isRunning()) {
            animator.pause();
        }
    }

    /**
     * 恢复闪烁
     */
    public void resumeFlashing() {
        if (animator != null && animator.isPaused()) {
            animator.resume();
        }
    }

    /**
     * 设置闪烁频率
     * @param durationMs 一次完整闪烁的时长（毫秒）
     */
    public void setFlashDuration(long durationMs) {
        this.flashDuration = durationMs;
        if (isFlashing) {
            stopFlashing();
            startFlashing();
        }
    }

    /**
     * 设置闪烁等级 (1-10)
     * 等级越高，闪烁越快
     */
    public void setLevel(int level) {
        // 等级1: 1000ms, 等级10: 100ms
        long duration = 1100 - (level * 100L);
        setFlashDuration(Math.max(100, duration));
    }

    /**
     * 设置红光颜色深度
     * @param intensity 0-255, 255为最深红色
     */
    public void setRedIntensity(int intensity) {
        this.redColor = Color.rgb(intensity, 0, 0);
    }

    public boolean isFlashing() {
        return isFlashing;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopFlashing();
    }
}
