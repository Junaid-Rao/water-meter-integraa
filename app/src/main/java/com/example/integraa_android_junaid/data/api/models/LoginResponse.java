package com.example.integraa_android_junaid.data.api.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("Token")
    private String token;

    @SerializedName("TrackingToken")
    private String trackingToken;

    @SerializedName("type")
    private String type;

    @SerializedName("block")
    private boolean block;

    @SerializedName("msg")
    private String msg;

    @SerializedName("startTimePause")
    private String startTimePause;

    @SerializedName("endTimePause")
    private String endTimePause;

    @SerializedName("userid")
    private String userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTrackingToken() {
        return trackingToken;
    }

    public void setTrackingToken(String trackingToken) {
        this.trackingToken = trackingToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStartTimePause() {
        return startTimePause;
    }

    public void setStartTimePause(String startTimePause) {
        this.startTimePause = startTimePause;
    }

    public String getEndTimePause() {
        return endTimePause;
    }

    public void setEndTimePause(String endTimePause) {
        this.endTimePause = endTimePause;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

