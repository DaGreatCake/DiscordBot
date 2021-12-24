package com.cakedevs.ChildLabourBot.tools;

public class Tools {
    public static String getReadableTime(Long nanos){

        long tempSec    = nanos/(1000*1000*1000);
        long sec        = tempSec % 60;
        long min        = (tempSec /60) % 60;
        long hour       = (tempSec /(60*60)) % 24;

        return String.format("%dh : %dm : %ds", hour,min,sec);
    }
}
