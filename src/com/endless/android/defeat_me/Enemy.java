package com.endless.android.defeat_me;

import android.graphics.PointF;
import android.util.SparseArray;

public class Enemy {
    private boolean died;
    private SparseArray<Operation> operations;
    private PointF center;
    private int lastFrame;

    public Enemy() {
        died = true;
        operations = new SparseArray<Operation>();
        operations.put(0, new Operation());
        center = new PointF(DefeatMeView.screenWidth / 2, DefeatMeView.screenHeight / 10);
        lastFrame = 1;
    }

    public String toString() {
        return "size(): " + operations.size();
    }

    public boolean isDied() {
        return died;
    }

    public void setDied(boolean died) {
        this.died = died;
    }

    public Operation getOperation(int index) {
        return operations.get(index % lastFrame);
    }

    public void putOperation(int frame, Operation operation) {
        operations.put(frame, operation);
        lastFrame = frame + 1;
    }

    public PointF getCenter() {
        return center;
    }

    public void setCenter(PointF center) {
        this.center = center;
    }

    public void stopSomeFrame(int stopFrame) {
        putOperation(lastFrame + stopFrame, new Operation(center.x, center.y, 0));
    }

//    public void clear(){
//        died = true;
//        operations.clear();
//        operations.put(0, new Operation());
//        center.set(DefeatMeView.screenWidth / 2, DefeatMeView.screenHeight / 10);
//        lastFrame = 1;
//    }
}
