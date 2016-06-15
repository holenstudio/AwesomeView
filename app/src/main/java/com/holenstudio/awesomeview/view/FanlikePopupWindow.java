package com.holenstudio.awesomeview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.PopupWindow;

/**
 * Created by Holen on 2016/6/15.
 */
public class FanlikePopupWindow extends PopupWindow {
    private Context mContext;
    private FanlikeView mFanlikeView;

    public FanlikePopupWindow(Context context) {
        this(context, null);
    }

    public FanlikePopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        initView();
    }

    private void initView() {
//        mFanlikeView = new FanlikeView();
    }

}
