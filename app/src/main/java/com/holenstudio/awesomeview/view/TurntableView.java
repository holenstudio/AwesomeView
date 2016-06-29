package com.holenstudio.awesomeview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.util.ImageUtil;

/**
 * 可转动的圆形自定义控件，类似于单反相机中调整参数的那个转盘。
 * Created by Holen on 2016/6/12.
 */
public class TurntableView extends View implements Rotatable {
    private final String TAG = "TurntableView";
    private static final float ENABLED_ALPHA = 1;
    private static final float DISABLED_ALPHA = 0.4f;
    private static final int ANIMATION_SPEED = 270; // 270 deg/sec

    private Context mContext;
    /**
     * 当屏幕旋转时，当前旋转的角度
     */
    private int mScreenCurrentDegree = 0;
    /**
     * 当屏幕旋转时，旋转的开始角度
     */
    private int mScreenStartDegree = 0;
    /**
     * 当屏幕旋转时，旋转的目标角度
     */
    private int mScreenTargetDegree = 0;
    /**
     * 当屏幕旋转时，是否是顺时针旋转
     */
    private boolean mScreenClockwise = false;
    /**
     * 当屏幕旋转时，是否启用动画
     */
    private boolean mEnableAnimation = true;
    /**
     * 当屏幕旋转时，动画开始时间
     */
    private long mAnimationStartTime = 0;
    /**
     * 当屏幕旋转时，动画结束时间
     */
    private long mAnimationEndTime = 0;
    /**
     * 是否打开filter，打开后，图片有两种状态，一种是不透明，一种是透明度为0.4(默认)
     */
    private boolean mFilterEnabled = true;
    /**
     * 图标外半径，方便画圈用
     */
    private float mOuterRadius;
    /**
     * 图标内半径，方便画圈用
     */
    private float mInnerRadius;
    /**
     * 从图标中心到圆心的距离，方便计算用。值为(外半径 + 内半径) / 2
     */
    private float mRadius;
    /**
     * 中心点坐标，相对于view的坐标，不是绝对坐标
     */
    private float mCenterX;
    private float mCenterY;
    /**
     * view的高度
     */
    private int mWidth;
    private int mHeight;
    /**
     * 圆上图标的资源id数组
     */
    private int[] mIconArray;
    /**
     * 选中的图标的资源id数组
     */
    private int[] mSelectedIconArray;
    /**
     * 当前选中的图标资源id
     */
    private int currentIcon;
    /**
     * 当前选中的图标资源索引
     */
    private int currentIconIndex;
    /**
     * 拖拽的监听器
     */
    private OnDragListener mDragListener;
    /**
     * 箭头的位置，也就是图标起点的位置。范围为0~360，顶部为0，顺时针方向
     */
    private int mArrowPosition;
    /**
     * 箭头图标的资源id
     */
    private int mArrowSrc;
    /**
     * 是否启用箭头图标
     */
    private boolean mIsShowArrow = false;
    /**
     *
     */
    private boolean mIsClockwiseFling;
    /**
     * 是否启用放大已选中图标
     */
    private boolean mIsZoomOutSelectedIcon = false;
    /**
     * 图标旋转的角度，也就是拖拽的时候每一次旋转的角度
     */
    private double rotateDegree = 0;
    private Paint mPaint;
    /**
     * 上一次手指所在的坐标点，在onTouchEvent中所用到
     */
    private float mLastX;
    private float mLastY;
    /**
     * 速度追踪器，可以获取手指在屏幕上滑动的速度
     */
    private VelocityTracker mVelocityTracker;
    /**
     * 是否需要更新view。
     */
    private boolean mIsRequiresUpdate = true;
    /**
     * 手指是否已抬起
     */
    private boolean mIsTouchUp = false;
    /**
     * 旋转偏移量
     */
    private float acceleration = 0.0f;
    /**
     * 判断是否正在自动滚动
     */
    private boolean isFling;
    private OnClickListener mClickListener;
    /**
     * 记录手指在view中上次操作时的时间
     */
    private long mLastTime;
    /**
     * 记录手指在view中当前操作时的时间
     */
    private long mCurrentTime;
    private int mCurrentRotatedDegree;
    /**
     * 在圆上(不是园中)选中的图标缩放的比值
     */
    private float mSelectedIconZoomRate;
    /**
     * 上一次选中的图标索引id
     */
    private int mLastSelectedIconId;
    /**
     * 自动滚动的Runnable
     */
    private AutoFlingRunnable mFlingRunnable;
    /**
     * 自动滚动时的速度
     */
    private float mFlingVelocity;
    /**
     * 自动滚动时的加速度
     */
    private float mFlingAcceleration;
    private OnItemClickListener mItemClickListener;

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
        mInnerRadius = ta.getFloat(R.styleable.AwesomeView_innerRadius, 50);
        mArrowPosition = ta.getInt(R.styleable.AwesomeView_arrowPosition, 0);
        mArrowSrc = ta.getResourceId(R.styleable.AwesomeView_arrowSrc, R.drawable.arrow_to_down);
        mSelectedIconZoomRate = ta.getFloat(R.styleable.AwesomeView_selectedIconZoomRate, 2.0f);
        init();
    }

    private void init() {
        mRadius = (mOuterRadius + mInnerRadius) / 2;
        currentIcon = R.drawable.auto_selected;
        currentIconIndex = 0;
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
        PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
        mPaint.setPathEffect(effects);
        canvas.drawCircle(mCenterX, mCenterY, mOuterRadius, mPaint);
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mPaint);
        if (mScreenCurrentDegree != mScreenTargetDegree) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < mAnimationEndTime) {
                int deltaTime = (int) (time - mAnimationStartTime);
                int degree = mScreenStartDegree + ANIMATION_SPEED
                        * (mScreenClockwise ? deltaTime : -deltaTime) / 1000;
                degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
                mScreenCurrentDegree = degree;
                invalidate();
            } else {
                mScreenCurrentDegree = mScreenTargetDegree;
            }
        }

        canvas.rotate(0 - mScreenCurrentDegree, mCenterX, mCenterY);
        drawArrow(canvas);
        updateCanvas(canvas);

        super.onDraw(canvas);
    }

    /**
     * 绘制箭头
     *
     * @param canvas
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drawArrow(Canvas canvas) {

        if (mIsShowArrow) {
            Bitmap arrow = BitmapFactory.decodeResource(getResources(), mArrowSrc);
            float arrowLeft = mCenterX - arrow.getWidth() / 2;
            float arrowTop = mCenterY - mOuterRadius - arrow.getHeight() * 3 / 4;
            canvas.drawBitmap(arrow, arrowLeft, arrowTop, mPaint);
        }
        canvas.rotate(mArrowPosition, mCenterX, mCenterY);
        mPaint.setStyle(Paint.Style.FILL);//充满
        mPaint.setColor(Color.RED);
        float tmpRadius = (float) (mOuterRadius * Math.sin(Math.toRadians(360.0f / mIconArray.length / 4)));
        Path path = new Path();
        path.moveTo(mCenterX - tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(360.0 / mIconArray.length / 4))));
        //startAngle的0度是在3点方向，所以还原到顶点的话需要减去90度
        path.arcTo(mCenterX - mOuterRadius, mCenterY - mOuterRadius, mCenterX + mOuterRadius, mCenterY + mOuterRadius, 0 - 90 -  360.0f / mIconArray.length / 4, 360.0f / mIconArray.length / 4 * 2, false);
        path.lineTo(mCenterX + tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(360.0 / mIconArray.length / 4))) + 2 * tmpRadius);
        path.arcTo(mCenterX - tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(360.0 / mIconArray.length / 4))) + tmpRadius, mCenterX + tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(360.0 / mIconArray.length / 4)) + 3 * tmpRadius), 0, 180, false);
        path.close();
        canvas.drawPath(path, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    private void updateCanvas(Canvas canvas) {
        drawIcons(canvas);
        drawSelectedIcon(canvas);
        if (!mIsTouchUp) {
            return;
        }
//        if (Math.abs(acceleration) > 10) {
//            acceleration *= 0.9;
//            invalidate();
//        } else if (Math.abs(acceleration) > 1) {
////            for (int i = 0; i < mSelectedIconArray.length; i++) {
////                if (currentIcon == mSelectedIconArray[i]) {
////                    rotateDegree = 360 / mSelectedIconArray.length * i;
////                }
////            }
////            if (mIsRequiresUpdate) {
////                invalidate();
////                mIsRequiresUpdate = false;
////            }
//            if (acceleration > 0) {
//                acceleration--;
//            }
//            if (acceleration < 0) {
//                acceleration++;
//            }
//            invalidate();
//        } else {
//            if (mLastSelectedIconId != currentIcon) {
//                return;
//            }
//            mLastSelectedIconId = currentIcon;
//            if (mClickListener != null) {
//                mClickListener.onClick(this);
//            }
//        }

    }

    /**
     * 绘制外围的图标
     *
     * @param canvas
     */
    private void drawIcons(Canvas canvas) {
        canvas.save();
        if (mIconArray == null || mIconArray.length <= 0) {
            return;
        }

        int length = mIconArray.length;
        rotateDegree += acceleration;
//        if (Math.abs(acceleration) > 1) {
//        }
        canvas.rotate((float) rotateDegree, mCenterX, mCenterY);
        double iconLeft;
        double iconTop;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), mIconArray[0]);
        float singleDegree = 360.0f / length;
        for (int i = 0; i < length; i++) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), mIconArray[i]);
            icon = ImageUtil.rotatingImageView((int) (360 - rotateDegree + singleDegree * (length - i) - mArrowPosition), icon);
            iconLeft = mCenterX - icon.getWidth() / 2;
            iconTop = mCenterY - mRadius - bmp.getHeight() / 2;
            if (mIsZoomOutSelectedIcon) {
                //计算每个图标当前的位置
                int position = Math.abs((int) (360 + rotateDegree + singleDegree * i + mArrowPosition) % 360);
                //计算该图标的位置距离起点相差多少
                position = Math.min(Math.abs(position - mArrowPosition), Math.abs(360 + mArrowPosition - position));
                //放大靠近起点的图标
                if (position < singleDegree) {
                    double zoomRate = (1 - position / (singleDegree * 1.0f)) * (mSelectedIconZoomRate - 1);
                    icon = Bitmap.createScaledBitmap(icon, (int) (icon.getWidth() * (1 + zoomRate)), (int) (icon.getHeight() * (1 + zoomRate)), false);
                    iconLeft = mCenterX - (mCenterX - iconLeft) * (1 + zoomRate);
                    iconTop = mCenterY - mRadius - (mCenterY - mRadius - iconTop) * (1 + zoomRate);
                }
            }
            canvas.drawBitmap(icon, (float) iconLeft, (float) iconTop, mPaint);
            canvas.rotate(360.0f / length, mCenterX, mCenterY);
        }
        canvas.restore();
    }

    /**
     * 绘制中心的图标,也就是箭头选中的图标
     *
     * @param canvas
     */
    private void drawSelectedIcon(Canvas canvas) {
        selecteIcon();
        Bitmap seletedBmp = BitmapFactory.decodeResource(getResources(), currentIcon);
        canvas.drawBitmap(ImageUtil.rotatingImageView(0 - mArrowPosition, seletedBmp), mCenterX - seletedBmp.getWidth() / 2, mCenterY - seletedBmp.getHeight() / 2, mPaint);
    }

    private void selecteIcon() {
        //第i个图标所在的位置
        int position;
        int length = mIconArray.length;
        for (int i = 0; i < length; i++) {
            position = (int) (360 + rotateDegree + 360.0f / length * i + mArrowPosition) % 360;
            if (Math.abs(position - mArrowPosition) < (360.0f / length / 2) || Math.abs(position - mArrowPosition) > (360 - 360.0f / length / 2)) {
                currentIcon = mSelectedIconArray[i];
                currentIconIndex = i;
            }
        }
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
                if (isFling) {
                    removeCallbacks(mFlingRunnable);
                    isFling = false;
                    return true;
                }
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
                rotateDegree += Math.toDegrees(Math.asin(arcSinDegree)) / 2;
                rotateDegree %= 360;
                mLastX = event.getX();
                mLastY = event.getY();
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                mCurrentTime = System.currentTimeMillis();
                mVelocityTracker.computeCurrentVelocity(1);
                mIsTouchUp = true;
                currentX = event.getX();
                currentY = event.getY();
                if (mCurrentTime - mLastTime < 100 && mVelocityTracker.getXVelocity() < 0.1 && mVelocityTracker.getYVelocity() < 0.1 && Math.abs(currentX - mLastX) < 2 && Math.abs(currentY - mLastY) < 2 ) {
                    //触摸的点与中心点组成向量(currentX - mCenterX) (, (mCenterY - currentY))
//                    float degree = (float) Math.asin((currentX - mCenterX) / Math.sqrt((currentX - mCenterX) * (currentX - mCenterX) + (currentY - mCenterY) * (currentY - mCenterY)));
                    float degree = (float) Math.toDegrees(Math.acos((mCenterY - currentY) / Math.sqrt((currentX - mCenterX) * (currentX - mCenterX) + (currentY - mCenterY) * (currentY - mCenterY))));
                    if ((currentX - mCenterX) < 0) {
                        degree = 360 - degree;
                    }
                    degree += mScreenCurrentDegree;
                    degree %= 360;

                    int clickIndex = (int) ((360 - 270) + degree + 360 / 2 / mIconArray.length) % 360 / (360 / mIconArray.length);

                    if (mItemClickListener != null) {
                        mItemClickListener.onClickItem(this, clickIndex);
                    }
                    return true;
                }
                if (Math.abs(mVelocityTracker.getXVelocity()) < 1 || Math.abs(mVelocityTracker.getYVelocity()) < 1) {
                    rotateDegree = (mIconArray.length - currentIconIndex) * (360.0f / mIconArray.length);
                    invalidate();
                } else {
                    Log.d(TAG, "acceleration=" + acceleration);
                    mIsClockwiseFling = calculateClockwise(mCenterX, mCenterY, event.getX(), event.getY(), mVelocityTracker.getXVelocity(), mVelocityTracker.getYVelocity());
                    updateFlingView();
                }

                return true;

        }

        return super.onTouchEvent(event);
    }

    /**
     * 根据http://hellerfu.com/android-3d-cube.html，5.4.4（7），参考三角形面积公式可以得到中心点与手指离开时的向量的位置(中心点位于向量的左边还是右边)
     * 结合位置再根据向量的方向可以得到是顺时针还是逆时针
     * @param centerX
     * @param centerY
     * @param x
     * @param y
     * @param xVelocity
     * @param yVelocity
     * @return
     */
    private boolean calculateClockwise(float centerX, float centerY, float x, float y, float xVelocity, float yVelocity) {
        float area = (y * (x + xVelocity)) - (x * (y + yVelocity)) - xVelocity * centerY + yVelocity * centerX;
        //小于0是说明中心点在向量的右边，那么也就是说向量的方向是顺时针方向
        return area < 0;
    }

    private void updateFlingView() {
        mFlingVelocity = (float) Math.abs(mVelocityTracker.getXVelocity() * Math.sin(Math.toRadians(rotateDegree % 360)) + mVelocityTracker.getYVelocity() * Math.cos(Math.toRadians(rotateDegree % 360)));
        mFlingVelocity =  mIsClockwiseFling? mFlingVelocity : 0 - mFlingVelocity;
//        mFlingAcceleration = mFlingVelocity / (mCurrentTime - mLastTime) * 1000;
        mFlingAcceleration =  mIsClockwiseFling? 0 - 2f : 2f;
//        acceleration = mFlingAcceleration;
        Log.d(TAG, "FlingAcceleration=" + mFlingAcceleration);
        post(mFlingRunnable = new AutoFlingRunnable());
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
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
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
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

    @SuppressWarnings("deprecation")
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mFilterEnabled) {
            if (enabled) {
                setAlpha(ENABLED_ALPHA);
            } else {
                setAlpha(DISABLED_ALPHA);
            }
        }
    }

    public void enableFilter(boolean enabled) {
        mFilterEnabled = enabled;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public void setOrientation(int orientation, boolean animation) {
        mEnableAnimation = animation;
        // make sure in the range of [0, 359]
        orientation = orientation >= 0 ? orientation % 360 : orientation % 360 + 360;
        if (orientation == mScreenTargetDegree) return;

        mScreenTargetDegree = orientation;
        if (mEnableAnimation) {
            mScreenStartDegree = mScreenCurrentDegree;
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

            int diff = mScreenTargetDegree - mScreenCurrentDegree;
            diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

            // Make it in range [-179, 180]. That's the shorted distance between the
            // two angles
            diff = diff > 180 ? diff - 360 : diff;

            mScreenClockwise = diff >= 0;
            mAnimationEndTime = mAnimationStartTime
                    + Math.abs(diff) * 1000 / ANIMATION_SPEED;
        } else {
            mScreenCurrentDegree = mScreenTargetDegree;
        }

        invalidate();
    }

    public interface OnDragListener {
        public void onDragFinished(View view, int position);
    }
    public interface OnItemClickListener {
        public void onClickItem (View view, int position);
    }

    /**
     * 自动滚动的任务
     *
     */
    private class AutoFlingRunnable implements Runnable {

        public void run() {
            isFling = true;
            // 如果小于20,则停止
            if ((mIsClockwiseFling && mFlingVelocity < 0) || (!mIsClockwiseFling && mFlingVelocity > 0)) {
//            }
//            if ((int) Math.abs(mFlingVelocity) < 16) {
                isFling = false;
                rotateDegree = (mIconArray.length - currentIconIndex) * (360.0f / mIconArray.length);
                acceleration = 0;
                invalidate();
                return;
            }
            // 逐渐减小这个值
//            acceleration /= 1.0666F;
//            acceleration = 1 /2 * mFlingAcceleration * 16 * 16 / 1000 / 1000;
            acceleration = mFlingVelocity * 16 / 32;
            Log.d(TAG, "acceleration=" + acceleration);
            mFlingVelocity = mFlingVelocity + mFlingAcceleration * 16 / 64;
            Log.d(TAG, "FlingVelocity=" + mFlingVelocity);
            // 重新绘制
            invalidate();
            postDelayed(this, 8);
        }
    }


}
