package com.example.ivan.dogremotecontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Dimension;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class Joystick extends View implements View.OnTouchListener{
    private float stickRadius = 100.0f;
    private float fieldRadius = 120.0f;
    private float intent = 10.0f;
    private int fieldColor = Color.BLACK;
    private int stickColor = Color.BLUE;
    private float xPos;
    private float yPos;
    private Paint paint;
    private Bitmap bm;
    public Joystick(Context context) {
        super(context);
        init(null, 0, context);
    }

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, context);
    }

    public Joystick(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle, context);
    }

    private void init(AttributeSet attrs, int defStyle, Context context) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Joystick, defStyle, 0);
        stickRadius = a.getDimension(R.styleable.Joystick_stickRadius, stickRadius);
        stickColor = a.getColor(R.styleable.Joystick_stickColor, stickColor);
        intent = a.getDimension(R.styleable.Joystick_intent, intent);
        fieldRadius = a.getDimension(R.styleable.Joystick_fieldRadius, fieldRadius);
        fieldColor = a.getInt(R.styleable.Joystick_fieldColor, fieldColor);
        this.xPos = fieldRadius-stickRadius;
        this.yPos = fieldRadius-stickRadius;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Drawable draw = ContextCompat.getDrawable(context, R.drawable.joystick);
        draw = (DrawableCompat.wrap(draw)).mutate();
        Matrix m = new Matrix();
        bm = Bitmap.createBitmap(draw.getIntrinsicWidth(), draw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        bm = Bitmap.createScaledBitmap(bm, (int)(this.stickRadius*2), (int)(this.stickRadius*2), false);
        Canvas canvas = new Canvas(bm);
        draw.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        draw.draw(canvas);
        this.setOnTouchListener(this);
        a.recycle();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int canvasWidth = canvas.getWidth();
        final int canvasHeight = canvas.getHeight();
        paint.setColor(Color.GRAY);
        paint.setAlpha(80);
        canvas.drawCircle(this.fieldRadius, this.fieldRadius, this.fieldRadius, paint);
        paint.setAlpha(255);
        canvas.drawBitmap(bm, this.xPos, this.yPos,  paint);
    }
    public int getFieldColor() {return this.fieldColor;}
    public int getStickColor() {return this.stickColor;}
    public void setStickColor(int col) {this.stickColor = col;}
    public void setFieldColor(int col) {this.fieldColor = col;}

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.equals(this)) {
            if (motionEvent.getAction() != motionEvent.ACTION_UP) {
                if (borderCheck(motionEvent.getX()))
                    this.xPos = motionEvent.getX() - this.stickRadius;
                else
                    this.xPos = this.fieldRadius - (float)Math.sqrt(((this.stickRadius+this.intent)*
                            (this.stickRadius+this.intent) - (motionEvent.getY()-this.fieldRadius)*(motionEvent.getY()-this.fieldRadius)));
                if (borderCheck(motionEvent.getY()))
                    this.yPos = motionEvent.getY() - this.stickRadius;
                else
                    this.yPos = this.fieldRadius - (float)Math.sqrt(((this.stickRadius+this.intent)*
                            (this.stickRadius+this.intent) - (motionEvent.getX()-this.fieldRadius)*(motionEvent.getX()-this.fieldRadius)));
                Log.d("Ya", Float.toString(this.xPos) + " " + Float.toString(this.yPos));
            } else if (motionEvent.getAction() == motionEvent.ACTION_UP){
                this.xPos = this.fieldRadius - this.stickRadius;
                this.yPos = this.fieldRadius - this.stickRadius;
            }
            invalidate();
        }
        return true;
    }
    private boolean borderCheck(float a) {
        if(a < fieldRadius*2-intent  && a > intent) return true;
        return false;
    }
}
