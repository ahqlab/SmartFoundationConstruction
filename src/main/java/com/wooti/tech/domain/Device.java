package com.wooti.tech.domain;

import java.io.Serializable;

import lombok.Data;


@Data
public class Device implements Serializable {

    private int id;

    private int constructionIdx;

    private String lavelNo;

    private String bluetoothNo;

    private String tabletNo;

    private String password;

    private String tabletManager;

    private String startDate;

    private String endDate;

    public Device(String password) {
        this.password = password;
    }

    public Device() {

    }
}
