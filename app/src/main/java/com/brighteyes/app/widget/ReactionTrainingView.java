package com.brighteyes.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 视觉注意力与反应时间训练
 *
 * 功能：
 * 1. 快速目标识别 - 在多个干扰项中找到目标
 * 2. 短时呈现刺激 - 目标快速闪现，锻炼视觉处理速度
 * 3. 反应时间统计 - 记录并分析反应时间
 *
 * 训练模式：
 * - 简单模式：找到指定颜色的圆形
 * - 中等模式：找到指定形状
 * - 困难模式：找到指定颜色+形状的组合
 */
public class ReactionTrainingView extends View {

    public interface OnReactionEventListener {
        void onCorrectResponse(long reactionTimeMs);
        void onWrongResponse();
        void onMissed();
        void onRoundComplete(int round, float avgReactionTime);
    }

    private Paint shapePaint;
    private Paint textPaint;
    private Paint bgPaint;
    private Paint targetHintPaint;
    private Paint statsPaint;

    // 形状类型
    private static final int SHAPE_CIRCLE = 0;
    private static final int SHAPE_SQUARE = 1;
    private static final int SHAPE_TRIANGLE = 2;
    private static final int SHAPE_STAR = 3;
    private static final int SHAPE_DIAMOND = 4;

    // 颜色
    private static final int[] COLORS = {
            Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.MAGENTA, Color.CYAN
    };
    private static final String[] COLOR_NAMES = {
            "红色", "绿色", "蓝色", "黄色", "紫色", "青色"
    };
    private static final String[] SHAPE_NAMES = {
            "圆形", "方形", "三角形", "星形", "菱形"
    };

    // 游戏对象
    private static class GameShape {
        float x, y;
        float size;
        int shapeType;
        int color;
        boolean isTarget;

        GameShape(float x, float y, float size, int shapeType, int color, boolean isTarget) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.shapeType = shapeType;
            this.color = color;
            this.isTarget = isTarget;
        }

        boolean contains(float px, float py) {
            return px >= x - size && px <= x + size && py >= y - size && py <= y + size;
        }
    }

    private List<GameShape> shapes = new ArrayList<>();
    private int targetColor;
    private int targetShape;
    private String targetDescription;

    // 游戏状态
    private boolean isActive = false;
    private boolean isWaiting = false;  // 等待下一轮
    private boolean showingTarget = false;  // 目标是否可见
    private long targetAppearTime = 0;
    private long stimulusDuration = 2000;  // 刺激呈现时间(ms)
    private int difficulty = 1;  // 1=简单, 2=中等, 3=困难

    // 统计
    private List<Long> reactionTimes = new ArrayList<>();
    private int correctCount = 0;
    private int wrongCount = 0;
    private int missedCount = 0;
    private int currentRound = 0;
    private int totalRounds = 20;

    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    private OnReactionEventListener listener;

    // 显示区域
    private RectF gameArea;
    private RectF statsArea;

    public ReactionTrainingView(Context context) {
        super(context);
        init();
    }

    public ReactionTrainingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReactionTrainingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        shapePaint = new Paint();
        shapePaint.setAntiAlias(true);
        shapePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(32);

        bgPaint = new Paint();
        bgPaint.setColor(Color.rgb(30, 30, 30));
        bgPaint.setStyle(Paint.Style.FILL);

        targetHintPaint = new Paint();
        targetHintPaint.setAntiAlias(true);
        targetHintPaint.setColor(Color.rgb(80, 80, 80));
        targetHintPaint.setStyle(Paint.Style.FILL);

        statsPaint = new Paint();
        statsPaint.setAntiAlias(true);
        statsPaint.setColor(Color.WHITE);
        statsPaint.setTextSize(24);

        setBackgroundColor(Color.rgb(20, 20, 30));
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

        // 游戏区域（左侧大部分）
        gameArea = new RectF(20, 100, width - 250, height - 20);

        // 统计区域（右侧）
        statsArea = new RectF(width - 230, 100, width - 20, height - 20);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 标题
        textPaint.setTextSize(28);
        canvas.drawText("视觉注意力训练 - 快速找到目标！", width / 2f, 40, textPaint);

        // 目标提示
        textPaint.setTextSize(32);
        textPaint.setColor(Color.YELLOW);
        canvas.drawText("目标: " + targetDescription, width / 2f, 80, textPaint);
        textPaint.setColor(Color.WHITE);

        // 绘制游戏区域背景
        if (gameArea != null) {
            canvas.drawRoundRect(gameArea, 15, 15, targetHintPaint);
        }

        // 绘制形状
        if (showingTarget) {
            for (GameShape shape : shapes) {
                drawShape(canvas, shape);
            }
        } else if (isWaiting) {
            // 等待状态
            textPaint.setTextSize(40);
            canvas.drawText("准备...", gameArea.centerX(), gameArea.centerY(), textPaint);
        }

        // 绘制统计信息
        drawStats(canvas);

        // 进度
        textPaint.setTextSize(24);
        String progress = "第 " + currentRound + "/" + totalRounds + " 轮";
        canvas.drawText(progress, gameArea.centerX(), height - 40, textPaint);
    }

    private void drawShape(Canvas canvas, GameShape shape) {
        shapePaint.setColor(shape.color);

        switch (shape.shapeType) {
            case SHAPE_CIRCLE:
                canvas.drawCircle(shape.x, shape.y, shape.size, shapePaint);
                break;
            case SHAPE_SQUARE:
                canvas.drawRect(shape.x - shape.size, shape.y - shape.size,
                        shape.x + shape.size, shape.y + shape.size, shapePaint);
                break;
            case SHAPE_TRIANGLE:
                drawTriangle(canvas, shape.x, shape.y, shape.size, shapePaint);
                break;
            case SHAPE_STAR:
                drawStar(canvas, shape.x, shape.y, shape.size, shapePaint);
                break;
            case SHAPE_DIAMOND:
                drawDiamond(canvas, shape.x, shape.y, shape.size, shapePaint);
                break;
        }

        // 目标高亮边框（调试用，正式版可移除）
        // if (shape.isTarget) {
        //     Paint borderPaint = new Paint();
        //     borderPaint.setStyle(Paint.Style.STROKE);
        //     borderPaint.setStrokeWidth(3);
        //     borderPaint.setColor(Color.WHITE);
        //     canvas.drawCircle(shape.x, shape.y, shape.size + 5, borderPaint);
        // }
    }

    private void drawTriangle(Canvas canvas, float cx, float cy, float size, Paint paint) {
        Path path = new Path();
        path.moveTo(cx, cy - size);
        path.lineTo(cx - size, cy + size);
        path.lineTo(cx + size, cy + size);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float size, Paint paint) {
        Path path = new Path();
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

    private void drawDiamond(Canvas canvas, float cx, float cy, float size, Paint paint) {
        Path path = new Path();
        path.moveTo(cx, cy - size);
        path.lineTo(cx + size, cy);
        path.lineTo(cx, cy + size);
        path.lineTo(cx - size, cy);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawStats(Canvas canvas) {
        if (statsArea == null) return;

        // 背景
        canvas.drawRoundRect(statsArea, 10, 10, targetHintPaint);

        float x = statsArea.left + 20;
        float y = statsArea.top + 40;
        float lineHeight = 35;

        statsPaint.setTextSize(22);
        statsPaint.setTextAlign(Paint.Align.LEFT);

        // 标题
        statsPaint.setColor(Color.YELLOW);
        canvas.drawText("统计信息", x, y, statsPaint);
        y += lineHeight + 10;

        statsPaint.setColor(Color.WHITE);

        // 正确/错误/漏过
        canvas.drawText("正确: " + correctCount, x, y, statsPaint);
        y += lineHeight;
        canvas.drawText("错误: " + wrongCount, x, y, statsPaint);
        y += lineHeight;
        canvas.drawText("漏过: " + missedCount, x, y, statsPaint);
        y += lineHeight + 10;

        // 平均反应时间
        statsPaint.setColor(Color.CYAN);
        canvas.drawText("反应时间", x, y, statsPaint);
        y += lineHeight;

        statsPaint.setColor(Color.WHITE);
        if (!reactionTimes.isEmpty()) {
            long sum = 0;
            long min = Long.MAX_VALUE;
            long max = 0;
            for (Long t : reactionTimes) {
                sum += t;
                min = Math.min(min, t);
                max = Math.max(max, t);
            }
            long avg = sum / reactionTimes.size();

            canvas.drawText("平均: " + avg + "ms", x, y, statsPaint);
            y += lineHeight;
            canvas.drawText("最快: " + min + "ms", x, y, statsPaint);
            y += lineHeight;
            canvas.drawText("最慢: " + max + "ms", x, y, statsPaint);
        } else {
            canvas.drawText("暂无数据", x, y, statsPaint);
        }

        y += lineHeight + 20;

        // 难度
        statsPaint.setColor(Color.GREEN);
        String diffText = difficulty == 1 ? "简单" : (difficulty == 2 ? "中等" : "困难");
        canvas.drawText("难度: " + diffText, x, y, statsPaint);

        y += lineHeight;
        statsPaint.setColor(Color.GRAY);
        statsPaint.setTextSize(18);
        canvas.drawText("呈现: " + stimulusDuration + "ms", x, y, statsPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isActive || !showingTarget) return super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            // 检查是否点击了目标
            for (GameShape shape : shapes) {
                if (shape.contains(x, y)) {
                    if (shape.isTarget) {
                        // 正确！
                        long reactionTime = System.currentTimeMillis() - targetAppearTime;
                        reactionTimes.add(reactionTime);
                        correctCount++;

                        if (listener != null) {
                            listener.onCorrectResponse(reactionTime);
                        }

                        // 进入下一轮
                        handler.removeCallbacksAndMessages(null);
                        nextRound();
                        return true;
                    } else {
                        // 点错了
                        wrongCount++;
                        if (listener != null) {
                            listener.onWrongResponse();
                        }
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void generateRound() {
        shapes.clear();

        if (gameArea == null) return;

        // 根据难度确定目标
        switch (difficulty) {
            case 1: // 简单：找指定颜色
                targetColor = COLORS[random.nextInt(COLORS.length)];
                targetShape = -1; // 任意形状
                targetDescription = COLOR_NAMES[getColorIndex(targetColor)] + "的图形";
                break;
            case 2: // 中等：找指定形状
                targetColor = -1; // 任意颜色
                targetShape = random.nextInt(5);
                targetDescription = SHAPE_NAMES[targetShape];
                break;
            case 3: // 困难：找指定颜色+形状
                targetColor = COLORS[random.nextInt(COLORS.length)];
                targetShape = random.nextInt(5);
                targetDescription = COLOR_NAMES[getColorIndex(targetColor)] + SHAPE_NAMES[targetShape];
                break;
        }

        // 生成形状数量
        int shapeCount = 5 + difficulty * 3 + currentRound / 5;
        shapeCount = Math.min(shapeCount, 20);

        float margin = 60;
        float areaWidth = gameArea.width() - margin * 2;
        float areaHeight = gameArea.height() - margin * 2;

        // 生成目标
        float targetX = gameArea.left + margin + random.nextFloat() * areaWidth;
        float targetY = gameArea.top + margin + random.nextFloat() * areaHeight;
        float size = 35 + random.nextFloat() * 15;

        int finalTargetShape = targetShape >= 0 ? targetShape : random.nextInt(5);
        int finalTargetColor = targetColor >= 0 ? targetColor : COLORS[random.nextInt(COLORS.length)];

        shapes.add(new GameShape(targetX, targetY, size, finalTargetShape, finalTargetColor, true));

        // 生成干扰项
        for (int i = 1; i < shapeCount; i++) {
            float x = gameArea.left + margin + random.nextFloat() * areaWidth;
            float y = gameArea.top + margin + random.nextFloat() * areaHeight;
            float s = 30 + random.nextFloat() * 20;

            int shape, color;
            // 确保干扰项不与目标相同
            do {
                shape = random.nextInt(5);
                color = COLORS[random.nextInt(COLORS.length)];
            } while (isMatchingTarget(shape, color));

            shapes.add(new GameShape(x, y, s, shape, color, false));
        }

        // 打乱顺序
        Collections.shuffle(shapes);
    }

    private boolean isMatchingTarget(int shape, int color) {
        switch (difficulty) {
            case 1:
                return color == targetColor;
            case 2:
                return shape == targetShape;
            case 3:
                return color == targetColor && shape == targetShape;
        }
        return false;
    }

    private int getColorIndex(int color) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) return i;
        }
        return 0;
    }

    private void nextRound() {
        currentRound++;

        if (currentRound > totalRounds) {
            completeTraining();
            return;
        }

        showingTarget = false;
        isWaiting = true;
        invalidate();

        // 随机等待时间后显示下一轮
        int waitTime = 500 + random.nextInt(1500);
        handler.postDelayed(() -> {
            if (!isActive) return;

            generateRound();
            showingTarget = true;
            isWaiting = false;
            targetAppearTime = System.currentTimeMillis();
            invalidate();

            // 设置超时
            handler.postDelayed(() -> {
                if (showingTarget && isActive) {
                    // 超时未响应
                    missedCount++;
                    if (listener != null) {
                        listener.onMissed();
                    }
                    nextRound();
                }
            }, stimulusDuration);

        }, waitTime);
    }

    private void completeTraining() {
        isActive = false;
        showingTarget = false;

        float avgTime = 0;
        if (!reactionTimes.isEmpty()) {
            long sum = 0;
            for (Long t : reactionTimes) sum += t;
            avgTime = sum / (float) reactionTimes.size();
        }

        if (listener != null) {
            listener.onRoundComplete(totalRounds, avgTime);
        }

        invalidate();
    }

    public void startTraining() {
        isActive = true;
        correctCount = 0;
        wrongCount = 0;
        missedCount = 0;
        currentRound = 0;
        reactionTimes.clear();

        if (getWidth() > 0 && getHeight() > 0) {
            calculateLayout();
            nextRound();
        } else {
            post(() -> {
                calculateLayout();
                nextRound();
            });
        }
    }

    public void stopTraining() {
        isActive = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void pauseTraining() {
        isActive = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void resumeTraining() {
        if (!isActive) {
            isActive = true;
            nextRound();
        }
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(3, difficulty));
    }

    public void setStimulusDuration(long durationMs) {
        this.stimulusDuration = Math.max(500, Math.min(5000, durationMs));
    }

    public void setTotalRounds(int rounds) {
        this.totalRounds = Math.max(5, Math.min(50, rounds));
    }

    public void setOnReactionEventListener(OnReactionEventListener listener) {
        this.listener = listener;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public int getMissedCount() {
        return missedCount;
    }

    public List<Long> getReactionTimes() {
        return new ArrayList<>(reactionTimes);
    }

    public float getAverageReactionTime() {
        if (reactionTimes.isEmpty()) return 0;
        long sum = 0;
        for (Long t : reactionTimes) sum += t;
        return sum / (float) reactionTimes.size();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
