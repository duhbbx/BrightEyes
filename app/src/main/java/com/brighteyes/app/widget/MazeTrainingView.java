package com.brighteyes.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.brighteyes.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * 迷宫训练游戏
 * 带有红蓝光栅覆盖层，训练弱视眼
 */
public class MazeTrainingView extends View {

    public interface OnMazeEventListener {
        void onMazeComplete(long timeMs, int moves);
        void onMove();
        void onGameOver();  // 碰到虫子游戏结束
    }

    private Paint wallPaint;
    private Paint pathPaint;
    private Paint playerPaint;
    private Paint goalPaint;
    private Paint gratingPaint;
    private Paint textPaint;

    // 迷宫参数
    private int mazeWidth = 21;   // 迷宫宽度（格子数）- 更大更复杂
    private int mazeHeight = 13;  // 迷宫高度（格子数）- 更大更复杂
    private int[][] maze;         // 0=通道, 1=墙壁
    private float cellSize;       // 每个格子的像素大小

    // 玩家位置
    private int playerX = 1;
    private int playerY = 1;

    // 终点位置
    private int goalX;
    private int goalY;

    // 游戏状态
    private boolean isActive = false;
    private long startTime = 0;
    private int moveCount = 0;

    // 光栅参数
    private boolean showGrating = true;
    private float gratingStripeWidth = 15;
    private float gratingAlpha = 0.5f;
    private float gratingOffset = 0;
    private boolean isRedBlue = true;  // true=红蓝, false=黑白

    // 迷宫显示区域
    private RectF mazeArea;

    private Random random = new Random();
    private OnMazeEventListener listener;

    // 车辆图标
    private Drawable[] vehicleDrawables;
    private Drawable currentVehicle;
    private int currentVehicleIndex = 0;
    private String[] vehicleNames = {"小汽车", "挖掘机", "洒水车", "吊车"};

    // 虫子/敌人
    private Drawable[] bugDrawables;
    private List<Bug> bugs = new ArrayList<>();
    private static final int BUG_COUNT = 1;  // 只放一个虫子装装样子
    private Handler bugHandler = new Handler(Looper.getMainLooper());
    private Runnable bugMoveRunnable;
    private static final long BUG_MOVE_INTERVAL = 1500; // 虫子移动间隔（更慢）

    // 虫子类
    private static class Bug {
        int x, y;
        int drawableIndex;
        int direction; // 0=上, 1=右, 2=下, 3=左

        Bug(int x, int y, int drawableIndex) {
            this.x = x;
            this.y = y;
            this.drawableIndex = drawableIndex;
            this.direction = 0;
        }
    }

    public MazeTrainingView(Context context) {
        super(context);
        init();
    }

    public MazeTrainingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MazeTrainingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wallPaint = new Paint();
        wallPaint.setColor(Color.rgb(40, 40, 60));
        wallPaint.setStyle(Paint.Style.FILL);

        pathPaint = new Paint();
        pathPaint.setColor(Color.rgb(200, 200, 200));
        pathPaint.setStyle(Paint.Style.FILL);

        playerPaint = new Paint();
        playerPaint.setColor(Color.rgb(50, 150, 250));
        playerPaint.setAntiAlias(true);
        playerPaint.setStyle(Paint.Style.FILL);

        goalPaint = new Paint();
        goalPaint.setColor(Color.rgb(50, 200, 50));
        goalPaint.setAntiAlias(true);
        goalPaint.setStyle(Paint.Style.FILL);

        gratingPaint = new Paint();
        gratingPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(28);

        setBackgroundColor(Color.rgb(30, 30, 40));

        // 加载车辆图标
        vehicleDrawables = new Drawable[]{
                ContextCompat.getDrawable(getContext(), R.drawable.ic_car),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_excavator),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_sprinkler),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_crane)
        };

        // 加载虫子图标
        bugDrawables = new Drawable[]{
                ContextCompat.getDrawable(getContext(), R.drawable.ic_bug1),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_bug2),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_bug3)
        };

        // 虫子移动逻辑
        bugMoveRunnable = new Runnable() {
            @Override
            public void run() {
                if (isActive) {
                    moveBugs();
                    invalidate();
                    bugHandler.postDelayed(this, BUG_MOVE_INTERVAL);
                }
            }
        };
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

        // 计算迷宫区域（留出边距和顶部信息区域）
        int margin = 20;
        int topMargin = 80;
        int availableWidth = width - margin * 2;
        int availableHeight = height - topMargin - margin;

        // 计算格子大小
        float cellW = (float) availableWidth / mazeWidth;
        float cellH = (float) availableHeight / mazeHeight;
        cellSize = Math.min(cellW, cellH);

        // 居中显示迷宫
        float mazePixelWidth = cellSize * mazeWidth;
        float mazePixelHeight = cellSize * mazeHeight;
        float startX = (width - mazePixelWidth) / 2;
        float startY = topMargin + (availableHeight - mazePixelHeight) / 2;

        mazeArea = new RectF(startX, startY,
                startX + mazePixelWidth, startY + mazePixelHeight);
    }

    /**
     * 使用深度优先搜索生成迷宫
     */
    private void generateMaze() {
        maze = new int[mazeHeight][mazeWidth];

        // 初始化全部为墙
        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                maze[y][x] = 1;
            }
        }

        // DFS生成迷宫
        Stack<int[]> stack = new Stack<>();
        int startX = 1;
        int startY = 1;
        maze[startY][startX] = 0;
        stack.push(new int[]{startX, startY});

        int[] dx = {0, 2, 0, -2};
        int[] dy = {-2, 0, 2, 0};

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int cx = current[0];
            int cy = current[1];

            // 找到所有未访问的邻居
            List<Integer> neighbors = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                if (nx > 0 && nx < mazeWidth - 1 && ny > 0 && ny < mazeHeight - 1) {
                    if (maze[ny][nx] == 1) {
                        neighbors.add(i);
                    }
                }
            }

            if (neighbors.isEmpty()) {
                stack.pop();
            } else {
                // 随机选择一个邻居
                int dir = neighbors.get(random.nextInt(neighbors.size()));
                int nx = cx + dx[dir];
                int ny = cy + dy[dir];

                // 打通墙壁
                maze[cy + dy[dir] / 2][cx + dx[dir] / 2] = 0;
                maze[ny][nx] = 0;

                stack.push(new int[]{nx, ny});
            }
        }

        // 设置起点和终点
        playerX = 1;
        playerY = 1;

        // 终点在右下角区域
        goalX = mazeWidth - 2;
        goalY = mazeHeight - 2;

        // 确保终点是通道
        maze[goalY][goalX] = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 绘制标题和信息
        textPaint.setTextSize(26);
        String vehicleName = currentVehicle != null ? vehicleNames[currentVehicleIndex] : "小车";
        canvas.drawText("驾驶" + vehicleName + "到达终点，小心虫子！", width / 2f, 35, textPaint);

        if (isActive && startTime > 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            int seconds = (int) (elapsed / 1000);
            String info = String.format("时间: %d秒  |  步数: %d", seconds, moveCount);
            textPaint.setTextSize(22);
            canvas.drawText(info, width / 2f, 65, textPaint);
        }

        if (maze == null || mazeArea == null) return;

        // 绘制迷宫
        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                float left = mazeArea.left + x * cellSize;
                float top = mazeArea.top + y * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;

                if (maze[y][x] == 1) {
                    // 墙壁
                    canvas.drawRect(left, top, right, bottom, wallPaint);
                } else {
                    // 通道
                    canvas.drawRect(left, top, right, bottom, pathPaint);
                }
            }
        }

        // 绘制终点
        float goalLeft = mazeArea.left + goalX * cellSize;
        float goalTop = mazeArea.top + goalY * cellSize;
        float goalCx = goalLeft + cellSize / 2;
        float goalCy = goalTop + cellSize / 2;
        canvas.drawCircle(goalCx, goalCy, cellSize * 0.4f, goalPaint);

        // 绘制旗帜图标
        Paint flagPaint = new Paint();
        flagPaint.setColor(Color.WHITE);
        flagPaint.setTextAlign(Paint.Align.CENTER);
        flagPaint.setTextSize(cellSize * 0.5f);
        canvas.drawText("⚑", goalCx, goalCy + cellSize * 0.15f, flagPaint);

        // 绘制虫子
        for (Bug bug : bugs) {
            float bugLeft = mazeArea.left + bug.x * cellSize;
            float bugTop = mazeArea.top + bug.y * cellSize;
            Drawable bugDrawable = bugDrawables[bug.drawableIndex];
            if (bugDrawable != null) {
                int padding = (int) (cellSize * 0.1f);
                bugDrawable.setBounds(
                        (int) bugLeft + padding,
                        (int) bugTop + padding,
                        (int) (bugLeft + cellSize) - padding,
                        (int) (bugTop + cellSize) - padding
                );
                bugDrawable.draw(canvas);
            }
        }

        // 绘制玩家车辆
        float playerLeft = mazeArea.left + playerX * cellSize;
        float playerTop = mazeArea.top + playerY * cellSize;
        if (currentVehicle != null) {
            int padding = (int) (cellSize * 0.05f);
            currentVehicle.setBounds(
                    (int) playerLeft + padding,
                    (int) playerTop + padding,
                    (int) (playerLeft + cellSize) - padding,
                    (int) (playerTop + cellSize) - padding
            );
            currentVehicle.draw(canvas);
        } else {
            // 后备：绘制圆形
            float playerCx = playerLeft + cellSize / 2;
            float playerCy = playerTop + cellSize / 2;
            canvas.drawCircle(playerCx, playerCy, cellSize * 0.35f, playerPaint);
        }

        // 绘制光栅覆盖层
        if (showGrating) {
            drawGrating(canvas);
        }
    }

    private void drawGrating(Canvas canvas) {
        if (mazeArea == null) return;

        int alpha = (int) (255 * gratingAlpha);

        // 垂直条纹
        boolean isRed = true;
        for (float x = mazeArea.left + gratingOffset; x < mazeArea.right; x += gratingStripeWidth * 2) {
            if (isRedBlue) {
                gratingPaint.setColor(isRed ?
                        Color.argb(alpha, 255, 0, 0) :
                        Color.argb(alpha, 0, 255, 255));
            } else {
                gratingPaint.setColor(isRed ?
                        Color.argb(alpha, 0, 0, 0) :
                        Color.argb(alpha, 255, 255, 255));
            }

            float stripeLeft = Math.max(x, mazeArea.left);
            float stripeRight = Math.min(x + gratingStripeWidth, mazeArea.right);

            if (stripeRight > stripeLeft) {
                canvas.drawRect(stripeLeft, mazeArea.top, stripeRight, mazeArea.bottom, gratingPaint);
            }

            isRed = !isRed;
        }
    }

    /**
     * 移动玩家
     * @param direction 0=上, 1=右, 2=下, 3=左
     */
    public void movePlayer(int direction) {
        if (!isActive || maze == null) return;

        int newX = playerX;
        int newY = playerY;

        switch (direction) {
            case 0: // 上
                newY--;
                break;
            case 1: // 右
                newX++;
                break;
            case 2: // 下
                newY++;
                break;
            case 3: // 左
                newX--;
                break;
        }

        // 检查是否可以移动
        if (newX >= 0 && newX < mazeWidth && newY >= 0 && newY < mazeHeight) {
            if (maze[newY][newX] == 0) {
                playerX = newX;
                playerY = newY;
                moveCount++;

                if (listener != null) {
                    listener.onMove();
                }

                // 检查是否到达终点
                if (playerX == goalX && playerY == goalY) {
                    completeMaze();
                } else if (checkBugCollision()) {
                    gameOver();
                }

                invalidate();
            }
        }
    }

    /**
     * 检查是否与虫子碰撞
     */
    private boolean checkBugCollision() {
        for (Bug bug : bugs) {
            if (bug.x == playerX && bug.y == playerY) {
                return true;
            }
        }
        return false;
    }

    /**
     * 游戏结束（碰到虫子）
     */
    private void gameOver() {
        isActive = false;
        bugHandler.removeCallbacks(bugMoveRunnable);

        if (listener != null) {
            listener.onGameOver();
        }
    }

    /**
     * 移动虫子
     */
    private void moveBugs() {
        for (Bug bug : bugs) {
            // 获取可移动的方向
            List<Integer> validDirections = new ArrayList<>();
            int[] dx = {0, 1, 0, -1};
            int[] dy = {-1, 0, 1, 0};

            for (int dir = 0; dir < 4; dir++) {
                int nx = bug.x + dx[dir];
                int ny = bug.y + dy[dir];
                if (nx >= 0 && nx < mazeWidth && ny >= 0 && ny < mazeHeight) {
                    if (maze[ny][nx] == 0) {
                        validDirections.add(dir);
                    }
                }
            }

            if (!validDirections.isEmpty()) {
                // 优先继续当前方向，有50%概率
                if (validDirections.contains(bug.direction) && random.nextFloat() < 0.5f) {
                    // 继续当前方向
                } else {
                    // 随机选择新方向
                    bug.direction = validDirections.get(random.nextInt(validDirections.size()));
                }

                bug.x += dx[bug.direction];
                bug.y += dy[bug.direction];
            }

            // 检查虫子是否碰到玩家
            if (bug.x == playerX && bug.y == playerY) {
                gameOver();
                return;
            }
        }
    }

    /**
     * 生成虫子
     */
    private void spawnBugs() {
        bugs.clear();

        // 收集所有可用的通道位置（排除起点、终点及其附近）
        List<int[]> validPositions = new ArrayList<>();
        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                if (maze[y][x] == 0) {
                    // 排除起点附近
                    if (Math.abs(x - playerX) + Math.abs(y - playerY) < 3) continue;
                    // 排除终点附近
                    if (Math.abs(x - goalX) + Math.abs(y - goalY) < 2) continue;

                    validPositions.add(new int[]{x, y});
                }
            }
        }

        // 随机选择位置放置虫子
        int bugCount = Math.min(BUG_COUNT, validPositions.size());
        for (int i = 0; i < bugCount && !validPositions.isEmpty(); i++) {
            int index = random.nextInt(validPositions.size());
            int[] pos = validPositions.remove(index);
            int drawableIndex = random.nextInt(bugDrawables.length);
            Bug bug = new Bug(pos[0], pos[1], drawableIndex);
            bug.direction = random.nextInt(4);
            bugs.add(bug);
        }
    }

    public void moveUp() {
        movePlayer(0);
    }

    public void moveRight() {
        movePlayer(1);
    }

    public void moveDown() {
        movePlayer(2);
    }

    public void moveLeft() {
        movePlayer(3);
    }

    private void completeMaze() {
        isActive = false;
        long elapsed = System.currentTimeMillis() - startTime;
        bugHandler.removeCallbacks(bugMoveRunnable);

        if (listener != null) {
            listener.onMazeComplete(elapsed, moveCount);
        }
    }

    public void startGame() {
        isActive = true;
        moveCount = 0;
        startTime = System.currentTimeMillis();

        // 随机选择一个车辆
        currentVehicleIndex = random.nextInt(vehicleDrawables.length);
        currentVehicle = vehicleDrawables[currentVehicleIndex];

        if (getWidth() > 0 && getHeight() > 0) {
            calculateLayout();
            generateMaze();
            spawnBugs();
            // 启动虫子移动
            bugHandler.postDelayed(bugMoveRunnable, BUG_MOVE_INTERVAL);
            invalidate();
        } else {
            post(() -> {
                calculateLayout();
                generateMaze();
                spawnBugs();
                // 启动虫子移动
                bugHandler.postDelayed(bugMoveRunnable, BUG_MOVE_INTERVAL);
                invalidate();
            });
        }
    }

    public void stopGame() {
        isActive = false;
        bugHandler.removeCallbacks(bugMoveRunnable);
        bugs.clear();
    }

    public void setMazeSize(int width, int height) {
        // 确保是奇数（迷宫生成算法需要）
        this.mazeWidth = width % 2 == 0 ? width + 1 : width;
        this.mazeHeight = height % 2 == 0 ? height + 1 : height;
        this.mazeWidth = Math.max(7, Math.min(31, this.mazeWidth));
        this.mazeHeight = Math.max(7, Math.min(21, this.mazeHeight));
    }

    public void setShowGrating(boolean show) {
        this.showGrating = show;
        invalidate();
    }

    public void setGratingStripeWidth(float width) {
        this.gratingStripeWidth = Math.max(5, Math.min(50, width));
        invalidate();
    }

    public void setGratingAlpha(float alpha) {
        this.gratingAlpha = Math.max(0.1f, Math.min(0.8f, alpha));
        invalidate();
    }

    public void setRedBlueGrating(boolean redBlue) {
        this.isRedBlue = redBlue;
        invalidate();
    }

    public void setGratingOffset(float offset) {
        this.gratingOffset = offset;
        invalidate();
    }

    public void setOnMazeEventListener(OnMazeEventListener listener) {
        this.listener = listener;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public long getElapsedTime() {
        if (startTime == 0) return 0;
        return System.currentTimeMillis() - startTime;
    }

    public String getCurrentVehicleName() {
        if (currentVehicleIndex >= 0 && currentVehicleIndex < vehicleNames.length) {
            return vehicleNames[currentVehicleIndex];
        }
        return "小车";
    }

    /**
     * 暂停虫子移动
     */
    public void pauseBugs() {
        bugHandler.removeCallbacks(bugMoveRunnable);
    }

    /**
     * 恢复虫子移动
     */
    public void resumeBugs() {
        if (isActive) {
            bugHandler.postDelayed(bugMoveRunnable, BUG_MOVE_INTERVAL);
        }
    }
}
