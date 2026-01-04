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
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 形状配对训练视图
 * 提高图形识别与记忆
 */
public class ShapeMatchingView extends View {

    public interface OnShapeMatchListener {
        void onCorrectMatch();
        void onWrongMatch();
    }

    private static final int SHAPE_CIRCLE = 0;
    private static final int SHAPE_SQUARE = 1;
    private static final int SHAPE_TRIANGLE = 2;
    private static final int SHAPE_STAR = 3;
    private static final int SHAPE_DIAMOND = 4;
    private static final int SHAPE_HEART = 5;

    private Paint shapePaint;
    private Paint textPaint;
    private Paint borderPaint;

    private String[] shapeNames = {"圆形", "正方形", "三角形", "星形", "菱形", "心形"};
    private int[] shapeColors = {
            Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA
    };

    private int targetShape = 0;
    private List<Integer> displayedShapes = new ArrayList<>();
    private List<RectF> shapeRects = new ArrayList<>();
    private int gridSize = 3;
    private Random random = new Random();
    private OnShapeMatchListener listener;
    private boolean isActive = false;

    public ShapeMatchingView(Context context) {
        super(context);
        init();
    }

    public ShapeMatchingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShapeMatchingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        textPaint.setTextSize(50);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);

        setBackgroundColor(Color.rgb(30, 30, 30));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateNewRound();
    }

    private void generateNewRound() {
        displayedShapes.clear();
        shapeRects.clear();

        // 选择目标形状
        targetShape = random.nextInt(6);

        // 生成显示的形状
        int totalCells = gridSize * gridSize;
        for (int i = 0; i < totalCells - 1; i++) {
            int shape;
            do {
                shape = random.nextInt(6);
            } while (shape == targetShape);
            displayedShapes.add(shape);
        }

        // 随机位置插入目标形状
        int targetPosition = random.nextInt(totalCells);
        displayedShapes.add(targetPosition, targetShape);

        // 计算方块位置
        calculateRects();
        invalidate();
    }

    private void calculateRects() {
        shapeRects.clear();

        int width = getWidth();
        int height = getHeight();
        int topMargin = 120; // 给顶部提示文字和示例形状留空间
        int bottomMargin = 20;

        int availableHeight = height - topMargin - bottomMargin;
        int cellSize = Math.min((width - 40) / gridSize, availableHeight / gridSize) - 10;
        int startX = (width - (cellSize + 10) * gridSize) / 2;
        int startY = topMargin + (availableHeight - (cellSize + 10) * gridSize) / 2;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                float left = startX + col * (cellSize + 10);
                float top = startY + row * (cellSize + 10);
                shapeRects.add(new RectF(left, top, left + cellSize, top + cellSize));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制提示文字
        canvas.drawText("请找出: " + shapeNames[targetShape], getWidth() / 2f, 60, textPaint);

        // 绘制目标形状示例
        RectF targetRect = new RectF(getWidth() / 2f - 50, 80, getWidth() / 2f + 50, 160);
        shapePaint.setColor(shapeColors[targetShape]);
        drawShape(canvas, targetShape, targetRect);

        // 绘制形状方块
        for (int i = 0; i < shapeRects.size() && i < displayedShapes.size(); i++) {
            RectF rect = shapeRects.get(i);
            int shape = displayedShapes.get(i);

            // 绘制背景
            shapePaint.setColor(Color.rgb(50, 50, 50));
            canvas.drawRoundRect(rect, 15, 15, shapePaint);
            canvas.drawRoundRect(rect, 15, 15, borderPaint);

            // 绘制形状
            float padding = rect.width() * 0.15f;
            RectF shapeRect = new RectF(
                    rect.left + padding,
                    rect.top + padding,
                    rect.right - padding,
                    rect.bottom - padding
            );
            shapePaint.setColor(shapeColors[shape]);
            drawShape(canvas, shape, shapeRect);
        }
    }

    private void drawShape(Canvas canvas, int shape, RectF rect) {
        float centerX = rect.centerX();
        float centerY = rect.centerY();
        float size = Math.min(rect.width(), rect.height()) / 2;

        switch (shape) {
            case SHAPE_CIRCLE:
                canvas.drawCircle(centerX, centerY, size, shapePaint);
                break;

            case SHAPE_SQUARE:
                canvas.drawRect(
                        centerX - size, centerY - size,
                        centerX + size, centerY + size,
                        shapePaint
                );
                break;

            case SHAPE_TRIANGLE:
                Path trianglePath = new Path();
                trianglePath.moveTo(centerX, centerY - size);
                trianglePath.lineTo(centerX - size, centerY + size);
                trianglePath.lineTo(centerX + size, centerY + size);
                trianglePath.close();
                canvas.drawPath(trianglePath, shapePaint);
                break;

            case SHAPE_STAR:
                drawStar(canvas, centerX, centerY, size, shapePaint);
                break;

            case SHAPE_DIAMOND:
                Path diamondPath = new Path();
                diamondPath.moveTo(centerX, centerY - size);
                diamondPath.lineTo(centerX + size, centerY);
                diamondPath.lineTo(centerX, centerY + size);
                diamondPath.lineTo(centerX - size, centerY);
                diamondPath.close();
                canvas.drawPath(diamondPath, shapePaint);
                break;

            case SHAPE_HEART:
                drawHeart(canvas, centerX, centerY, size, shapePaint);
                break;
        }
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        Path path = new Path();
        float innerRadius = radius * 0.4f;
        double angle = -Math.PI / 2;
        double deltaAngle = Math.PI / 5;

        path.moveTo(cx + (float) (radius * Math.cos(angle)),
                cy + (float) (radius * Math.sin(angle)));

        for (int i = 0; i < 5; i++) {
            angle += deltaAngle;
            path.lineTo(cx + (float) (innerRadius * Math.cos(angle)),
                    cy + (float) (innerRadius * Math.sin(angle)));
            angle += deltaAngle;
            path.lineTo(cx + (float) (radius * Math.cos(angle)),
                    cy + (float) (radius * Math.sin(angle)));
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawHeart(Canvas canvas, float cx, float cy, float size, Paint paint) {
        Path path = new Path();
        float width = size * 2;
        float height = size * 2;

        path.moveTo(cx, cy + height * 0.3f);
        path.cubicTo(cx - width * 0.5f, cy - height * 0.3f,
                cx - width * 0.5f, cy - height * 0.5f,
                cx, cy - height * 0.15f);
        path.cubicTo(cx + width * 0.5f, cy - height * 0.5f,
                cx + width * 0.5f, cy - height * 0.3f,
                cx, cy + height * 0.3f);
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isActive) return super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < shapeRects.size(); i++) {
                if (shapeRects.get(i).contains(x, y)) {
                    if (displayedShapes.get(i) == targetShape) {
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
        gridSize = Math.min(5, 2 + (level / 3));
        if (getWidth() > 0) {
            calculateRects();
            generateNewRound();
        }
    }

    public void setOnShapeMatchListener(OnShapeMatchListener listener) {
        this.listener = listener;
    }

    public boolean isActive() {
        return isActive;
    }
}
