package edu.cs4730.surfaceviewdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Random;

/*
 * simple example of a surfaceView with a picture that moves across the screen with a touchlistener.
 *
 * All the code to make this work is in this class.
 */

public class AllinOneActivity extends AppCompatActivity  implements SurfaceHolder.Callback{

    private myThread thread;
    public Paint black;
    public int x, y;
    private int height = 480, width=480 ;  //defaults incase not set yet.
    private int alienheight, alienwidth;
    public Rect myRect;
    Bitmap alien;
    private Random myRand = new Random();
    float scale;

    SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup everything needed.
        //load a picture and draw it onto the screen.
        alien = BitmapFactory.decodeResource(getResources(), R.drawable.alien);
        scale = getResources().getDisplayMetrics().density; //this gives me the scale value for a mdpi baseline of 1.
        //scale up the alien, so it bigger for dp
        alienheight = (int) (alien.getHeight() * scale);
        alienwidth = (int) (alien.getWidth() * scale);
        myRect = new Rect();
        x = myRand.nextInt(width - alienwidth);  //kept it inside the screen.
        y = myRand.nextInt(height - alienheight);
        myRect.set(x,y,x +alienwidth,y+alienheight);

        black = new Paint();  //default is black and we really are not using it.  need it to draw the alien.


        //get a generic surface and all our callbacks to it, with a touchlistener.
        mSurfaceView = new SurfaceView(this);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                // Retrieve the new x and y touch positions
                int touchx = (int) event.getX();
                int touchy = (int) event.getY();
                if (myRect.contains(touchx,touchy)) {
                    //touched the alien
                    Log.v("hi", "collison");
                    x = myRand.nextInt(width - alienwidth);
                    y = myRand.nextInt(height - alienheight);
                    //we could set each manually, or just use the set method.
                    //myRect.left =  x;
                    // myRect.top = y;
                    myRect.set(x,y,x +alienwidth,y+alienheight);
                    mSurfaceView.invalidate();
                }
                return true;
            }
        });

        //setup the thread for animation.
        thread = new myThread(mSurfaceView.getHolder());

        setContentView(mSurfaceView);
    }

    //simple helper method to draw on the canvas.    (note in a SurfaceView, this is an overriden method.
    public void draw(Canvas c) {

        c.drawColor(Color.WHITE);
        c.drawBitmap(alien, null, myRect, black);

    }


    //all the methods needed for the SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //everything is setup, now start.
        height = mSurfaceView.getHeight();
        width = mSurfaceView.getWidth();
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //ignored.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // simply copied from sample application LunarLander:
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }

   /*
    * this is the thread that causes the drawing and movement of an alien (pic) moving accross the screen.
    */
    class myThread extends Thread {
        private SurfaceHolder _surfaceHolder;

        private boolean _run = false;

        public myThread(SurfaceHolder surfaceHolder) {
            _surfaceHolder = surfaceHolder;

        }
        public void setRunning(boolean run) {
            _run = run;
        }
        @Override
        public void run() {
            Canvas c;
            x=10;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        //call a mthod that draws all the required objects onto the canvas.
                        draw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
                //move the alien accross the screen.
                x += 2*scale;
                if (x>width - alienwidth) {x=10;}
                myRect.left = x; myRect.right =x + alienwidth;
                //sleep for a short period of time.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }
        }
    }

}
