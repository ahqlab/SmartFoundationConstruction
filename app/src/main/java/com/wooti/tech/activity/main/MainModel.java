package com.wooti.tech.activity.main;


import com.wooti.tech.domain.CommonListResponse;
import com.wooti.tech.domain.CommonResponse;
import com.wooti.tech.domain.Construction;
import com.wooti.tech.domain.Device;
import com.wooti.tech.model.common.CommonModel;
import com.wooti.tech.retrofit.NetRetrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainModel extends CommonModel {


    public void doLogin(Device domain , final DomainCallBackListner<CommonResponse<Device>> domainCallBackListner) {
        Call<CommonResponse<Device>> call = NetRetrofit.getInstance().getLoginService().doLogin(domain);
        call.enqueue(new Callback<CommonResponse<Device>>() {
            @Override
            public void onResponse(Call<CommonResponse<Device>> call, Response<CommonResponse<Device>> response) {
                domainCallBackListner.doPostExecute(response.body());
            }

            @Override
            public void onFailure(Call<CommonResponse<Device>> call, Throwable t) {
                domainCallBackListner.doPostExecute(null);
            }
        });
    }

    public void getAllDeivceList(final DomainCallBackListner<CommonListResponse<Device>> domainCallBackListner) {
        Call<CommonListResponse<Device>> call = NetRetrofit.getInstance().getLoginService().getDeviceAllList();
        call.enqueue(new Callback<CommonListResponse<Device>>() {
            @Override
            public void onResponse(Call<CommonListResponse<Device>> call, Response<CommonListResponse<Device>> response) {
                domainCallBackListner.doPostExecute(response.body());
            }

            @Override
            public void onFailure(Call<CommonListResponse<Device>> call, Throwable t) {
                domainCallBackListner.doPostExecute(null);
            }
        });
    }
}
