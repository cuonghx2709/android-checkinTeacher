package com.cuonghx.teacher.teachercheckin.screens.home;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.Student;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.PhotoCheckInResponse;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;
import com.cuonghx.teacher.teachercheckin.screens.adapter.ViewAdapter;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewActivity extends BaseActivity {

    private RecyclerView mRecycleView;
    private ViewAdapter mViewAdapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_view;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
        getData();
    }

    private void getData() {
        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                .getView(getIntent().getIntExtra("course_id", 0),getIntent().getIntExtra("student_id", 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                }).subscribe(new Consumer<List<PhotoCheckInResponse>>() {
            @Override
            public void accept(List<PhotoCheckInResponse> photoCheckInResponses) throws Exception {
                for (PhotoCheckInResponse p : photoCheckInResponses) {
                    mViewAdapter.addItem(p.getUrl());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    private void setupView(){
        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);
        mRecycleView = findViewById(R.id.rcv_view);

        mViewAdapter = new ViewAdapter(this);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecycleView.setAdapter(mViewAdapter);
    }
}
