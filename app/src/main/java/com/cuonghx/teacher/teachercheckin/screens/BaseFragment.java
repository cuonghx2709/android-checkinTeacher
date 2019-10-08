package com.cuonghx.teacher.teachercheckin.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuonghx.teacher.teachercheckin.util.navigator.Navigator;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public abstract class BaseFragment extends Fragment {

    protected Navigator mNavigator;

    @LayoutRes
    protected abstract int getLayoutResource();

    protected abstract void initComponentsOnCreate();

    protected abstract void initComponentsOnCreateView(@Nullable View rootView,
                                                       @Nullable Bundle savedInstanceState);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNavigator = new Navigator(this);
        initComponentsOnCreate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResource(), container, false);
        initComponentsOnCreateView(rootView, savedInstanceState);
        return rootView;
    }
}
