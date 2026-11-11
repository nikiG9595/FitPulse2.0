package com.fitpulse.service;
import java.util.*;
public final class AnatomyMuscles {
    private AnatomyMuscles(){}
    private static final Map<String,String> LABELS=Map.ofEntries(Map.entry("CHEST","гърди"),Map.entry("SHOULDERS_FRONT","предно рамо"),Map.entry("SHOULDERS_SIDE","средно рамо"),Map.entry("SHOULDERS_REAR","задно рамо"),Map.entry("BICEPS","бицепс"),Map.entry("TRICEPS","трицепс"),Map.entry("FOREARMS","предмишници"),Map.entry("TRAPEZIUS","трапец"),Map.entry("RHOMBOIDS","ромбовидни мускули"),Map.entry("LATS","широк гръбен мускул"),Map.entry("BACK_LOWER","долна част на гърба"),Map.entry("CORE","прав коремен мускул"),Map.entry("OBLIQUES","коси коремни мускули"),Map.entry("QUADS","предно бедро"),Map.entry("HAMSTRINGS","задно бедро"),Map.entry("ADDUCTORS","вътрешно бедро"),Map.entry("ABDUCTORS","странична част на таза"),Map.entry("GLUTES","седалище"),Map.entry("CALVES","прасци"));
    public static String names(String csv){if(csv==null||csv.isBlank())return "няма отделно посочени";return Arrays.stream(csv.split(",")).map(s->LABELS.getOrDefault(s.strip(),s)).distinct().reduce((a,b)->a+", "+b).orElse("");}
    /** Anatomical labels for the existing SVG zones; broad zones remain muscle groups, not invented subdivisions. */
    public static Map<String,String> anatomicalLabels(){
        var labels=new LinkedHashMap<>(LABELS);
        labels.put("CHEST","Голям гръден мускул");
        labels.put("SHOULDERS_FRONT","Предна част на делтовидния мускул");
        labels.put("SHOULDERS_SIDE","Средна част на делтовидния мускул");
        labels.put("SHOULDERS_REAR","Задна част на делтовидния мускул");
        labels.put("BICEPS","Двуглав мишничен мускул (бицепс)");
        labels.put("TRICEPS","Триглав мишничен мускул (трицепс)");
        labels.put("FOREARMS","Мускули на предмишницата");
        labels.put("TRAPEZIUS","Трапецовиден мускул");
        labels.put("RHOMBOIDS","Ромбовидни мускули");
        labels.put("LATS","Широк гръбен мускул");
        labels.put("BACK_LOWER","Разгъвачи на гръбначния стълб");
        labels.put("CORE","Прав коремен мускул");
        labels.put("OBLIQUES","Коси коремни мускули");
        labels.put("QUADS","Четириглав бедрен мускул (квадрицепс)");
        labels.put("HAMSTRINGS","Мускули на задното бедро");
        labels.put("ADDUCTORS","Аддуктори на бедрото");
        labels.put("ABDUCTORS","Абдуктори на тазобедрената става");
        labels.put("GLUTES","Седалищни мускули");
        labels.put("CALVES","Мускули на прасеца");
        return Collections.unmodifiableMap(labels);
    }
}
