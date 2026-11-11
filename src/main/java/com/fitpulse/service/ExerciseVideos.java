package com.fitpulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;

/** Curated videos keyed by stable exercise code. Only independently duration-confirmed clips are exposed. */
@Component
public final class ExerciseVideos {
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Video(String code,String url,String title,String channel,String checkedOn,String verification,
                        Integer durationSeconds,boolean durationConfirmed,String durationSource,String movementEvidence){
        public String getEmbedUrl(){return "https://www.youtube-nocookie.com/embed/"+url.replaceFirst("^https://www\\.youtube\\.com/(?:watch\\?v=|shorts/)","")+"?autoplay=0&rel=0";}
    }
    private final Map<String,Video> videos;
    public ExerciseVideos(ObjectMapper mapper){
        try(var stream=ExerciseVideos.class.getResourceAsStream("/content/exercise-videos.json")){
            if(stream==null)throw new IOException("Липсва каталогът с видеа.");
            Map<String,Video> found=new LinkedHashMap<>();
            Set<String> codes=new HashSet<>();
            for(var video:mapper.readValue(stream,Video[].class)){
                if(video.code()==null||video.url()==null||!video.url().matches("https://www\\.youtube\\.com/(?:watch\\?v=|shorts/)[A-Za-z0-9_-]{11}")||!codes.add(video.code()))
                    throw new IOException("Невалиден или дублиран адрес на видео.");
                if(video.durationConfirmed()&&video.durationSeconds()!=null&&video.durationSeconds()>0&&video.durationSeconds()<=59
                        &&video.durationSource()!=null&&!video.durationSource().isBlank()&&video.movementEvidence()!=null&&!video.movementEvidence().isBlank())found.put(video.code(),video);
            }
            videos=Map.copyOf(found);
        }catch(IOException e){throw new IllegalStateException("Каталогът с видеа не може да се прочете.",e);}
    }
    public Video find(String code){return videos.get(code);}
    public Map<String,Video> all(){return videos;}
}
