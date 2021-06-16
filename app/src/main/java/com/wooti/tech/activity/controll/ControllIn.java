package com.wooti.tech.activity.controll;

import com.wooti.tech.domain.Penetration;
import com.wooti.tech.domain.Report;
import com.wooti.tech.domain.Ton;

import java.util.List;

public class ControllIn {

    interface View{

        void setPenetrationItems(List<Penetration> penetrationItems);

        void setPieceListview(List<Penetration> penetrationItems);

        void sendResult(Boolean domain);

        void setTonItems(List<Ton> list);

        void sendResultAtConnectNetwork(Boolean domain);
    }

    interface Presenter{

        void getPenetrationItems();

        void getPeiceItems();

        void getPenetrationItems(List<Penetration> data);

        void doSendReport(Report report);

        void doSendReportOfConnectNetwork(Report report);

        void getTonItems();

        void getTonItems(List<Ton> data);
    }
}
