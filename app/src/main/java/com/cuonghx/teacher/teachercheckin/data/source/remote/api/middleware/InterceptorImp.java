package com.cuonghx.teacher.teachercheckin.data.source.remote.api.middleware;


import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class InterceptorImp implements Interceptor {


    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = initializeHeader(chain);
        Request request = builder.build();
        return chain.proceed(request);
    }

    private Request.Builder initializeHeader(Chain chain) {
        Request originRequest = chain.request();
        return originRequest.newBuilder()
                .method(originRequest.method(), originRequest.body());
    }
}
