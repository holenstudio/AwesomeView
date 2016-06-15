package com.holenstudio.awesomeview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.model.OvalItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Holen on 2016/6/14.
 */
public class FanlikeView extends View {
    private final static String TAG = "FanlikeView";

    private Context mContext;
    private float mOuterRadius;
    private float mInnerRadius;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private boolean isVisible;
    private float mCenterX;
    private float mCenterY;
    private long mTouchDownTime;
    private long mCurrentTime;
    private List<OvalItem> mOvalItems;
    private RectF mOutterOval;
    private RectF mInnerOval;

    public FanlikeView(Context context) {
        this(context, null);
    }

    public FanlikeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FanlikeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.AwesomeView);
        mOuterRadius = ta.getFloat(R.styleable.AwesomeView_outerRadius, 400);
        mInnerRadius = ta.getFloat(R.styleable.AwesomeView_innerRadius, 300);
        mCenterX = mOuterRadius;
        mCenterY = mOuterRadius;
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
        mPaint.setStrokeWidth(1.0f);

        mOutterOval = new RectF(mCenterX - mOuterRadius, mCenterY - mOuterRadius, mCenterX + mOuterRadius, mCenterY + mOuterRadius);
        mInnerOval = new RectF(mCenterX - mInnerRadius, mCenterY - mInnerRadius, mCenterX + mInnerRadius, mCenterY + mInnerRadius);

        mOvalItems = new ArrayList<>();
        mOvalItems.add(new OvalItem(R.drawable.auto));
        mOvalItems.add(new OvalItem(R.drawable.flower));
        mOvalItems.add(new OvalItem(R.drawable.night));
        mOvalItems.add(new OvalItem(R.drawable.scene));
        mOvalItems.add(new OvalItem(R.drawable.run));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = measure(widthMeasureSpec);
        mHeight = measure(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    private int measure(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int resultMeasure;
        if (mode == MeasureSpec.EXACTLY) {
            resultMeasure = size;
        } else {
            resultMeasure = 800;
        }
        return resultMeasure;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isVisible) {
            return;
        }

        isVisible = true;
        if (mOvalItems == null || mOvalItems.isEmpty()) {
            return;
        }
        int size = mOvalItems.size();
        if (size > 6 || size < 0) {
            return;
        }
        float angle = 150 / size;
        for (int i = 0; i < size; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), mOvalItems.get(i).getIconSrc());
            float iconLeft = mCenterX + mOuterRadius * (float) Math.sin(angle);
            float iconTop = mCenterY - mOuterRadius * (float) Math.cos(angle);
            canvas.drawBitmap(icon, iconLeft, iconTop, mPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        float currentX;
//        float currentY;
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mTouchDownTime = System.currentTimeMillis();
//                mCenterX = event.getX();
//                mCenterY = event.getY();
//                return true;
//            case MotionEvent.ACTION_MOVE:
//                mCurrentTime = System.currentTimeMillis();
//                currentX = event.getX();
//                currentY = event.getY();
//                if (mCurrentTime - mTouchDownTime < 3) {
//                    isVisible = false;
//                } else if ((Math.abs(mCenterX - currentX) < 20) && (Math.abs(mCenterY - currentY) < 20)){
//                    isVisible = true;
//                }
//
//                invalidate();
//
//                break;
//            case MotionEvent.ACTION_UP:
//
//                return true;
//
//        }

        return super.onTouchEvent(event);
    }

    public void hide() {
        isVisible = false;
        invalidate();
        destroyDrawingCache();
    }

    public void show() {
        invalidate();
    }

    public void setCenterX(float centerX) {
        mCenterX = centerX;
    }

    public void setCenterY(float centerY) {
        mCenterY = centerY;
    }

    public void setCenter(float centerX, float centerY) {
        mCenterX = centerX;
        mCenterY = centerY;
    }
}
