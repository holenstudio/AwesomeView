package com.holenstudio.awesomeview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.util.ImageUtil;

/**
 * 可转动的圆形自定义控件，类似于单反相机中调整参数的那个转盘。
 * Created by Holen on 2016/6/12.
 */
public class TurntableView extends View {
    private final String TAG = "TurntableView";

    private Context mContext;
    private float mOuterRadius;
    private float mInnerRadius;
    private float mCenterX;
    private float mCenterY;
    private int mWidth;
    private int mHeight;
    private int[] mIconArray;
    private int[] mSelectedIconArray;
    private int currentIcon;
    private OnDragListener mDragListener;
    //箭头的位置，范围为0~360，顶部为0，顺时针方向
    private int mArrowPosition;
    private int mArrowSrc;
    private double rotateDegree = 0;
    private Paint mPaint;
    private float mLastX;
    private float mLastY;
    private VelocityTracker mVelocityTracker;
    private boolean mIsRequiresUpdate = true;
    private boolean mIsTouchUp = false;
    private float acceleration = 0.0f;
    private OnClickListener mClickListener;
    private long mLastTime;
    private long mCurrentTime;

    public TurntableView(Context context) {
        this(context, null);
    }

    public TurntableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TurntableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.AwesomeView);
        mOuterRadius = ta.getFloat(R.styleable.AwesomeView_outerRadius, 100);
        mInnerRadius = ta.getFloat(R.styleable.AwesomeView_innerRadius, 100);
        mArrowPosition = ta.getInt(R.styleable.AwesomeView_arrowPosition, 180);
        mArrowSrc = ta.getResourceId(R.styleable.AwesomeView_arrowSrc, R.drawable.arrow);
        init();
    }

    private void init() {
        currentIcon = R.drawable.auto_selected;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
        mPaint.setStrokeWidth(1.0f);
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
            resultMeasure = 200;
        }
        return resultMeasure;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
        canvas.drawCircle(mCenterX, mCenterY, mOuterRadius, mPaint);
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mPaint);
        drawArrow(canvas);
        updateCanvas(canvas);

        super.onDraw(canvas);
    }

    /**
     * 绘制箭头
     *
     * @param canvas
     */
    private void drawArrow(Canvas canvas) {
        Bitmap arrow = BitmapFactory.decodeResource(getResources(), mArrowSrc);
        float arrowLeft = mCenterX - arrow.getWidth() / 2;
        float arrowTop = mCenterY - mOuterRadius - arrow.getHeight() * 3 / 4;
        canvas.rotate(mArrowPosition, mCenterX, mCenterY);
        canvas.drawBitmap(ImageUtil.rotatingImageView(180, arrow), arrowLeft, arrowTop, mPaint);
    }

    private void updateCanvas(Canvas canvas) {
        drawIcons(canvas);
        drawSelectedIcon(canvas);
        Log.d(TAG, "acceleration" + acceleration);
        if (!mIsTouchUp) {
            return;
        }
        if (Math.abs(acceleration) > 1) {
//            if (acceleration > 0) {
//                acceleration -= 1;
//            }
//            else {
//                acceleration += 1;
//            }
            acceleration *= 0.8;
            invalidate();
        } else {
            for (int i = 0; i < mSelectedIconArray.length; i++) {
                if (currentIcon == mSelectedIconArray[i]) {
                    rotateDegree = 360 / mSelectedIconArray.length * i;
                }
            }
            if (mIsRequiresUpdate) {
                invalidate();
                mIsRequiresUpdate = false;
            }
        }

    }

    /**
     * 绘制外围的图标
     *
     * @param canvas
     */
    private void drawIcons(Canvas canvas) {
        canvas.save();
        if (mIconArray != null && mIconArray.length > 0) {
            if (Math.abs(acceleration) > 1) {
                rotateDegree += acceleration;
            }
            Log.d(TAG, "rotateDegree" + rotateDegree);
            canvas.rotate((float) rotateDegree, mCenterX, mCenterY);
            int length = mIconArray.length;
            double iconLeft;
            double iconTop;
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), mIconArray[0]);
            //每一个图标的top到圆中心的距离
            double iconLength = mInnerRadius + (mOuterRadius - mInnerRadius) / 2 + bmp.getHeight() / 2;
            //每一个图标的左上角到圆中心的距离
            double iconRadius = Math.sqrt(bmp.getWidth() * bmp.getWidth() / 4 + iconLength * iconLength);
            double offsetDegree = Math.acos(iconLength / iconRadius);
            for (int i = 0; i < length; i++) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), mIconArray[i]);
                iconLeft = mCenterX + iconRadius * Math.sin(0 - offsetDegree);
                iconTop = mCenterY - iconRadius * Math.cos(0 - offsetDegree);
                canvas.drawBitmap(icon, (float) iconLeft, (float) iconTop, mPaint);
                canvas.rotate(360.0f / length * (length - 1), mCenterX, mCenterY);
            }
        }
        canvas.restore();
    }

    /**
     * 绘制中心的图标,也就是箭头选中的图标
     *
     * @param canvas
     */
    private void drawSelectedIcon(Canvas canvas) {
        zoomOutSelectedIcon();
        Bitmap seletedBmp = BitmapFactory.decodeResource(getResources(), currentIcon);
        canvas.drawBitmap(seletedBmp, mCenterX - seletedBmp.getWidth() / 2, mCenterY - seletedBmp.getHeight() / 2, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker(event);
        float currentX;
        float currentY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mIsRequiresUpdate = true;
                mIsTouchUp = false;
                mLastTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                currentY = event.getY();
                //利用余弦公式cosA = (b*b + c*c - a*a )/ 2*b*c简化后得出角度
//                arcCosDegree = ((mCenterX - lastX) * (mCenterX - currentX) + (mCenterY - lastY) * (mCenterY - currentY)) /
//                        Math.sqrt(((mCenterX - lastX) * (mCenterX - lastX) + (mCenterY - lastY) * (mCenterY - lastY)) *
//                                ((mCenterX - currentX) * (mCenterX - currentX) + (mCenterY - currentY) * (mCenterY - currentY)));
                //利用叉乘|AXB|=x1 * y2 - x2 * y1 = |A| * |B| * sinA得到角度的正弦值
                double arcSinDegree = ((mLastX - mCenterX) * (currentY - mCenterY) - (mLastY - mCenterY) * (currentX - mCenterX)) /
                        Math.sqrt(((mLastX - mCenterX) * (mLastX - mCenterX) + (mLastY - mCenterY) * (mLastY - mCenterY)) * ((currentX - mCenterX) *
                                (currentX - mCenterX) + (currentY - mCenterY) * (currentY - mCenterY)));
                rotateDegree += Math.toDegrees(Math.asin(arcSinDegree));
                rotateDegree %= 360;
                Log.d(TAG, "rotateDegree = " + rotateDegree);
                Log.d(TAG, "asdafa mLastX = " + mLastX + "mLastY = " + mLastY);
                Log.d(TAG, "asdafa currentX = " + currentX + "currentY = " + currentY);
                //将选择的icon放大
                mLastX = event.getX();
                mLastY = event.getY();
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                mCurrentTime = System.currentTimeMillis();
                mVelocityTracker.computeCurrentVelocity(1);
                mIsTouchUp = true;
                if (mCurrentTime - mLastTime < 100 && mVelocityTracker.getXVelocity() < 0.1 && mVelocityTracker.getYVelocity() < 0.1) {
                    if (mClickListener != null) {
                        mClickListener.onClick(this);
                    }
                }
                updateFlingView();
                Log.d(TAG, "VelocityTracker:X=" + mVelocityTracker.getXVelocity() + ",Y=" + mVelocityTracker.getYVelocity());
                return true;

        }

        return super.onTouchEvent(event);
    }

    private void updateFlingView() {
        acceleration = mVelocityTracker.getXVelocity() * mVelocityTracker.getYVelocity();
        invalidate();
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void zoomOutSelectedIcon() {
        int minRange;
        int maxRange;
        for (int i = 0; i < mIconArray.length; i++) {
            minRange = i * 360 / mIconArray.length - 360 / mIconArray.length / 2;
            maxRange = i * 360 / mIconArray.length + 360 / mIconArray.length / 2;
            //这里因为第一个图标的范围是- 360 / mIconArray.length / 2到360 / mIconArray.length / 2，
            //所以当旋转角度大于360 - 360 / mIconArray.length / 2时会判断失误，所以作此处理
            if (minRange < 0) {
                maxRange = 360 + 360 / mIconArray.length / 2;
            }
            if ((minRange <= (rotateDegree + 360) % 360) && (maxRange >= (rotateDegree + 360) % 360)) {
                currentIcon = mSelectedIconArray[i];
                if (mDragListener != null) {
                    mDragListener.onDragFinished(this, i);
                }
            }
        }
    }

    public void setIconArray(int[] array) {
        mIconArray = array;
        invalidate();
    }

    public void setSelectedIconArray(int[] array) {
        mSelectedIconArray = array;
        invalidate();
    }

    public void setOnDragListener(OnDragListener listener) {
        mDragListener = listener;
    }

    public int getDisplayWidth() {
        return mWidth;
    }

    public void setDisplayWidth(int width) {
        mWidth = width;
        setMeasuredDimension(mWidth, mHeight);
    }

    public int getDisplayHeight() {
        return mHeight;
    }

    public void setDisplayHeight(int height) {
        mHeight = height;
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    public interface OnDragListener {
        public void onDragFinished(View view, int position);
    }
}
