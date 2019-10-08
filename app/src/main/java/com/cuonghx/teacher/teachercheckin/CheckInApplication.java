package com.cuonghx.teacher.teachercheckin;

import android.app.Application;
import android.util.Log;

import com.cuonghx.teacher.teachercheckin.data.source.remote.api.service.CheckInApi;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.service.CheckInService;
import com.cuonghx.teacher.teachercheckin.util.StringUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class CheckInApplication extends Application {

    private static CheckInApplication sCheckInApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sCheckInApplication = this;
    }

    public static CheckInApplication getInstance() {
        return sCheckInApplication;
    }

    public CheckInApi getCheckInApi() {
        return CheckInService.getInstance(this);
    }


    public String getCurrentEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("cuonghx", "getCurrentEmail: nullllll");
            return  null;
        }
        for (UserInfo userInfo : user.getProviderData()){
            if (!StringUtils.checkNullOrEmpty(userInfo.getEmail())){
                return  userInfo.getEmail();
            }
        }

        return  null;
    }
}
