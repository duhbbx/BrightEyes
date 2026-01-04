package com.brighteyes.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * 双眼融合训练
 * 显示需要双眼协作才能看到的完整图案
 * 弱眼看到一部分（红色），健眼看到另一部分（青色）
 * 只有双眼融合才能识别完整图案
 */
public class DichopticFusionView extends View {

    public interface OnFusionEventListener {
        void onCorrectAnswer();
        void onWrongAnswer();
    }

    private Paint weakEyePaint;      // 弱眼（红色）
    private Paint strongEyePaint;    // 健眼（青色）
    private Paint buttonPaint;
    private Paint textPaint;
    private Paint outlinePaint;

    // 对比度设置
    private float weakEyeContrast = 1.0f;
    private float strongEyeContrast = 0.4f;

    // 图案类型
    private static final int SHAPE_CIRCLE = 0;
    private static final int SHAPE_SQUARE = 1;
    private static final int SHAPE_TRIANGLE = 2;
    private static final int SHAPE_STAR = 3;
    private static final int SHAPE_HEART = 4;
    private static final int SHAPE_DIAMOND = 5;

    private String[] shapeNames = {"圆形", "正方形", "三角形", "星形", "心形", "菱形"};

    private int currentShape = 0;
    private int[] answerOptions = new int[4];
    private RectF[] optionRects = new RectF[4];

    private boolean isActive = false;
    private int correctCount = 0;
    private int totalCount = 0;
    private Random random = new Random();
    private OnFusionEventListener listener;

    public DichopticFusionView(Context context) {
        super(context);
        init();
    }

    public DichopticFusionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DichopticFusionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        weakEyePaint = new Paint();
        weakEyePaint.setAntiAlias(true);
        weakEyePaint.setStyle(Paint.Style.STROKE);
        weakEyePaint.setStrokeWidth(8);

        strongEyePaint = new Paint();
        strongEyePaint.setAntiAlias(true);
        strongEyePaint.setStyle(Paint.Style.STROKE);
        strongEyePaint.setStrokeWidth(8);

        buttonPaint = new Paint();
        buttonPaint.setAntiAlias(true);
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setColor(Color.rgb(60, 60, 60));

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(36);

        outlinePaint = new Paint();
        outlinePaint.setAntiAlias(true);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        outlinePaint.setColor(Color.WHITE);

        updateColors();
        setBackgroundColor(Color.BLACK);
    }

    private void updateColors() {
        int redValue = (int) (255 * weakEyeContrast);
        weakEyePaint.setColor(Color.rgb(redValue, 0, 0));

        int cyanValue = (int) (255 * strongEyeContrast);
        strongEyePaint.setColor(Color.rgb(0, cyanValue, cyanValue));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateOptionRects();
        if (isActive) {
            generateQuestion();
        }
    }

    private void calculateOptionRects() {
        int width = getWidth();
        int height = getHeight();

        // 确保尺寸有效
        if (width <= 0 || height <= 0) {
            return;
        }

        int buttonWidth = 200;
        int buttonHeight = 60;
        int spacing = 30;
        int totalWidth = buttonWidth * 4 + spacing * 3;
        int startX = Math.max(0, (width - totalWidth) / 2);
        int startY = Math.max(0, height - 120);

        for (int i = 0; i < 4; i++) {
            optionRects[i] = new RectF(
                    startX + i * (buttonWidth + spacing),
                    startY,
                    startX + i * (buttonWidth + spacing) + buttonWidth,
                    startY + buttonHeight
            );
        }
    }

    private void generateQuestion() {
        // 随机选择目标图案
        currentShape = random.nextInt(6);

        // 生成答案选项（包含正确答案）
        int correctPosition = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            if (i == correctPosition) {
                answerOptions[i] = currentShape;
            } else {
                int wrongShape;
                do {
                    wrongShape = random.nextInt(6);
                } while (wrongShape == currentShape || containsShape(answerOptions, wrongShape, i));
                answerOptions[i] = wrongShape;
            }
        }

        invalidate();
    }

    private boolean containsShape(int[] arr, int shape, int upTo) {
        for (int i = 0; i < upTo; i++) {
            if (arr[i] == shape) return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 绘制说明
        textPaint.setTextSize(32);
        canvas.drawText("双眼融合：红色(弱眼) + 青色(健眼) = 完整图案", width / 2f, 45, textPaint);
        canvas.drawText("请识别下方显示的是什么图形？", width / 2f, 85, textPaint);

        // 绘制融合图案区域
        float centerX = width / 2f;
        float centerY = height / 2f - 50;
        float shapeSize = Math.min(width, height) / 4f;

        // 绘制背景框
        RectF shapeArea = new RectF(centerX - shapeSize - 20, centerY - shapeSize - 20,
                centerX + shapeSize + 20, centerY + shapeSize + 20);
        canvas.drawRoundRect(shapeArea, 20, 20, outlinePaint);

        // 绘制双眼融合图案
        drawDichopticShape(canvas, currentShape, centerX, centerY, shapeSize);

        // 绘制进度
        textPaint.setTextSize(28);
        String progress = "正确: " + correctCount + "/" + totalCount;
        canvas.drawText(progress, width / 2f, height - 160, textPaint);

        // 绘制选项按钮
        textPaint.setTextSize(30);
        for (int i = 0; i < 4; i++) {
            canvas.drawRoundRect(optionRects[i], 10, 10, buttonPaint);
            canvas.drawRoundRect(optionRects[i], 10, 10, outlinePaint);
            canvas.drawText(shapeNames[answerOptions[i]],
                    optionRects[i].centerX(), optionRects[i].centerY() + 10, textPaint);
        }
    }

    private void drawDichopticShape(Canvas canvas, int shape, float cx, float cy, float size) {
        // 绘制弱眼部分（红色）- 图案的一半
        // 绘制健眼部分（青色）- 图案的另一半
        // 双眼融合后才能看到完整图案

        switch (shape) {
            case SHAPE_CIRCLE:
                // 弱眼看左半圆，健眼看右半圆
                canvas.drawArc(cx - size, cy - size, cx + size, cy + size,
                        90, 180, false, weakEyePaint);
                canvas.drawArc(cx - size, cy - size, cx + size, cy + size,
                        -90, 180, false, strongEyePaint);
                break;

            case SHAPE_SQUARE:
                // 弱眼看上半，健眼看下半
                canvas.drawLine(cx - size, cy - size, cx + size, cy - size, weakEyePaint);
                canvas.drawLine(cx - size, cy - size, cx - size, cy, weakEyePaint);
                canvas.drawLine(cx + size, cy - size, cx + size, cy, weakEyePaint);

                canvas.drawLine(cx - size, cy + size, cx + size, cy + size, strongEyePaint);
                canvas.drawLine(cx - size, cy, cx - size, cy + size, strongEyePaint);
                canvas.drawLine(cx + size, cy, cx + size, cy + size, strongEyePaint);
                break;

            case SHAPE_TRIANGLE:
                // 弱眼看两条边，健眼看一条边
                Path weakPath = new Path();
                weakPath.moveTo(cx, cy - size);
                weakPath.lineTo(cx - size, cy + size);
                weakPath.lineTo(cx, cy + size);
                canvas.drawPath(weakPath, weakEyePaint);

                Path strongPath = new Path();
                strongPath.moveTo(cx, cy - size);
                strongPath.lineTo(cx + size, cy + size);
                strongPath.lineTo(cx, cy + size);
                canvas.drawPath(strongPath, strongEyePaint);
                break;

            case SHAPE_STAR:
                drawStarDichoptic(canvas, cx, cy, size);
                break;

            case SHAPE_HEART:
                drawHeartDichoptic(canvas, cx, cy, size);
                break;

            case SHAPE_DIAMOND:
                // 弱眼看左半，健眼看右半
                canvas.drawLine(cx, cy - size, cx - size, cy, weakEyePaint);
                canvas.drawLine(cx - size, cy, cx, cy + size, weakEyePaint);

                canvas.drawLine(cx, cy - size, cx + size, cy, strongEyePaint);
                canvas.drawLine(cx + size, cy, cx, cy + size, strongEyePaint);
                break;
        }
    }

    private void drawStarDichoptic(Canvas canvas, float cx, float cy, float size) {
        float innerRadius = size * 0.4f;
        double angle = -Math.PI / 2;
        double deltaAngle = Math.PI / 5;

        Path weakPath = new Path();
        Path strongPath = new Path();

        float startX = cx + (float) (size * Math.cos(angle));
        float startY = cy + (float) (size * Math.sin(angle));
        weakPath.moveTo(startX, startY);
        strongPath.moveTo(startX, startY);

        for (int i = 0; i < 5; i++) {
            angle += deltaAngle;
            float innerX = cx + (float) (innerRadius * Math.cos(angle));
            float innerY = cy + (float) (innerRadius * Math.sin(angle));
            angle += deltaAngle;
            float outerX = cx + (float) (size * Math.cos(angle));
            float outerY = cy + (float) (size * Math.sin(angle));

            if (i % 2 == 0) {
                weakPath.lineTo(innerX, innerY);
                weakPath.lineTo(outerX, outerY);
            } else {
                strongPath.lineTo(innerX, innerY);
                strongPath.lineTo(outerX, outerY);
            }
        }

        canvas.drawPath(weakPath, weakEyePaint);
        canvas.drawPath(strongPath, strongEyePaint);
    }

    private void drawHeartDichoptic(Canvas canvas, float cx, float cy, float size) {
        // 左半心形（弱眼）
        Path leftPath = new Path();
        leftPath.moveTo(cx, cy + size * 0.7f);
        leftPath.cubicTo(cx - size, cy, cx - size, cy - size * 0.5f, cx, cy - size * 0.2f);
        canvas.drawPath(leftPath, weakEyePaint);

        // 右半心形（健眼）
        Path rightPath = new Path();
        rightPath.moveTo(cx, cy + size * 0.7f);
        rightPath.cubicTo(cx + size, cy, cx + size, cy - size * 0.5f, cx, cy - size * 0.2f);
        canvas.drawPath(rightPath, strongEyePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isActive) return super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < 4; i++) {
                if (optionRects[i].contains(x, y)) {
                    totalCount++;
                    if (answerOptions[i] == currentShape) {
                        correctCount++;
                        if (listener != null) {
                            listener.onCorrectAnswer();
                        }
                    } else {
                        if (listener != null) {
                            listener.onWrongAnswer();
                        }
                    }
                    generateQuestion();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void startTraining() {
        isActive = true;
        correctCount = 0;
        totalCount = 0;
        // 确保 View 已经布局完成
        if (getWidth() > 0 && getHeight() > 0) {
            calculateOptionRects();
            generateQuestion();
        } else {
            post(() -> {
                calculateOptionRects();
                generateQuestion();
            });
        }
    }

    public void stopTraining() {
        isActive = false;
    }

    public void pauseTraining() {
        isActive = false;
    }

    public void resumeTraining() {
        isActive = true;
    }

    public void setWeakEyeContrast(float contrast) {
        this.weakEyeContrast = Math.max(0.1f, Math.min(1.0f, contrast));
        updateColors();
        invalidate();
    }

    public void setStrongEyeContrast(float contrast) {
        this.strongEyeContrast = Math.max(0.0f, Math.min(1.0f, contrast));
        updateColors();
        invalidate();
    }

    public void setLevel(int level) {
        // 等级越高，健眼对比度越高（更接近弱眼）
        strongEyeContrast = Math.min(0.9f, 0.3f + (level * 0.06f));
        updateColors();
    }

    public void setOnFusionEventListener(OnFusionEventListener listener) {
        this.listener = listener;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
