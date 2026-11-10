package com.fitpulse.service;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.progress.BodyMeasurementForm;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.model.entity.*;
import com.fitpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service @Transactional(readOnly=true)
public class ProgressDashboardService {
    public static final Map<String,String> TYPES=types();
    private static Map<String,String> types(){Map<String,String> result=new LinkedHashMap<>();result.put("waist","Талия");result.put("chest","Гърди");result.put("hips","Ханш");result.put("biceps","Бицепс");result.put("thigh","Бедро");return Collections.unmodifiableMap(result);}
    private final ProgressService progress;private final UserService users;private final BodyMeasurementRepository measurements;
    private final WorkoutSetRepository sets;private final WorkoutLogRepository logs;private final WorkoutEventRepository events;
    public ProgressDashboardService(ProgressService progress,UserService users,BodyMeasurementRepository measurements,WorkoutSetRepository sets,WorkoutLogRepository logs,WorkoutEventRepository events){this.progress=progress;this.users=users;this.measurements=measurements;this.sets=sets;this.logs=logs;this.events=events;}
    public record Point(LocalDate date,BigDecimal value){}
    public record Lift(String key,String name,BigDecimal last,BigDecimal best,List<Point> points){}
    public record Dashboard(BigDecimal current,LocalDate lastDate,BigDecimal change,long completed,int records,List<Point> weights,Map<String,List<Point>> body,List<Lift> lifts){}
    public record Measurement(Long id,String type,String label,LocalDate date,BigDecimal value){}
    public List<Measurement> history(){return measurements.findAllByUserOrderByRecordedAtDesc(users.getCurrentUser()).stream().map(m->new Measurement(m.getId(),m.getType(),TYPES.get(m.getType()),m.getRecordedAt(),m.getValue())).toList();}
    public Dashboard data(String period){
        LocalDate from=switch(period){case "30"->LocalDate.now().minusDays(29);case "90"->LocalDate.now().minusDays(89);case "365"->LocalDate.now().minusYears(1);case "all"->LocalDate.MIN;default->throw new FitPulseException("Избери валиден период.");};
        User user=users.getCurrentUser();List<ProgressResponse> weights=progress.getCurrentUserProgress();
        BigDecimal current=weights.isEmpty()?null:weights.get(0).weight();LocalDate lastDate=weights.isEmpty()?null:weights.get(0).recordedAt();
        BigDecimal change=weights.isEmpty()?null:current.subtract(weights.get(weights.size()-1).weight());
        List<Point> points=weights.stream().filter(w->!w.recordedAt().isBefore(from)).sorted(Comparator.comparing(ProgressResponse::recordedAt)).map(w->new Point(w.recordedAt(),w.weight())).toList();
        List<Measurement> measurements=history();Map<String,List<Point>> body=new LinkedHashMap<>();for(String type:TYPES.keySet())body.put(type,measurements.stream().filter(m->m.type().equals(type)&&!m.date().isBefore(from)).sorted(Comparator.comparing(Measurement::date)).map(m->new Point(m.date(),m.value())).toList());
        Set<String> completed=new HashSet<>();for(var log:logs.findAllByUserOrderByPerformedAtDesc(user))if(!log.getPerformedAt().toLocalDate().isBefore(from))completed.add(log.getEvent()==null?"log:"+log.getId():"event:"+log.getEvent().getId());
        for(var event:events.findAllByUserOrderByStartsAt(user))if(event.getStatus()==com.fitpulse.model.enums.WorkoutStatus.COMPLETED&&!event.getStartsAt().toLocalDate().isBefore(from)&&!event.getStartsAt().toLocalDate().isAfter(LocalDate.now()))completed.add("event:"+event.getId());
        Map<String,List<WorkoutSet>> grouped=new LinkedHashMap<>();for(var set:sets.findAllByWorkoutLog_UserOrderByWorkoutLog_PerformedAtAsc(user)){String key=set.getExercise()==null?"legacy:"+ExerciseLibraryService.normalize(set.getExerciseName()):set.getExercise().getCode();grouped.computeIfAbsent(key,k->new ArrayList<>()).add(set);}
        List<Lift> lifts=new ArrayList<>();for(var entry:grouped.entrySet()){
            Map<LocalDate,BigDecimal> daily=new TreeMap<>();BigDecimal best=BigDecimal.ZERO;String name=entry.getValue().get(0).getExercise()==null?entry.getValue().get(0).getExerciseName():entry.getValue().get(0).getExercise().getName();
            for(var set:entry.getValue()){best=best.max(set.getWeight());LocalDate date=set.getWorkoutLog().getPerformedAt().toLocalDate();if(!date.isBefore(from))daily.merge(date,set.getWeight(),BigDecimal::max);}
            var series=daily.entrySet().stream().map(e->new Point(e.getKey(),e.getValue())).toList();lifts.add(new Lift(entry.getKey(),name,series.isEmpty()?null:series.get(series.size()-1).value(),best,series));
        }
        lifts.sort(Comparator.comparing(Lift::name));return new Dashboard(current,lastDate,change,completed.size(),lifts.size(),points,body,lifts);
    }
    public BodyMeasurementForm form(Long id){var entry=measurements.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Измерването не е намерено."));var form=new BodyMeasurementForm();form.setId(entry.getId());form.setType(entry.getType());form.setValue(entry.getValue());form.setRecordedAt(entry.getRecordedAt());return form;}
    @Transactional public void save(BodyMeasurementForm form){
        if(!TYPES.containsKey(form.getType()))throw new FitPulseException("Избери валидна обиколка.");
        BigDecimal minimum=new BigDecimal(form.getType().equals("biceps")?"10":form.getType().equals("thigh")?"15":"30");BigDecimal maximum=new BigDecimal(form.getType().equals("biceps")?"100":form.getType().equals("thigh")?"180":"300");
        if(form.getValue()==null||form.getValue().compareTo(minimum)<0||form.getValue().compareTo(maximum)>0||form.getRecordedAt()==null||form.getRecordedAt().isAfter(LocalDate.now())||form.getValue().stripTrailingZeros().scale()>2)throw new FitPulseException("Провери датата и обиколката. Допустимо за този тип: "+minimum+"–"+maximum+" см.");
        var user=users.getCurrentUser();var duplicate=measurements.findByUserAndTypeAndRecordedAt(user,form.getType(),form.getRecordedAt());if(duplicate.isPresent()&&!duplicate.get().getId().equals(form.getId()))throw new FitPulseException("Вече има такава обиколка за тази дата. Редактирай съществуващия запис.");
        var entry=form.getId()==null?new BodyMeasurement():measurements.findByIdAndUser(form.getId(),user).orElseThrow(()->new FitPulseException("Измерването не е намерено."));entry.setUser(user);entry.setType(form.getType());entry.setValue(form.getValue());entry.setRecordedAt(form.getRecordedAt());measurements.save(entry);
    }
    @Transactional public void delete(Long id){measurements.delete(measurements.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Измерването не е намерено.")));}
}
