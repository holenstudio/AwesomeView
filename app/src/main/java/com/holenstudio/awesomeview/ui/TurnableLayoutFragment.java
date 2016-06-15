package com.holenstudio.awesomeview.ui;

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

        return view;
    }

}
