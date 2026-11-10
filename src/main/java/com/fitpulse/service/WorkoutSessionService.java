package com.fitpulse.service;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.entity.*;
import com.fitpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service @Transactional(readOnly=true)
public class WorkoutSessionService {
    private final WorkoutSessionRepository sessions;private final WorkoutSessionSetRepository draftSets;private final UserProgramRepository programs;
    private final WorkoutLogRepository logs;private final WorkoutSetRepository sets;private final UserService users;
    public WorkoutSessionService(WorkoutSessionRepository sessions,WorkoutSessionSetRepository draftSets,UserProgramRepository programs,WorkoutLogRepository logs,WorkoutSetRepository sets,UserService users){this.sessions=sessions;this.draftSets=draftSets;this.programs=programs;this.logs=logs;this.sets=sets;this.users=users;}
    public WorkoutSession owned(Long id){return sessions.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Тренировъчната сесия не е намерена."));}
    private WorkoutSession locked(Long id){return sessions.lockOwned(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Тренировъчната сесия не е намерена."));}
    private void draft(WorkoutSession session){if(!session.getStatus().equals("DRAFT"))throw new FitPulseException("Тази сесия вече е приключила.");}
    public List<WorkoutSession> history(){var user=users.getCurrentUser();Map<Long,WorkoutSession> result=new LinkedHashMap<>();sessions.findAllByUserAndStatusOrderByCreatedAtDesc(user,"DRAFT").forEach(s->result.put(s.getId(),s));sessions.findTop30ByUserOrderByCreatedAtDesc(user).forEach(s->result.putIfAbsent(s.getId(),s));return List.copyOf(result.values());}
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public Long start(Long programId,int day){
        var user=users.getCurrentUser();var program=programs.lockOwned(programId,user).orElseThrow(()->new FitPulseException("Личната програма не е намерена."));
        String key=user.getId()+":"+programId+":"+day;var existing=sessions.findByActiveKeyAndUser(key,user);if(existing.isPresent())return existing.get().getId();
        var items=program.getItems().stream().filter(i->i.getDayNumber()==day).sorted(Comparator.comparingInt(UserProgramItem::getSortOrder).thenComparing(UserProgramItem::getId)).toList();
        if(items.isEmpty()||items.stream().mapToInt(UserProgramItem::getSetsCount).sum()>400)throw new FitPulseException("Избери сесия с упражнения и не повече от 400 планирани серии.");
        var session=new WorkoutSession();session.setUser(user);session.setProgram(program);session.setDayNumber(day);session.setTitle(items.get(0).getSessionTitle().isBlank()?"Лична тренировка":items.get(0).getSessionTitle());session.setActiveKey(key);sessions.save(session);
        int order=0;for(var item:items){order++;for(int number=1;number<=item.getSetsCount();number++){
            var set=new WorkoutSessionSet();set.setSession(session);set.setExercise(item.getExercise());set.setExerciseName(item.getExerciseName());set.setExerciseOrder(order);set.setSetNumber(number);set.setPlannedRepetitions(item.getRepetitions());set.setRestSeconds(item.getRestSeconds());draftSets.save(set);
        }}return session.getId();
    }
    public record Previous(BigDecimal weight,int repetitions){}
    public record ExerciseBlock(int order,String name,Exercise exercise,int rest,String planned,List<WorkoutSessionSet> rows,Previous previous,List<Previous> previousRows){}
    public List<ExerciseBlock> blocks(Long id){
        var session=owned(id);var all=draftSets.findAllBySessionOrderByExerciseOrderAscSetNumberAsc(session);Map<Integer,List<WorkoutSessionSet>> groups=new LinkedHashMap<>();all.forEach(s->groups.computeIfAbsent(s.getExerciseOrder(),k->new ArrayList<>()).add(s));
        var history=new ArrayList<>(sets.findAllByWorkoutLog_UserOrderByWorkoutLog_PerformedAtAsc(users.getCurrentUser()));history.sort(Comparator.comparing((WorkoutSet s)->s.getWorkoutLog().getPerformedAt()).thenComparing(s->s.getWorkoutLog().getId()).thenComparingInt(WorkoutSet::getSetNumber).thenComparing(WorkoutSet::getId));List<ExerciseBlock> result=new ArrayList<>();
        for(var entry:groups.entrySet()){var first=entry.getValue().get(0);Previous previous=null;List<Previous> previousRows=List.of();
            var matches=history.stream().filter(old->(first.getExercise()!=null&&old.getExercise()!=null&&first.getExercise().getId().equals(old.getExercise().getId()))||(first.getExercise()==null&&old.getExercise()==null&&ExerciseLibraryService.normalize(first.getExerciseName()).equals(ExerciseLibraryService.normalize(old.getExerciseName())))).toList();
            if(!matches.isEmpty()){var last=matches.get(matches.size()-1);previous=new Previous(last.getWeight(),last.getRepetitions());previousRows=matches.stream().filter(old->old.getWorkoutLog().getId().equals(last.getWorkoutLog().getId())).map(old->new Previous(old.getWeight(),old.getRepetitions())).toList();}
            result.add(new ExerciseBlock(entry.getKey(),first.getExerciseName(),first.getExercise(),first.getRestSeconds(),first.getPlannedRepetitions(),List.copyOf(entry.getValue()),previous,previousRows));
        }return result;
    }
    @Transactional public void saveSet(Long id,Long setId,Integer repetitions,BigDecimal weight,boolean completed){
        var session=locked(id);draft(session);var set=draftSets.findByIdAndSession(setId,session).orElseThrow(()->new FitPulseException("Серията не е намерена."));
        if((repetitions!=null&&(repetitions<1||repetitions>1000))||(weight!=null&&(weight.signum()<0||weight.compareTo(new BigDecimal("99999.99"))>0||weight.stripTrailingZeros().scale()>2))||(completed&&(repetitions==null||weight==null)))throw new FitPulseException("За завършена серия въведи 1–1000 повторения и тежест от 0 до 99999.99 кг.");
        set.setRepetitions(repetitions);set.setWeight(weight);set.setCompleted(completed);session.setUpdatedAt(LocalDateTime.now());
    }
    @Transactional public Long finish(Long id){
        var session=locked(id);if(session.getStatus().equals("FINISHED")){if(session.getFinishedLog()==null)throw new FitPulseException("Сесията е завършена, но дневниковият запис вече е премахнат.");return session.getFinishedLog().getId();}draft(session);
        var completed=draftSets.findAllBySessionOrderByExerciseOrderAscSetNumberAsc(session).stream().filter(WorkoutSessionSet::isCompleted).toList();if(completed.isEmpty())throw new FitPulseException("Отбележи поне една реално изпълнена серия.");
        var log=new WorkoutLog();log.setUser(session.getUser());log.setTitle(session.getTitle());log.setPerformedAt(LocalDateTime.now());log.setNotes("Запис от тренировъчна сесия във FitPulse.");logs.save(log);
        for(var saved:completed){if(saved.getRepetitions()==null||saved.getWeight()==null)throw new FitPulseException("Попълни завършените серии.");var set=new WorkoutSet();set.setWorkoutLog(log);set.setExercise(saved.getExercise());set.setExerciseName(saved.getExerciseName());set.setSetNumber(saved.getSetNumber());set.setRepetitions(saved.getRepetitions());set.setWeight(saved.getWeight());sets.save(set);}
        session.setFinishedLog(log);session.setStatus("FINISHED");session.setActiveKey(null);session.setTimerDeadline(null);session.setTimerRemaining(0);session.setUpdatedAt(LocalDateTime.now());return log.getId();
    }
    @Transactional public void cancel(Long id){var session=locked(id);draft(session);session.setStatus("CANCELLED");session.setActiveKey(null);session.setTimerDeadline(null);session.setUpdatedAt(LocalDateTime.now());}
    public record Timer(long serverNow,Long deadline,int remaining){}
    public Timer timerState(WorkoutSession session){int remaining=session.getTimerDeadline()==null?session.getTimerRemaining():(int)Math.max(0,(session.getTimerDeadline()-System.currentTimeMillis()+999)/1000);return new Timer(System.currentTimeMillis(),session.getTimerDeadline(),remaining);}
    @Transactional public Timer timer(Long id,String action,int seconds){var session=locked(id);draft(session);if(seconds<5||seconds>900)throw new FitPulseException("Почивката трябва да е от 5 до 900 секунди.");var state=timerState(session);
        switch(action){case "start"->{int remaining=state.remaining()>0?state.remaining():seconds;session.setTimerRemaining(remaining);session.setTimerDeadline(System.currentTimeMillis()+remaining*1000L);}case "pause"->{session.setTimerRemaining(state.remaining());session.setTimerDeadline(null);}case "reset"->{session.setTimerRemaining(seconds);session.setTimerDeadline(null);}default->throw new FitPulseException("Невалидно действие за таймера.");}session.setUpdatedAt(LocalDateTime.now());return timerState(session);
    }
}
