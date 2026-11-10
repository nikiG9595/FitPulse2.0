package com.fitpulse.service;
import java.util.*;
public final class AnatomyMuscles {
    private AnatomyMuscles(){}
    private static final Map<String,String> LABELS=Map.ofEntries(Map.entry("CHEST","гърди"),Map.entry("SHOULDERS_FRONT","предно рамо"),Map.entry("SHOULDERS_SIDE","средно рамо"),Map.entry("SHOULDERS_REAR","задно рамо"),Map.entry("BICEPS","бицепс"),Map.entry("TRICEPS","трицепс"),Map.entry("FOREARMS","предмишници"),Map.entry("TRAPEZIUS","трапец"),Map.entry("RHOMBOIDS","ромбовидни мускули"),Map.entry("LATS","широк гръбен мускул"),Map.entry("BACK_LOWER","долна част на гърба"),Map.entry("CORE","прав коремен мускул"),Map.entry("OBLIQUES","коси коремни мускули"),Map.entry("QUADS","предно бедро"),Map.entry("HAMSTRINGS","задно бедро"),Map.entry("ADDUCTORS","вътрешно бедро"),Map.entry("ABDUCTORS","странична част на таза"),Map.entry("GLUTES","седалище"),Map.entry("CALVES","прасци"));
    public static String names(String csv){if(csv==null||csv.isBlank())return "няма отделно посочени";return Arrays.stream(csv.split(",")).map(s->LABELS.getOrDefault(s.strip(),s)).distinct().reduce((a,b)->a+", "+b).orElse("");}
}
