package com.cuonghx.teacher.teachercheckin.screens.home;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.Course;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;
import com.cuonghx.teacher.teachercheckin.screens.BaseRecyclerViewAdapter;
import com.cuonghx.teacher.teachercheckin.screens.adapter.CourseAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class HomeActivity extends BaseActivity implements View.OnClickListener, BaseRecyclerViewAdapter.RecyclerViewItemListener<Course, CourseAdapter.ViewHolder> {

    private RecyclerView mRecyclerView;
    private CourseAdapter mRecycleAdapter;
    private Dialog dialogConfirm;
    private Course currentCourse;
    private static final int PERMISSION_REQUESTS = 1240;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }


    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setUpView();
    }
    private void setUpView(){

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        } else {
            Log.d("cuonghx", "onClick: ");
//            mNavigator.startActivity(HomeActivity.class, null);
        }

        mRecyclerView = findViewById(R.id.rcv_all_course);
        mRecycleAdapter = new CourseAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecycleAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        getData();
        findViewById(R.id.fab_add_course).setOnClickListener(this);
        mRecycleAdapter.setRecyclerViewItemListener(this);
        setTitle("Home");
        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (allPermissionsGranted()) {
//            mNavigator.startActivity(CheckInRealTime.class, null);
        }
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void getData() {
        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                .getCourse(CheckInApplication.getInstance().getCurrentEmail())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                }).subscribe(new Consumer<List<Course>>() {
            @Override
            public void accept(List<Course> courses) throws Exception {
                if(courses.size() != mRecycleAdapter.getItemCount()){
                    mRecycleAdapter.clearCollection();
                    mRecycleAdapter.addCollection(courses);
                }
                for (Course course : courses) {
                    Log.d("cuonghxx", "accept: " + course.getName());
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

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public void IsFinish(String alertmessage) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
//                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                        homeIntent.addCategory( Intent.CATEGORY_HOME );
//                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(homeIntent);
                        moveTaskToBack(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            finishAndRemoveTask();
                        }
                        Process.killProcess(Process.myPid());
//                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
//                        HomeActivity.super.onBackPressed();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertmessage)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_add_course:
                mNavigator.startActivity(CreateActivity.class, null);
                break;
            case R.id.tv_cancel_dialog:
                dialogConfirm.cancel();
                break;
            case R.id.tv_send_dialog:
                dialogConfirm.cancel();
                Toast.makeText(this, "Xin chờ", Toast.LENGTH_SHORT).show();
                AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                        .deleteCourse(currentCourse.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {

                            }
                        }).subscribe(new Consumer<BaseResponse>() {
                    @Override
                    public void accept(BaseResponse baseResponse) throws Exception {
                        if (baseResponse.getStatus() == 1){
                            Toast.makeText(HomeActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                            getData();
                        }else if (baseResponse.getStatus()  == -2){
                            Toast.makeText(HomeActivity.this, getText(R.string.msg_something_went_wrong_course), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(HomeActivity.this, getText(R.string.msg_something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handleErrors(throwable);
                    }
                });
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            Log.d("cuonghx", "onOptionsItemSelected: logout" );
            FirebaseAuth.getInstance().signOut();
            super.onBackPressed();
            return true;
        }else if (id == R.id.menu_create){
            mNavigator.startActivity(CreateActivity.class, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRecyclerViewItemClicked(RecyclerView recyclerView, final Course data, int position, CourseAdapter.ViewHolder viewholder) {
        PopupMenu popupMenu = new PopupMenu(this, viewholder.itemView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_select_course, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.menu_delete:
//                        Toast.makeText(HomeActivity.this, "Xoá" + data.getName(), Toast.LENGTH_SHORT).show();
                        dialogConfirm = new Dialog(HomeActivity.this);
                        dialogConfirm.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogConfirm.setCancelable(false);
                        dialogConfirm.setContentView(R.layout.dialog_confirm_delete);
                        TextView textView = dialogConfirm.findViewById(R.id.tv_dialog_course);
                        textView.setText(data.getName());
                        dialogConfirm.show();
                        currentCourse = data;
                        dialogConfirm.findViewById(R.id.tv_cancel_dialog).setOnClickListener(HomeActivity.this);
                        dialogConfirm.findViewById(R.id.tv_send_dialog).setOnClickListener(HomeActivity.this);
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
