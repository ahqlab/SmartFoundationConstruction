package com.wooti.tech.activity.main;


import com.wooti.tech.domain.CommonListResponse;
import com.wooti.tech.domain.CommonResponse;
import com.wooti.tech.domain.Construction;
import com.wooti.tech.domain.Device;
import com.wooti.tech.model.common.CommonModel;

import java.io.Serializable;

public class MainPresenter implements MainIn.Presenter {

    MainIn.View view;

    MainModel model;

    public MainPresenter(MainIn.View view) {
        this.view = view;
        this.model = new MainModel();

    }

    @Override
    public void doLogin(Device domain) {
        model.doLogin(domain, new CommonModel.DomainCallBackListner<CommonResponse<Device>>() {
            @Override
            public void doPostExecute(CommonResponse<Device> response) {
                if(response.getDomain() != null){
                    view.setLoginResult(response.getDomain());
                }else{
                    view.idAndPasswordDoNotMatch(response.getResultMessage());
                    view.setProgressGone();
                }

            }

            @Override
            public void doPreExecute() {

            }

            @Override
            public void doCancelled() {

            }
        });
    }

    @Override
    public void getAllDeviceList() {
        model.getAllDeivceList(new CommonModel.DomainCallBackListner<CommonListResponse<Device>>() {
            @Override
            public void doPostExecute(CommonListResponse<Device> deviceCommonListResponse) {
                view.setDeviceList(deviceCommonListResponse.getDomain());
            }

            @Override
            public void doPreExecute() {

            }

            @Override
            public void doCancelled() {

            }
        });
    }
}
