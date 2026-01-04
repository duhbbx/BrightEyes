package com.brighteyes.app.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * 光栅训练视图
 * 支持多种图案、颜色模式和动画效果
 */
public class GratingView extends View {

    // 颜色模式
    public static final int COLOR_BLACK_WHITE = 0;
    public static final int COLOR_RED_BLUE = 1;
    public static final int COLOR_RED_CYAN = 2;
    public static final int COLOR_RED_WHITE = 3;
    public static final int COLOR_BLUE_WHITE = 4;
    public static final int COLOR_RED_WHITE_BLACK = 5;
    public static final int COLOR_RAINBOW = 6;
    public static final int COLOR_GREEN_MAGENTA = 7;
    public static final int COLOR_YELLOW_BLUE = 8;
    // 纯色+透明模式
    public static final int COLOR_RED_TRANSPARENT = 9;
    public static final int COLOR_BLUE_TRANSPARENT = 10;
    public static final int COLOR_BLACK_TRANSPARENT = 11;
    public static final int COLOR_WHITE_TRANSPARENT = 12;

    // 兼容旧常量名
    public static final int MODE_BLACK_WHITE = COLOR_BLACK_WHITE;
    public static final int MODE_RED_BLUE = COLOR_RED_BLUE;
    public static final int MODE_RED_CYAN = COLOR_RED_CYAN;
    public static final int MODE_RED_WHITE = COLOR_RED_WHITE;
    public static final int MODE_BLUE_WHITE = COLOR_BLUE_WHITE;
    public static final int MODE_RED_WHITE_BLACK = COLOR_RED_WHITE_BLACK;
    public static final int MODE_RAINBOW = COLOR_RAINBOW;

    // 图案类型
    public static final int PATTERN_STRIPES = 0;      // 条纹
    public static final int PATTERN_BLOCKS = 1;       // 方块/棋盘格
    public static final int PATTERN_CIRCLES = 2;      // 同心圆
    public static final int PATTERN_RADIAL = 3;       // 放射线
    public static final int PATTERN_SINE_WAVE = 4;    // 正弦波纹

    // 动画类型
    public static final int ANIM_SCROLL = 0;          // 滚动
    public static final int ANIM_ZOOM = 1;            // 缩放
    public static final int ANIM_ROTATE = 2;          // 旋转
    public static final int ANIM_PULSE = 3;           // 脉冲(闪烁)
    public static final int ANIM_WAVE = 4;            // 波浪
    public static final int ANIM_RANDOM = 5;          // 随机轮换

    private Paint paint;
    private ValueAnimator animator;
    private float animValue = 0;
    private boolean isAnimating = false;

    // 可调参数
    private int stripeWidth = 40;
    private int colorMode = COLOR_BLACK_WHITE;
    private int patternType = PATTERN_STRIPES;
    private int animationType = ANIM_SCROLL;
    private long animationSpeed = 1000;
    private boolean isVertical = true;
    private boolean reverseDirection = false;
    private float rotationAngle = 0;

    // 颜色定义
    private int color1 = Color.BLACK;
    private int color2 = Color.WHITE;
    private int color3 = Color.RED;

    // 彩虹色
    private int[] rainbowColors = {
            Color.RED, Color.rgb(255, 127, 0), Color.YELLOW,
            Color.GREEN, Color.BLUE, Color.rgb(75, 0, 130), Color.rgb(148, 0, 211)
    };

    // 缩放和旋转
    private float scale = 1.0f;
    private float rotation = 0f;
    private float accumulatedRotation = 0f; // 累积旋转角度，用于连续旋转
    private Matrix transformMatrix = new Matrix();

    // 预分配的颜色数组，避免在绘制时创建对象
    private int[] twoColors = new int[2];
    private int[] threeColors = new int[3];

    // 随机动画切换
    private Handler randomAnimHandler = new Handler(Looper.getMainLooper());
    private Runnable randomAnimRunnable;
    private int currentRandomAnim = ANIM_SCROLL;
    private int[] randomAnimTypes = {ANIM_SCROLL, ANIM_ZOOM, ANIM_ROTATE, ANIM_PULSE};
    private Random random = new Random();
    private static final long RANDOM_SWITCH_INTERVAL = 10000; // 10秒切换一次

    public GratingView(Context context) {
        super(context);
        init();
    }

    public GratingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GratingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false); // 关闭抗锯齿提升性能，方块不需要
        setLayerType(LAYER_TYPE_HARDWARE, null); // 启用硬件加速
        updateColors();

        // 随机动画切换逻辑
        randomAnimRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAnimating && animationType == ANIM_RANDOM) {
                    switchToNextRandomAnim();
                    randomAnimHandler.postDelayed(this, RANDOM_SWITCH_INTERVAL);
                }
            }
        };
    }

    /**
     * 切换到下一个随机动画
     */
    private void switchToNextRandomAnim() {
        // 随机选择一个不同的动画
        int newAnim;
        do {
            newAnim = randomAnimTypes[random.nextInt(randomAnimTypes.length)];
        } while (newAnim == currentRandomAnim && randomAnimTypes.length > 1);

        currentRandomAnim = newAnim;

        // 重新启动动画
        if (animator != null) {
            animator.cancel();
        }
        startAnimationInternal(currentRandomAnim);
    }

    private void updateColors() {
        switch (colorMode) {
            case COLOR_BLACK_WHITE:
                color1 = Color.BLACK;
                color2 = Color.WHITE;
                break;
            case COLOR_RED_BLUE:
                color1 = Color.RED;
                color2 = Color.BLUE;
                break;
            case COLOR_RED_CYAN:
                color1 = Color.RED;
                color2 = Color.CYAN;
                break;
            case COLOR_RED_WHITE:
                color1 = Color.RED;
                color2 = Color.WHITE;
                break;
            case COLOR_BLUE_WHITE:
                color1 = Color.BLUE;
                color2 = Color.WHITE;
                break;
            case COLOR_RED_WHITE_BLACK:
                color1 = Color.RED;
                color2 = Color.WHITE;
                color3 = Color.BLACK;
                break;
            case COLOR_GREEN_MAGENTA:
                color1 = Color.GREEN;
                color2 = Color.MAGENTA;
                break;
            case COLOR_YELLOW_BLUE:
                color1 = Color.YELLOW;
                color2 = Color.BLUE;
                break;
            case COLOR_RED_TRANSPARENT:
                color1 = Color.RED;
                color2 = Color.TRANSPARENT;
                break;
            case COLOR_BLUE_TRANSPARENT:
                color1 = Color.BLUE;
                color2 = Color.TRANSPARENT;
                break;
            case COLOR_BLACK_TRANSPARENT:
                color1 = Color.BLACK;
                color2 = Color.TRANSPARENT;
                break;
            case COLOR_WHITE_TRANSPARENT:
                color1 = Color.WHITE;
                color2 = Color.TRANSPARENT;
                break;
            case COLOR_RAINBOW:
                break;
        }
        // 更新预分配的颜色数组
        twoColors[0] = color1;
        twoColors[1] = color2;
        threeColors[0] = color1;
        threeColors[1] = color2;
        threeColors[2] = color3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        canvas.save();

        // 应用变换（缩放、旋转）
        if (animationType == ANIM_ZOOM || animationType == ANIM_ROTATE) {
            canvas.translate(width / 2f, height / 2f);
            if (animationType == ANIM_ZOOM) {
                canvas.scale(scale, scale);
            } else if (animationType == ANIM_ROTATE) {
                canvas.rotate(rotation);
            }
            canvas.translate(-width / 2f, -height / 2f);
        }

        // 根据图案类型绘制
        switch (patternType) {
            case PATTERN_STRIPES:
                drawStripes(canvas, width, height);
                break;
            case PATTERN_BLOCKS:
                drawBlocks(canvas, width, height);
                break;
            case PATTERN_CIRCLES:
                drawCircles(canvas, width, height);
                break;
            case PATTERN_RADIAL:
                drawRadial(canvas, width, height);
                break;
            case PATTERN_SINE_WAVE:
                drawSineWave(canvas, width, height);
                break;
        }

        canvas.restore();
    }

    private void drawStripes(Canvas canvas, int width, int height) {
        if (colorMode == COLOR_RED_WHITE_BLACK) {
            drawThreeColorStripes(canvas, width, height);
        } else if (colorMode == COLOR_RAINBOW) {
            drawRainbowStripes(canvas, width, height);
        } else {
            drawTwoColorStripes(canvas, width, height);
        }
    }

    private void drawTwoColorStripes(Canvas canvas, int width, int height) {
        int size = isVertical ? width : height;
        int stripeCount = (size / stripeWidth) + 3;
        float animOffset = reverseDirection ? -animValue : animValue;
        float start = -stripeWidth * 2 + (animOffset % (stripeWidth * 2));

        for (int i = 0; i < stripeCount; i++) {
            paint.setColor(i % 2 == 0 ? color1 : color2);
            float pos = start + (i * stripeWidth);
            if (isVertical) {
                canvas.drawRect(pos, 0, pos + stripeWidth, height, paint);
            } else {
                canvas.drawRect(0, pos, width, pos + stripeWidth, paint);
            }
        }
    }

    private void drawThreeColorStripes(Canvas canvas, int width, int height) {
        int size = isVertical ? width : height;
        int stripeCount = (size / stripeWidth) + 4;
        float animOffset = reverseDirection ? -animValue : animValue;
        float start = -stripeWidth * 3 + (animOffset % (stripeWidth * 3));

        int[] colors = {color1, color2, color3};
        for (int i = 0; i < stripeCount; i++) {
            paint.setColor(colors[i % 3]);
            float pos = start + (i * stripeWidth);
            if (isVertical) {
                canvas.drawRect(pos, 0, pos + stripeWidth, height, paint);
            } else {
                canvas.drawRect(0, pos, width, pos + stripeWidth, paint);
            }
        }
    }

    private void drawRainbowStripes(Canvas canvas, int width, int height) {
        int size = isVertical ? width : height;
        int stripeCount = (size / stripeWidth) + rainbowColors.length + 1;
        float animOffset = reverseDirection ? -animValue : animValue;
        float start = -stripeWidth * rainbowColors.length + (animOffset % (stripeWidth * rainbowColors.length));

        for (int i = 0; i < stripeCount; i++) {
            paint.setColor(rainbowColors[i % rainbowColors.length]);
            float pos = start + (i * stripeWidth);
            if (isVertical) {
                canvas.drawRect(pos, 0, pos + stripeWidth, height, paint);
            } else {
                canvas.drawRect(0, pos, width, pos + stripeWidth, paint);
            }
        }
    }

    private void drawBlocks(Canvas canvas, int width, int height) {
        int blockSize = stripeWidth;
        int colCount = (width / blockSize) + 4;
        int rowCount = (height / blockSize) + 4;

        float animOffset = reverseDirection ? -animValue : animValue;

        // 使用预分配的颜色数组
        int[] colors;
        if (colorMode == COLOR_RAINBOW) {
            colors = rainbowColors;
        } else if (colorMode == COLOR_RED_WHITE_BLACK) {
            colors = threeColors;
        } else {
            colors = twoColors;
        }

        int colorCount = colors.length;
        float cycleSize = blockSize * colorCount;

        float offsetX = isVertical ? (animOffset % cycleSize) : 0;
        float offsetY = isVertical ? 0 : (animOffset % cycleSize);

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                int sum = row + col;
                paint.setColor(colors[sum % colorCount]);

                float left = (col - 1) * blockSize + offsetX - blockSize;
                float top = (row - 1) * blockSize + offsetY - blockSize;
                canvas.drawRect(left, top, left + blockSize, top + blockSize, paint);
            }
        }
    }

    private void drawCircles(Canvas canvas, int width, int height) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxRadius = (float) Math.sqrt(centerX * centerX + centerY * centerY);

        float animOffset = animValue;
        int ringCount = (int) (maxRadius / stripeWidth) + 2;

        for (int i = ringCount; i >= 0; i--) {
            float radius = i * stripeWidth + (animOffset % stripeWidth);
            if (colorMode == COLOR_RAINBOW) {
                paint.setColor(rainbowColors[i % rainbowColors.length]);
            } else if (colorMode == COLOR_RED_WHITE_BLACK) {
                int[] colors = {color1, color2, color3};
                paint.setColor(colors[i % 3]);
            } else {
                paint.setColor(i % 2 == 0 ? color1 : color2);
            }
            canvas.drawCircle(centerX, centerY, radius, paint);
        }
    }

    private void drawRadial(Canvas canvas, int width, int height) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxRadius = (float) Math.sqrt(centerX * centerX + centerY * centerY);

        int segmentCount = 360 / stripeWidth * 2;
        if (segmentCount < 8) segmentCount = 8;
        float angleStep = 360f / segmentCount;
        float animOffset = animValue * 0.1f;

        RectF oval = new RectF(-maxRadius, -maxRadius, maxRadius, maxRadius);

        canvas.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(reverseDirection ? -animOffset : animOffset);

        for (int i = 0; i < segmentCount; i++) {
            if (colorMode == COLOR_RAINBOW) {
                paint.setColor(rainbowColors[i % rainbowColors.length]);
            } else if (colorMode == COLOR_RED_WHITE_BLACK) {
                int[] colors = {color1, color2, color3};
                paint.setColor(colors[i % 3]);
            } else {
                paint.setColor(i % 2 == 0 ? color1 : color2);
            }
            canvas.drawArc(oval, i * angleStep, angleStep, true, paint);
        }

        canvas.restore();
    }

    private void drawSineWave(Canvas canvas, int width, int height) {
        int waveCount = (isVertical ? width : height) / stripeWidth + 2;
        float amplitude = stripeWidth * 0.5f;
        float frequency = 2 * (float) Math.PI / (stripeWidth * 4);

        float animOffset = reverseDirection ? -animValue : animValue;

        for (int i = 0; i < waveCount; i++) {
            if (colorMode == COLOR_RAINBOW) {
                paint.setColor(rainbowColors[i % rainbowColors.length]);
            } else if (colorMode == COLOR_RED_WHITE_BLACK) {
                int[] colors = {color1, color2, color3};
                paint.setColor(colors[i % 3]);
            } else {
                paint.setColor(i % 2 == 0 ? color1 : color2);
            }

            for (int j = 0; j < (isVertical ? height : width); j += 2) {
                float waveOffset = (float) Math.sin(frequency * (j + animOffset)) * amplitude;
                float basePos = i * stripeWidth + animOffset % stripeWidth;

                if (isVertical) {
                    canvas.drawRect(basePos + waveOffset, j, basePos + stripeWidth + waveOffset, j + 2, paint);
                } else {
                    canvas.drawRect(j, basePos + waveOffset, j + 2, basePos + stripeWidth + waveOffset, paint);
                }
            }
        }
    }

    public void startAnimation() {
        if (isAnimating) return;

        isAnimating = true;
        accumulatedRotation = 0f; // 重置累积旋转

        if (animationType == ANIM_RANDOM) {
            // 随机模式：随机选择一个动画开始
            currentRandomAnim = randomAnimTypes[random.nextInt(randomAnimTypes.length)];
            startAnimationInternal(currentRandomAnim);
            // 启动定时切换
            randomAnimHandler.postDelayed(randomAnimRunnable, RANDOM_SWITCH_INTERVAL);
        } else {
            startAnimationInternal(animationType);
        }
    }

    /**
     * 内部启动动画方法
     */
    private void startAnimationInternal(int animType) {
        // 重置状态
        scale = 1.0f;

        switch (animType) {
            case ANIM_ZOOM:
                animator = ValueAnimator.ofFloat(0.8f, 1.2f);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                break;
            case ANIM_ROTATE:
                // 使用 0-360 循环，但通过累积实现连续旋转
                animator = ValueAnimator.ofFloat(0, 360);
                animator.setRepeatMode(ValueAnimator.RESTART);
                break;
            case ANIM_PULSE:
                animator = ValueAnimator.ofFloat(0, 1);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                break;
            case ANIM_SCROLL:
            case ANIM_WAVE:
            default:
                int cycleSize = stripeWidth * (colorMode == COLOR_RED_WHITE_BLACK ? 3 :
                        (colorMode == COLOR_RAINBOW ? rainbowColors.length : 2));
                animator = ValueAnimator.ofFloat(0, cycleSize);
                break;
        }

        animator.setDuration(animationSpeed);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        final int currentAnimType = animType;
        final float[] lastRotationValue = {0f}; // 用于跟踪上一次的旋转值

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();

            switch (currentAnimType) {
                case ANIM_ZOOM:
                    scale = value;
                    break;
                case ANIM_ROTATE:
                    // 检测是否发生了循环（从360跳到0）
                    if (lastRotationValue[0] > 300 && value < 60) {
                        // 发生了循环，累积360度
                        accumulatedRotation += 360;
                    }
                    lastRotationValue[0] = value;
                    rotation = accumulatedRotation + value;
                    break;
                case ANIM_PULSE:
                    // 脉冲效果：交换颜色
                    if (value > 0.5f && animValue <= 0.5f) {
                        int temp = color1;
                        color1 = color2;
                        color2 = temp;
                    }
                    break;
                default:
                    break;
            }
            animValue = value;
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
        // 停止随机动画切换
        randomAnimHandler.removeCallbacks(randomAnimRunnable);
        scale = 1.0f;
        rotation = 0f;
        accumulatedRotation = 0f;
        animValue = 0;
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

    // ============ Setters with real-time update ============

    public void setColorMode(int mode) {
        this.colorMode = mode;
        updateColors();
        if (isAnimating) {
            stopAnimation();
            startAnimation();
        }
        invalidate();
    }

    public int getColorMode() {
        return colorMode;
    }

    public void setPatternType(int type) {
        this.patternType = type;
        invalidate();
    }

    public int getPatternType() {
        return patternType;
    }

    public void setAnimationType(int type) {
        this.animationType = type;
        if (isAnimating) {
            stopAnimation();
            startAnimation();
        }
    }

    public int getAnimationType() {
        return animationType;
    }

    public void setStripeWidth(int width) {
        this.stripeWidth = Math.max(10, Math.min(100, width));
        if (isAnimating) {
            stopAnimation();
            startAnimation();
        }
        invalidate();
    }

    public int getStripeWidth() {
        return stripeWidth;
    }

    public void setAnimationSpeed(long speedMs) {
        this.animationSpeed = Math.max(100, Math.min(5000, speedMs));
        if (animator != null && isAnimating) {
            animator.setDuration(animationSpeed);
        }
    }

    public long getAnimationSpeed() {
        return animationSpeed;
    }

    public void setReverseDirection(boolean reverse) {
        this.reverseDirection = reverse;
    }

    public boolean isReverseDirection() {
        return reverseDirection;
    }

    public void setVertical(boolean vertical) {
        isVertical = vertical;
        invalidate();
    }

    public boolean isVertical() {
        return isVertical;
    }

    public void toggleOrientation() {
        isVertical = !isVertical;
        invalidate();
    }

    public void setLevel(int level) {
        stripeWidth = Math.max(10, 60 - (level * 5));
        animationSpeed = Math.max(200, 1200 - (level * 100));
        if (animator != null) {
            animator.setDuration(animationSpeed);
        }
        if (isAnimating) {
            stopAnimation();
            startAnimation();
        }
        invalidate();
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    // ============ Static helper methods ============

    public static String getColorModeName(int mode) {
        switch (mode) {
            case COLOR_BLACK_WHITE: return "黑白";
            case COLOR_RED_BLUE: return "红蓝";
            case COLOR_RED_CYAN: return "红青";
            case COLOR_RED_WHITE: return "红白";
            case COLOR_BLUE_WHITE: return "蓝白";
            case COLOR_RED_WHITE_BLACK: return "红白黑";
            case COLOR_RAINBOW: return "彩虹";
            case COLOR_GREEN_MAGENTA: return "绿紫";
            case COLOR_YELLOW_BLUE: return "黄蓝";
            case COLOR_RED_TRANSPARENT: return "纯红";
            case COLOR_BLUE_TRANSPARENT: return "纯蓝";
            case COLOR_BLACK_TRANSPARENT: return "纯黑";
            case COLOR_WHITE_TRANSPARENT: return "纯白";
            default: return "未知";
        }
    }

    public static String getPatternTypeName(int type) {
        switch (type) {
            case PATTERN_STRIPES: return "条纹";
            case PATTERN_BLOCKS: return "方块";
            case PATTERN_CIRCLES: return "同心圆";
            case PATTERN_RADIAL: return "放射线";
            case PATTERN_SINE_WAVE: return "波浪";
            default: return "未知";
        }
    }

    public static String getAnimationTypeName(int type) {
        switch (type) {
            case ANIM_SCROLL: return "滚动";
            case ANIM_ZOOM: return "缩放";
            case ANIM_ROTATE: return "旋转";
            case ANIM_PULSE: return "闪烁";
            case ANIM_WAVE: return "波浪";
            case ANIM_RANDOM: return "随机";
            default: return "未知";
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
