package com.holenstudio.awesomeview.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.holenstudio.awesomeview.R;
import com.holenstudio.awesomeview.util.ImageUtil;
import com.holenstudio.awesomeview.view.TurntableView;

/**
 * Created by Holen on 2016/6/14.
 */
public class TurnableFragment extends Fragment{
    private final String TAG = "TurnableFragment";
    private TurntableView turntableView;
    private int mOrientation;
    private int[] mIconArray = {
            R.drawable.auto
            , R.drawable.flower
            , R.drawable.night
            , R.drawable.run
            , R.drawable.scene
            , R.drawable.time
            , R.drawable.clock
            , R.drawable.flag
            , R.drawable.flight
            , R.drawable.heart
            , R.drawable.shield
    };
    private int[] mSelectedArray = {
            R.drawable.auto_selected
            , R.drawable.flower_selected
            , R.drawable.night_selected
            , R.drawable.run_selected
            , R.drawable.scene_selected
            , R.drawable.time_selected
            , R.drawable.clock_selected
            , R.drawable.flag_selected
            , R.drawable.flight_selected
            , R.drawable.heart_selected
            , R.drawable.shield_selected
    };
    private int[] mSeekbarIdArray = {
            R.id.outer_radius_seekbar
            , R.id.inner_radius_seekbar
            , R.id.vibrate_time_seekbar
            , R.id.acceleration_seekbar
    };
    private int[] mSeekbarValueIdArray = {
            R.id.outer_radius_value
            , R.id.inner_radius_value
            , R.id.vibrate_time_value
            , R.id.acceleration_value
    };
    private TurnableOrientationEventListener mOrientationEventListener;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener;

    public static TurnableFragment getInstance(Bundle args) {
        TurnableFragment fragment = new TurnableFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_turnable, container, false);
        turntableView = (TurntableView) view.findViewById(R.id.turntable_view);
        turntableView.setIconArray(mIconArray);
        turntableView.setSelectedIconArray(mSelectedArray);
        turntableView.setOnSelectItemListener(new TurntableView.OnSelectItemListener() {
            @Override
            public void onSelected(View view, int position) {
                Toast.makeText(getContext(), "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        mOrientationEventListener = new TurnableOrientationEventListener(getContext());

        ((SeekBar) (view.findViewById(mSeekbarIdArray[0]))).setMax(500);
        ((SeekBar) (view.findViewById(mSeekbarIdArray[1]))).setMax(300);
        ((SeekBar) (view.findViewById(mSeekbarIdArray[2]))).setMax(100);
        ((SeekBar) (view.findViewById(mSeekbarIdArray[3]))).setMax(500);

        mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int id = seekBar.getId();
                if (id == mSeekbarIdArray[0]) {
                    ((TextView) view.findViewById(mSeekbarValueIdArray[0])).setText("" + progress);
                } else if (id == mSeekbarIdArray[1]) {
                    ((TextView) view.findViewById(mSeekbarValueIdArray[1])).setText("" + progress);
                } else if (id == mSeekbarIdArray[2]) {
                    ((TextView) view.findViewById(mSeekbarValueIdArray[2])).setText("" + progress);
                    turntableView.setVibratorTime(progress);
                } else if (id == mSeekbarIdArray[3]) {
                    ((TextView) view.findViewById(mSeekbarValueIdArray[3])).setText("" + (progress * 1.0f / 1000 + 1) );
                    turntableView.setAccelerator(progress * 1.0f / 1000 + 1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        for (int i = 0; i < mSeekbarIdArray.length; i++) {
            ((SeekBar) (view.findViewById(mSeekbarIdArray[i]))).setOnSeekBarChangeListener(mSeekBarChangeListener);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mOrientationEventListener.enable();
    }

    @Override
    public void onPause() {
        super.onPause();
        mOrientationEventListener.disable();
    }

    public class TurnableOrientationEventListener extends OrientationEventListener {

        public TurnableOrientationEventListener(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if(orientation == OrientationEventListener.ORIENTATION_UNKNOWN){
                return;
            }
            Log.d(TAG, "orientation = " + orientation);
            mOrientation = ImageUtil.roundOrientation(orientation, mOrientation);
            turntableView.setOrientation(mOrientation, true);
        }
    }
}
