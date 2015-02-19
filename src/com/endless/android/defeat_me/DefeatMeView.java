package com.endless.android.defeat_me;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefeatMeView extends SurfaceView implements Runnable,
        SurfaceHolder.Callback {
    private static final String TAG = DefeatMeView.class.getSimpleName();

    private static final int[] COLORS_ARRAY = {
            Color.rgb(159, 224, 246), // soft blue
            Color.rgb(151, 236, 133), // soft green
            Color.rgb(250, 110, 134), // soft pink
            Color.rgb(243, 229, 154), // soft yellow
            Color.rgb(222, 157, 214), // soft purple
            Color.rgb(177, 148, 153), // soft chocolate
    };

    private static final float PLAYER_R = 60.0f;
    private static final float ENEMY_R = 60.0f;

    private static final long FPS = 55;
    private static final long FRAME_TIME = 1000L / FPS;

    public static float screenWidth;
    public static float screenHeight;

    private int frameCount;

    private SurfaceHolder surfaceHolder;
    private Thread thread;
    private Canvas canvas;
    private Paint paint;
    private Paint bgPaint;

    private PointF playerCenter;

    private Set<Operation> bullets;
    private Set<Operation> addBullets;
    private Set<Operation> removeBullets;

    private ArrayList<Enemy> enemies;
    private Enemy nextEnemy;
    private int leftEnemies;

    private boolean inCircle;
    private boolean loseOut;

    public DefeatMeView(Context context) {
        this(context, null);
    }

    public DefeatMeView(Context context, AttributeSet attrs) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        bullets = Collections.synchronizedSet(new HashSet<Operation>());
        addBullets = Collections.synchronizedSet(new HashSet<Operation>());
        removeBullets = Collections.synchronizedSet(new HashSet<Operation>());
        enemies = new ArrayList<Enemy>();
        playerCenter = new PointF();

        bgPaint = new Paint();
        bgPaint.setStyle(Style.FILL);
        bgPaint.setColor(Color.WHITE);

        paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(COLORS_ARRAY[0]);
        paint.setTextSize(100);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        screenWidth = width;
        screenHeight = height;

        startGame();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread = null;
    }

    private void startGame() {
        loseOut = false;

        enemies.clear();
        enemies.add(new Enemy());

        newLevel();
    }

    private void newLevel() {
        addBullets.clear();
        removeBullets.clear();
        bullets.clear();

        playerCenter.set(screenWidth / 2, screenHeight - screenHeight / 10);
        leftEnemies = enemies.size();
        for (Enemy enemy : enemies) {
            enemy.setDied(false);
        }
        nextEnemy = new Enemy();

        frameCount = 0;
    }

    private void endLevel() {
        nextEnemy.stopSomeFrame(30);
        enemies.add(nextEnemy);
        newLevel();
    }

    @Override
    public void run() {
        long waitTime = 0L;
        long startTime = System.currentTimeMillis();

        for (long endTime = FRAME_TIME; thread != null; endTime += FRAME_TIME, ++frameCount) {
            canvas = surfaceHolder.lockCanvas();

            canvas.drawRect(0.0f, 0.0f, screenWidth, screenHeight, bgPaint);
            paint.setColor(COLORS_ARRAY[enemies.size() % COLORS_ARRAY.length]);
            canvas.drawText(String.valueOf(enemies.size()), 20, 100, paint);
            canvas.drawCircle(playerCenter.x, playerCenter.y, PLAYER_R, paint);
            for (int i = 0; i < enemies.size(); ++i) {
                Enemy enemy = enemies.get(i);
                if (!enemy.isDied()) {
                    paint.setColor(COLORS_ARRAY[i % COLORS_ARRAY.length]);
                    drawOperation(enemy);
                }
            }
            drawBullets();
            if (leftEnemies == 0) endLevel();
            if (loseOut) startGame();

            surfaceHolder.unlockCanvasAndPost(canvas);

            waitTime = endTime - (System.currentTimeMillis() - startTime);
            if (waitTime > 0L) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                checkDownPositionInCircle(event.getX(), event.getY());
                if (!inCircle) shoot();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                shoot();
                break;
            case MotionEvent.ACTION_MOVE:
                if (inCircle) move(event.getX(), event.getY());
                break;
        }
        return true;
    }

    private void checkDownPositionInCircle(float x, float y) {
        inCircle = (x - playerCenter.x) * (x - playerCenter.x) + (y - playerCenter.y) * (y - playerCenter.y) < PLAYER_R * PLAYER_R;
    }

    private void move(float x, float y) {
        if (x < PLAYER_R) x = PLAYER_R;
        else if (x + PLAYER_R > screenWidth) x = screenWidth - PLAYER_R;
        if (y < PLAYER_R) y = PLAYER_R;
        else if (y + PLAYER_R > screenHeight) y = screenHeight - PLAYER_R;
        playerCenter.set(x, y);
        nextEnemy.putOperation(frameCount, new Operation(x, screenHeight - y, 0));
        Log.i(TAG, "frame: " + frameCount + " move: " + nextEnemy.toString());
    }

    private void shoot() {
        addBullets.add(new Operation(playerCenter.x, playerCenter.y, -enemies.size()));
        nextEnemy.putOperation(frameCount, new Operation(playerCenter.x, screenHeight - playerCenter.y, enemies.size()));
        Log.i(TAG, "frame: " + frameCount + " shoot: " + nextEnemy.toString());
    }

    private void drawBullets() {
        bullets.addAll(addBullets);
        addBullets.clear();
        for (Operation bullet : bullets) {
            if (loseOut) return;
            if (!bullet.changePos() || checkCollided(bullet)) {
                removeBullets.add(bullet);
            }
            paint.setColor(COLORS_ARRAY[(bullet.isPlayerBullet() ? enemies.size() : bullet.getType()) % COLORS_ARRAY.length]);
            canvas.drawCircle(bullet.getCenter().x, bullet.getCenter().y, Operation.R, paint);
        }
        bullets.removeAll(removeBullets);
        removeBullets.clear();
    }

    private boolean checkCollided(Operation operation) {
        if (operation.isPlayerBullet()) {
            for (Enemy enemy : enemies) {
                if (!enemy.isDied() && checkTwoCircleCollided(operation.getCenter(), enemy.getCenter())) {
                    enemy.setDied(true);
                    --leftEnemies;
                    return true;
                }
            }
        } else { // operation.isEnemyBullet()
            if (checkTwoCircleCollided(operation.getCenter(), playerCenter)) {
                loseOut = true;
                return true;
            }
        }
        return false;
    }

    private boolean checkTwoCircleCollided(PointF p1, PointF p2) {
        return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) < (Operation.R + PLAYER_R) * (Operation.R + PLAYER_R);
    }

    private void drawOperation(Enemy enemy) {
        if (enemy.getOperation(frameCount) == null) {
            canvas.drawCircle(enemy.getCenter().x, enemy.getCenter().y, ENEMY_R, paint);
            return;
        }
        Operation operation = new Operation(enemy.getOperation(frameCount));
        if (operation.isMove()) {
            canvas.drawCircle(operation.getCenter().x, operation.getCenter().y, ENEMY_R, paint);
            enemy.setCenter(operation.getCenter());
        } else { // shoot
            addBullets.add(operation);
        }
    }
}