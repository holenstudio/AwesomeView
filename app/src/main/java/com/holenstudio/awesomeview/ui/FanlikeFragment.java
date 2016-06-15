package com.holenstudio.awesomeview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.view.FanlikePopupWindow;

/**
 * Created by Holen on 2016/6/14.
 */
public class FanlikeFragment extends Fragment {
    private FanlikePopupWindow mFanlikePopupWindow;
    private View mView;
    private float mCenterX;
    private float mCenterY;
    private long mTouchDownTime;
    private long mCurrentTime;

    public static FanlikeFragment getInstance(Bundle args) {
        FanlikeFragment fragment = new FanlikeFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fanlike, container, false);
        mView = view.findViewById(R.id.view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float currentX;
                float currentY;

//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mTouchDownTime = System.currentTimeMillis();
//                        mCenterX = event.getX();
//                        mCenterY = event.getY();
////                        if (mFanlikePopupWindow != null && mFanlikePopupWindow.isShowing()) {
////                            mFanlikePopupWindow.dismiss();
////                        }
//                        return true;
//                    case MotionEvent.ACTION_MOVE:
//                        mCurrentTime = System.currentTimeMillis();
//                        currentX = event.getX();
//                        currentY = event.getY();
//                        if (mCurrentTime - mTouchDownTime > 300 && (Math.abs(mCenterX - currentX) < 20) && (Math.abs(mCenterY - currentY) < 20)){
//                            if (mFanlikePopupWindow == null) {
////                                mFanlikePopupWindow = new FanlikePopupWindow();
//                            }
//                            mFanlikePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, (int) mCenterX, (int) mCenterY);
//                        }
//
//                        break;
//                    case MotionEvent.ACTION_UP:
//
//                        return true;
//
//                }
                return false;
            }
        });
    }
}
