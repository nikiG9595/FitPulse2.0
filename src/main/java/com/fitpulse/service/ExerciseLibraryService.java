package com.fitpulse.service;

import com.fitpulse.model.entity.Exercise;
import com.fitpulse.repository.ExerciseRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class ExerciseLibraryService {
    private final ExerciseRepository exercises;
    public ExerciseLibraryService(ExerciseRepository exercises){this.exercises=exercises;}
    public Exercise get(String code){return exercises.findByCode(code).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
    public Exercise get(Long id){return exercises.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Невалидно упражнение."));}
    public List<Exercise> all(){return exercises.findAllByOrderByName();}
    public Page<Exercise> search(String q,String muscle,String difficulty,String equipment,String location,int page){
        String needle=normalize(q);
        List<Exercise> found=all().stream().filter(e->needle.isEmpty()||normalize(e.getName()+" "+e.getAliases()).contains(needle))
            .filter(e->empty(muscle)||e.getMuscle().equals(muscle)).filter(e->empty(difficulty)||e.getDifficulty().equals(difficulty))
            .filter(e->empty(equipment)||e.getEquipment().equals(equipment)).filter(e->empty(location)||e.getLocation().equals("Навсякъде")||e.getLocation().equals(location)).toList();
        int number=Math.min(Math.max(0,page),Math.max(0,(found.size()-1)/12));int start=number*12;
        return new PageImpl<>(found.subList(start,Math.min(start+12,found.size())),PageRequest.of(number,12),found.size());
    }
    public Exercise resolve(String name){String key=normalize(name);return all().stream().filter(e->normalize(e.getName()).equals(key)||Arrays.stream(e.getAliases().split("\\|")).anyMatch(a->normalize(a).equals(key))).findFirst().orElse(null);}
    public static String normalize(String text){return text==null?"":text.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");}
    private boolean empty(String text){return text==null||text.isBlank();}
}
