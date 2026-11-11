package com.fitpulse.service;

import java.net.URI;

/** Shared validation for optional links supplied through the gym forms. Never fetches external URLs. */
public final class GymContent {
    private GymContent(){}
    public static String text(String value){return value==null?"":value.strip();}
    public static boolean webUrl(String value){
        if(value==null||value.isBlank())return true;
        try{var uri=URI.create(value.strip());return ("https".equalsIgnoreCase(uri.getScheme())||"http".equalsIgnoreCase(uri.getScheme()))&&uri.getHost()!=null&&uri.getRawUserInfo()==null;}
        catch(IllegalArgumentException e){return false;}
    }
    public static boolean imageUrl(String value){
        if(value==null||value.isBlank())return true;
        if(value.startsWith("/images/")||value.startsWith("/css/images/")){
            try{var uri=URI.create(value);return uri.getQuery()==null&&uri.getFragment()==null&&uri.normalize().getPath().equals(value)&&!value.contains("\\");}catch(IllegalArgumentException e){return false;}
        }
        return webUrl(value);
    }
}
