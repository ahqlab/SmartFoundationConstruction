package com.wooti.tech.domain;


import java.io.Serializable;

import lombok.Data;

@Data
public class Construction implements Serializable {

    private int id;

    private int role;

    private String name;

    private String location;

    private String partner;

    private String manager;

    private String password;

    private String contact;

    private String createDate;
}
