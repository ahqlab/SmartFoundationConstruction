package com.wooti.tech.util.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String getCurrentDateTimeSec(){
        return new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss").format (System.currentTimeMillis());
        //return new SimpleDateFormat ( "yyyy-MM-dd").format (System.currentTimeMillis());
    }

    public static String getCurrentDateTime(){
       // return new SimpleDateFormat ( "yyyy-MM-dd hh:mm:ss").format (System.currentTimeMillis());
        return new SimpleDateFormat ( "yyyy-MM-dd").format (System.currentTimeMillis());
    }

    public static String getCurrentYear(){
        return new SimpleDateFormat ( "yyyy").format (System.currentTimeMillis());
    }

    public static String getCurrentMonth(){
        return new SimpleDateFormat ( "MM").format (System.currentTimeMillis());
    }

    public static String getCurrentDay(){
        return new SimpleDateFormat ( "dd").format (System.currentTimeMillis());
    }

    public static String getFullDateToTime(String fullDateTime){
        SimpleDateFormat orgDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat conDateFormat = new SimpleDateFormat("HH:mm");

        String conTime = null;
        try {
            Date orgDateTime = orgDateFormat.parse(fullDateTime);
            conTime = conDateFormat.format(orgDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  conTime;
    }

    public static String getFullDateToDate(String fullDateTime){
        SimpleDateFormat orgDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat conDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String conTime = null;
        try {
            Date orgDateTime = orgDateFormat.parse(fullDateTime);
            conTime = conDateFormat.format(orgDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  conTime;
    }

    public static int getLastDateNumberOfMonth(String year, String month){
        Log.e("HJLEE", DateUtil.getCurrentMonth());
        Log.e("HJLEE", month);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(year), Integer.parseInt(month)-1, 1); //?????? -1????????? ???????????? ??????
        //System.out.println(dateFormat.format(cal.getTime()));
        //System.out.println("????????????: "+cal.get(Calendar.YEAR));
        //System.out.println("?????????: "+(cal.get(Calendar.MONTH)+1)); //???????????? +1??? ?????? ?????????????????? ??????
        //System.out.println("????????? ???: "+cal.getMinimum(Calendar.DAY_OF_MONTH));
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
