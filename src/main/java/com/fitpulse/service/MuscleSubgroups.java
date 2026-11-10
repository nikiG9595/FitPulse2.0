package com.fitpulse.service;

import com.fitpulse.model.entity.Exercise;
import java.util.*;

/** Uses the existing anatomical metadata, never infers a subgroup from the broad category alone. */
public final class MuscleSubgroups {
    public record Subgroup(String code,String name,String group,String zone){}
    public static final List<Subgroup> ALL=List.of(
        new Subgroup("quads","Квадрицепс","Крака","QUADS"),new Subgroup("hamstrings","Задно бедро","Крака","HAMSTRINGS"),
        new Subgroup("calves","Прасци","Крака","CALVES"),new Subgroup("adductors","Аддуктори","Крака","ADDUCTORS"),
        new Subgroup("lats","Широк гръбен","Гръб","LATS"),new Subgroup("traps","Трапец","Гръб","TRAPEZIUS"),new Subgroup("erectors","Разгъвачи на гърба","Гръб","BACK_LOWER"),
        new Subgroup("front-delts","Предно рамо","Рамене","SHOULDERS_FRONT"),new Subgroup("side-delts","Средно рамо","Рамене","SHOULDERS_SIDE"),new Subgroup("rear-delts","Задно рамо","Рамене","SHOULDERS_REAR"),
        new Subgroup("abs","Прав коремен мускул","Корем","CORE"),new Subgroup("obliques","Коси коремни мускули","Корем","OBLIQUES"));
    private MuscleSubgroups(){}
    public static Subgroup valid(String group,String code){return ALL.stream().filter(s->s.group().equals(group)&&s.code().equals(code)).findFirst().orElse(null);}
    public static boolean matches(Exercise exercise,Subgroup subgroup){
        if(subgroup==null)return true;
        return Arrays.stream(Objects.toString(exercise.getPrimaryZones(),"").split(",")).map(String::strip).anyMatch(subgroup.zone()::equals);
    }
}
