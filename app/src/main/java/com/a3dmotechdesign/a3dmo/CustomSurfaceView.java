package com.a3dmotechdesign.a3dmo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.a3dmotechdesign.a3dmo.DeviceControlActivity;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "a3dmo";
    private DeviceControlActivity deviceControlActivity;

    MySurfaceThread thread;
    Paint paint1;
    boolean run =true;
    float x, y, dx, dy, c, angle;
    Bitmap ball, max;
    int zeroX, zeroY, radius;

    public CustomSurfaceView(Context context) {
        super(context);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        //Log.i(TAG,"Surface Init");
        thread = new MySurfaceThread(getHolder(),this);
        getHolder().addCallback(this);
        paint1 = new Paint();
        paint1.setTextSize(40);
        radius=175;
        dx = dy = 0;
        paint1.setColor(Color.rgb(255, 0, 0));
        ball = BitmapFactory.decodeResource(getResources(),R.drawable.ball);
        max = BitmapFactory.decodeResource(getResources(),R.drawable.max);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.i(TAG,"Surface Created");
        thread.execute((Void[]) null);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    public void sendmovedata(){


        if (dx > 0.0) {
            //new DeviceControlActivity().makeChangeRightPressed();
            Log.i(TAG, Float.toString(dx));
        }
        if (dx < 0.0) {
            //new DeviceControlActivity().makeChangeLeftPressed();
            Log.i(TAG, Float.toString(dx));
        }
        if (dy > 0.0) {
            //String str = "d" + "\n";
            //Log.d(TAG, "NewSending result=" + str);
            //byte[] tx = str.getBytes();
            try {
                //new DeviceControlActivity().reconnect_to_ble();
                new DeviceControlActivity().makeChangeDownPressed();
            } catch (Exception e){
                e.printStackTrace();
            }

            //new DeviceControlActivity().makeChangeDownPressed();
            Log.i(TAG, Float.toString(dy));
        }
        if (dy < 0.0) {
            //new DeviceControlActivity().makeChangeUpPressed();
            Log.i(TAG, Float.toString(dy));
        }
    }

    protected void Draw(Canvas canvas, float x, float y, float zx, float zy){
        canvas.drawRGB(69, 90, 100);
        canvas.drawBitmap(max, canvas.getWidth() / 2 - max.getWidth() / 2, canvas.getHeight() / 2 - max.getHeight() / 2+30, paint1);
        canvas.drawLine(0, 0, canvas.getWidth(), canvas.getHeight(), paint1);
        canvas.drawText(Float.toString(x), 20, 40, paint1);
        canvas.drawText(Float.toString(y), 20, 200, paint1);
        if (x == 0 && y == 0)
            canvas.drawBitmap(ball,canvas.getWidth()/2-ball.getWidth()/2, canvas.getHeight()/2-ball.getHeight()/2, null);
        else
            canvas.drawBitmap(ball,x-ball.getWidth()/2, y-ball.getHeight()/2, null);
        //Log.i(TAG, "Draw");
        //super.onDraw(canvas);
    }


    public class MySurfaceThread extends AsyncTask<Void , Void, Void>{

        SurfaceHolder mSurfaceHolder;
        CustomSurfaceView cSurfaceView;

        public MySurfaceThread(SurfaceHolder sh, CustomSurfaceView csv) {
            mSurfaceHolder = sh;
            cSurfaceView = csv;
            x = y = 0;

            csv.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    x=e.getX();
                    y=e.getY();
                    calculateValues(x,y);

                    switch (e.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            break;
                        case MotionEvent.ACTION_UP:
                            x = y = 0;
                            dx = dy = 0;
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }

                    return true;
                }
            });
        }

        private void calculateValues(float xx, float yy) {
            dx = xx - zeroX;
            dy = yy - zeroY;
            angle = (float) Math.atan(Math.abs(dy / dx));
            c = (float) Math.sqrt(dx * dx + dy * dy);
            if (c > radius) {
                if (dx > 0 && dy > 0) {    //bottom right
                    xx = (float) (zeroX + (radius * (float) Math.cos(angle)));
                    yy = (float) (zeroY + (radius * (float) Math.sin(angle)));
                } else if (dx > 0 && dy < 0) { // top right
                    xx = (float) (zeroX + (radius * (float) Math.cos(angle)));
                    yy = (float) (zeroY - (radius * (float) Math.sin(angle)));
                } else if (dx < 0 && dy < 0) { //top left
                    xx = (float) (zeroX - (radius * (float) Math.cos(angle)));
                    yy = (float) (zeroY - (radius * (float) Math.sin(angle)));
                } else if (dx < 0 && dy > 0) { //bottom left
                    xx = (float) (zeroX - (radius * (float) Math.cos(angle)));
                    yy = (float) (zeroY + (radius * (float) Math.sin(angle)));
                }
            } else {
                xx = zeroX + dx;
                yy = zeroY + dy;


            }
            if (c > radius/3)
                sendmovedata();
            x = xx;
            y = yy;
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Log.i(TAG, "Thread");
            while (run) {

                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        //Log.i(TAG, "Thread onDraw");
                        zeroX = canvas.getWidth()/2;
                        zeroY = canvas.getHeight()/2;
                        cSurfaceView.Draw(canvas, x, y, zeroX, zeroY);
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {

                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
            return null;
        }
    }

}



