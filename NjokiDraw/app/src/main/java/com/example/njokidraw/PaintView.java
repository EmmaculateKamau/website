package com.example.njokidraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PaintView extends View {
    public static int BRUSH_SIZE=10;
    public static final int DEFAULT_COLOR= Color.BLUE;
    public static final int DEFAULT_BG_COLOR=Color.WHITE;
    public static final float TOUCH_TOLERANCE=4;

    private float mX,mY;
    private Paint mPaint;
    private Path mPath;
    private int currentcolor;
    private int backgroundcolor=DEFAULT_BG_COLOR;
    private int stokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint=new Paint(Paint.DITHER_FLAG);

    private ArrayList<Draw> paths=new ArrayList<>();
    private ArrayList<Draw> undo=new ArrayList<>();

    public PaintView(Context context) {
        super(context, null);

        mPaint=new Paint();
       mPaint.setAntiAlias(true);
       mPaint.setDither(true);
       mPaint.setColor(DEFAULT_COLOR);
       mPaint.setStyle(Paint.Style.STROKE);
       mPaint.setStrokeJoin(Paint.Join.ROUND);
       mPaint.setStrokeCap(Paint.Cap.ROUND);
       mPaint.setXfermode(null);
       mPaint.setAlpha(0xff);
    }
    public void initialize(DisplayMetrics displayMetrics){
        int height=displayMetrics.heightPixels;
        int width=displayMetrics.widthPixels;

        mBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        mCanvas=new Canvas(mBitmap);

        currentcolor=DEFAULT_COLOR;
        stokeWidth=BRUSH_SIZE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
       canvas.save();
       mCanvas.drawColor(DEFAULT_BG_COLOR);

       for (Draw draw:paths) {


           mPaint.setColor(DEFAULT_COLOR);
           mPaint.setStrokeWidth(stokeWidth);
           mPaint.setMaskFilter(null);

           mCanvas.drawPath((Path) draw.path, mPaint);

       }
        canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
       canvas.restore();
    }

  private void touchStart(float x,float y){
        mPath=new Path();
        Draw draw=new Draw(stokeWidth,currentcolor, (java.nio.file.Path) mPath);
        paths.add(draw);

        mPath.reset();
        mPath.moveTo(x, y);

        mX=x;
        mY=y;

    }
    private void touchMove(float x,float y){

          float dx=Math.abs(x-mX);
          float dy=Math.abs(y-mY);

          if (dx>=TOUCH_TOLERANCE ||dy>=TOUCH_TOLERANCE ){
              mPath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2);

              mY=y;
              mX=x;
          }
    }
    private void touchUp(float x,float y){

        mPath.lineTo(mX,mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
     float x=event.getX();
     float y=event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;
        }
        return true;
    }
    public void clear(){

        backgroundcolor=DEFAULT_BG_COLOR;
        paths.clear();
        invalidate();
    }
    public void undo(){

        if(undo.size()>0){
            paths.add(undo.remove(undo.size()-1));
        }else {
            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_SHORT).show();
        }

    }
    public void redo(){
        if(undo.size()>0){
            paths.add(undo.remove(undo.size()-1));
        }else {
            Toast.makeText(getContext(), "Nothing to redo", Toast.LENGTH_SHORT).show();
        }

    }
    public void setStokeWidth(int width){
         stokeWidth=width;
    }
    public void setColor(int color){
         currentcolor=color;

    }
    public void saveImage(){
        int count=0;

        File sdDirectory= Environment.getExternalStorageDirectory();
        File subDirectory=new File(sdDirectory.toString()+"/pictures/Paint");

        if (subDirectory.exists()){
            File[] existing=subDirectory.listFiles();

            for (File file:existing){
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")){
                    count++;

                }
            }
        } else {
            subDirectory.mkdir();
        }
        if (subDirectory.exists()){
            File image=new File(subDirectory,"/drawing_"+(count+1)+".png");
            FileOutputStream fileOutputStream;

            try {
                fileOutputStream=new FileOutputStream(image);
                mBitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();

                Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();
            }catch (FileNotFoundException e){
            } catch(IOException e){

            }
        }
    }
}

