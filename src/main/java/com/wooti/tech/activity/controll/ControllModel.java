package com.wooti.tech.activity.controll;

import android.content.Context;

import com.wooti.tech.domain.CommonResponse;
import com.wooti.tech.domain.Device;
import com.wooti.tech.domain.Penetration;
import com.wooti.tech.domain.Report;
import com.wooti.tech.model.common.CommonModel;
import com.wooti.tech.retrofit.NetRetrofit;
import com.wooti.tech.sharedPref.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ControllModel extends CommonModel {

    Context context;

    public SharedPrefManager sharedPrefManager;

    public List<Penetration> getPenetrationItems() {

        List<Penetration> list = new ArrayList<Penetration>();
       /* list.add(new Penetration("1회","0"));
        list.add(new Penetration("2회","0"));
        list.add(new Penetration("3회","0"));
        list.add(new Penetration("4회","0"));
        list.add(new Penetration("5회","0"));*/
        return list;
    }

    public List<Penetration> getPeiceItems() {
        List<Penetration> list = new ArrayList<Penetration>();
        list.add(new Penetration("단본",""));
        list.add(new Penetration("하단",""));
        list.add(new Penetration("상단",""));
        return list;
    }

    public void doSendReport(Report report, final DomainCallBackListner<CommonResponse<Boolean>> domainCallBackListner) {

        Call<CommonResponse<Boolean>> call = NetRetrofit.getInstance().getReportService().sendReport(report);
        call.enqueue(new Callback<CommonResponse<Boolean>>() {
            @Override
            public void onResponse(Call<CommonResponse<Boolean>> call, Response<CommonResponse<Boolean>> response) {
                domainCallBackListner.doPostExecute(response.body());
            }

            @Override
            public void onFailure(Call<CommonResponse<Boolean>> call, Throwable t) {
                //domainCallBackListner.doPostExecute(null);
            }
        });
    }
}
