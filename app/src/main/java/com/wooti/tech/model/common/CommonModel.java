package com.wooti.tech.model.common;

import android.content.Context;

import com.wooti.tech.domain.CommonResponse;

import java.io.Serializable;
import java.util.List;

public class CommonModel {

    Context context;

    public static final int limitedTime = 10000;
    public static final int interval = 1000;

    public void loadData(Context context) {
        this.context = context;
    }

    public interface DomainCallBackListner<D extends Serializable> {
        void doPostExecute(D d);
        void doPreExecute();
        void doCancelled();

    }

    public interface DomainListCallBackListner<D extends Serializable> {
        void doPostExecute(List<D> d);
        void doPreExecute();
        void doCancelled();
    }
}
