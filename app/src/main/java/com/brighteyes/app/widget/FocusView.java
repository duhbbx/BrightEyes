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

/**
 * 聚焦训练视图
 * 远近聚焦训练调节能力
 */
public class FocusView extends View {

    public interface OnFocusCompleteListener {
        void onFocusComplete();
    }

    private Paint circlePaint;
    private Paint textPaint;
    private ValueAnimator scaleAnimator;
    private boolean isAnimating = false;

    private float currentScale = 1.0f;
    private float minScale = 0.2f;
    private float maxScale = 1.5f;
    private boolean isExpanding = true;

    private int circleColor = Color.GREEN;
    private OnFocusCompleteListener listener;

    // 聚焦目标字符
    private String[] targetChars = {"E", "F", "P", "T", "O", "Z", "L", "D", "C"};
    private String currentChar = "E";
    private int charIndex = 0;

    public FocusView(Context context) {
        super(context);
        init();
    }

    public FocusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(4);
        circlePaint.setColor(circleColor);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float baseRadius = Math.min(getWidth(), getHeight()) / 4f;
        float radius = baseRadius * currentScale;

        // 绘制多层同心圆
        circlePaint.setColor(Color.argb(50, 0, 255, 0));
        canvas.drawCircle(centerX, centerY, radius * 1.3f, circlePaint);

        circlePaint.setColor(Color.argb(100, 0, 255, 0));
        canvas.drawCircle(centerX, centerY, radius * 1.1f, circlePaint);

        circlePaint.setColor(circleColor);
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // 绘制中心字符
        textPaint.setTextSize(radius * 0.8f);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = centerY - (fm.ascent + fm.descent) / 2;
        canvas.drawText(currentChar, centerX, textY, textPaint);

        // 绘制提示文字
        textPaint.setTextSize(36);
        textPaint.setColor(Color.GRAY);
        canvas.drawText("盯住中心字母", centerX, getHeight() - 30, textPaint);
        textPaint.setColor(Color.WHITE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (listener != null) {
                listener.onFocusComplete();
            }
            nextChar();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void startAnimation() {
        if (isAnimating) return;

        isAnimating = true;
        startScaleAnimation();
    }

    private void startScaleAnimation() {
        if (!isAnimating) return;

        float startScale = isExpanding ? minScale : maxScale;
        float endScale = isExpanding ? maxScale : minScale;

        scaleAnimator = ValueAnimator.ofFloat(startScale, endScale);
        scaleAnimator.setDuration(3000);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimator.addUpdateListener(animation -> {
            currentScale = (float) animation.getAnimatedValue();
            invalidate();
        });

        scaleAnimator.start();

        // 动画结束后切换方向
        postDelayed(() -> {
            isExpanding = !isExpanding;
            startScaleAnimation();
        }, 3100);
    }

    private void nextChar() {
        charIndex = (charIndex + 1) % targetChars.length;
        currentChar = targetChars[charIndex];
        invalidate();
    }

    public void stopAnimation() {
        isAnimating = false;
        if (scaleAnimator != null) {
            scaleAnimator.cancel();
            scaleAnimator = null;
        }
        removeCallbacks(null);
    }

    public void pauseAnimation() {
        if (scaleAnimator != null && scaleAnimator.isRunning()) {
            scaleAnimator.pause();
        }
    }

    public void resumeAnimation() {
        if (scaleAnimator != null && scaleAnimator.isPaused()) {
            scaleAnimator.resume();
        }
    }

    public void setLevel(int level) {
        // 等级越高，缩放范围越大，速度越快
        minScale = Math.max(0.1f, 0.3f - (level * 0.02f));
        maxScale = Math.min(2.0f, 1.3f + (level * 0.07f));
    }

    public void setOnFocusCompleteListener(OnFocusCompleteListener listener) {
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
