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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 对比敏感度训练 (Contrast Sensitivity Function)
 *
 * 功能：
 * 1. 显示不同空间频率和对比度的光栅
 * 2. 用户判断光栅方向（水平/垂直/左斜/右斜）
 * 3. 难度根据正确率自适应调整
 * 4. 记录并生成 CSF 曲线数据
 *
 * 医学意义：
 * - 真实反映弱视改善程度
 * - 比单纯视力表更科学
 */
public class CSFTrainingView extends View {

    public interface OnCSFEventListener {
        void onCorrectAnswer(int frequency, float contrast);
        void onWrongAnswer(int frequency, float contrast);
        void onTestComplete(float[] csfCurve);
    }

    private Paint gratingPaint;
    private Paint bgPaint;
    private Paint textPaint;
    private Paint buttonPaint;
    private Paint buttonTextPaint;
    private Paint curvePaint;
    private Paint axisPaint;

    // 空间频率等级 (cycles per degree, 简化为像素周期)
    private static final int[] FREQUENCIES = {2, 4, 6, 8, 12, 16, 24, 32};
    private static final String[] FREQ_LABELS = {"0.5", "1", "2", "4", "6", "8", "12", "16"};

    // 对比度等级 (0.0 - 1.0)
    private static final float[] CONTRAST_LEVELS = {1.0f, 0.5f, 0.25f, 0.125f, 0.0625f, 0.03125f, 0.015625f};

    // 方向: 0=垂直, 1=水平, 2=左斜45°, 3=右斜45°
    private static final int DIR_VERTICAL = 0;
    private static final int DIR_HORIZONTAL = 1;
    private static final int DIR_LEFT_DIAGONAL = 2;
    private static final int DIR_RIGHT_DIAGONAL = 3;
    private String[] directionNames = {"垂直", "水平", "左斜", "右斜"};

    // 当前测试参数
    private int currentFrequencyIndex = 0;
    private int currentContrastIndex = 0;
    private int currentDirection = 0;

    // 自适应参数
    private int consecutiveCorrect = 0;
    private int consecutiveWrong = 0;
    private static final int CORRECT_TO_INCREASE = 2;  // 连续2次正确提高难度
    private static final int WRONG_TO_DECREASE = 1;    // 连续1次错误降低难度

    // CSF 曲线数据 (每个频率的阈值对比度)
    private float[] csfThresholds = new float[FREQUENCIES.length];
    private int[] testCountPerFreq = new int[FREQUENCIES.length];

    // 按钮区域
    private RectF[] answerButtons = new RectF[4];
    private RectF gratingArea;

    // 游戏状态
    private boolean isActive = false;
    private boolean showingResult = false;
    private boolean lastAnswerCorrect = false;
    private int totalCorrect = 0;
    private int totalTrials = 0;
    private Random random = new Random();
    private OnCSFEventListener listener;

    // 显示模式
    private boolean showCurve = false;

    public CSFTrainingView(Context context) {
        super(context);
        init();
    }

    public CSFTrainingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CSFTrainingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gratingPaint = new Paint();
        gratingPaint.setAntiAlias(true);
        gratingPaint.setStyle(Paint.Style.FILL);

        bgPaint = new Paint();
        bgPaint.setColor(Color.rgb(128, 128, 128)); // 中灰色背景
        bgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(32);

        buttonPaint = new Paint();
        buttonPaint.setAntiAlias(true);
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setColor(Color.rgb(60, 60, 80));

        buttonTextPaint = new Paint();
        buttonTextPaint.setAntiAlias(true);
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTextSize(36);

        curvePaint = new Paint();
        curvePaint.setAntiAlias(true);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeWidth(4);
        curvePaint.setColor(Color.rgb(100, 200, 100));

        axisPaint = new Paint();
        axisPaint.setAntiAlias(true);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(2);
        axisPaint.setColor(Color.WHITE);

        setBackgroundColor(Color.rgb(40, 40, 40));

        // 初始化阈值
        for (int i = 0; i < csfThresholds.length; i++) {
            csfThresholds[i] = -1; // -1 表示未测试
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateLayout();
    }

    private void calculateLayout() {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        // 光栅显示区域 (左侧)
        int gratingSize = Math.min(width / 2 - 100, height - 250);
        gratingSize = Math.max(200, gratingSize);
        int gratingCenterX = width / 3;
        int gratingCenterY = height / 2 - 30;
        gratingArea = new RectF(
                gratingCenterX - gratingSize / 2f,
                gratingCenterY - gratingSize / 2f,
                gratingCenterX + gratingSize / 2f,
                gratingCenterY + gratingSize / 2f
        );

        // 方向按钮 (右侧 2x2 排列)
        int buttonSize = 120;
        int buttonSpacing = 20;
        int buttonsStartX = width * 2 / 3 - buttonSize - buttonSpacing / 2;
        int buttonsStartY = height / 2 - buttonSize - buttonSpacing / 2;

        answerButtons[DIR_VERTICAL] = new RectF(
                buttonsStartX, buttonsStartY,
                buttonsStartX + buttonSize, buttonsStartY + buttonSize);
        answerButtons[DIR_HORIZONTAL] = new RectF(
                buttonsStartX + buttonSize + buttonSpacing, buttonsStartY,
                buttonsStartX + buttonSize * 2 + buttonSpacing, buttonsStartY + buttonSize);
        answerButtons[DIR_LEFT_DIAGONAL] = new RectF(
                buttonsStartX, buttonsStartY + buttonSize + buttonSpacing,
                buttonsStartX + buttonSize, buttonsStartY + buttonSize * 2 + buttonSpacing);
        answerButtons[DIR_RIGHT_DIAGONAL] = new RectF(
                buttonsStartX + buttonSize + buttonSpacing, buttonsStartY + buttonSize + buttonSpacing,
                buttonsStartX + buttonSize * 2 + buttonSpacing, buttonsStartY + buttonSize * 2 + buttonSpacing);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (showCurve) {
            drawCSFCurve(canvas);
            return;
        }

        // 标题
        textPaint.setTextSize(28);
        canvas.drawText("对比敏感度训练 - 判断光栅方向", width / 2f, 35, textPaint);

        // 当前参数信息
        textPaint.setTextSize(24);
        String freqInfo = "空间频率: " + FREQ_LABELS[currentFrequencyIndex] + " cpd";
        String contrastInfo = "对比度: " + String.format("%.1f%%", CONTRAST_LEVELS[currentContrastIndex] * 100);
        canvas.drawText(freqInfo + "  |  " + contrastInfo, width / 2f, 70, textPaint);

        // 进度信息
        String progress = "正确: " + totalCorrect + "/" + totalTrials +
                "  正确率: " + (totalTrials > 0 ? String.format("%.0f%%", totalCorrect * 100f / totalTrials) : "--");
        canvas.drawText(progress, width / 2f, height - 20, textPaint);

        // 绘制光栅
        if (gratingArea != null) {
            drawGrating(canvas);
        }

        // 绘制方向按钮
        drawDirectionButtons(canvas);

        // 显示反馈
        if (showingResult) {
            textPaint.setTextSize(40);
            textPaint.setColor(lastAnswerCorrect ? Color.GREEN : Color.RED);
            canvas.drawText(lastAnswerCorrect ? "正确!" : "错误", width / 2f, height - 60, textPaint);
            textPaint.setColor(Color.WHITE);
        }
    }

    private void drawGrating(Canvas canvas) {
        if (gratingArea == null) return;

        float cx = gratingArea.centerX();
        float cy = gratingArea.centerY();
        float radius = gratingArea.width() / 2;

        // 绘制圆形背景（中灰色）
        canvas.drawCircle(cx, cy, radius, bgPaint);

        // 绘制光栅条纹
        float contrast = CONTRAST_LEVELS[currentContrastIndex];
        int frequency = FREQUENCIES[currentFrequencyIndex];
        float stripeWidth = radius * 2 / frequency;

        int lightGray = (int) (128 + 127 * contrast);
        int darkGray = (int) (128 - 127 * contrast);

        canvas.save();

        // 裁剪为圆形
        Path clipPath = new Path();
        clipPath.addCircle(cx, cy, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        // 根据方向绘制条纹
        switch (currentDirection) {
            case DIR_VERTICAL:
                drawVerticalStripes(canvas, cx, cy, radius, stripeWidth, lightGray, darkGray);
                break;
            case DIR_HORIZONTAL:
                drawHorizontalStripes(canvas, cx, cy, radius, stripeWidth, lightGray, darkGray);
                break;
            case DIR_LEFT_DIAGONAL:
                canvas.rotate(-45, cx, cy);
                drawVerticalStripes(canvas, cx, cy, radius * 1.5f, stripeWidth, lightGray, darkGray);
                break;
            case DIR_RIGHT_DIAGONAL:
                canvas.rotate(45, cx, cy);
                drawVerticalStripes(canvas, cx, cy, radius * 1.5f, stripeWidth, lightGray, darkGray);
                break;
        }

        canvas.restore();

        // 绘制边框
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, radius, borderPaint);
    }

    private void drawVerticalStripes(Canvas canvas, float cx, float cy, float radius,
                                     float stripeWidth, int light, int dark) {
        float startX = cx - radius;
        float endX = cx + radius;
        boolean isLight = true;

        for (float x = startX; x < endX; x += stripeWidth) {
            gratingPaint.setColor(isLight ? Color.rgb(light, light, light) : Color.rgb(dark, dark, dark));
            canvas.drawRect(x, cy - radius, x + stripeWidth, cy + radius, gratingPaint);
            isLight = !isLight;
        }
    }

    private void drawHorizontalStripes(Canvas canvas, float cx, float cy, float radius,
                                       float stripeWidth, int light, int dark) {
        float startY = cy - radius;
        float endY = cy + radius;
        boolean isLight = true;

        for (float y = startY; y < endY; y += stripeWidth) {
            gratingPaint.setColor(isLight ? Color.rgb(light, light, light) : Color.rgb(dark, dark, dark));
            canvas.drawRect(cx - radius, y, cx + radius, y + stripeWidth, gratingPaint);
            isLight = !isLight;
        }
    }

    private void drawDirectionButtons(Canvas canvas) {
        String[] buttonLabels = {"|||", "≡", "\\\\\\", "///"};
        String[] buttonSubLabels = {"垂直", "水平", "左斜", "右斜"};

        for (int i = 0; i < 4; i++) {
            if (answerButtons[i] == null) continue;

            // 按钮背景
            canvas.drawRoundRect(answerButtons[i], 15, 15, buttonPaint);

            // 按钮图标
            buttonTextPaint.setTextSize(36);
            canvas.drawText(buttonLabels[i],
                    answerButtons[i].centerX(),
                    answerButtons[i].centerY(), buttonTextPaint);

            // 按钮文字
            buttonTextPaint.setTextSize(20);
            canvas.drawText(buttonSubLabels[i],
                    answerButtons[i].centerX(),
                    answerButtons[i].centerY() + 35, buttonTextPaint);
        }
    }

    private void drawCSFCurve(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // 标题
        textPaint.setTextSize(32);
        canvas.drawText("对比敏感度曲线 (CSF)", width / 2f, 50, textPaint);

        // 绘制坐标轴
        float chartLeft = 100;
        float chartRight = width - 100;
        float chartTop = 120;
        float chartBottom = height - 100;
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;

        // X轴
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);
        // Y轴
        canvas.drawLine(chartLeft, chartBottom, chartLeft, chartTop, axisPaint);

        // X轴标签 (空间频率)
        textPaint.setTextSize(18);
        for (int i = 0; i < FREQ_LABELS.length; i++) {
            float x = chartLeft + (i + 0.5f) * chartWidth / FREQ_LABELS.length;
            canvas.drawText(FREQ_LABELS[i], x, chartBottom + 25, textPaint);
        }
        textPaint.setTextSize(20);
        canvas.drawText("空间频率 (cpd)", (chartLeft + chartRight) / 2, chartBottom + 55, textPaint);

        // Y轴标签 (对比敏感度)
        textPaint.setTextSize(18);
        canvas.save();
        canvas.rotate(-90, chartLeft - 50, (chartTop + chartBottom) / 2);
        canvas.drawText("对比敏感度", chartLeft - 50, (chartTop + chartBottom) / 2, textPaint);
        canvas.restore();

        // 绘制曲线
        Path curvePath = new Path();
        boolean pathStarted = false;

        for (int i = 0; i < csfThresholds.length; i++) {
            if (csfThresholds[i] > 0) {
                float x = chartLeft + (i + 0.5f) * chartWidth / FREQ_LABELS.length;
                // 对比敏感度 = 1 / 阈值对比度
                float sensitivity = 1f / csfThresholds[i];
                // 归一化到图表高度 (log scale)
                float normalizedY = (float) (Math.log10(sensitivity) / Math.log10(100));
                normalizedY = Math.max(0, Math.min(1, normalizedY));
                float y = chartBottom - normalizedY * chartHeight;

                if (!pathStarted) {
                    curvePath.moveTo(x, y);
                    pathStarted = true;
                } else {
                    curvePath.lineTo(x, y);
                }

                // 绘制数据点
                canvas.drawCircle(x, y, 8, curvePaint);
            }
        }

        if (pathStarted) {
            canvas.drawPath(curvePath, curvePaint);
        }

        // 显示说明
        textPaint.setTextSize(22);
        canvas.drawText("曲线越高表示对比敏感度越好", width / 2f, height - 30, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isActive || showingResult) return super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < 4; i++) {
                if (answerButtons[i] != null && answerButtons[i].contains(x, y)) {
                    checkAnswer(i);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void checkAnswer(int selectedDirection) {
        totalTrials++;
        testCountPerFreq[currentFrequencyIndex]++;

        boolean correct = (selectedDirection == currentDirection);
        lastAnswerCorrect = correct;
        showingResult = true;

        if (correct) {
            totalCorrect++;
            consecutiveCorrect++;
            consecutiveWrong = 0;

            if (listener != null) {
                listener.onCorrectAnswer(FREQUENCIES[currentFrequencyIndex],
                        CONTRAST_LEVELS[currentContrastIndex]);
            }

            // 连续正确，提高难度（降低对比度）
            if (consecutiveCorrect >= CORRECT_TO_INCREASE) {
                consecutiveCorrect = 0;
                if (currentContrastIndex < CONTRAST_LEVELS.length - 1) {
                    currentContrastIndex++;
                } else {
                    // 记录当前频率的阈值
                    recordThreshold();
                    // 切换到下一个频率
                    nextFrequency();
                }
            }
        } else {
            consecutiveWrong++;
            consecutiveCorrect = 0;

            if (listener != null) {
                listener.onWrongAnswer(FREQUENCIES[currentFrequencyIndex],
                        CONTRAST_LEVELS[currentContrastIndex]);
            }

            // 连续错误，降低难度（提高对比度）
            if (consecutiveWrong >= WRONG_TO_DECREASE) {
                consecutiveWrong = 0;
                if (currentContrastIndex > 0) {
                    currentContrastIndex--;
                } else {
                    // 记录阈值并切换频率
                    recordThreshold();
                    nextFrequency();
                }
            }
        }

        invalidate();

        // 延迟后显示下一题
        postDelayed(() -> {
            showingResult = false;
            generateNewTrial();
        }, 800);
    }

    private void recordThreshold() {
        // 记录当前频率的对比度阈值
        csfThresholds[currentFrequencyIndex] = CONTRAST_LEVELS[currentContrastIndex];
    }

    private void nextFrequency() {
        currentFrequencyIndex++;
        currentContrastIndex = 2; // 重置到中间对比度
        consecutiveCorrect = 0;
        consecutiveWrong = 0;

        if (currentFrequencyIndex >= FREQUENCIES.length) {
            // 测试完成
            completeTest();
        }
    }

    private void completeTest() {
        isActive = false;
        showCurve = true;

        if (listener != null) {
            listener.onTestComplete(csfThresholds.clone());
        }

        invalidate();
    }

    private void generateNewTrial() {
        if (!isActive) return;

        // 随机选择方向
        currentDirection = random.nextInt(4);
        invalidate();
    }

    public void startTraining() {
        isActive = true;
        showCurve = false;
        totalCorrect = 0;
        totalTrials = 0;
        currentFrequencyIndex = 0;
        currentContrastIndex = 2; // 从中间对比度开始
        consecutiveCorrect = 0;
        consecutiveWrong = 0;

        // 重置阈值
        for (int i = 0; i < csfThresholds.length; i++) {
            csfThresholds[i] = -1;
            testCountPerFreq[i] = 0;
        }

        if (getWidth() > 0 && getHeight() > 0) {
            calculateLayout();
            generateNewTrial();
        } else {
            post(() -> {
                calculateLayout();
                generateNewTrial();
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

    public void showCSFCurve() {
        showCurve = true;
        invalidate();
    }

    public void hideCSFCurve() {
        showCurve = false;
        invalidate();
    }

    public void setOnCSFEventListener(OnCSFEventListener listener) {
        this.listener = listener;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getTotalCorrect() {
        return totalCorrect;
    }

    public int getTotalTrials() {
        return totalTrials;
    }

    public float[] getCSFThresholds() {
        return csfThresholds.clone();
    }
}
