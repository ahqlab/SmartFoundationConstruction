package com.wooti.tech.retrofit;


import com.wooti.tech.retrofit.service.LoginService;
import com.wooti.tech.retrofit.service.ReportService;

import lombok.Getter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Getter
public class NetRetrofit {

    private static NetRetrofit ourInstance = new NetRetrofit();

    public static NetRetrofit getInstance() {
        return ourInstance;
    }

    private NetRetrofit() {

    }

    Retrofit retrofit = new Retrofit.Builder()
            //.baseUrl("http://211.169.248.220/")
            .baseUrl("https://www.we8104.com/")
            //.baseUrl("http://192.168.0.113:8080/web-template-mybatis/")
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .build();


    LoginService loginService = retrofit.create(LoginService.class);

    ReportService reportService = retrofit.create(ReportService.class);

    public LoginService getLoginService() {
        return loginService;
    }

    public ReportService getReportService() {
        return reportService;
    }

}

