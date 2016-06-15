package com.holenstudio.awesomeview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.holenstudio.awesomeview.R;

/**
 * 可旋转的Layout
 * 参考自https://github.com/hongyangAndroid/Android-CircleMenu
 * 采用ViewGroup方式来管理各个图标，相对于自定义View更加灵活。
 * Created by Holen on 2016/6/15.
 */
public class TurntableLayout extends ViewGroup {

    /**
     * 该容器内child item的默认尺寸
     */
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 4f;
    /**
     * 菜单的中心child的默认尺寸
     */
    private float RADIO_DEFAULT_CENTERITEM_DIMENSION = 1 / 3f;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;

    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private static final int FLINGABLE_VALUE = 300;

    /**
     * 如果移动角度达到该值，则屏蔽点击
     */
    private static final int NOCLICK_VALUE = 3;

    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private int mFlingableValue = FLINGABLE_VALUE;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private float mPadding = 0;
    /**
     * 布局时的开始角度
     * <p>
     * private double mStartAngle = 0;
     * /**
     * 检测按下到抬起时旋转的角度
     */
    private float mTmpAngle;
    /**
     * 检测按下到抬起时使用的时间
     */
    private long mDownTime;

    /**
     * 判断是否正在自动滚动
     */
    private boolean isFling;
    private double mStartAngle = 0;
    private int[] mIconArray;
    private int[] mSelectedIconArray;
    private float mOuterRadius;
    private float mInnerRadius;
    private Context mContext;
    private OnItemClickListener mItemClickListener;
    /**
     * 记录上一次的x，y坐标
     */
    private float mLastX;
    private float mLastY;

    /**
     * 自动滚动的Runnable
     */
    private AutoFlingRunnable mFlingRunnable;

    public TurntableLayout(Context context) {
        this(context, null);
    }

    public TurntableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.AwesomeView);
        mOuterRadius = ta.getFloat(R.styleable.AwesomeView_outerRadius, 0);
        mInnerRadius = ta.getFloat(R.styleable.AwesomeView_innerRadius, 0);

        //无视padding
//        setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth = 0;
        int resHeight = 0;

        /**
         * 根据传入的参数，分别获取测量模式和测量值
         */
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        /**
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != MeasureSpec.EXACTLY
                || heightMode != MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            // 如果都设置为精确值，则直接取小值；
            resWidth = resHeight = Math.min(width, height);
        }

        setMeasuredDimension(resWidth, resHeight);

        //如果没有设置外半径和内半径
        if (mOuterRadius < 1) {
            mOuterRadius = Math.max(getMeasuredWidth(), getMeasuredHeight());
            mInnerRadius = mOuterRadius / 2;
        }

        //开始测量子view
        final int count = getChildCount();
        // icon尺寸
        int childSize = (int) (mOuterRadius * RADIO_DEFAULT_CHILD_DIMENSION);
        // icon测量模式
        int childMode = MeasureSpec.EXACTLY;

        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeasureSpec = -1;

            makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
                    childMode);
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }

        mPadding = RADIO_PADDING_LAYOUT * mOuterRadius;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mIconArray == null || mSelectedIconArray == null || mIconArray.length < 1 || mSelectedIconArray.length < 1) {
            return;
        }
        // Laying out the child views
        final int childCount = getChildCount();
        float left, top;
        //icon的尺寸
        int cWidth = (int) (mOuterRadius * RADIO_DEFAULT_CHILD_DIMENSION);
        //根据icon的个数计算角度
        float angleDelay = 360 / (getChildCount() - 1);
        //遍历去设置icon的位置
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            mStartAngle %= 360;
            //计算中心店到icon中心距离的位置
            float distance = mOuterRadius / 2f - cWidth / 2 - mPadding;
            //计算item横坐标
            left = mOuterRadius / 2 + Math.round(distance * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);
            //计算item纵坐标
            top = mOuterRadius / 2 + Math.round(distance * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f  * cWidth);
            child.layout((int) left, (int) top, (int) left + cWidth, (int) top + cWidth);
            mStartAngle += angleDelay;
        }
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.viewClick(v);
                }
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;

                // 如果当前已经在快速滚动
                if (isFling) {
                    // 移除快速滚动的回调
                    removeCallbacks(mFlingRunnable);
                    isFling = false;
                    return true;
                }

                break;
            case MotionEvent.ACTION_MOVE:

                /**
                 * 获得开始的角度
                 */
                float start = getAngle(mLastX, mLastY);
                /**
                 * 获得当前的角度
                 */
                float end = getAngle(x, y);

                // Log.e("TAG", "start = " + start + " , end =" + end);
                // 如果是一、四象限，则直接end-start，角度值都是正值
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mStartAngle += end - start;
                    mTmpAngle += end - start;
                } else {
                    // 二、三象限，色角度值是付值
                    mStartAngle += start - end;
                    mTmpAngle += start - end;
                }
                // 重新布局
                requestLayout();

                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:

                // 计算，每秒移动的角度
                float anglePerSecond = mTmpAngle * 1000
                        / (System.currentTimeMillis() - mDownTime);

                // Log.e("TAG", anglePrMillionSecond + " , mTmpAngel = " +
                // mTmpAngle);

                // 如果达到该值认为是快速移动
                if (Math.abs(anglePerSecond) > mFlingableValue && !isFling) {
                    // post一个任务，去自动滚动
                    post(mFlingRunnable = new AutoFlingRunnable(anglePerSecond));

                    return true;
                }

                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) {
                    return true;
                }

                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 主要为了action_down时，返回true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /**
     * 根据触摸的位置，计算角度
     *
     * @param xTouch
     * @param yTouch
     * @return
     */
    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - (mOuterRadius / 2d);
        double y = yTouch - (mOuterRadius / 2d);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    /**
     * 根据当前位置计算象限
     *
     * @param x
     * @param y
     * @return
     */
    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - mOuterRadius / 2);
        int tmpY = (int) (y - mOuterRadius / 2);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }

    }

    /**
     * 如果每秒旋转角度到达该值，则认为是自动滚动
     *
     * @param mFlingableValue
     */
    public void setFlingableValue(int mFlingableValue) {
        this.mFlingableValue = mFlingableValue;
    }

    /**
     * 设置内边距的比例
     *
     * @param mPadding
     */
    public void setPadding(float mPadding) {
        this.mPadding = mPadding;
    }

    /**
     * 获得默认该layout的尺寸
     *
     * @return
     */
    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }

    public void setIconArray(int[] iconArray, int[] selectedIconArray) {
        mIconArray = iconArray;
        mSelectedIconArray = selectedIconArray;
        addIcon();
//        requestLayout();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * 添加icon
     */
    private void addIcon() {

        /**
         * 根据用户设置的参数，初始化view
         */
        for (int i = 0; i < mIconArray.length; i++) {
            final int j = i;
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(mIconArray[i]);
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mItemClickListener != null) {
                        mItemClickListener.itemClick(v, j);
                    }
                }
            });
            // 添加view到容器中
            addView(imageView);
        }
    }


    /**
     * Item的点击事件接口
     */
    public interface OnItemClickListener {
        void itemClick(View view, int pos);

        void viewClick(View view);
    }

    /**
     * 自动滚动的任务
     *
     * @author zhy
     */
    private class AutoFlingRunnable implements Runnable {

        private float angelPerSecond;

        public AutoFlingRunnable(float velocity) {
            this.angelPerSecond = velocity;
        }

        public void run() {
            // 如果小于20,则停止
            if ((int) Math.abs(angelPerSecond) < 20) {
                isFling = false;
                return;
            }
            isFling = true;
            // 不断改变mStartAngle，让其滚动，/30为了避免滚动太快
            mStartAngle += (angelPerSecond / 30);
            // 逐渐减小这个值
            angelPerSecond /= 1.0666F;
            postDelayed(this, 30);
            // 重新布局
            requestLayout();
        }
    }
}
