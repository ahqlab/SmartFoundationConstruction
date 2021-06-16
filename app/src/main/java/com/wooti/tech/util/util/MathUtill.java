package com.wooti.tech.util.util;

import android.util.Log;

public class MathUtill {


    public static double calDanish(float EH, float WR, float H, float L, float A, float E, double S) {
       /* */

        Log.e("HJLEE"  , "EH (헤머의 효율) : " + EH);
        Log.e("HJLEE"  , "H (낙하고 cm) : " + H);
        Log.e("HJLEE"  , "S (관입량) : " + S);
        Log.e("HJLEE"  , "L (말뚝길이) : " + L);
        Log.e("HJLEE"  , "A (말뚝이 단면적) : " + A);
        Log.e("HJLEE"  , "E (말뚝의 탄성계수) : " + E);
        Log.e("HJLEE"  , "WR (램중량) : " + WR);


        double l =  ((WR * H * L) * (EH / 100));
        Log.e("HJLEE", "분모1 : " + (WR * H * L));
        Log.e("HJLEE"  , "분모값 : " + l);
        double br =  (A * E) * 2;
        double r =  Math.round(br * 10 ) / 10.0;
        Log.e("HJLEE"  , "분자계산값 : " + r);
        double CT = Math.sqrt((l / r));
        //double CT = Math.round(bCT * 1000 ) / 1000.0;
        Log.e("HJLEE"  , "CT (탄성계수) : " + CT);
        Log.e("HJLEE"  , "RU 분모값 : " + (EH / 100) * WR * H);
        double RUBUNJA = Math.round((S + CT) * 10 ) / 10.0;
        Log.e("HJLEE"  , "RU 분자값 : " + RUBUNJA);
        double RU = ((EH / 100) * WR * H) / (S + CT);

        if(Double.isInfinite(RU)){
            return 0;
        }
       // Infinity

        //double RU = Math.round((S + CT) * 10 ) / 10.0;
        Log.e("HJLEE"  , "RU : " + RU);
        double rt = Math.round(RU * 10 ) / 10.0;
        Log.e("HJLEE", "리턴값 1 : " + Math.round(RU * 10 ) / 10.0);
        Log.e("HJLEE", "리턴값 2 : " + rt);

        return rt;
    }
}
