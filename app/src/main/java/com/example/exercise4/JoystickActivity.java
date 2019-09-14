package com.example.exercise4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class JoystickActivity extends AppCompatActivity {

    private Client client;
    private JoystickView joystickView;
    private boolean isTouchingJoystick;
    private boolean isExceptionOccurred;

    // holding the most used values of JoystickView
    private int jsCenterX;
    private int jsCenterY;
    private int jsOuterRadius;
    private int jsInnerRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.joystickView = new JoystickView(this);
        this.isExceptionOccurred = false;
        this.establishConnection();
    }

    private void establishConnection() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = getIntent();
                    String ip = intent.getStringExtra("ip");
                    int port = Integer.parseInt(intent.getStringExtra("port"));
                    client = new Client();
                    client.connect(ip, port);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });
        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                System.out.println("Uncaught exception: " + ex);
                isExceptionOccurred = true;
            }
        };
        t.setUncaughtExceptionHandler(h);
        t.start();
        try {
            t.join();
            if (!isExceptionOccurred) {
                setContentView(this.joystickView);
                this.isTouchingJoystick = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get the updated values based on the orientation of the screen
        if (this.joystickView.isScreenOriented()) {
            int[] args = this.joystickView.getDisplayArgs();
            this.jsCenterX = args[0];
            this.jsCenterY = args[1];
            this.jsOuterRadius = args[2];
            this.jsInnerRadius = args[3];
            this.joystickView.notifyArgsUpdated();
        }

        int action = event.getActionMasked();
        int touchX = (int) event.getRawX();
        int touchY = (int) event.getRawY();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // if the touch happened outside the joystick then ignore it
                if (!this.isInsideJoystick(touchX, touchY)) {
                    return false;
                }
                // otherwise, update the flag for upcoming move actions
                this.isTouchingJoystick = true;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!this.isTouchingJoystick) {
                    return false;
                }
                // used to normalize the values
                double distance = this.distance(touchX, touchY, this.jsCenterX, this.jsCenterY);
                double magnitude = (distance + this.jsInnerRadius) / this.jsOuterRadius;
                if (magnitude >= 1) {
                    magnitude = 1;
                }
                // get the values to send
                double angle = this.getAngle(touchX - this.jsCenterX, touchY - this.jsCenterY);
                float elevator = (float) (Math.sin(Math.toRadians(angle)) * magnitude * -1);
                float aileron = (float) (Math.cos(Math.toRadians(angle)) * magnitude);

                this.client.sendCommand("elevator", String.valueOf(elevator));
                this.client.sendCommand("aileron", String.valueOf(aileron));

                // draw the new position
                int[] newPos = this.getAdjustedPosition(touchX, touchY, angle, distance);
                this.updateJoystickPosition(newPos[0], newPos[1]);
                break;
            }
            case MotionEvent.ACTION_UP:         // fallthrough
            case MotionEvent.ACTION_CANCEL: {
                // place the joystick in its original position
                this.updateJoystickPosition(this.jsCenterX, this.jsCenterY);
                this.isTouchingJoystick = false;
                break;
            }
        }
        return true;
    }

    private boolean isInsideJoystick(int touchX, int touchY) {
        return this.distance(touchX, touchY, this.joystickView.getCurrX(), this.joystickView.getCurrY()) <=
                this.jsInnerRadius;
    }

    private double distance(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private double getAngle(float dx, float dy) {
        if (dx >= 0 && dy >= 0) return Math.toDegrees(Math.atan(dy / dx));
        else if (dx < 0 && dy >= 0) return Math.toDegrees(Math.atan(dy / dx)) + 180;
        else if (dx < 0 && dy < 0) return Math.toDegrees(Math.atan(dy / dx)) + 180;
        else if (dx >= 0 && dy < 0) return Math.toDegrees(Math.atan(dy / dx)) + 360;
        return 0;
    }

    private int[] getAdjustedPosition(int touchX, int touchY, double angle, double distanceFromCenter) {
        // if the position isn't outside the joystick, return the original values
        if (distanceFromCenter + this.jsInnerRadius <= this.jsOuterRadius) {
            return new int[]{touchX, touchY};
        }
        // placing the joystick on the edge of the pad according to the relative position to the center
        int newX = this.jsCenterX + (int) (Math.cos(Math.toRadians(angle)) * (jsOuterRadius - jsInnerRadius));
        int newY = this.jsCenterY + (int) (Math.sin(Math.toRadians(angle)) * (jsOuterRadius - jsInnerRadius));
        return new int[]{newX, newY};
    }

    private void updateJoystickPosition(int newX, int newY) {
        this.joystickView.setX(newX);
        this.joystickView.setY(newY);
        this.joystickView.postInvalidate();
    }

    @Override
    protected void onDestroy() {
        this.client.disconnect();
        super.onDestroy();
    }
}