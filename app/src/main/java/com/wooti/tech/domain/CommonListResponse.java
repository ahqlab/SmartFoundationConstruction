package com.wooti.tech.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class CommonListResponse<D> implements Serializable {

    public String resultMessage;

    public List<D> domain;

}