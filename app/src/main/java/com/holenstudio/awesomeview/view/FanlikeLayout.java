package com.holenstudio.awesomeview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.model.OvalItem;

import java.util.List;

/**
 * 扇形的Layout
 * 采用ViewGroup方式来管理各个图标，相对于自定义View更加灵活。
 * Created by Holen on 2016/6/15.
 */
public class FanlikeLayout extends ViewGroup {
    private final static String TAG = "TurntableLayout";

    /**
     * 该容器内child item的默认尺寸
     */
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 4f;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private float mPadding = 0;
    /**
     * 布局时的开始角度
     * <p>
     */
    private double mStartAngle = 0;
    /**
     * 半径
     */
    private float mRadius;
    private Context mContext;
    /**
     * 布局中心的坐标点
     */
    private int mCenterX;
    private int mCenterY;
    /**
     * 每一个图标之间相隔的角度，数值为360/圆上图标的个数(不是所有图标的个数，因为图标还包括箭头图标和中间选中的图标)
     */
    private float mAngelDegree;
    /**
     * 记录上一次的x，y坐标
     */
    private float mLastX;
    private float mLastY;
    /**
     * 选中的位置
     */
    private int mSelectedIndex = 0;
    private List<OvalItem> mOvalItems;

    public FanlikeLayout(Context context) {
        this(context, null);
    }

    public FanlikeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.AwesomeView);
        mRadius = ta.getFloat(R.styleable.AwesomeView_radius, 0);

        //无视padding
        setPadding(0, 0, 0, 0);
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

        //如果没有设置半径
        if (mRadius < 1) {
            mRadius = Math.max(getMeasuredWidth(), getMeasuredHeight()) / 2;
        }

        //开始测量子view
        final int count = getChildCount();
        // icon尺寸
        int childSize = (int) (mRadius * RADIO_DEFAULT_CHILD_DIMENSION);
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

        mPadding = RADIO_PADDING_LAYOUT * mRadius;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mOvalItems == null || mOvalItems.size() < 1) {
            return;
        }
        mCenterX = (r - l) / 2;
        mCenterY = (b - t) / 2;
//        mStartAngle %= 360;
//        if (mStartAngle < 0) {
//            mStartAngle += 360;
//        }
//        // Laying out the child views
//        final int childCount = getChildCount();
//        //根据icon的个数计算角度
//        mAngelDegree = 360 / arrayLength;
//        float left, top;
//        //icon的尺寸
//        int iconWidth = (int) (mRadius * RADIO_DEFAULT_CHILD_DIMENSION);
//        int seletecdIconWidth = (int) (mRadius * RADIO_DEFAULT_CENTERITEM_DIMENSION);
//
//        //绘制箭头
//        View arrowView = getChildAt(childCount - 1);
//        int arrowWidth = (int) (mRadius * RADIO_DEFAULT_ARROW_DIMENSION);
//        int arrowDistance = (int) (mRadius + arrowWidth / 2);
//        float arrowLeft = mCenterX - Math.round(arrowDistance * Math.cos(Math.toRadians(mArrowPosition)) + arrowWidth / 2f);
//        float arrowTop = mCenterY - Math.round(arrowDistance * Math.sin(Math.toRadians(mArrowPosition)) + arrowWidth / 2f);
//        arrowView.layout((int) arrowLeft, (int) arrowTop, (int) arrowLeft + arrowWidth, (int) arrowTop + arrowWidth);
//
//        //绘制中间图片的位置及图片
//        View selectedView = getChildAt(childCount - 2);
//        //将mStartAngle增加一个偏移量可以使selected判定区域不再死板，比如第一个位置的判定区域不是0到angleDelay，而是-angleDelay/2到angleDelay/2
//        mSelectedIndex = ((int) (mAngelDegree / 2 + (mStartAngle - mArrowPosition)) / (int) mAngelDegree);
//        Log.d(TAG, "mSelectedIndex=" + mSelectedIndex + " mStartAngle=" + mStartAngle);
//        //因为索引跟旋转的方向恰好是相反的，所以得用最大值减去索引得出最终的索引；
//        mSelectedIndex = arrayLength - mSelectedIndex;
//        mSelectedIndex %= arrayLength;
//        ((ImageView) selectedView).setImageResource(mSelectedIconArray[mSelectedIndex]);
//        selectedView.layout((int) (mCenterX - seletecdIconWidth / 2), (int) (mCenterY - seletecdIconWidth / 2), (int) (mCenterX + seletecdIconWidth / 2), (int) (mCenterY + seletecdIconWidth / 2));
//        //遍历去设置icon的位置
//        for (int i = 0; i < childCount - 2; i++) {
//            final View child = getChildAt(i);
//            if (child.getVisibility() == GONE) {
//                continue;
//            }
//            //计算中心点到icon中心距离的位置
//            float distance = mRadius - iconWidth / 2 - mPadding;
//            //计算item横坐标
//            left = mCenterX - Math.round(distance * Math.cos(Math.toRadians(mStartAngle)) + iconWidth / 2f);
//            //计算item纵坐标
//            top = mCenterY - Math.round(distance * Math.sin(Math.toRadians(mStartAngle)) + iconWidth / 2f);
//            child.layout((int) left, (int) top, (int) left + iconWidth, (int) top + iconWidth);
//
//            mStartAngle += mAngelDegree;
//        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_MOVE:

                // 重新布局
                requestLayout();

                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:



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

//    /**
//     * 添加icon
//     */
//    private void addIcon() {
//
//        /**
//         * 根据用户设置的参数，初始化view
//         */
//        for (int i = 0; i < mIconArray.length; i++) {
//            final int j = i;
//            ImageView imageView = new ImageView(getContext());
//            imageView.setImageResource(mIconArray[i]);
//            imageView.setVisibility(View.VISIBLE);
//            imageView.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (!mIsZoomOut) {
//                        mClickListener.onClick(v);
//                        mIsZoomOut = true;
//                    } else if (mItemClickListener != null) {
//                        mItemClickListener.itemClick(v, j);
//                    }
//                }
//            });
//            // 添加view到容器中
//            addView(imageView);
//        }
//        //设置selectedview
//        ImageView selectedView = new ImageView(getContext());
//        selectedView.setImageResource(mSelectedIconArray[0]);
//        selectedView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mIsZoomOut) {
//                    mClickListener.onClick(v);
//                    mIsZoomOut = true;
//                } else if (mItemClickListener != null) {
//                    mIsZoomOut = false;
//                    mItemClickListener.viewClick(v);
//                }
//            }
//        });
//        addView(selectedView);
//        //设置arrowview
//        ImageView arrowView = new ImageView(getContext());
//        arrowView.setImageResource(mArrowResId);
//        arrowView.setRotation(mArrowPosition);
//        addView(arrowView);
//    }


}
