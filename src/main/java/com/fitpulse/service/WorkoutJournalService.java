package com.fitpulse.service;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.*;
import com.fitpulse.model.entity.*;
import com.fitpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional(readOnly=true)
public class WorkoutJournalService { @org.springframework.beans.factory.annotation.Autowired private ExerciseLibraryService exerciseLibrary;
    private final WorkoutLogRepository logs; private final WorkoutSetRepository sets; private final WorkoutEventRepository events; private final UserService users;
    public WorkoutJournalService(WorkoutLogRepository l,WorkoutSetRepository s,WorkoutEventRepository e,UserService u) { logs=l;sets=s;events=e;users=u; }
    public List<WorkoutLog> history() { return logs.findAllByUserOrderByPerformedAtDesc(users.getCurrentUser()); }
    public WorkoutLog owned(Long id) { return logs.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Тренировката не е намерена.")); }
    public List<WorkoutSet> sets(Long id) { return sets.findAllByWorkoutLogOrderById(owned(id)); }
    @Transactional public Long save(Long id,WorkoutLogForm f) {
        WorkoutLog log=id==null?new WorkoutLog():owned(id); log.setUser(users.getCurrentUser()); log.setTitle(f.title.trim()); log.setPerformedAt(f.performedAt); log.setNotes(f.notes);
        log.setEvent(f.eventId==null?null:events.findByIdAndUser(f.eventId,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Събитието не е намерено.")));
        return logs.save(log).getId();
    }
    @Transactional public void saveSet(Long logId,Long setId,WorkoutSetForm f) {
        WorkoutLog log=owned(logId); WorkoutSet set=setId==null?new WorkoutSet():sets.findByIdAndWorkoutLog(setId,log).orElseThrow(()->new FitPulseException("Серията не е намерена."));
        var exercise=exerciseLibrary==null?null:(f.exerciseId==null?exerciseLibrary.resolve(f.exerciseName):exerciseLibrary.get(f.exerciseId));set.setExercise(exercise);set.setWorkoutLog(log); set.setExerciseName(exercise==null?f.exerciseName.trim():exercise.getName()); set.setSetNumber(f.setNumber); set.setRepetitions(f.repetitions); set.setWeight(f.weight); sets.save(set);
    }
    @Transactional public void deleteSet(Long logId,Long setId) { sets.delete(sets.findByIdAndWorkoutLog(setId,owned(logId)).orElseThrow(()->new FitPulseException("Серията не е намерена."))); }
    @Transactional public void delete(Long id) { WorkoutLog log=owned(id); sets.deleteAll(sets.findAllByWorkoutLogOrderById(log)); logs.delete(log); }
}
