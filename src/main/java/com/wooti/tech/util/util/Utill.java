package com.wooti.tech.util.util;

public class Utill {

    public static boolean stringNullCheck(String str){
        if(str != null){
            return !str.isEmpty();
        }
        return false;
    }
}
