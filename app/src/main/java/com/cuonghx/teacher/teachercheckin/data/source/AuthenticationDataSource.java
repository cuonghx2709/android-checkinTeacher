package com.cuonghx.teacher.teachercheckin.data.source;

import com.cuonghx.teacher.teachercheckin.data.model.Course;
import com.cuonghx.teacher.teachercheckin.data.model.Student;
import com.cuonghx.teacher.teachercheckin.data.model.Teacher;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.PhotoCheckInResponse;


import java.util.List;

import io.reactivex.Observable;


public interface AuthenticationDataSource {

    interface RemoteDataSource {
        Observable<BaseResponse> register(String email, String id, String name);
        Observable<List<Course>> getCourse(String email);
        Observable<BaseResponse> sendMessage( int courseId, String message, String fromId, String isTeacher, String name);
        Observable<BaseResponse> startCheckIn( int courseId);
        Observable<BaseResponse> notification(int courseId);
        Observable<List<Student>> getCheckin(int courseId);
        Observable<List<PhotoCheckInResponse>> getView(int courseId, int studenId);
        Observable<BaseResponse> createCourse(String email, String course_name, Double lat, Double log, String place);
        Observable<BaseResponse> deleteCourse(int course_id);
    }

    interface LocalDataSource {
        void saveStudent(Teacher teacher);

        void deleteStudent();

        Teacher getLoggedStudent();

        String getLoginToken();
    }
}
