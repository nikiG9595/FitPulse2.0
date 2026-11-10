package com.fitpulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;

/** Curated external links keyed by the existing stable exercise code; no embedded player. */
@Component
public final class ExerciseVideos {
    public record Video(String code,String url,String title,String channel,String checkedOn,String verification){}
    private final Map<String,Video> videos;
    public ExerciseVideos(ObjectMapper mapper){
        try(var stream=ExerciseVideos.class.getResourceAsStream("/content/exercise-videos.json")){
            if(stream==null)throw new IOException("Липсва каталогът с видеа.");
            Map<String,Video> found=new LinkedHashMap<>();
            for(var video:mapper.readValue(stream,Video[].class)){
                if(video.code()==null||!video.url().matches("https://www\\.youtube\\.com/watch\\?v=[A-Za-z0-9_-]{11}")||found.putIfAbsent(video.code(),video)!=null)
                    throw new IOException("Невалиден или дублиран адрес на видео.");
            }
            videos=Map.copyOf(found);
        }catch(IOException e){throw new IllegalStateException("Каталогът с видеа не може да се прочете.",e);}
    }
    public Video find(String code){return videos.get(code);}
}
