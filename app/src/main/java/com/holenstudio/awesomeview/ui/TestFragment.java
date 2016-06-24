package com.holenstudio.awesomeview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.holenstudio.awesomeview.R;

/**
 * Created by Holen on 2016/6/14.
 */
public class TestFragment extends Fragment{

    private static final String TAG = "TestFragment";

    public static TestFragment getInstance(Bundle args) {
        TestFragment fragment = new TestFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);


        return view;
    }

}
