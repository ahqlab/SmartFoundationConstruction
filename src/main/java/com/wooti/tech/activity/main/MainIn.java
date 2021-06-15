package com.wooti.tech.activity.main;


import com.wooti.tech.domain.Construction;
import com.wooti.tech.domain.Device;

import java.util.List;

public interface MainIn {

    interface View{

        void setServerError(String message);

        void idAndPasswordDoNotMatch(String message);

        void setLoginResult(Device domain);

        void setProgressGone();

        void setDeviceList(List<Device> domain);
    }
    interface Presenter{

        void doLogin(Device domain);

        void getAllDeviceList();
    }
}
