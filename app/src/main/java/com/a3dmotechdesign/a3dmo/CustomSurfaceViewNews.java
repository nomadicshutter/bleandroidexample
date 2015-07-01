package com.a3dmotechdesign.a3dmo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.a3dmotechdesign.a3dmo.DeviceControlActivity;

public class CustomSurfaceViewNews extends Activity implements OnTouchListener {

    private static final String TAG = "a3dmo";

    private DeviceControlActivity deviceControlActivity;
    private BluetoothLeService bluetoothLeService;

    private final static int NO_MOVEMENT=0;
    private final static int JOYSTICK_MOVE_DOWN=1;
    private final static int JOYSTICK_MOVE_UP=2;

    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NO_MOVEMENT: {
                    break;
                }
                case JOYSTICK_MOVE_DOWN:{
                    Log.e(TAG, "Handler Down");
                    new DeviceControlActivity().makeChangeDownPressed();
                    break;
                }
                case JOYSTICK_MOVE_UP:{
                    Log.e(TAG, "Handler Up");
                    new DeviceControlActivity().makeChangeUpPressed();
                    break;
                }
            }
//            super.handleMessage(msg);
        }
    };

    OurView v;
    Paint paint1;
    float x, y, dx, dy, c, angle;
    Bitmap ball, max;
    int zeroX, zeroY, radius;
    Button customButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = new OurView(this);
        v.setOnTouchListener(this);

        paint1 = new Paint();
        paint1.setTextSize(40);
        radius = 175;
        dx = dy = 0;
        paint1.setColor(Color.rgb(255, 0, 0));

        ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        max = BitmapFactory.decodeResource(getResources(), R.drawable.max);
        x = y = 0;

        customButton = (Button) findViewById(R.id.custom_sv_Button);

//        customButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                new DeviceControlActivity().makeChangeDownPressed();
//                Log.e(TAG, "Custom SurfaceView Button Pressed");
//
//            }
//        });

        setContentView(v);
    }

    @Override
    protected void onPause() {
        super.onPause();
        v.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        v.resume();
    }

    public class OurView extends SurfaceView implements Runnable {

 //       private final Handler handler;

        Thread t = null;

        SurfaceHolder holder;
        boolean isItOk = false;

        public OurView(Context context) {
            super(context);
            holder = getHolder();
//            this.handler = handler;
        }

        public void run() {

            Message msg = null;
            msg = handler.obtainMessage(NO_MOVEMENT);
            handler.sendMessage(msg);

            while (isItOk == true) {
                if (!holder.getSurface().isValid()) {
                    continue;
                }

                Canvas c = holder.lockCanvas();
                c.drawARGB(255, 150, 150, 10);
                c.drawText(Float.toString(x), 20, 40, paint1);
                c.drawText(Float.toString(y), 20, 200, paint1);
                c.drawBitmap(ball, x - (ball.getWidth() / 2), y - (ball.getHeight() / 2), paint1);
                holder.unlockCanvasAndPost(c);

                if (y > 800) {
                    msg = handler.obtainMessage(JOYSTICK_MOVE_DOWN);
                    handler.sendMessage(msg);
                }
                if (y < 400) {
                    msg = handler.obtainMessage(JOYSTICK_MOVE_UP);
                    handler.sendMessage(msg);
                }
                sleep();
            }
        }

        private void sleep(){
            try{
                Thread.sleep(200);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        public void pause() {
            isItOk = false;
            while (true) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            t = null;
        }

        public void resume() {
            isItOk = true;
            t = new Thread(this);
            t.start();
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent me) {

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switch (me.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = me.getX();
                y = me.getY();
                break;
            case MotionEvent.ACTION_UP:
                x = me.getX();
                y = me.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                x = me.getX();
                y = me.getY();
                break;
        }
        return true;
    }
}

