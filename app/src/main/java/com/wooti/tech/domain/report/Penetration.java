package com.wooti.tech.domain.report;


import com.wooti.tech.domain.Domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Penetration extends Domain {

    private int reportIdx;

    private String name;

    private String value;

    public Penetration(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Penetration() {
    }
}
