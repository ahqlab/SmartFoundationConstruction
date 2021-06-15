package com.wooti.tech.retrofit.service;

import com.wooti.tech.domain.CommonResponse;
import com.wooti.tech.domain.Device;
import com.wooti.tech.domain.Report;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReportService {

    @POST("mobile/regist/report")
    Call<CommonResponse<Boolean>> sendReport(@Body Report report);

}
