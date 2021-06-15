package com.wooti.tech.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Report implements Serializable {

    private int id;

    private String reportIdx; //SQL Light 때문에 추가

    private int deviceIdx;
    //시공일자
    private String currentDateTime;



    //파일종류 2020-05-13 new
    private String pileType;
    //공법 2020-05-13 new
    private String method;


    //파일규격
    private String pileStandard;
    //위치
    private String location;
    //헤머무게
    private String hammaT;
    //낙하높이
    private String fallMeter;
    //관리기준
    private String managedStandard;
    //잔량
    private float balance;

    //파일넘버
    private String pileNo;
    //천공깊이
    private String drillingDepth;
    //관잎깊이
    private String intrusionDepth;

    //조각
    private List<com.wooti.tech.domain.report.Piece> piece;
    //용접개소
    private String connectLength;
    //합계
    private String totalConnectWidth;
    //비고
    private String bigo;


    private List<com.wooti.tech.domain.report.Penetration> penetrations;
    //평균관입량
    private String avgPenetrationValue;
    //최종관입량
    private String totalPenetrationValue;

    private String createDate;

    //극한지지력
    private String ultimateBearingCapacity;
    //단면적
    private String crossSection;
    //함마 효율
    private String hammaEfficiency;
    //탄성 계수
    private String modulusElasticity;


}
