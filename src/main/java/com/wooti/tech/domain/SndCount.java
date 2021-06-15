package com.wooti.tech.domain;

import lombok.Data;

@Data
public class SndCount extends Domain {

    private int sndCount;

    private String currentDateTime;

    public SndCount(int sndCount) {
        this.sndCount = sndCount;
    }
}