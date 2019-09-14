package com.example.exercise4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;

public class JoystickView extends View {
    private Paint outerColor;
    private Paint innerColor;
    private Paint backgroundColor;

    private int CENTER_X;
    private int CENTER_Y;
    private int currX;
    private int currY;
    private int OUTER_RADIUS;
    private int INNER_RADIUS;

    private int statusBarHeight;

    private boolean isScreenOriented;

    public JoystickView(Context context) {
        super(context);

        // set the colors of the background and the joystick components
        this.outerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.outerColor.setColor(Color.GRAY);
        this.outerColor.setStyle(Paint.Style.FILL);

        this.innerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.innerColor.setColor(Color.rgb(244, 163, 0));
        this.innerColor.setStyle(Paint.Style.FILL);

        this.backgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.backgroundColor.setColor(Color.rgb(0, 128, 128));
        this.backgroundColor.setStyle(Paint.Style.FILL);

        // get the dimensions of the actual screen
        DisplayMetrics dm = getResources().getDisplayMetrics();
        currX = CENTER_X = dm.widthPixels / 2;
        currY = CENTER_Y = dm.heightPixels / 2;
        OUTER_RADIUS = dm.widthPixels / 3;
        INNER_RADIUS = OUTER_RADIUS / 3;

        // account for padding with the status bar
        this.statusBarHeight = this.getStatusBarHeight() * (int) dm.density;

        this.isScreenOriented = true;
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, CENTER_X * 2, CENTER_Y * 2, backgroundColor);
        canvas.drawCircle(CENTER_X, CENTER_Y - statusBarHeight, OUTER_RADIUS, outerColor);
        canvas.drawCircle(currX, currY - statusBarHeight, INNER_RADIUS, innerColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.isScreenOriented = true;

        currX = CENTER_X = w / 2;
        currY = CENTER_Y = (h + statusBarHeight) / 2;

        OUTER_RADIUS = Math.min(CENTER_X, CENTER_Y) * 2 / 3;
        INNER_RADIUS = OUTER_RADIUS / 3;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int getCurrX() {
        return this.currX;
    }

    public int getCurrY() {
        return this.currY;
    }

    public boolean isScreenOriented() {
        return this.isScreenOriented;
    }

    public int[] getDisplayArgs() {
        return new int[]{CENTER_X, CENTER_Y, OUTER_RADIUS, INNER_RADIUS};
    }

    public void setX(int x) {
        this.currX = x;
    }

    public void setY(int y) {
        this.currY = y;
    }

    public void notifyArgsUpdated() {
        this.isScreenOriented = false;
    }
}
