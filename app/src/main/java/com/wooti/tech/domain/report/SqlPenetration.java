package com.wooti.tech.domain.report;


import com.wooti.tech.domain.Domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class SqlPenetration extends Domain {

    private String reportIdx;

    private String name;

    private String value;

    public SqlPenetration(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public SqlPenetration(Penetration penetration, String reportIdx) {
        this.name = penetration.getName();
        this.value = penetration.getValue();
        this.reportIdx = reportIdx;
    }
}
