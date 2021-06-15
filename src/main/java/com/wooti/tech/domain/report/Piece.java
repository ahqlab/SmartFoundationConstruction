package com.wooti.tech.domain.report;


import com.wooti.tech.domain.Domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class Piece extends Domain {

    private int reportIdx;

    private String name;

    private String value;

    public Piece(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public Piece() {
    }
}
