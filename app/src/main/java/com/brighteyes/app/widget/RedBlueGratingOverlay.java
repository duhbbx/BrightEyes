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
 * 红蓝光栅叠加视图
 * 用于在视频上方显示滚动的红蓝相间条纹
 */
public class RedBlueGratingOverlay extends View {

    private Paint redPaint;
    private Paint bluePaint;
    private ValueAnimator animator;
    private float offset = 0;
    private boolean isAnimating = false;

    // 条纹宽度
    private int stripeWidth = 20;
    // 透明度 (0-255)
    private int stripeAlpha = 100;
    // 是否垂直条纹
    private boolean isVertical = true;
    // 动画时长
    private long animationDuration = 2000;

    public RedBlueGratingOverlay(Context context) {
        super(context);
        init();
    }

    public RedBlueGratingOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedBlueGratingOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        redPaint = new Paint();
        redPaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.RED);
        redPaint.setAlpha(stripeAlpha);

        bluePaint = new Paint();
        bluePaint.setStyle(Paint.Style.FILL);
        bluePaint.setColor(Color.BLUE);
        bluePaint.setAlpha(stripeAlpha);

        // 设置透明背景
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (isVertical) {
            drawVerticalStripes(canvas, width, height);
        } else {
            drawHorizontalStripes(canvas, width, height);
        }
    }

    private void drawVerticalStripes(Canvas canvas, int width, int height) {
        int stripeCount = (width / stripeWidth) + 4;
        float startX = -stripeWidth * 2 + (offset % (stripeWidth * 2));

        for (int i = 0; i < stripeCount; i++) {
            Paint paint = (i % 2 == 0) ? redPaint : bluePaint;
            float left = startX + (i * stripeWidth);
            canvas.drawRect(left, 0, left + stripeWidth, height, paint);
        }
    }

    private void drawHorizontalStripes(Canvas canvas, int width, int height) {
        int stripeCount = (height / stripeWidth) + 4;
        float startY = -stripeWidth * 2 + (offset % (stripeWidth * 2));

        for (int i = 0; i < stripeCount; i++) {
            Paint paint = (i % 2 == 0) ? redPaint : bluePaint;
            float top = startY + (i * stripeWidth);
            canvas.drawRect(0, top, width, top + stripeWidth, paint);
        }
    }

    public void startAnimation() {
        if (isAnimating) return;

        isAnimating = true;
        setVisibility(VISIBLE);

        animator = ValueAnimator.ofFloat(0, stripeWidth * 2);
        animator.setDuration(animationDuration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            offset = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void stopAnimation() {
        isAnimating = false;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        setVisibility(GONE);
    }

    public void pauseAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.pause();
        }
    }

    public void resumeAnimation() {
        if (animator != null && animator.isPaused()) {
            animator.resume();
        }
    }

    /**
     * 设置条纹宽度
     */
    public void setStripeWidth(int width) {
        this.stripeWidth = width;
        invalidate();
    }

    /**
     * 设置透明度 (0-255)
     */
    public void setStripeAlpha(int alpha) {
        this.stripeAlpha = alpha;
        redPaint.setAlpha(alpha);
        bluePaint.setAlpha(alpha);
        invalidate();
    }

    /**
     * 设置是否垂直条纹
     */
    public void setVertical(boolean vertical) {
        this.isVertical = vertical;
        invalidate();
    }

    /**
     * 切换条纹方向
     */
    public void toggleOrientation() {
        isVertical = !isVertical;
        invalidate();
    }

    /**
     * 设置滚动速度
     * @param durationMs 完成一个周期的时间（毫秒）
     */
    public void setScrollSpeed(long durationMs) {
        this.animationDuration = durationMs;
        if (isAnimating) {
            stopAnimation();
            startAnimation();
        }
    }

    /**
     * 设置条纹颜色
     */
    public void setStripeColors(int color1, int color2) {
        redPaint.setColor(color1);
        redPaint.setAlpha(stripeAlpha);
        bluePaint.setColor(color2);
        bluePaint.setAlpha(stripeAlpha);
        invalidate();
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
