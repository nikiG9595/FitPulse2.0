package com.fitpulse.service;

import com.fitpulse.model.entity.Gym;
import com.fitpulse.model.entity.GymPhoto;
import java.net.URI;
import java.util.Comparator;
import java.util.List;

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

    public static List<GymPhoto> orderedPhotos(Gym gym) {
        return gym.getPhotos().stream()
                .filter(photo -> !text(photo.getImageUrl()).isBlank() && imageUrl(photo.getImageUrl()))
                .sorted(Comparator.comparing(GymPhoto::isCoverPhoto).reversed()
                        .thenComparingInt(GymPhoto::getSortOrder))
                .toList();
    }
}
