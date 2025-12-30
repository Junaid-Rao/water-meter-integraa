package com.example.integraa_android_junaid.data.api;

import com.example.integraa_android_junaid.data.api.models.LoginResponse;
import com.example.integraa_android_junaid.data.api.models.PermissionResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("login")
    @FormUrlEncoded
    Call<LoginResponse> login(
            @Field("u") String username,
            @Field("p") String password,
            @Field("lat") double latitude,
            @Field("lng") double longitude
    );

    @GET("waterPermissions")
    Call<PermissionResponse> getPermissions(
            @Header("token") String token
    );
}

