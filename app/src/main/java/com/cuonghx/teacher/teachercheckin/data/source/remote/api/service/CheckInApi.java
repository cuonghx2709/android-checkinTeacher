package com.cuonghx.teacher.teachercheckin.data.source.remote.api.service;

import com.cuonghx.teacher.teachercheckin.data.model.Course;
import com.cuonghx.teacher.teachercheckin.data.model.Student;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.PhotoCheckInResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CheckInApi {

    @POST("/teacher")
    @FormUrlEncoded
    Observable<BaseResponse> registerTeacher(@Field("teacher_id") String teacherId, @Field("email") String email,
                                             @Field("teacher_name") String teacher_name);
    @GET("/teacher/{email}/course")
    Observable<List<Course>> getCourses(@Path("email") String email);

    @POST("/message/{course_id}")
    @FormUrlEncoded
    Observable<BaseResponse> sendMessage(@Path("course_id") int courseId,
                                         @Field("message") String message,
                                         @Field("fromId") String fromId,
                                         @Field("isTeacher") String isTeacher,
                                         @Field("name") String name);

    @GET("/check_in/{course_id}/start/")
    Observable<BaseResponse> startCheckin(@Path("course_id") int courseId);

    @POST("/notification")
    @FormUrlEncoded
    Observable<BaseResponse> notification(@Field("course_id") int courseId,
            @Field("message") String message,
            @Field("context") String context);

    @GET("/check_in/course/{course_id}")
    Observable<List<Student>> getCheckIn(@Path("course_id") int courseId);

    @GET("/check_in/photo/{course_id}/{student_id}")
    Observable<List<PhotoCheckInResponse>> getPhoto(@Path("course_id") int courseId,
                                                    @Path("student_id") int studentId);

    @POST("/teacher/{email}/course")
    @FormUrlEncoded
    Observable<BaseResponse> createCourse(@Path("email") String email, @Field("course_name") String course_name, @Field("lat") Double lat, @Field("long") Double log, @Field("place") String place);

    @GET("/course/delete/{course_id}")
    Observable<BaseResponse> deleteCourse(@Path("course_id") int courseId);
}
