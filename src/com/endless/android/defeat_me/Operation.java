package com.endless.android.defeat_me;

import android.graphics.PointF;

public class Operation {
    public static float R = 10.0f;

    public static final float[][] BULLET_DXDY = {
            {0.0f, 18.0f},
            {0.0f, 18.0f},
            {0.0f, 18.0f, -9.0f * (float) Math.sqrt(2.0f), 9.0f * (float) Math.sqrt(2.0f), 9.0f * (float) Math.sqrt(2.0f), 9.0f * (float) Math.sqrt(2.0f)},
            {0.0f, 18.0f, -9.0f * (float) Math.sqrt(2.0f), 9.0f * (float) Math.sqrt(2.0f), 9.0f * (float) Math.sqrt(2.0f), 9.0f * (float) Math.sqrt(2.0f)},
    };

    private PointF center;
    private int type;

    public Operation() {
        center = new PointF(DefeatMeView.screenWidth / 2, DefeatMeView.screenHeight / 10);
        type = 0;
    }

    public Operation(float x, float y, int type) {
        center = new PointF(x, y);
        this.type = type;
    }

    public Operation(Operation operation) {
        center = new PointF(operation.center.x, operation.center.y);
        type = operation.type;
    }

    public String toString() {
        return "x: " + center.x + " y: " + center.y + " type: " + type;
    }

    public PointF getCenter() {
        return center;
    }

    public int getType() {
        return type;
    }

    public int getNormalType() {
        return Math.abs(type) - 1;
    }

    public boolean isPlayerBullet() {
        return type < 0;
    }

    public boolean isMove() {
        return type == 0;
    }

    public boolean isEnemyBullet() {
        return type > 0;
    }

//    public int getType() {
//        return type;
//    }

    public boolean changePos() {
        int ttype = getNormalType() % BULLET_DXDY.length;
        if (isPlayerBullet()) {
            center.x -= BULLET_DXDY[ttype][0];
            center.y -= BULLET_DXDY[ttype][1];
        } else { // isEnemyBullet()
            center.x += BULLET_DXDY[ttype][0];
            center.y += BULLET_DXDY[ttype][1];
        }
        return center.x >= 0 && center.x <= DefeatMeView.screenWidth &&
                center.y >= 0 && center.y <= DefeatMeView.screenHeight;
    }
}
