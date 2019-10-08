package com.cuonghx.teacher.teachercheckin.screens;

import android.os.Bundle;

import com.cuonghx.teacher.teachercheckin.util.navigator.Navigator;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected Navigator mNavigator;

    @LayoutRes
    protected abstract int getLayoutResource();

    protected abstract void initComponents(Bundle savedInstanceState);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mNavigator = new Navigator(this);
        initComponents(savedInstanceState);
    }
}
