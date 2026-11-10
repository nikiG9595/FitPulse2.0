from pathlib import Path
root=Path('src/main')
def write(p,s):
 p=root/p; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(s,encoding='utf-8')
def edit(p,a,b):
 p=root/p;s=p.read_text(encoding='utf-8');assert a in s,(p,a);p.write_text(s.replace(a,b),encoding='utf-8')
def page(title,body):
 return '<!doctype html><html lang="bg" xmlns:th="http://www.thymeleaf.org"><head th:replace="~{fragments/head :: head(\''+title+'\')}"></head><body class="page-bg"><div th:replace="~{fragments/nav :: nav}"></div><main class="container page">'+body+'</main></body></html>'
write(Path('java/com/fitpulse/model/entity/Trainer.java'),'''package com.fitpulse.model.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="trainers")
public class Trainer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank @Size(max=150) @Column(nullable=false,length=150) private String name;
 @NotBlank @Size(max=2000) @Column(nullable=false,length=2000) private String bio;
 @NotBlank @Size(max=1000) @Column(nullable=false,length=1000) private String specializations;
 private boolean active=true;
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getBio(){return bio;} public void setBio(String v){bio=v;}
 public String getSpecializations(){return specializations;} public void setSpecializations(String v){specializations=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}''')
write(Path('java/com/fitpulse/repository/TrainerRepository.java'),'''package com.fitpulse.repository;
import com.fitpulse.model.entity.Trainer; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface TrainerRepository extends JpaRepository<Trainer,Long>{List<Trainer> findAllByActiveTrueOrderByName();}''')
for entity in ['ProgramTemplate','MealPlanTemplate']:
 edit(Path('java/com/fitpulse/model/entity/'+entity+'.java'),'public Long getId()', '@ManyToOne @JoinColumn(name="trainer_id") private Trainer trainer; public Trainer getTrainer(){return trainer;} public void setTrainer(Trainer v){trainer=v;} public Long getId()')
write(Path('java/com/fitpulse/controller/TrainerController.java'),'''package com.fitpulse.controller;
import com.fitpulse.model.entity.*; import com.fitpulse.repository.*; import com.fitpulse.exception.FitPulseException;
import jakarta.validation.Valid; import org.springframework.stereotype.Controller; import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; import org.springframework.web.bind.annotation.*; import org.springframework.transaction.annotation.Transactional;
@Controller public class TrainerController {
 private final TrainerRepository trainers; private final ProgramTemplateRepository programs; private final MealPlanTemplateRepository meals;
 public TrainerController(TrainerRepository t,ProgramTemplateRepository p,MealPlanTemplateRepository m){trainers=t;programs=p;meals=m;}
 private Trainer find(Long id){return trainers.findById(id).orElseThrow(()->new FitPulseException("Треньорът не е намерен."));}
 @GetMapping("/trainers") public String list(Model m){m.addAttribute("trainers",trainers.findAllByActiveTrueOrderByName());return "trainers/list";}
 @GetMapping("/trainers/{id}") @Transactional(readOnly=true) public String profile(@PathVariable Long id,Model m){Trainer t=find(id);if(!t.isActive())throw new FitPulseException("Треньорът не е достъпен.");m.addAttribute("trainer",t);m.addAttribute("programs",programs.findAllByActiveTrueOrderByName().stream().filter(p->p.getTrainer()!=null&&p.getTrainer().getId().equals(id)).toList());m.addAttribute("meals",meals.findAllByActiveTrueOrderByName().stream().filter(p->p.getTrainer()!=null&&p.getTrainer().getId().equals(id)).toList());return "trainers/profile";}
 @GetMapping("/admin/trainers") public String admin(Model m){m.addAttribute("trainers",trainers.findAll());return "admin/trainers";}
 @GetMapping("/admin/trainers/create") public String create(Model m){m.addAttribute("trainer",new Trainer());return "admin/trainer-form";}
 @GetMapping("/admin/trainers/{id}/edit") public String edit(@PathVariable Long id,Model m){m.addAttribute("trainer",find(id));return "admin/trainer-form";}
 @PostMapping("/admin/trainers/save") @Transactional public String save(@RequestParam(required=false)Long id,@Valid @ModelAttribute("trainer") Trainer form,BindingResult errors){if(errors.hasErrors())return "admin/trainer-form";Trainer t=id==null?new Trainer():find(id);t.setName(form.getName());t.setBio(form.getBio());t.setSpecializations(form.getSpecializations());trainers.save(t);return "redirect:/admin/trainers";}
 @PostMapping("/admin/trainers/{id}/active") @Transactional public String active(@PathVariable Long id,@RequestParam boolean active){find(id).setActive(active);return "redirect:/admin/trainers";}
 @PostMapping("/admin/trainers/{id}/delete") @Transactional public String delete(@PathVariable Long id){Trainer t=find(id);programs.findAll().stream().filter(p->p.getTrainer()!=null&&p.getTrainer().getId().equals(id)).forEach(p->p.setTrainer(null));meals.findAll().stream().filter(p->p.getTrainer()!=null&&p.getTrainer().getId().equals(id)).forEach(p->p.setTrainer(null));programs.flush();meals.flush();trainers.delete(t);return "redirect:/admin/trainers";}
}''')
write(Path('resources/templates/trainers/list.html'),page('Треньори','''<h1>Треньори</h1><div class="row g-4"><div class="col-md-6 col-lg-4" th:each="t:${trainers}"><article class="fit-card h-100"><h2 th:text="${t.name}"></h2><p th:text="${t.specializations}"></p><a class="btn btn-pulse" th:href="@{/trainers/{id}(id=${t.id})}">Виж профила</a></article></div></div>'''))
write(Path('resources/templates/trainers/profile.html'),page('Профил на треньор','''<article class="fit-card"><h1 th:text="${trainer.name}"></h1><p th:text="${trainer.bio}"></p><h2>Специализации</h2><p th:text="${trainer.specializations}"></p></article><h2 class="mt-4">Тренировъчни програми</h2><div class="fit-card mb-3" th:each="p:${programs}"><h3 th:text="${p.name}"></h3><p th:text="${p.description}"></p><a th:href="@{/programs}">Отвори програмите</a></div><h2>Хранителни режими</h2><div class="fit-card mb-3" th:each="p:${meals}"><h3 th:text="${p.name}"></h3><p th:text="${p.description}"></p><p th:text="${p.disclaimer}"></p><a th:href="@{/meal-plans}">Отвори режимите</a></div>'''))
write(Path('resources/templates/admin/trainers.html'),page('Управление на треньори','''<h1>Управление на треньори</h1><a class="btn btn-pulse mb-3" th:href="@{/admin/trainers/create}">Добави треньор</a><article class="fit-card mb-3" th:each="t:${trainers}"><h2 th:text="${t.name}"></h2><p th:text="${t.active?'Активен':'Неактивен'}"></p><a th:href="@{/admin/trainers/{id}/edit(id=${t.id})}">Редактирай</a><form method="post" th:action="@{/admin/trainers/{id}/active(id=${t.id})}"><input type="hidden" name="active" th:value="${!t.active}"><button class="btn btn-outline-light my-2" th:text="${t.active?'Деактивирай':'Активирай'}"></button></form><form method="post" th:action="@{/admin/trainers/{id}/delete(id=${t.id})}"><button class="btn btn-outline-danger">Премахни</button></form></article>'''))
write(Path('resources/templates/admin/trainer-form.html'),page('Редактор на треньор','''<div class="fit-card"><h1>Данни на треньора</h1><form method="post" th:action="@{/admin/trainers/save}" th:object="${trainer}"><input type="hidden" name="id" th:value="*{id}"><label>Име</label><input class="form-control" th:field="*{name}" maxlength="150" required><label>Описание</label><textarea class="form-control" th:field="*{bio}" maxlength="2000" required></textarea><label>Специализации</label><textarea class="form-control" th:field="*{specializations}" maxlength="1000" required></textarea><div class="alert alert-danger" th:if="${#fields.hasAnyErrors()}" th:each="e:${#fields.allErrors()}" th:text="${e}"></div><button class="btn btn-pulse mt-3">Запази</button></form></div>'''))
# Trainer selection reuses existing template editor and service.
p=Path('java/com/fitpulse/controller/AdminPanelController.java')
edit(p,'private void prepareGym','''@org.springframework.beans.factory.annotation.Autowired private com.fitpulse.repository.TrainerRepository trainers;
 @ModelAttribute("trainers") public java.util.List<Trainer> trainerOptions(){return trainers.findAll();}
 private void prepareGym''')
edit(p,'@RequestParam String description){wellness.saveTemplate(id,name,goal,experience,trainingDays,location,description);','@RequestParam String description,@RequestParam(required=false) Long trainerId){wellness.saveTemplate(id,name,goal,experience,trainingDays,location,description);assignTrainer(id,trainerId,name,false);')
edit(p,'@RequestParam String disclaimer){wellness.saveMealTemplate(id,name,goal,description,meals,exampleFoods,disclaimer);','@RequestParam String disclaimer,@RequestParam(required=false) Long trainerId){var saved=wellness.saveMealTemplate(id,name,goal,description,meals,exampleFoods,disclaimer);wellness.assignMealTrainer(saved.getId(),trainerId);')
# saveTemplate returns entity, so use returned ID rather than lookup by name
edit(p,'wellness.saveTemplate(id,name,goal,experience,trainingDays,location,description);assignTrainer(id,trainerId,name,false);','var saved=wellness.saveTemplate(id,name,goal,experience,trainingDays,location,description);wellness.assignProgramTrainer(saved.getId(),trainerId);')
p=Path('java/com/fitpulse/service/WellnessService.java')
edit(p,'public long eventCount()', '''@org.springframework.beans.factory.annotation.Autowired private TrainerRepository trainers;
    private Trainer trainer(Long id){return id==null?null:trainers.findById(id).orElseThrow(()->new FitPulseException("Треньорът не е намерен."));}
    @Transactional public void assignProgramTrainer(Long id,Long trainerId){template(id).setTrainer(trainer(trainerId));}
    @Transactional public void assignMealTrainer(Long id,Long trainerId){mealTemplate(id).setTrainer(trainer(trainerId));}
    public long eventCount()''')
# Eager ManyToOne trainer is usable with open-in-view disabled.
for f in ['program-form','meal-plan-form']:
 edit(Path('resources/templates/admin/'+f+'.html'),'<label>Име</label>','''<label>Треньор</label><select class="form-select" name="trainerId"><option value="">Без треньор</option><option th:each="t:${trainers}" th:value="${t.id}" th:text="${t.name}" th:selected="${template.trainer!=null and template.trainer.id==t.id}"></option></select><label>Име</label>''')
edit(Path('java/com/fitpulse/config/SecurityConfig.java'),'"/css/**",','"/css/**", "/trainers/**",')
edit(Path('resources/templates/fragments/nav.html'),'<li class="nav-item"><a class="nav-link" th:href="@{/gyms}">Фитнеси</a></li>','<li class="nav-item"><a class="nav-link" th:href="@{/gyms}">Фитнеси</a></li><li class="nav-item"><a class="nav-link" th:href="@{/trainers}">Треньори</a></li>')
edit(Path('resources/templates/admin/dashboard.html'),'<main class="container page">','<main class="container page"><a class="btn btn-pulse mb-3" th:href="@{/admin/trainers}">Треньори</a>')
# Privacy defaults: private city/favorites, no public progress.
p=Path('java/com/fitpulse/model/entity/User.java')
edit(p,'public UUID getId()', '''@Column(nullable=false) private boolean showCity;
    @Column(nullable=false) private boolean showFavorites;
    @Column(nullable=false) private boolean enabled=true;
    @Lob @Column(columnDefinition="MEDIUMBLOB") private byte[] avatar;
    @Column(length=30) private String avatarType;
    public boolean isShowCity(){return showCity;} public void setShowCity(boolean v){showCity=v;}
    public boolean isShowFavorites(){return showFavorites;} public void setShowFavorites(boolean v){showFavorites=v;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public byte[] getAvatar(){return avatar;} public void setAvatar(byte[] v){avatar=v;}
    public String getAvatarType(){return avatarType;} public void setAvatarType(String v){avatarType=v;}
    public UUID getId()''')
p=Path('java/com/fitpulse/model/dto/ProfileUpdateRequest.java')
edit(p,'private boolean publicProfile;','''private boolean publicProfile;
    private boolean showCity; private boolean showFavorites;
    public boolean isShowCity(){return showCity;} public void setShowCity(boolean v){showCity=v;}
    public boolean isShowFavorites(){return showFavorites;} public void setShowFavorites(boolean v){showFavorites=v;}''')
p=Path('java/com/fitpulse/service/impl/UserServiceImpl.java')
edit(p,'request.setPublicProfile(currentUser.isPublicProfile());','request.setPublicProfile(currentUser.isPublicProfile());request.setShowCity(currentUser.isShowCity());request.setShowFavorites(currentUser.isShowFavorites());')
edit(p,'currentUser.setPublicProfile(request.isPublicProfile());','currentUser.setPublicProfile(request.isPublicProfile());currentUser.setShowCity(request.isShowCity());currentUser.setShowFavorites(request.isShowFavorites());')
edit(p,'return userRepository.findByUsername(username).orElseThrow','return userRepository.findByUsername(username).filter(User::isEnabled).orElseThrow')
edit(Path('java/com/fitpulse/service/impl/UserDetailsServiceImpl.java'),'.roles(user.getRole().name())','.roles(user.getRole().name()).disabled(!user.isEnabled())')
p=Path('java/com/fitpulse/controller/ProfileController.java')
edit(p,'.filter(com.fitpulse.model.entity.User::isPublicProfile)','.filter(u -> u.isPublicProfile() && u.isEnabled())')
edit(p,'model.addAttribute("favorites", favoriteGymRepository.findAllByUserOrderByCreatedAtDesc(user));','model.addAttribute("favorites", user.isShowFavorites()?favoriteGymRepository.findAllByUserOrderByCreatedAtDesc(user):java.util.List.of());')
edit(p,'model.addAttribute("progress", progressRecordRepository.findAllByUserOrderByRecordedAtDesc(user));','')
p=Path('resources/templates/public-profile.html')
s=(root/p).read_text(encoding='utf-8');s=s[:s.index('<h2 class="mt-5">История на прогреса')]+ '</main></body></html>';s=s.replace('th:text="${user.homeCity}"','th:if="${user.showCity}" th:text="${user.homeCity}"').replace('<h2 class="mt-5">Любими фитнеси','<h2 th:if="${user.showFavorites}" class="mt-5">Любими фитнеси');write(p,s)
p=Path('resources/templates/profile-edit.html')
edit(p,'Профилът, любимите фитнеси и прогресът ми да са публични','Профилът ми да е публичен')
edit(p,'<div class="d-flex gap-2 mt-3">','''<div class="form-check"><input type="checkbox" class="form-check-input" th:field="*{showCity}"><label>Показвай града в публичния профил</label></div><div class="form-check"><input type="checkbox" class="form-check-input" th:field="*{showFavorites}"><label>Показвай любимите фитнеси в публичния профил</label></div><div class="d-flex gap-2 mt-3">''')
write(Path('resources/db/migration/V5__trainers_profiles_and_workout_journal.sql'),'''CREATE TABLE trainers (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(150) NOT NULL, bio VARCHAR(2000) NOT NULL, specializations VARCHAR(1000) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE);
ALTER TABLE program_templates ADD COLUMN trainer_id BIGINT NULL, ADD CONSTRAINT fk_program_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL;
ALTER TABLE meal_plan_templates ADD COLUMN trainer_id BIGINT NULL, ADD CONSTRAINT fk_meal_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL;
INSERT INTO trainers(name,bio,specializations,active) VALUES ('Charles Walker (Чарълз Уокър)','Практичен подход към редовното движение, постепенното натоварване и устойчивите навици.','Силови тренировки, обща физическа подготовка, тренировки за начинаещи',TRUE);
UPDATE program_templates SET trainer_id=(SELECT id FROM trainers WHERE name='Charles Walker (Чарълз Уокър)' LIMIT 1) WHERE trainer_id IS NULL;
UPDATE meal_plan_templates SET trainer_id=(SELECT id FROM trainers WHERE name='Charles Walker (Чарълз Уокър)' LIMIT 1) WHERE trainer_id IS NULL;
ALTER TABLE users ADD COLUMN show_city BOOLEAN NOT NULL DEFAULT FALSE, ADD COLUMN show_favorites BOOLEAN NOT NULL DEFAULT FALSE, ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE, ADD COLUMN avatar MEDIUMBLOB NULL, ADD COLUMN avatar_type VARCHAR(30) NULL;
CREATE TABLE workout_logs (id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BINARY(16) NOT NULL,title VARCHAR(150) NOT NULL,performed_on DATE NOT NULL,notes VARCHAR(1500), CONSTRAINT fk_log_user FOREIGN KEY(user_id) REFERENCES users(id));
CREATE INDEX ix_log_user_date ON workout_logs(user_id,performed_on);
CREATE TABLE workout_log_exercises (id BIGINT AUTO_INCREMENT PRIMARY KEY,log_id BIGINT NOT NULL,name VARCHAR(150) NOT NULL,sets_count INT NOT NULL,repetitions INT NOT NULL,weight DECIMAL(8,2) NOT NULL,CONSTRAINT fk_log_exercise FOREIGN KEY(log_id) REFERENCES workout_logs(id) ON DELETE CASCADE);
''')
print('Trainer module, template links, privacy fields and V5 written.')
