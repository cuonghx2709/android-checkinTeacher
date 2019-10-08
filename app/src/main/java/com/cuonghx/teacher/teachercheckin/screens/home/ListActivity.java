package com.cuonghx.teacher.teachercheckin.screens.home;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.Course;
import com.cuonghx.teacher.teachercheckin.data.model.Student;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;
import com.cuonghx.teacher.teachercheckin.screens.adapter.ListAdapter;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class ListActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private ListAdapter mListAdapter;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
        getData();
    }
    private void setupView(){
        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);
        mRecyclerView = findViewById(R.id.rcv_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mListAdapter = new ListAdapter(this, getIntent().getIntExtra("course_id", 0));
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
    private void getData(){
        int course_id = getIntent().getIntExtra("course_id", 0);
        Log.d("cuonghx", "getData: " + course_id);
        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                .getCheckin(course_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                }).subscribe(new Consumer<List<Student>>() {
            @Override
            public void accept(List<Student> students) throws Exception {
                for (Student student : students) {
                    mListAdapter.addItem(student);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                handleErrors(throwable);
            }
        });
    }
    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
