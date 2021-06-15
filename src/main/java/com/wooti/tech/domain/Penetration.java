package com.wooti.tech.domain;


import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Penetration extends Domain {

    private String penetrationNumber;

    private String penetrationValue;

    public Penetration(String penetrationNumber, String penetrationValue) {
        this.penetrationNumber = penetrationNumber;
        this.penetrationValue = penetrationValue;
    }
}
