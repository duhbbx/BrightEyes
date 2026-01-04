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
 * 双眼对抗拼图训练
 * 使用红蓝眼镜：红色部分弱眼可见，蓝色部分健眼可见
 * 需要双眼协作才能看到完整图案并完成拼图
 */
public class DichopticPuzzleView extends View {

    public interface OnPuzzleCompleteListener {
        void onPuzzleComplete();
        void onPiecePlaced();
    }

    private Paint weakEyePaint;      // 弱眼（红色通道）
    private Paint strongEyePaint;    // 健眼（青色通道）
    private Paint outlinePaint;
    private Paint textPaint;
    private Paint bgPaint;

    // 弱眼对比度 (0.0 - 1.0)
    private float weakEyeContrast = 1.0f;
    // 健眼对比度 (0.0 - 1.0)
    private float strongEyeContrast = 0.3f;

    // 拼图相关
    private int gridSize = 3;
    private List<PuzzlePiece> pieces = new ArrayList<>();
    private List<RectF> targetSlots = new ArrayList<>();
    private PuzzlePiece selectedPiece = null;
    private float touchOffsetX, touchOffsetY;

    private int completedCount = 0;
    private boolean isActive = false;
    private Random random = new Random();
    private OnPuzzleCompleteListener listener;

    // 图案类型
    private int currentPattern = 0;
    private static final int PATTERN_STAR = 0;
    private static final int PATTERN_HEART = 1;
    private static final int PATTERN_HOUSE = 2;

    public DichopticPuzzleView(Context context) {
        super(context);
        init();
    }

    public DichopticPuzzleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DichopticPuzzleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 弱眼用红色（高对比度）
        weakEyePaint = new Paint();
        weakEyePaint.setAntiAlias(true);
        weakEyePaint.setStyle(Paint.Style.FILL);

        // 健眼用青色（低对比度）
        strongEyePaint = new Paint();
        strongEyePaint.setAntiAlias(true);
        strongEyePaint.setStyle(Paint.Style.FILL);

        outlinePaint = new Paint();
        outlinePaint.setAntiAlias(true);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        outlinePaint.setColor(Color.WHITE);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(40);

        bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);

        updateColors();
        setBackgroundColor(Color.BLACK);
    }

    private void updateColors() {
        // 弱眼：纯红色，根据对比度调整亮度
        int redValue = (int) (255 * weakEyeContrast);
        weakEyePaint.setColor(Color.rgb(redValue, 0, 0));

        // 健眼：青色（绿+蓝），根据对比度调整亮度
        int cyanValue = (int) (255 * strongEyeContrast);
        strongEyePaint.setColor(Color.rgb(0, cyanValue, cyanValue));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (isActive) {
            generatePuzzle();
        }
    }

    private void generatePuzzle() {
        pieces.clear();
        targetSlots.clear();
        completedCount = 0;

        int width = getWidth();
        int height = getHeight();

        // 确保尺寸有效
        if (width <= 0 || height <= 0) {
            return;
        }

        // 左侧是目标区域，右侧是拼图块
        int puzzleSize = Math.max(100, Math.min(width / 2 - 100, height - 200));
        int pieceSize = Math.max(30, puzzleSize / gridSize);

        // 目标区域（左侧）
        int targetStartX = Math.max(10, (width / 4) - (puzzleSize / 2));
        int targetStartY = Math.max(100, (height / 2) - (puzzleSize / 2));

        // 计算右侧随机区域的范围
        int randomRangeX = Math.max(1, width / 2 - pieceSize - 100);
        int randomRangeY = Math.max(1, height - pieceSize - 200);

        // 创建目标槽位和拼图块
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int index = row * gridSize + col;

                // 目标槽位
                float slotLeft = targetStartX + col * pieceSize;
                float slotTop = targetStartY + row * pieceSize;
                targetSlots.add(new RectF(slotLeft, slotTop,
                        slotLeft + pieceSize, slotTop + pieceSize));

                // 拼图块（随机位置在右侧）
                float pieceX = width / 2 + 50 + random.nextInt(randomRangeX);
                float pieceY = 100 + random.nextInt(randomRangeY);

                // 交替使用弱眼和健眼颜色
                boolean isWeakEye = (index % 2 == 0);

                pieces.add(new PuzzlePiece(index, pieceX, pieceY, pieceSize, isWeakEye));
            }
        }

        // 打乱顺序
        Collections.shuffle(pieces);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制说明
        canvas.drawText("拖动拼图块到左侧目标位置", getWidth() / 2f, 50, textPaint);
        canvas.drawText("红色块 - 弱眼可见 | 蓝色块 - 健眼可见", getWidth() / 2f, 90, textPaint);

        // 绘制目标区域轮廓
        for (int i = 0; i < targetSlots.size(); i++) {
            RectF slot = targetSlots.get(i);
            canvas.drawRect(slot, outlinePaint);

            // 绘制目标图案（双眼都能看到的灰色轮廓）
            drawPatternPiece(canvas, i, slot, true);
        }

        // 绘制拼图块
        for (PuzzlePiece piece : pieces) {
            if (!piece.isPlaced && piece != selectedPiece) {
                drawPuzzlePiece(canvas, piece);
            }
        }

        // 最后绘制选中的块（在最上层）
        if (selectedPiece != null) {
            drawPuzzlePiece(canvas, selectedPiece);
        }

        // 绘制进度
        String progress = "完成: " + completedCount + "/" + (gridSize * gridSize);
        canvas.drawText(progress, getWidth() / 2f, getHeight() - 30, textPaint);
    }

    private void drawPuzzlePiece(Canvas canvas, PuzzlePiece piece) {
        Paint paint = piece.isWeakEye ? weakEyePaint : strongEyePaint;
        RectF rect = new RectF(piece.x, piece.y,
                piece.x + piece.size, piece.y + piece.size);

        // 绘制背景
        canvas.drawRoundRect(rect, 10, 10, paint);

        // 绘制图案
        drawPatternPiece(canvas, piece.targetIndex, rect, false);

        // 绘制边框
        canvas.drawRoundRect(rect, 10, 10, outlinePaint);
    }

    private void drawPatternPiece(Canvas canvas, int index, RectF rect, boolean isOutlineOnly) {
        float cx = rect.centerX();
        float cy = rect.centerY();
        float size = rect.width() * 0.3f;

        Paint paint = isOutlineOnly ? outlinePaint : weakEyePaint;
        if (isOutlineOnly) {
            paint = new Paint(outlinePaint);
            paint.setColor(Color.GRAY);
            paint.setAlpha(100);
        }

        // 根据位置绘制图案的一部分
        int row = index / gridSize;
        int col = index % gridSize;

        // 简单图案：数字
        Paint numPaint = new Paint(textPaint);
        numPaint.setTextSize(rect.width() * 0.5f);
        if (!isOutlineOnly) {
            numPaint.setColor(Color.WHITE);
        } else {
            numPaint.setColor(Color.GRAY);
            numPaint.setAlpha(100);
        }
        canvas.drawText(String.valueOf(index + 1), cx, cy + rect.width() * 0.15f, numPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isActive) return super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 查找点击的拼图块
                for (int i = pieces.size() - 1; i >= 0; i--) {
                    PuzzlePiece piece = pieces.get(i);
                    if (!piece.isPlaced && piece.contains(x, y)) {
                        selectedPiece = piece;
                        touchOffsetX = x - piece.x;
                        touchOffsetY = y - piece.y;
                        // 移到列表末尾（最上层）
                        pieces.remove(i);
                        pieces.add(piece);
                        invalidate();
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (selectedPiece != null) {
                    selectedPiece.x = x - touchOffsetX;
                    selectedPiece.y = y - touchOffsetY;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (selectedPiece != null) {
                    // 检查是否放到正确位置
                    RectF targetSlot = targetSlots.get(selectedPiece.targetIndex);
                    if (isNearSlot(selectedPiece, targetSlot)) {
                        // 吸附到目标位置
                        selectedPiece.x = targetSlot.left;
                        selectedPiece.y = targetSlot.top;
                        selectedPiece.isPlaced = true;
                        completedCount++;

                        if (listener != null) {
                            listener.onPiecePlaced();
                        }

                        if (completedCount >= gridSize * gridSize) {
                            if (listener != null) {
                                listener.onPuzzleComplete();
                            }
                            // 生成新拼图
                            postDelayed(this::generatePuzzle, 1500);
                        }
                    }
                    selectedPiece = null;
                    invalidate();
                    return true;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private boolean isNearSlot(PuzzlePiece piece, RectF slot) {
        float threshold = piece.size * 0.5f;
        return Math.abs(piece.x - slot.left) < threshold &&
                Math.abs(piece.y - slot.top) < threshold;
    }

    public void startTraining() {
        isActive = true;
        // 确保 View 已经布局完成再生成拼图
        if (getWidth() > 0 && getHeight() > 0) {
            generatePuzzle();
        } else {
            post(this::generatePuzzle);
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

    /**
     * 设置弱眼对比度 (0.0 - 1.0)
     */
    public void setWeakEyeContrast(float contrast) {
        this.weakEyeContrast = Math.max(0.1f, Math.min(1.0f, contrast));
        updateColors();
        invalidate();
    }

    /**
     * 设置健眼对比度 (0.0 - 1.0)
     */
    public void setStrongEyeContrast(float contrast) {
        this.strongEyeContrast = Math.max(0.0f, Math.min(1.0f, contrast));
        updateColors();
        invalidate();
    }

    public void setLevel(int level) {
        // 等级越高，格子越多，健眼对比度越高
        gridSize = Math.min(5, 2 + (level / 2));
        strongEyeContrast = Math.min(0.8f, 0.2f + (level * 0.05f));
        updateColors();
        if (isActive && getWidth() > 0) {
            generatePuzzle();
        }
    }

    public void setOnPuzzleCompleteListener(OnPuzzleCompleteListener listener) {
        this.listener = listener;
    }

    public boolean isActive() {
        return isActive;
    }

    // 拼图块类
    private static class PuzzlePiece {
        int targetIndex;
        float x, y;
        int size;
        boolean isWeakEye;
        boolean isPlaced = false;

        PuzzlePiece(int targetIndex, float x, float y, int size, boolean isWeakEye) {
            this.targetIndex = targetIndex;
            this.x = x;
            this.y = y;
            this.size = size;
            this.isWeakEye = isWeakEye;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= x + size && py >= y && py <= y + size;
        }
    }
}
