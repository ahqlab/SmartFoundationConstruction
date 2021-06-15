package com.wooti.tech.domain.report;


import com.wooti.tech.domain.Domain;

import lombok.Data;

@Data
public class SqlPiece extends Domain {

    private String reportIdx;

    private String name;

    private String value;

    public SqlPiece(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public SqlPiece(Piece piece, String reportIdx) {
        this.name = piece.getName();
        this.value = piece.getValue();
        this.reportIdx = reportIdx;
    }
}
