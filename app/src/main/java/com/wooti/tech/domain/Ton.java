package com.wooti.tech.domain;


import com.wooti.tech.domain.Domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class Ton extends Domain {

    //private int Idx;

    private String value;

    public Ton(String value) {
        this.value = value;
    }
}
