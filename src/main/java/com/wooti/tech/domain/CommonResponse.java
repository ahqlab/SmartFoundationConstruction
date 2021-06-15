package com.wooti.tech.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CommonResponse<D> implements Serializable {

    public String resultMessage;

    public D domain;

}