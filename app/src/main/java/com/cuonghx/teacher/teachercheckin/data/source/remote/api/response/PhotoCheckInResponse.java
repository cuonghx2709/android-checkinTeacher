package com.cuonghx.teacher.teachercheckin.data.source.remote.api.response;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoCheckInResponse extends BaseResponse {

    @Expose
    @SerializedName("upload_link")
    private String url;

    protected PhotoCheckInResponse(Parcel in) {
        super(in);
    }

    public PhotoCheckInResponse(int status) {
        super(status);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
