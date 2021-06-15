package com.wooti.tech.retrofit.service;

import com.wooti.tech.domain.CommonListResponse;
import com.wooti.tech.domain.CommonResponse;
import com.wooti.tech.domain.Device;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LoginService {

    @POST("mobile/device/login")
    Call<CommonResponse<Device>> doLogin(@Body Device domain);


    @GET("mobile/device/all/list")
    Call<CommonListResponse<Device>> getDeviceAllList();
}
