package com.cuonghx.teacher.teachercheckin.screens.chat;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.ChatLog;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;
import com.cuonghx.teacher.teachercheckin.screens.adapter.ChatLogAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class ChatLogActivity extends BaseActivity implements View.OnClickListener {


    private RecyclerView mRecyclerView;
    private ChatLogAdapter mAdapter;
    private EditText mEditTextChat;
    private ImageView mImageViewSender;
    private int courseId;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_chat_log;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        courseId = getIntent().getIntExtra("course_id", 0);
        mRecyclerView = findViewById(R.id.rcv_text_chat);
        mAdapter = new ChatLogAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);

        mEditTextChat = findViewById(R.id.et_text_chat);
        mImageViewSender = findViewById(R.id.bt_sent_chat);
        mEditTextChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    mImageViewSender.setImageResource(R.drawable.ic_sent_chat);
                    mImageViewSender.setClickable(true);
                }else{
                    mImageViewSender.setImageResource(R.drawable.ic_send);
                    mImageViewSender.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mImageViewSender.setOnClickListener(this);
        mImageViewSender.setClickable(false);
        setTitle(getIntent().getStringExtra("course_name"));
        addObsever();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_sent_chat:
                String message = String.valueOf(mEditTextChat.getText());
                mEditTextChat.setText("");
                AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                        .sendMessage(courseId, message, FirebaseAuth.getInstance().getCurrentUser().getUid(), "1", FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {

                            }
                        }).subscribe(new Consumer<BaseResponse>() {
                    @Override
                    public void accept(BaseResponse baseResponse) throws Exception {
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

    private void addObsever() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Messenger");
        myRef.child("Course_" + courseId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatLog chatLog = dataSnapshot.getValue(ChatLog.class);
//                Log.d("cuonghx", "onChildAdded: " + chatLog.isTeacher);
                mAdapter.addItem(chatLog);
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
