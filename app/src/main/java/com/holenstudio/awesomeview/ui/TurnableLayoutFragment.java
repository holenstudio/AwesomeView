package com.holenstudio.awesomeview.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.view.TurntableLayout;
import com.holenstudio.awesomeview.view.TurntableView;

/**
 * Created by Holen on 2016/6/14.
 */
public class TurnableLayoutFragment extends Fragment{
    private TurntableLayout turntableLayout;
    private int[] mIconArray = {
            R.drawable.auto
            , R.drawable.flower
            , R.drawable.night
            , R.drawable.run
            , R.drawable.scene
            , R.drawable.time
    };
    private int[] mSelectedArray = {
            R.drawable.auto_selected
            , R.drawable.flower_selected
            , R.drawable.night_selected
            , R.drawable.run_selected
            , R.drawable.scene_selected
            , R.drawable.time_selected
    };

    public static TurnableLayoutFragment getInstance(Bundle args) {
        TurnableLayoutFragment fragment = new TurnableLayoutFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_turnable_layout, container, false);
        turntableLayout = (TurntableLayout) view.findViewById(R.id.turntable_layout);
        turntableLayout.setIconArray(mIconArray, mSelectedArray);
        turntableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ObjectAnimator oa = ObjectAnimator.ofFloat(turntableLayout, "scaleX", 10);
//                oa.setDuration(300);
//                oa.start();
                PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("translationX", 300f);
                PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("translationY", 300f);
                PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat("scaleX", 3);
                PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat("scaleY", 3);
                ObjectAnimator.ofPropertyValuesHolder(turntableLayout, pvh1, pvh2, pvh3, pvh4).setDuration(300).start();
            }
        });
        turntableLayout.setOnItemClickListener(new TurntableLayout.OnItemClickListener() {
            @Override
            public void itemClick(View view, int pos) {
                Toast.makeText(getContext(), "点击了一下" + pos, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void viewClick(View view) {
                PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("translationX", 0f);
                PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("translationY", 0f);
                PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat("scaleX", 1);
                PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat("scaleY", 1);
                ObjectAnimator.ofPropertyValuesHolder(turntableLayout, pvh1, pvh2, pvh3, pvh4).setDuration(300).start();
            }
        });
        turntableLayout.setOnDragStopListener(new TurntableLayout.OnDragStopListener() {
            @Override
            public void doDragStopped(View view, int pos) {
                Toast.makeText(getContext(), "选择的是：" + pos, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
