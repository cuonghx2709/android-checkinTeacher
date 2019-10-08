package com.cuonghx.teacher.teachercheckin.screens.timer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.ChatLog;
import com.cuonghx.teacher.teachercheckin.data.model.CheckIn;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import androidx.appcompat.widget.Toolbar;

final public class TimerCountDownActivity extends BaseActivity {

    private TextView mTextViewCountDown;

    private CountDownTimer mCountDownTimer;
    private TextView mTextViewTotalStudent;
    private TextView mTextViewAttendStudent;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("CheckIn");

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private int mCourseId;



    @Override
    protected int getLayoutResource() {
        return R.layout.activity_timer_count_down;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        configView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        save();
    }

    private void save() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 3600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();

            } else {
                startTimer();
            }
        }
    }

    private void configView() {
        configToolBar("Timer");
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        setTime(600000);
        mTimerRunning = true;
        mEndTime =  System.currentTimeMillis() + mTimeLeftInMillis;
        save();

        mCourseId = getIntent().getIntExtra("course_id", 0);
        String name = getIntent().getStringExtra("course_name");
        TextView nametv = findViewById(R.id.tv_name);
        nametv.setText(name);
        mTextViewTotalStudent = findViewById(R.id.tv_total_sv);
        mTextViewAttendStudent = findViewById(R.id.tv_attend_num);
        connectFirebase();
    }

    private void connectFirebase() {
        myRef.child("Course_" + mCourseId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CheckIn checkin = dataSnapshot.getValue(CheckIn.class);
                if ( checkin != null ) {
                    mTextViewAttendStudent.setText(checkin.attendStudent);
                    mTextViewTotalStudent.setText(checkin.totalStudent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void configToolBar(String title) {
        setTitle(title);
        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);
    }

    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }

    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
            }
        }.start();

        mTimerRunning = true;
    }

    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
    }

    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        mTextViewCountDown.setText(timeLeftFormatted);
    }

}
