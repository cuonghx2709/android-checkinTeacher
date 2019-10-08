package com.cuonghx.teacher.teachercheckin.screens.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.Course;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.screens.BaseRecyclerViewAdapter;
import com.cuonghx.teacher.teachercheckin.screens.chat.ChatLogActivity;
import com.cuonghx.teacher.teachercheckin.screens.home.ListActivity;
import com.cuonghx.teacher.teachercheckin.screens.timer.TimerCountDownActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class CourseAdapter extends BaseRecyclerViewAdapter<Course, CourseAdapter.ViewHolder> {

    public CourseAdapter(Context context) {
        super(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//        // Inflate the custom layout
        View  courseView = layoutInflater.inflate(R.layout.item_rcview_course_2, parent, false);
        return new ViewHolder(courseView, mContext);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setupView(mCollection.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView nameCourse;
        private TextView mCode;
        private Context mContext;
        private Course mCurrentCourse;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            mContext = context;
            nameCourse = itemView.findViewById(R.id.tv_it_course_name);
            mCode = itemView.findViewById(R.id.tv_it_course_code);

            itemView.findViewById(R.id.bt_checkin_item).setOnClickListener(this);
            itemView.findViewById(R.id.bt_sendMessage_item).setOnClickListener(this);
            itemView.findViewById(R.id.bt_tk_item).setOnClickListener(this);
        }

        public void setupView(Course course){
            nameCourse.setText(course.getName());
            mCurrentCourse = course;
            mCode.setText(course.getmCode().toUpperCase());
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.bt_checkin_item :
                    Log.d("cuonghx", "onClick: check" );
                    AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                            .startCheckIn(mCurrentCourse.getId())
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
                                Toast.makeText(mContext, "Hoàn tất !", Toast.LENGTH_SHORT).show();
                                AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                                        .notification(mCurrentCourse.getId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnSubscribe(new Consumer<Disposable>() {
                                            @Override
                                            public void accept(Disposable disposable) throws Exception {
                                                Log.d("cuonghx", "accept: ");
                                                Intent intent = new Intent(mContext, TimerCountDownActivity.class);
                                                intent.putExtra("course_id", mCurrentCourse.getId());
                                                intent.putExtra("course_name", mCurrentCourse.getName());
                                                mContext.startActivity(intent);
                                            }
                                        }).subscribe(new Consumer<BaseResponse>() {
                                    @Override
                                    public void accept(BaseResponse baseResponse) throws Exception {
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
//                                        handleErrors(throwable);
                                    }
                                });
                            }else {
                                Toast.makeText(mContext, R.string.msg_something_went_wrong, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            handleErrors(throwable);
                        }
                    });
                    break;
                case R.id.bt_sendMessage_item:
                    Log.d("cuonghx", "onClick: check" );
                    Intent intent = new Intent(mContext, ChatLogActivity.class);
                    intent.putExtra("course_id", mCurrentCourse.getId());
                    intent.putExtra("course_name", mCurrentCourse.getName());
                    mContext.startActivity(intent);
                    break;
                case R.id.bt_tk_item:
                    Log.d("cuonghx", "onClick: check" );
                    intent = new Intent(mContext, ListActivity.class);
                    intent.putExtra("course_id", mCurrentCourse.getId());
                    intent.putExtra("course_name", mCurrentCourse.getName());
                    mContext.startActivity(intent);
                    break;
            }

        }
        private void handleErrors(Throwable throwable) {
            if (throwable instanceof HttpException) {
                handleHttpExceptions((HttpException) throwable);
                return;
            } else if (throwable instanceof UnknownHostException) {
                Toast.makeText(this.itemView.getContext(), R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this.itemView.getContext(),
                    R.string.msg_something_went_wrong,
                    Toast.LENGTH_SHORT)
                    .show();
        }

        private void handleHttpExceptions(HttpException httpException) {
            switch (httpException.code()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Toast.makeText(this.itemView.getContext(),
                            R.string.msg_wrong_email_or_password,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this.itemView.getContext(), httpException.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
