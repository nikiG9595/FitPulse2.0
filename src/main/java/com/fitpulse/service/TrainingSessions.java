package com.fitpulse.service;

import com.fitpulse.model.entity.*;
import com.fitpulse.model.enums.*;
import java.util.*;

/** Shared source for the preview and new personal copies; never rewrites saved copies. */
public final class TrainingSessions {
    private TrainingSessions() {}
    public record Session(int number, String title, String schedule, List<?> exercises) {}

    public static List<ProgramTemplateItem> outline(ProgramTemplate p) {
        if (!p.getItems().isEmpty()) return List.copyOf(p.getItems());
        boolean home=p.getLocation()==TrainingLocation.HOME;
        boolean strength=p.getGoal()==FitnessGoal.STRENGTH;
        List<ProgramTemplateItem> result=new ArrayList<>();
        if(p.getGoal()==FitnessGoal.MOBILITY) {
            add(result,"Мобилност на рамене и гръб",60,"6–8 бавни повторения","Движи се в безболезнен обхват, без засилка.","Котешки гръб","Ротации на гръден отдел","Плъзгане на ръце по стена");
            add(result,"Таз, глезени и баланс",60,"8–10 на страна","Контролирай движението и дишай спокойно.","Мобилизация на глезен","Преходи 90/90 за таз","Баланс на един крак");
            add(result,"Възстановяване на цяло тяло",45,"30–40 секунди","Разтягането е леко, без болка; не задържай дъха.","Разтягане на сгъвачи на таза","Поза дете","Лека разходка на място");
        } else if(p.getGoal()==FitnessGoal.ENDURANCE) {
            add(result,"Равномерна аеробна тренировка",60,"20–30 минути","Поддържай темпо, при което можеш да говориш в кратки изречения.","Бързо ходене");
            add(result,"Силова издръжливост",60,"12–15","Движи се контролирано; остави няколко повторения в резерв.","Клек до стол","Лицеви опори от висока опора","Глутеус мост");
            add(result,"Леки интервали",60,"6 × 1 минута","Редувай минута по-бързо ходене с две минути спокойно ходене.","Интервално ходене");
            add(result,"Стабилност и мобилност",45,"8–12 на страна","Запази неутрален гръб и спокойно дишане.","Птица-куче","Страничен планк от колене","Ротации на гръден отдел");
            add(result,"Продължителна спокойна активност",60,"30–45 минути","Избери равен терен и лесно темпо; скъси при натрупана умора.","Спокойна разходка");
        } else if(p.getTrainingDays()>=4 && !home && p.getGoal()==FitnessGoal.MUSCLE && p.getTrainingDays()>=5) {
            add(result,"Гърди и трицепс",90,"8–12","Спускай плавно; дръж китките стабилни и остави 2 повторения в резерв.","Лежанка с дъмбели","Избутване на наклонена пейка","Разгъване за трицепс на скрипец");
            add(result,"Гръб и бицепс",90,"8–12","Не люлей тялото; прибирай лопатките и отпускай тежестта контролирано.","Вертикален скрипец","Гребане с опора","Сгъване с дъмбели");
            add(result,"Крака и корем",120,"8–12","Коленете следват посоката на стъпалата; стегни корема.","Лег преса","Румънска тяга с дъмбели","Сгъване за задно бедро","Коремно сгъване на постелка");
            add(result,"Горна част и рамене",90,"10–12","Избери умерена тежест и работи без повдигане на раменете към ушите.","Раменна преса с дъмбели","Хоризонтален скрипец","Разтваряне встрани");
            add(result,"Седалище и задна верига",90,"10–12","Движението идва от таза; не преразгъвай кръста.","Хип тръст","Качване на платформа","Повдигане за прасци","Птица-куче");
        } else if(p.getTrainingDays()>=4 && !home) {
            add(result,"Горна част — избутване и дърпане",strength?150:90,strength?"5–6":"8–12","Загрей с леки серии; движи се контролирано и остави 2 повторения в резерв.","Лежанка с дъмбели","Гребане с опора","Раменна преса с дъмбели");
            add(result,"Долна част — квадрицепс и корем",120,strength?"5–8":"10–12","Коленете следват стъпалата; не жертвай обхвата за по-голяма тежест.","Клек с дъмбел","Лег преса","Повдигане за прасци","Планк");
            add(result,"Горна част — гръб и ръце",90,"8–12","Прибирай лопатките; не използвай засилка.","Вертикален скрипец","Избутване на наклонена пейка","Сгъване с дъмбели","Разгъване за трицепс на скрипец");
            add(result,"Долна част — седалище и задно бедро",120,"8–12","Стегни корема и води движението от таза при стабилен гръб.","Румънска тяга с дъмбели","Хип тръст","Сгъване за задно бедро","Птица-куче");
        } else if(home && p.getTrainingDays()>=4) {
            add(result,"Горна част — избутване",90,"8–12","Използвай стабилна опора; стегни корема и спускай контролирано.","Лицеви опори от висока опора","Раменна преса с раница","Тясна лицева опора от стена");
            add(result,"Крака и седалище",90,"10–12","Коленете следват стъпалата; дръж таза стабилен и работи без болка.","Клек до стол","Обратен напад с опора","Глутеус мост","Повдигане за прасци");
            add(result,"Гръб и корем",75,"10–12","Без засилка; дръж кръста неутрален и прибирай лопатките.","Гребане с раница","Птица-куче","Страничен планк от колене");
            add(result,"Лека кондиция и мобилност",45,"8–10 бавни повторения","Запази леко темпо и безболезнен обхват. Добави спокойна разходка по желание.","Ротации на гръден отдел","Преходи 90/90 за таз","Мобилизация на глезен");
        } else {
            add(result,home?"Цяло тяло — основни движения":"Цяло тяло — клек и избутване",strength?120:75,strength?"6–8":"8–12","Работи плавно, в удобен обхват и с 2–3 повторения в резерв.",home?"Клек до стол":"Клек с дъмбел",home?"Лицеви опори от висока опора":"Лежанка с дъмбели",home?"Гребане с раница":"Гребане с опора","Планк");
            add(result,home?"Цяло тяло — таз и стабилност":"Задна верига и гръб",90,"8–12","Дръж гърба неутрален; контролирай спускането и дишай свободно.",home?"Глутеус мост":"Румънска тяга с дъмбели",home?"Обратен напад с опора":"Вертикален скрипец",home?"Птица-куче":"Качване на платформа","Страничен планк от колене");
            if(p.getTrainingDays()>2)add(result,home?"Кондиция и контрол на тялото":"Цяло тяло — едностранни движения",75,"10–12","Започни с по-слабата страна и запази бавно, равномерно темпо.",home?"Качване на ниско стъпало":"Обратен напад с дъмбели",home?"Раменна преса с раница":"Раменна преса с дъмбели",home?"Повдигане за прасци":"Хоризонтален скрипец","Коремно сгъване на постелка");
            if(p.getTrainingDays()>3)add(result,"Лека активност и мобилност",45,"8–10 бавни повторения","Денят е лек; не го превръщай в допълнителна тежка силова тренировка.","Ротации на гръден отдел","Преходи 90/90 за таз","Мобилизация на глезен");
        }
        return result.stream().filter(i->i.getDayNumber()<=Math.max(1,p.getTrainingDays())).toList();
    }

    private static void add(List<ProgramTemplateItem> items,String title,int rest,String reps,String instructions,String... names){
        int session=items.stream().mapToInt(ProgramTemplateItem::getDayNumber).max().orElse(0)+1;
        for(int i=0;i<names.length;i++){
            ProgramTemplateItem item=new ProgramTemplateItem();item.setDayNumber(session);item.setSessionTitle(title);item.setExerciseName(names[i]);item.setSetsCount(names.length==1?1:3);item.setRepetitions(names[i].contains("Планк")||names[i].contains("планк")?"20–30 секунди":reps);item.setRestSeconds(rest);item.setInstructions(instructions);item.setSortOrder(i+1);items.add(item);
        }
    }
    public static List<Session> templateSessions(List<ProgramTemplateItem> items){
        Map<Integer,List<ProgramTemplateItem>> groups=new TreeMap<>();items.forEach(i->groups.computeIfAbsent(i.getDayNumber(),n->new ArrayList<>()).add(i));
        return groups.entrySet().stream().map(e->new Session(e.getKey(),title(e.getValue().get(0).getSessionTitle()),schedule(e.getKey(),groups.size()),List.copyOf(e.getValue()))).toList();
    }
    public static List<Session> personalSessions(UserProgram p){
        Map<Integer,List<UserProgramItem>> groups=new TreeMap<>();p.getItems().forEach(i->groups.computeIfAbsent(i.getDayNumber(),n->new ArrayList<>()).add(i));
        return groups.entrySet().stream().map(e->new Session(e.getKey(),title(e.getValue().get(0).getSessionTitle()),schedule(e.getKey(),groups.size()),List.copyOf(e.getValue()))).toList();
    }
    private static String title(String title){return title==null||title.isBlank()?"Тренировка за цяло тяло":title;}
    private static String schedule(int index,int total){
        String[] days=total<=2?new String[]{"Понеделник","Четвъртък"}:total==3?new String[]{"Понеделник","Сряда","Петък"}:total==4?new String[]{"Понеделник","Вторник","Четвъртък","Събота"}:new String[]{"Понеделник","Вторник","Сряда","Петък","Събота"};
        return index>0&&index<=days.length?"Примерен график: "+days[index-1]:"Планирай според възстановяването си";
    }
}
