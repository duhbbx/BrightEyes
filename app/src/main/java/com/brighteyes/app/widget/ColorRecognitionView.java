package com.brighteyes.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 色彩识别训练视图
 * 增强色彩辨识能力
 */
public class ColorRecognitionView extends View {

    public interface OnColorMatchListener {
        void onCorrectMatch();
        void onWrongMatch();
    }

    private Paint colorPaint;
    private Paint textPaint;
    private Paint borderPaint;

    private int[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.rgb(255, 165, 0), // Orange
            Color.rgb(128, 0, 128), // Purple
            Color.rgb(255, 192, 203) // Pink
    };

    private String[] colorNames = {
            "红色", "绿色", "蓝色", "黄色",
            "青色", "品红", "橙色", "紫色", "粉色"
    };

    private int targetColorIndex = 0;
    private List<Integer> displayedColors = new ArrayList<>();
    private List<RectF> colorRects = new ArrayList<>();
    private int gridSize = 3;
    private Random random = new Random();
    private OnColorMatchListener listener;
    private boolean isActive = false;

    public ColorRecognitionView(Context context) {
        super(context);
        init();
    }

    public ColorRecognitionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorRecognitionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
        colorPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(60);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setColor(Color.WHITE);

        setBackgroundColor(Color.rgb(30, 30, 30));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateNewRound();
    }

    private void generateNewRound() {
        displayedColors.clear();
        colorRects.clear();

        // 选择目标颜色
        targetColorIndex = random.nextInt(colors.length);

        // 生成显示的颜色（包含目标颜色）
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < colors.length; i++) {
            availableIndices.add(i);
        }
        Collections.shuffle(availableIndices);

        int totalCells = gridSize * gridSize;
        for (int i = 0; i < totalCells - 1 && i < availableIndices.size(); i++) {
            if (availableIndices.get(i) != targetColorIndex) {
                displayedColors.add(availableIndices.get(i));
            }
        }

        // 确保目标颜色在列表中
        while (displayedColors.size() < totalCells - 1) {
            int idx = random.nextInt(colors.length);
            if (idx != targetColorIndex && !displayedColors.contains(idx)) {
                displayedColors.add(idx);
            }
        }

        // 随机位置插入目标颜色
        int targetPosition = random.nextInt(totalCells);
        displayedColors.add(targetPosition, targetColorIndex);

        // 计算方块位置
        calculateRects();
        invalidate();
    }

    private void calculateRects() {
        colorRects.clear();

        int width = getWidth();
        int height = getHeight();
        int topMargin = 100; // 给顶部提示文字留空间
        int bottomMargin = 20;

        int availableHeight = height - topMargin - bottomMargin;
        int cellSize = Math.min((width - 40) / gridSize, availableHeight / gridSize) - 10;
        int startX = (width - (cellSize + 10) * gridSize) / 2;
        int startY = topMargin + (availableHeight - (cellSize + 10) * gridSize) / 2;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                float left = startX + col * (cellSize + 10);
                float top = startY + row * (cellSize + 10);
                colorRects.add(new RectF(left, top, left + cellSize, top + cellSize));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制提示文字
        textPaint.setTextSize(50);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("请点击: " + colorNames[targetColorIndex], getWidth() / 2f, 80, textPaint);

        // 绘制目标颜色示例
        colorPaint.setColor(colors[targetColorIndex]);
        canvas.drawRoundRect(getWidth() / 2f - 40, 100, getWidth() / 2f + 40, 140, 10, 10, colorPaint);

        // 绘制颜色方块
        for (int i = 0; i < colorRects.size() && i < displayedColors.size(); i++) {
            RectF rect = colorRects.get(i);
            colorPaint.setColor(colors[displayedColors.get(i)]);
            canvas.drawRoundRect(rect, 20, 20, colorPaint);
            canvas.drawRoundRect(rect, 20, 20, borderPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isActive) return super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < colorRects.size(); i++) {
                if (colorRects.get(i).contains(x, y)) {
                    if (displayedColors.get(i) == targetColorIndex) {
                        if (listener != null) {
                            listener.onCorrectMatch();
                        }
                    } else {
                        if (listener != null) {
                            listener.onWrongMatch();
                        }
                    }
                    generateNewRound();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void startTraining() {
        isActive = true;
        generateNewRound();
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

    public void setLevel(int level) {
        // 等级越高，格子越多
        gridSize = Math.min(5, 2 + (level / 3));
        if (getWidth() > 0) {
            calculateRects();
            generateNewRound();
        }
    }

    public void setOnColorMatchListener(OnColorMatchListener listener) {
        this.listener = listener;
    }

    public boolean isActive() {
        return isActive;
    }
}
