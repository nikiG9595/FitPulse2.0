package com.fitpulse.service;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProgramItemForm;
import com.fitpulse.model.dto.WorkoutEventForm;
import com.fitpulse.model.entity.*;
import com.fitpulse.model.enums.*;
import com.fitpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WellnessService {
    @org.springframework.beans.factory.annotation.Autowired private ExerciseLibraryService exerciseLibrary;
    @org.springframework.beans.factory.annotation.Autowired private UserRepository accountLocks;
    @org.springframework.beans.factory.annotation.Autowired private ShoppingCheckRepository shoppingChecks;
    @org.springframework.beans.factory.annotation.Autowired private WorkoutSessionRepository workoutSessions;
    @org.springframework.beans.factory.annotation.Autowired private WorkoutSessionSetRepository workoutSessionSets;
    @org.springframework.beans.factory.annotation.Autowired private PersonalNutritionService nutrition;
    @org.springframework.beans.factory.annotation.Autowired private WeeklyMenuReader menuReader;

    private final WorkoutEventRepository events; private final GymRepository gyms;
    private final ProgramTemplateRepository templates; private final UserProgramRepository programs;
    private final UserProgramItemRepository items; private final MealPlanTemplateRepository mealTemplates;
    private final UserMealPlanRepository mealPlans; private final UserService users;

    public WellnessService(WorkoutEventRepository events, GymRepository gyms, ProgramTemplateRepository templates,
                           UserProgramRepository programs, UserProgramItemRepository items,
                           MealPlanTemplateRepository mealTemplates, UserMealPlanRepository mealPlans, UserService users) {
        this.events=events; this.gyms=gyms; this.templates=templates; this.programs=programs; this.items=items;
        this.mealTemplates=mealTemplates; this.mealPlans=mealPlans; this.users=users;
    }

    public List<WorkoutEvent> calendar(){return events.findAllByUserOrderByStartsAt(users.getCurrentUser());}
    @Transactional public void saveEvent(Long id,WorkoutEventForm f){User u=users.getCurrentUser();WorkoutEvent e=id==null?new WorkoutEvent():events.findByIdAndUser(id,u).orElseThrow(()->new FitPulseException("Тренировката не е намерена."));if(id==null)e.setReminderMinutes(0);
        e.setProgram(f.getProgramId()==null?null:ownedProgram(f.getProgramId()));
        e.setMealPlan(f.getMealPlanId()==null?null:ownedMeal(f.getMealPlanId()));
        e.setUser(u);e.setTitle(f.getTitle());e.setNotes(f.getNotes());e.setStartsAt(f.getStartsAt());e.setDurationMinutes(f.getDurationMinutes());e.setGym(f.getGymId()==null?null:gyms.findById(f.getGymId()).orElseThrow(()->new FitPulseException("Фитнесът не е намерен.")));events.save(e);}
    public WorkoutEventForm eventForm(Long id){WorkoutEvent e=events.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Тренировката не е намерена."));WorkoutEventForm f=new WorkoutEventForm();f.setProgramId(e.getProgram()==null?null:e.getProgram().getId());f.setMealPlanId(e.getMealPlan()==null?null:e.getMealPlan().getId());f.setReminderMinutes(e.getReminderMinutes());f.setTitle(e.getTitle());f.setNotes(e.getNotes());f.setStartsAt(e.getStartsAt());f.setDurationMinutes(e.getDurationMinutes());f.setGymId(e.getGym()==null?null:e.getGym().getId());return f;}
    @Transactional public void eventStatus(Long id,WorkoutStatus s){events.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Тренировката не е намерена.")).setStatus(s);}
    @Transactional public void deleteEvent(Long id){events.delete(events.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Тренировката не е намерена.")));}

    public List<ProgramTemplate> library(){return templates.findAllByActiveTrueOrderByName();}
    @Transactional(readOnly=true) public ProgramTemplate publishedProgram(Long id){ProgramTemplate p=templates.findById(id).filter(ProgramTemplate::isActive).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));p.getItems().size();return p;}
    public List<MealPlanTemplate> mealLibrary(){return mealTemplates.findAllByActiveTrueOrderByName();}
    public List<UserProgram> myPrograms(){return programs.findAllByUserOrderByCreatedAtDesc(users.getCurrentUser()).stream().filter(UserProgram::isListed).toList();}
    public List<UserMealPlan> myMeals(){return mealPlans.findAllByUserOrderByCreatedAtDesc(users.getCurrentUser()).stream().filter(UserMealPlan::isListed).toList();}

    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public Long copyProgram(Long templateId){
        User user=accountLocks.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();
        ProgramTemplate template=templates.findById(templateId).filter(ProgramTemplate::isActive).orElseThrow(()->new FitPulseException("Програмата не е достъпна."));
        var existing=programs.findAllByUserOrderByCreatedAtDesc(user).stream().filter(p->p.getSourceTemplate()!=null&&p.getSourceTemplate().getId().equals(templateId)).sorted(java.util.Comparator.comparing(UserProgram::getId)).toList();
        if(existing.stream().anyMatch(UserProgram::isListed))throw new FitPulseException("Вече имате тази тренировъчна програма в профила си.");
        
        return copyTemplate(user,template).getId();
    }
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public void removeProgram(Long id){
        User user=accountLocks.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();
        UserProgram program=programs.lockOwned(id,user).orElseThrow(()->new FitPulseException("Личната програма не е намерена."));
        for(var session:workoutSessions.lockForProgram(program,user)){
            if(session.getFinishedLog()!=null||"FINISHED".equals(session.getStatus())){session.setProgram(null);session.setActiveKey(null);}
            else{workoutSessionSets.deleteAll(workoutSessionSets.findAllBySessionOrderByExerciseOrderAscSetNumberAsc(session));workoutSessions.delete(session);}
        }
        events.lockForProgram(program,user).forEach(event->event.setProgram(null));
        programs.delete(program); // Only personal items cascade; shared exercises and finished logs remain.
        programs.flush();
    }
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public void removeMeal(Long id){
        User user=accountLocks.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();
        UserMealPlan plan=mealPlans.findOwnedForUpdate(id,user).orElseThrow(()->new FitPulseException("Личният режим не е намерен."));
        events.lockForMeal(plan,user).forEach(event->event.setMealPlan(null));
        shoppingChecks.deleteAll(shoppingChecks.findAllByPlanOrderByIdDesc(plan));
        mealPlans.delete(plan);mealPlans.flush(); // Personal meals and quantities live inside this row's JSON.
    }
    private UserProgram copyTemplate(User u,ProgramTemplate t){UserProgram p=new UserProgram();p.setUser(u);p.setSourceTemplate(t);p.setName(t.getDisplayName());programs.save(p);for(ProgramTemplateItem x:programOutline(t)){UserProgramItem item=addItem(p,x.getDayNumber(),x.getExerciseName(),x.getSetsCount(),x.getRepetitions(),x.getSortOrder());if(x.getExercise()!=null)item.setExercise(x.getExercise());item.setSessionTitle(x.getSessionTitle());item.setRestSeconds(x.getRestSeconds());item.setInstructions(x.getInstructions());}return p;}
    public List<ProgramTemplateItem> programOutline(ProgramTemplate template){var outline=TrainingSessions.outline(template);if(exerciseLibrary!=null)for(var item:outline)if(item.getExercise()==null)item.setExercise(exerciseLibrary.resolve(item.getExerciseName()));return outline;}
    @Transactional(readOnly=true) public UserProgram ownedProgram(Long id){UserProgram p=programs.findByIdAndUser(id,users.getCurrentUser()).filter(UserProgram::isListed).orElseThrow(()->new FitPulseException("Програмата не е намерена."));p.getItems().size();return p;}
    @Transactional public void addProgramItem(Long id,ProgramItemForm f){UserProgram p=ownedProgram(id);UserProgramItem added=addItem(p,f.getDayNumber(),f.getExerciseName(),f.getSetsCount(),f.getRepetitions(),p.getItems().size()+1);added.setSessionTitle(sessionFocus(p,f.getDayNumber(),f.getSessionTitle()));added.setRestSeconds(f.getRestSeconds());added.setInstructions(f.getInstructions());}
    @Transactional public void updateProgramItem(Long programId,Long itemId,ProgramItemForm f){UserProgram p=ownedProgram(programId);UserProgramItem x=p.getItems().stream().filter(i->i.getId().equals(itemId)).findFirst().orElseThrow(()->new FitPulseException("Упражнението не е намерено."));x.setDayNumber(f.getDayNumber());x.setExerciseName(f.getExerciseName());if(exerciseLibrary!=null)x.setExercise(exerciseLibrary.resolve(f.getExerciseName()));x.setSetsCount(f.getSetsCount());x.setRepetitions(f.getRepetitions());x.setSessionTitle(sessionFocus(p,f.getDayNumber(),f.getSessionTitle()));x.setRestSeconds(f.getRestSeconds());x.setInstructions(f.getInstructions());items.save(x);}
    @Transactional public void deleteProgramItem(Long programId,Long itemId){UserProgram p=ownedProgram(programId);UserProgramItem x=p.getItems().stream().filter(i->i.getId().equals(itemId)).findFirst().orElseThrow(()->new FitPulseException("Упражнението не е намерено."));items.delete(x);}
    @Transactional public void addDayToCalendar(Long programId,int day,LocalDateTime startsAt){UserProgram p=ownedProgram(programId);WorkoutEvent e=new WorkoutEvent();if(p.getItems().stream().noneMatch(i->i.getDayNumber()==day))throw new FitPulseException("Няма упражнения за този ден.");e.setProgram(p);e.setUser(users.getCurrentUser());e.setTitle(p.getDisplayName()+" — "+TrainingSessions.personalSessions(p).stream().filter(session->session.number()==day).findFirst().orElseThrow().title());e.setNotes(p.getItems().stream().filter(i->i.getDayNumber()==day).map(UserProgramItem::getExerciseName).reduce((a,b)->a+", "+b).orElse("Лична тренировка"));e.setStartsAt(startsAt);e.setDurationMinutes(60);events.save(e);}

    public UserMealPlan savedMealCopy(Long id){return mealPlans.findAllByUserOrderByCreatedAtDesc(users.getCurrentUser()).stream().filter(UserMealPlan::isListed).filter(p->p.getSourceTemplate()!=null&&p.getSourceTemplate().getId().equals(id)).sorted(java.util.Comparator.comparing(UserMealPlan::isListed).reversed().thenComparing(UserMealPlan::getId)).findFirst().orElse(null);}
    public com.fitpulse.service.PersonalNutritionService.CalorieEstimate mealEstimate(Long id,com.fitpulse.model.dto.MealPersonalizationForm form){return nutrition.estimate(form,menuReader.sourceJson(mealTemplate(id)));}
    public long mealCalories(Long id){return nutrition.originalCalories(menuReader.sourceJson(mealTemplate(id)));}
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public Long copyMeal(Long id,java.math.BigDecimal target){
        User user=accountLocks.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();
        MealPlanTemplate t=mealTemplates.findById(id).filter(MealPlanTemplate::isActive).orElseThrow(()->new FitPulseException("Режимът не е достъпен."));
        var existing=mealPlans.findAllByUserOrderByCreatedAtDesc(user).stream().filter(p->p.getSourceTemplate()!=null&&p.getSourceTemplate().getId().equals(id)).sorted(java.util.Comparator.comparing(UserMealPlan::getId)).toList();
        if(existing.stream().anyMatch(UserMealPlan::isListed))throw new FitPulseException("Вече имате този хранителен режим в профила си.");
          
          UserMealPlan p=new UserMealPlan();p.setUser(user);p.setSourceTemplate(t);p.setGoal(t.getGoal());p.setName(t.getDisplayName());
          p.setWeeklyMenu(nutrition.adaptedJson(menuReader.sourceJson(t),target));p.setMeals(nutrition.menuText(p.getWeeklyMenu()));p.setExampleFoods(t.getExampleFoods());return mealPlans.save(p).getId();
    }
    @Transactional public void updateMeal(Long id,String name,String meals,String exampleFoods){if(name==null||name.isBlank()||name.length()>150||meals==null||meals.isBlank()||meals.length()>100000||exampleFoods==null||exampleFoods.length()>3000)throw new FitPulseException("Проверете името и съдържанието на режима.");UserMealPlan p=mealPlans.findOwnedForUpdate(id,users.getCurrentUser()).filter(UserMealPlan::isListed).orElseThrow(()->new FitPulseException("Режимът не е намерен."));if(!java.util.Objects.equals(p.getMeals(),meals))p.setWeeklyMenu(null);p.setName(name);p.setMeals(meals);p.setExampleFoods(exampleFoods);}

    public List<ProgramTemplate> allTemplates(){return templates.findAll();}
    public ProgramTemplate template(Long id){return templates.findById(id).orElseThrow(()->new FitPulseException("Програмата не е намерена."));}
    @Transactional public ProgramTemplate saveTemplate(Long id,String name,FitnessGoal goal,ExperienceLevel experience,int days,TrainingLocation location,String description){ProgramTemplate p=id==null?new ProgramTemplate():template(id);p.setName(name);p.setGoal(goal);p.setExperience(experience);p.setTrainingDays(days);p.setLocation(location);p.setDescription(description);return templates.save(p);}
    @Transactional public void templateActive(Long id,boolean active){template(id).setActive(active);}
    @Transactional public void deleteTemplate(Long id){templates.delete(template(id));}

    public List<MealPlanTemplate> allMealTemplates(){return mealTemplates.findAll();}
    public MealPlanTemplate mealTemplate(Long id){return mealTemplates.findById(id).orElseThrow(()->new FitPulseException("Режимът не е намерен."));}
    @Transactional public MealPlanTemplate saveMealTemplate(Long id,String name,FitnessGoal goal,String description,String meals,String exampleFoods,String disclaimer){MealPlanTemplate p=id==null?new MealPlanTemplate():mealTemplate(id);if(!java.util.Objects.equals(p.getMeals(),meals))p.setWeeklyMenu(null);p.setName(name);p.setGoal(goal);p.setDescription(description);p.setMeals(meals);p.setExampleFoods(exampleFoods);p.setDisclaimer(disclaimer);return mealTemplates.save(p);}
    @Transactional public void mealTemplateActive(Long id,boolean active){mealTemplate(id).setActive(active);}
    @Transactional public void deleteMealTemplate(Long id){mealTemplates.delete(mealTemplate(id));}

    public WorkoutEvent ownedEvent(Long id){return events.findByIdAndUser(id,users.getCurrentUser()).orElseThrow(()->new FitPulseException("Тренировката не е намерена."));}
    public UserMealPlan ownedMeal(Long id){return mealPlans.findByIdAndUser(id,users.getCurrentUser()).filter(UserMealPlan::isListed).orElseThrow(()->new FitPulseException("Режимът не е намерен."));}
    public List<WorkoutEvent> upcoming(){return calendar().stream().filter(e->e.getStatus()==WorkoutStatus.PLANNED && !e.getStartsAt().isBefore(LocalDateTime.now())).limit(5).toList();}
    public List<WorkoutEvent> reminders(){return java.util.List.of();}
    @Transactional public void dismissReminder(Long id){ownedEvent(id).setReminderDismissed(true);}
    @Transactional public void programSettings(Long id,String name,boolean active){if(name==null || name.isBlank() || name.length()>150)throw new FitPulseException("Въведете име до 150 знака.");UserProgram p=ownedProgram(id);p.setName(name.trim());p.setActive(active);}
    public long eventCount(){return events.count();} public long programCount(){return programs.count();}
    private String sessionFocus(UserProgram program,int number,String requested){
        String title=requested==null||requested.isBlank()?program.getItems().stream().filter(i->i.getDayNumber()==number && !i.getSessionTitle().isBlank()).map(UserProgramItem::getSessionTitle).findFirst().orElse("Тренировка за цяло тяло"):requested.strip();
        program.getItems().stream().filter(i->i.getDayNumber()==number).forEach(i->i.setSessionTitle(title));return title;
    }
    private UserProgramItem addItem(UserProgram p,int day,String name,int sets,String reps,int order){UserProgramItem x=new UserProgramItem();x.setUserProgram(p);x.setDayNumber(day);x.setExerciseName(name);if(exerciseLibrary!=null)x.setExercise(exerciseLibrary.resolve(name));x.setSetsCount(sets);x.setRepetitions(reps);x.setSortOrder(order);items.save(x);p.getItems().add(x);return x;}
}
