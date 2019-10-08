package com.cuonghx.teacher.teachercheckin.data.source.remote;

import com.cuonghx.teacher.teachercheckin.data.model.Course;
import com.cuonghx.teacher.teachercheckin.data.model.Student;
import com.cuonghx.teacher.teachercheckin.data.source.AuthenticationDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.PhotoCheckInResponse;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.service.CheckInApi;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;

public class AuthenticationRemoteDataSource implements AuthenticationDataSource.RemoteDataSource {

    private static AuthenticationRemoteDataSource sAuthenticationRemoteDataSource;
    private CheckInApi mCheckInApi;

    public static AuthenticationRemoteDataSource getInstance(@NonNull CheckInApi checkInApi) {
        if (sAuthenticationRemoteDataSource == null) {
            sAuthenticationRemoteDataSource = new AuthenticationRemoteDataSource(checkInApi);
        }

        return sAuthenticationRemoteDataSource;
    }

    private AuthenticationRemoteDataSource(CheckInApi checkInApi) {
        mCheckInApi = checkInApi;
    }


    @Override
    public Observable<BaseResponse> register(String email, String id, String name) {
        return mCheckInApi.registerTeacher(id, email, name);
    }

    @Override
    public Observable<List<Course>> getCourse(String email) {
        return mCheckInApi.getCourses(email);
    }

    @Override
    public Observable<BaseResponse> sendMessage(int courseId, String message, String fromId, String isTeacher, String name) {
        return mCheckInApi.sendMessage(courseId, message, fromId, isTeacher, name);
    }

    @Override
    public Observable<BaseResponse> startCheckIn(int courseId) {
        return mCheckInApi.startCheckin(courseId);
    }


    @Override
    public Observable<BaseResponse> notification(int courseId) {
        return mCheckInApi.notification(courseId, "Start check in!","checkin" );
    }

    @Override
    public Observable<List<Student>> getCheckin(int courseId) {
        return mCheckInApi.getCheckIn(courseId);
    }

    @Override
    public Observable<List<PhotoCheckInResponse>> getView(int courseId, int studenId) {
        return mCheckInApi.getPhoto(courseId, studenId);
    }

    @Override
    public Observable<BaseResponse> createCourse(String email, String course_name, Double lat, Double log, String place) {
        return mCheckInApi.createCourse(email, course_name, lat, log, place);
    }

    @Override
    public Observable<BaseResponse> deleteCourse(int course_id) {
        return mCheckInApi.deleteCourse(course_id);
    }
}
