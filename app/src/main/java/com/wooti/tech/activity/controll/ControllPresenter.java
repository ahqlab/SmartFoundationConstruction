package com.wooti.tech.activity.controll;


import com.wooti.tech.domain.CommonResponse;
import com.wooti.tech.domain.Penetration;
import com.wooti.tech.domain.Report;
import com.wooti.tech.domain.Ton;
import com.wooti.tech.model.common.CommonModel;

import java.util.ArrayList;
import java.util.List;

public class ControllPresenter implements ControllIn.Presenter {

    ControllIn.View view;

    ControllModel model;

    public ControllPresenter(ControllIn.View view) {
        this.view = view;
        this.model = new ControllModel();
    }

    @Override
    public void getPenetrationItems() {
        view.setPenetrationItems(model.getPenetrationItems()) ;
    }

    @Override
    public void getPeiceItems() {
        view.setPieceListview(model.getPeiceItems());
    }

    @Override
    public void getPenetrationItems(List<Penetration> data) {
        List<Penetration> list = new ArrayList<Penetration>();
        for (int i = 0; i < data.size(); i++) {
            list.add(new Penetration((i + 1) + "회", data.get(i).getPenetrationValue()));
        }
        view.setPenetrationItems(list);
    }

    @Override
    public void getTonItems(List<Ton> data) {
        List<Ton> list = new ArrayList<Ton>();
        for (int i = 0; i < data.size(); i++) {
            //list.add(new Penetration((i + 1) + "회", data.get(i).getPenetrationValue()));
            list.add(new Ton(data.get(i).getValue()));
        }
        view.setTonItems(list);
    }

    @Override
    public void doSendReport(Report report) {
        model.doSendReport(report, new CommonModel.DomainCallBackListner<CommonResponse<Boolean>>() {
            @Override
            public void doPostExecute(CommonResponse<Boolean> response) {
                view.sendResult(response.getDomain());
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
    public void doSendReportOfConnectNetwork(Report report) {
        model.doSendReport(report, new CommonModel.DomainCallBackListner<CommonResponse<Boolean>>() {
            @Override
            public void doPostExecute(CommonResponse<Boolean> response) {
                view.sendResultAtConnectNetwork(response.getDomain());
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
    public void getTonItems() {
        List<Ton> list = new ArrayList<Ton>();
        view.setTonItems(list);
    }


}
