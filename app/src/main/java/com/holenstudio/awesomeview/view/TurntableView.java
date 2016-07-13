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
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.util.ImageUtil;
import com.holenstudio.awesomeview.util.VibratorUtil;

import java.util.List;

/**
 * 可转动的圆形自定义控件，类似于单反相机中调整参数的那个转盘。
 * Created by Holen on 2016/6/12.
 */
public class TurntableView extends View implements Rotatable {
    private final String TAG = "TurntableView";
    private static final float ENABLED_ALPHA = 1;
    private static final float DISABLED_ALPHA = 0.4f;
    private static final int ANIMATION_SPEED = 360; // 270 deg/sec

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
     * 当前选中的图标资源索引
     */
    private int mCurrentIconIndex;
    /**
     * 拖拽的监听器
     */
    private OnSelectItemListener mSeletectItemListener;
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
    private float mOffsetDegree = 0.0f;
    /**
     * 一次旋转中总偏移量
     */
    private float mOffsetTotalDegree = 0.0f;
    /**
     * 判断是否正在自动滚动
     */
    private boolean mIsFling;
    private boolean mIsFront = false;
    /**
     * 记录手指在view中上次操作时的时间
     */
    private long mLastTime;
    /**
     * 记录手指在view中当前操作时的时间
     */
    private long mCurrentTime;
    /**
     * 在圆上(不是园中)选中的图标缩放的比值
     */
    private float mSelectedIconZoomRate;
    /**
     * 在fling过程中上一次选中的图标索引
     */
    private int mLastFlingSelectedIconIndex;
    /**
     * 自动滚动的Runnable
     */
    private AutoFlingRunnable mFlingRunnable;
    /**
     * 自动滚动时的加速度
     */
    private float mFlingAcceleration = 1.066f;
    /**
     * 震动时间
     */
    private int mVibratorTime = 20;
    /**
     * 上一次选中的图标索引
     */
    private int mLastSelectedIconIndex = 0;
    /**
     * 图标实体list
     */
    private List<Entity> mEntities;
    /**
     * 是否被放大，因为需要根据是否被放大的状态改变点击view的事件
     */
    private boolean mIsZoomOut = false;
    /**
     * 上一个不是disable状态的图标索引
     */
    private int mLastAvailableIndex;

    public TurntableView(Context context) {
        this(context, null);
    }

    public TurntableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TurntableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AwesomeView);
        mOuterRadius = ta.getFloat(R.styleable.AwesomeView_outerRadius, 100);
        mInnerRadius = ta.getFloat(R.styleable.AwesomeView_innerRadius, 50);
        mArrowPosition = ta.getInt(R.styleable.AwesomeView_arrowPosition, 0);
        mArrowSrc = ta.getResourceId(R.styleable.AwesomeView_arrowSrc, R.drawable.arrow_to_down);
        mSelectedIconZoomRate = ta.getFloat(R.styleable.AwesomeView_selectedIconZoomRate, 1.4f);
        init();
    }

    private void init() {
        mRadius = (mOuterRadius + mInnerRadius) / 2;
        mCurrentIconIndex = 0;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
        mPaint.setStrokeWidth(1.0f);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
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
        long a = System.currentTimeMillis();
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

        Log.d(TAG, " draw:" + (System.currentTimeMillis() - a) + "currentIdIndex=" + mCurrentIconIndex);
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
        float arcWidth = 360.0f / mEntities.size() / 3;
        float tmpRadius = (float) (mOuterRadius * Math.sin(Math.toRadians(arcWidth)));
        Path path = new Path();
        path.moveTo(mCenterX - tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(arcWidth))));
        //startAngle的0度是在3点方向，所以还原到顶点的话需要减去90度
        path.arcTo(mCenterX - mOuterRadius, mCenterY - mOuterRadius, mCenterX + mOuterRadius, mCenterY + mOuterRadius, 0 - 90 -  arcWidth, arcWidth * 2, false);
        path.lineTo(mCenterX + tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(arcWidth))) + 2 * tmpRadius);
        path.arcTo(mCenterX - tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(arcWidth))) + tmpRadius, mCenterX + tmpRadius, (float) (mCenterY - mOuterRadius * Math.cos(Math.toRadians(arcWidth)) + 3 * tmpRadius), 0, 180, false);
        path.close();
        canvas.drawPath(path, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    private void updateCanvas(Canvas canvas) {
        drawIcons(canvas);
        drawSelectedIcon(canvas);
//        if (Math.abs(mOffsetDegree) > 10) {
//            mOffsetDegree *= 0.9;
//            invalidate();
//        } else if (Math.abs(mOffsetDegree) > 1) {
//            for (int i = 0; i < mSelectedIconArray.length; i++) {
//                if (currentIcon == mSelectedIconArray[i]) {
//                    rotateDegree = 360 / mSelectedIconArray.length * i;
//                }
//            }
//            if (mIsRequiresUpdate) {
//                invalidate();
//                mIsRequiresUpdate = false;
//            }
//            if (mOffsetDegree > 0) {
//                mOffsetDegree--;
//            }
//            if (mOffsetDegree < 0) {
//                mOffsetDegree++;
//            }
//            invalidate();
//        }

    }

    /**
     * 绘制外围的图标
     *
     * @param canvas
     */
    private void drawIcons(Canvas canvas) {
        canvas.save();
        if (mEntities == null || mEntities.size() == 0) {
            return;
        }

        int size = mEntities.size();
        rotateDegree += mOffsetDegree;
        canvas.rotate((float) rotateDegree, mCenterX, mCenterY);
        double iconLeft;
        double iconTop;
        float singleDegree = 360.0f / size;
        for (int i = 0; i < size; i++) {
//            Bitmap icon = ImageUtil.rotatingImageView((int) (360 - rotateDegree + singleDegree * (length - i) - mArrowPosition), mIconBmpArray[i]);
            Bitmap icon;
            if (mIsFront && mEntities.get(i).frontDisable) {
                icon = mEntities.get(i).disabledBmp;
            } else {
                icon = mEntities.get(i).normalBmp;
            }
            iconLeft = mCenterX - icon.getWidth() / 2;
            iconTop = mCenterY - mRadius - icon.getHeight() / 2;
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
            canvas.rotate(-singleDegree, mCenterX, mCenterY);
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
        Bitmap seletedBmp;
        if (mIsFront && mEntities.get(mCurrentIconIndex).frontDisable) {
            seletedBmp = mEntities.get(mLastSelectedIconIndex).selectedBmp;
        } else {
            seletedBmp = mEntities.get(mCurrentIconIndex).selectedBmp;
        }
        canvas.drawBitmap(seletedBmp, mCenterX - seletedBmp.getWidth() / 2, mCenterY - seletedBmp.getHeight() / 2, mPaint);
        if (mCurrentIconIndex != mLastFlingSelectedIconIndex) {
            VibratorUtil.Vibrate(getContext(), mVibratorTime);
            mLastFlingSelectedIconIndex = mCurrentIconIndex;
        }
        if (!mIsFling && mIsTouchUp && (mCurrentIconIndex != mLastSelectedIconIndex)) {
            mLastSelectedIconIndex = mCurrentIconIndex;
            invokeSelectItem();
        }
    }

    private void selecteIcon() {
        //第i个图标所在的位置
        int position;
        int size = mEntities.size();
        boolean isSelectedIndex;
        for (int i = 0; i < size; i++) {
            position = (int) (360 + rotateDegree - 360.0f / size * i + mArrowPosition) % 360;
            isSelectedIndex = Math.abs(position - mArrowPosition) < (360.0f / size / 2) || Math.abs(position - mArrowPosition) > (360 - 360.0f / size / 2);
            if (isSelectedIndex) {
                if (mIsFront) {
                    mCurrentIconIndex = findClosestAvailableIndex(i);
                } else {
                    mCurrentIconIndex = i;
                }
            }
        }
    }

    /**
     * 找到距离position最近的不是disable状态的图标索引
     * @param position
     * @return
     */
    private int findClosestAvailableIndex (int position) {
        if (!mEntities.get(position).frontDisable) {
            return position;
        }
        int size = mEntities.size() / 2;
        int result;
        for (int i = 1; i < size; i++) {
            result = position + i;
            if (result > mEntities.size() - 1) {
                result -= (mEntities.size() - 1);
            }
            if (!mEntities.get(result).frontDisable) {
                return result;
            }
            result = position - i;
            if (result < 0) {
                result += (mEntities.size() - 1);
            }
            if (!mEntities.get(result).frontDisable) {
                return result;
            }
        }
        return position;
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
                if (mIsFling) {
//                    removeCallbacks(mFlingRunnable);
//                    mIsFling = false;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                currentY = event.getY();
                //利用叉乘|AXB|=x1 * y2 - x2 * y1 = |A| * |B| * sinA得到角度的正弦值
                double arcSinDegree = ((mLastX - mCenterX) * (currentY - mCenterY) - (mLastY - mCenterY) * (currentX - mCenterX)) /
                        Math.sqrt(((mLastX - mCenterX) * (mLastX - mCenterX) + (mLastY - mCenterY) * (mLastY - mCenterY)) * ((currentX - mCenterX) *
                                (currentX - mCenterX) + (currentY - mCenterY) * (currentY - mCenterY)));
                //这里除以2是想让转盘转动的角度不会太大
                rotateDegree += Math.toDegrees(Math.asin(arcSinDegree)) / 2;
                mOffsetTotalDegree += Math.toDegrees(Math.asin(arcSinDegree)) / 2;
                rotateDegree %= 360;
                mLastX = event.getX();
                mLastY = event.getY();
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                mIsTouchUp = true;
                mIsFling = true;
                mCurrentTime = System.currentTimeMillis();
                mVelocityTracker.computeCurrentVelocity(1);
                currentX = event.getX();
                currentY = event.getY();
                if (Math.abs((mVelocityTracker.getXVelocity())) > 0.5 || Math.abs(mVelocityTracker.getYVelocity()) > 0.5) {
                    updateFlingView(mVelocityTracker.getXVelocity(), mVelocityTracker.getYVelocity());
                } else if (mCurrentTime - mLastTime < 300 && Math.abs(currentX - mLastX) < 2 && Math.abs(currentY - mLastY) < 2 ) {
                    //handle click event
                    handleClickEvent(currentX, currentY);
                } else {
                    mIsFling = false;
                    handleDragEvent();
                }
                mOffsetTotalDegree = 0.0f;
                return true;

        }

        return super.onTouchEvent(event);
    }

    private void handleClickEvent(float currentX, float currentY) {
        if (!mIsZoomOut) {
            mIsFling = false;
            invokeClickInnerCircle();
            mIsZoomOut = true;
            return;
        }
        if (Math.sqrt((currentX - mCenterX) * (currentX - mCenterX) + (currentY - mCenterY) * (currentY - mCenterY)) > mOuterRadius) {
            //TODO click out of circle
        } else if (Math.sqrt((currentX - mCenterX) * (currentX - mCenterX) + (currentY - mCenterY) * (currentY - mCenterY)) < mInnerRadius) {
            mIsFling = false;
            invokeClickInnerCircle();
            mIsZoomOut = false;
        } else {
            handleClickItem(currentX, currentY);
        }
    }

    private void invokeClickInnerCircle() {
        if (!mIsFling && mIsTouchUp && mSeletectItemListener != null) {
            mSeletectItemListener.onClickInnerCircle();
        }
    }

    private void handleDragEvent() {
        setRotateDegree(mCurrentIconIndex * (360.0f / mEntities.size()));
    }

    private void setRotateDegree(float degree) {
        rotateDegree = degree;
        invalidate();
    }

    public void setRotateIndex(int index) {
        int size = mEntities.size();
        if (index < 0 || index > size) {
            return;
        }
        setRotateDegree(index * (360.0f / size));
    }

    private void handleClickItem(float currentX, float currentY) {
        //触摸的点与中心点组成向量(currentX - mCenterX) (, (mCenterY - currentY))
        float degree = (float) Math.toDegrees(Math.acos((mCenterY - currentY) / Math.sqrt((currentX - mCenterX) * (currentX - mCenterX) + (currentY - mCenterY) * (currentY - mCenterY))));
        if ((currentX - mCenterX) < 0) {
            degree = 360 - degree;
        }
        degree += mScreenCurrentDegree;
        degree %= 360;
        //以正上方为0度，degree在90度与270度之间，圆盘是按顺时针方向旋转
        boolean clockwise = degree >90 && degree < 270;
        //计算相对于index为0的偏移量
        degree -= 270;
        if (degree < 0) {
            degree = 360 + degree;
        }
        //因为是图标是按逆时针顺序排列，所以需要360-degree
        int clickIndex = (int) (360 - degree + rotateDegree + 360 / 2 / mEntities.size()) % 360 / (360 / mEntities.size());
        //如果处于前置的状态且点击的图标是disable的，那么就没有反应
        if (mIsFront && mEntities.get(clickIndex).frontDisable) {
            return;
        }
        float flingDegree;
        int offsetIndex;
        if (clockwise) {
            offsetIndex = clickIndex - mCurrentIconIndex;
        } else {
            offsetIndex = mCurrentIconIndex - clickIndex;
        }
        if (offsetIndex < 0) {
            offsetIndex += mEntities.size();
        }
        flingDegree = 360.0f / mEntities.size() * offsetIndex * (clockwise? 1 : -1);
        post(mFlingRunnable = new AutoFlingRunnable(flingDegree, false, clockwise, 10));
    }

    /**
     * 根据http://hellerfu.com/android-3d-cube.html，5.4.4（7），参考三角形面积公式可以得到中心点与手指离开时的向量的位置(中心点位于向量的左边还是右边)
     * 结合位置再根据向量的方向可以得到是顺时针还是逆时针
     */
    private boolean calculateClockwise(float centerX, float centerY, float x, float y, float xVelocity, float yVelocity) {
        float area = (y * (x + xVelocity)) - (x * (y + yVelocity)) - xVelocity * centerY + yVelocity * centerX;
        //小于0是说明中心点在向量的右边，那么也就是说向量的方向是顺时针方向
        return area < 0;
    }

    private void updateFlingView(float xVelocity, float yVelocity) {
        float flingDegree = (float) ((mOffsetTotalDegree * 1000) / (mCurrentTime * 1.0 - mLastTime * 1.0));
        if (Math.abs(flingDegree) < 10 && (Math.abs(xVelocity) > 10 || Math.abs(yVelocity) > 10)) {
            float velocity = Math.max(Math.abs(xVelocity), Math.abs(yVelocity));
            flingDegree = velocity * velocity;
            if (mOffsetTotalDegree < 0) {
                flingDegree = 0 - flingDegree;
            }
        }
        post(mFlingRunnable = new AutoFlingRunnable(flingDegree, true, true, mFlingAcceleration));
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    public void setOnSelectItemListener(OnSelectItemListener listener) {
        mSeletectItemListener = listener;
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

    public void setEntities(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        mEntities = entities;
        int length = mEntities.size();
        float singleDegree = 360.0f / length;
        for (int i = 0; i < length; i++) {
            mEntities.get(i).normalBmp = ImageUtil.rotatingImageView((int) (360 - rotateDegree + singleDegree * length - mArrowPosition),  mEntities.get(i).normalBmp);
            if (mEntities.get(i).disabledBmp == null) {
                mEntities.get(i).disabledBmp = mEntities.get(i).normalBmp;
            } else {
                mEntities.get(i).disabledBmp = ImageUtil.rotatingImageView((int) (360 - rotateDegree + singleDegree * length - mArrowPosition),  mEntities.get(i).disabledBmp);
            }
            mEntities.get(i).selectedBmp = ImageUtil.rotatingImageView(0 - mArrowPosition, mEntities.get(i).selectedBmp);
        }
        invalidate();
    }

    public void enableFilter(boolean enabled) {
        mFilterEnabled = enabled;
    }

    public void invokeSelectItem() {
        if (!mIsFling && mIsTouchUp && mSeletectItemListener != null) {
            mSeletectItemListener.onSelected(this, mCurrentIconIndex);
        }
    }

    public void switchType() {
        mIsFront = !mIsFront;
        setRotateIndex(mLastAvailableIndex);
        mLastAvailableIndex = mCurrentIconIndex;
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    /**
     * 用来处理这个view的转屏事件
     * @param orientation
     * @param animation
     */
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

    public interface OnSelectItemListener {
        public void onSelected(View view, int position);
        public void onClickInnerCircle();
    }

    public void setVibratorTime (int time) {
        mVibratorTime = time;
    }

    public void setAccelerator (float accelerator) {
        mFlingAcceleration = accelerator;
    }

    /**
     * 自动滚动的任务
     *
     */
    private class AutoFlingRunnable implements Runnable {
        private boolean isGradient;
        private float flingVelocity;
        private boolean isClockwiseFling;
        private float flingAcceleration;

        public AutoFlingRunnable(float flingVelocity, boolean isGradient, boolean isClockwiseFling, float flingAcceleration) {
            this.isClockwiseFling = isClockwiseFling;
            this.flingVelocity = flingVelocity;
            this.isGradient = isGradient;
            this.flingAcceleration = flingAcceleration;
        }

        public void run() {
            mIsFling = true;
            if ((int) Math.abs(flingVelocity) < 5) {
                mIsFling = false;
                mIsTouchUp = true;
                mOffsetDegree = 0;
                setRotateDegree(mCurrentIconIndex * (360.0f / mEntities.size()));
                return;
            }
            if (isGradient) {
                // 逐渐减小这个值
                mOffsetDegree = flingVelocity / 1.066f / 30;
                flingVelocity /= flingAcceleration;
            } else {
                mOffsetDegree = isClockwiseFling ? flingAcceleration : -flingAcceleration;
                flingVelocity += isClockwiseFling ? -flingAcceleration : flingAcceleration;
            }
            // 重新绘制
            invalidate();
            postDelayed(this, 16);
        }
    }

    public static class Entity {
        public Bitmap normalBmp;
        public Bitmap selectedBmp;
        public Bitmap disabledBmp;
        public int index;
        public boolean frontDisable;

        public Entity(Bitmap normalBmp, Bitmap selectedBmp, Bitmap disabledBmp, int index, boolean frontDisable) {
            this.normalBmp = normalBmp;
            this.selectedBmp = selectedBmp;
            this.disabledBmp = disabledBmp;
            this.index = index;
            this.frontDisable = frontDisable;
        }
    }
}
