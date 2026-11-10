# FitPulse 2.0 — V12
Дата: 10.11.2026 г.

## Реализирани промени
1. **Анатомия:** съществуващите front.svg/back.svg от V11, геометрията, d координатите, viewBox и transform не са променяни във V12. JavaScript обхожда ВСИЧКИ елементи с еднакъв data-zone, включително LEFT/RIGHT и съставните контури, и задава fill на реалните path елементи с достатъчен приоритет. Hover/focus също обхващат двете страни. „Всички“ изчиства активното оцветяване. Основни/второстепенни зони остават различими.
2. **Подгрупи:** 12 подгрупи за крака, гръб, рамене и корем. MuscleSubgroups съпоставя конкретните зони с наличните primaryZones на упражненията, без извеждане от общия етикет. Вторият ред идва от същото съпоставяне и подава зоната към JavaScript. Подгрупата е в URL и пагинацията; смяната на основната група нулира подгрупата и страницата. Второстепенни стабилизатори не се използват за включване във филтъра. За подгрупа без основно натоварващо упражнение се показва договореното празно състояние.
3. **Навигатор:** кратките дублирани линкове са заменени с указание и достъпен бутон/връзка за плавно превъртане до картите. Отстъпът използва измерената височина на фиксирания хедър; изборът на група не задейства превъртане.
4. **Упражнения:** бутонът към дневника и интерфейсът с алтернативи са заменени с „Видео за правилно изпълнение“. Външните връзки са target="_blank", rel="noopener noreferrer", без вграден плейър. Данните за алтернативите и интеграцията с дневника не са изтрити. Структурираният каталог използва стабилните кодове на упражненията и приема само конкретни HTTPS YouTube watch адреси с валиден формат.
5. **Лични списъци:** добавено е потвърдено премахване само на членството чрез listed=false. Редакции, свързани покупки, сесии, календар, дневник и прогрес се запазват; собственикът може да отвори историческия URL. Повторното добавяне на скрит оригинал възстановява съществуващото копие, вместо да създава ново. При няколко стари скрити копия се възстановява най-старото.
   - Добавянето е READ_COMMITTED транзакция с PESSIMISTIC_WRITE заключване на реда на текущия потребител ПРЕДИ проверката и създаването. Така конкурентни заявки от този потребител се изпълняват последователно и виждат вече записаното членство.
   - Публичните каталози и подробности показват „Отвори моя режим“/„Отвори моята програма“ за вече добавен оригинал. Повторната заявка показва съответното съобщение.
   - Няма уникално ограничение върху старите копия и няма автоматично изтриване/сливане. Целевата read-only справка в fitpulse2 установи **1 група стари дубликати при режимите и 0 при програмите**. Те са запазени; личната страница показва предупреждение за активните дубликати. Разрешаването им остава отделна безопасна задача.
6. **Начална страница:** точно три видими outline бутона: режими, профил, програми. За гост профилът сочи към login с фиксирано безопасно връщане към /profile; общият механизъм за връщане от други линкове е запазен. Фитнесите и предложението използват съществуващите меню маршрути. Цялата регистрационна секция е само за гости чрез Spring Security/Thymeleaf. Слоганът е 20–24 px на десктоп, компактен на телефон; съществуващото адаптивно центриране се запазва.
7. **Пазаруване:** изричното „Обнови списъка“ е CSRF-защитен POST. Под заключване само на собствения режим всички негови съществуващи отметки, включително старите обхвати, стават false в базата. Дните се запазват в redirect URL и количествата се преизчисляват. GET за дните/„Цяла седмица“ не нулира. Асинхронните отметки от V11 остават; обновяването изчаква започнатите от тази страница записи.
8. **Лично меню:** записът на грамаж връща точните сървърни сборове за храненето и деня. Грешка възстановява последната запазена стойност. Замяната на продукт/хранене връща към конкретен meal anchor, който отваря съответните ден И хранене с новите данни. Личният отговор е no-store; връщане от BFCache презарежда данните. Запазено е съобщението за замяна само в личното копие.
9. **Инструкции:** локалната recipe-002, подготовката и списъкът със стъпки имат по една инструкция. Целевата read-only MySQL справка за лично копие №2 също върна по една инструкция в preparation и cookingSteps. Справката за повторена известна инструкция в preparation на тази рецепта върна 0. **Първопричината на наблюдавания визуален бъг не е доказана и той не е възпроизведен.**
   - Добавени са неизменяемо връщане на стъпките и JsonIgnore за производния cookingSteps при сериализация на нови копия, за да не се клонира изгледът като втори източник на инструкции.
   - RecipeInstructions разпознава само точно известното ястие с извара и ябълка и съответните продукти; поправя единствено съседни идентични повторения на точно известното изречение, без общ distinct() за други рецепти.
   - Идемпотентният RecipeInstructionRepair при следващ dev/prod старт записва само действително засегнатите лични JSON копия. Различните и потребителските инструкции се запазват. В тази сесия не е стартиран и няма запис в базата.

## Миграция и следващо стартиране
Нова безопасна миграция: `V12__personal_library_visibility.sql` — добавя listed=true към user_programs и user_meal_plans. V9 и по-старите миграции не са редактирани. Миграцията НЕ е приложена в тази сесия.

При следващото обичайно стартиране използвайте актуалния код и конфигурация към **fitpulse2**; Flyway трябва да приложи V12 преди JPA validate. След това се изпълнява ограничената поправка на рецептата и логът отчита реално променените копия. Не насочвайте приложението към старата fitpulse.

## Проверки и ограничения
Не са изпълнявани Maven, build, автоматични тестове, общ одит, commit или push. Няма реална браузърна проверка на V12: оцветяване, адаптивност, клиентските взаимодействия, транзакциите и миграцията не са потвърдени в работещо приложение. Извършени са само целеви прочити на променяните източници, read-only справките за описаната рецепта/дубликати и интернет търсенето на конкретни видеа. Не са добавяни снимки и не е докосвана базата fitpulse.

## Файлове във V12
Нови:
- src/main/java/com/fitpulse/service/MuscleSubgroups.java
- src/main/java/com/fitpulse/service/ExerciseVideos.java
- src/main/java/com/fitpulse/service/RecipeInstructions.java
- src/main/java/com/fitpulse/bootstrap/RecipeInstructionRepair.java
- src/main/resources/content/exercise-videos.json
- src/main/resources/db/migration/V12__personal_library_visibility.sql
- src/main/resources/static/css/upgrade12.css
- docs/FITPULSE_V12.md

Редактирани:
- config/SecurityConfig.java; controller/ExerciseLibraryController.java, ProgramController.java, PersonalNutritionController.java
- model/entity/UserProgram.java, UserMealPlan.java; model/dto/WeeklyMenu.java; repository/UserRepository.java
- service/ExerciseLibraryService.java, WellnessService.java, PersonalNutritionService.java, RecipeAssets.java
- static/js/anatomy.js, navigation.js, nutrition.js, weekly-menu.js
- templates/home.html; exercises/list.html, details.html
- templates/fragments/anatomy.html, head.html, nav.html, weekly-menu.html
- templates/programs/library.html, preview.html, meals.html, meal-details.html, my.html, my-meals.html, personal-meal.html, shopping.html, substitutions.html, meal-replacements.html
- README.md

Посочените Java и ресурсни относителни имена са под src/main/java/com/fitpulse и src/main/resources. Други видими git промени принадлежат на предходните V10/V11 задачи и са запазени.

## Видеа — обхват на потвърждението
**55 конкретни URL адреса** са свързани към съществуващите упражнения. Потвърдени са адресът и съответствието по публично YouTube заглавие/описание към 10.11.2026 г. Част от директните отваряния на YouTube са ограничени; наличните първични индексирани метаданни са използвани вместо това. **Нито едно видео не е гледано, а възпроизвеждането и цялата техника не са проверени визуално.** За две демонстрации каналът не е потвърден и е оставен null, вместо да се измисля автор. В приложението не се показват тези технически бележки или чуждоезичните заглавия.

Без потвърден подходящ URL остават:
- Повдигане на крака от лег (`leg-raise`): намерените по-надеждни резултати бяха за вис или странично отвеждане; не са използвани като заместител.
- Бързо ходене (`fast-walk`): намерени са общи материали за ходене/бягане, но не е избран достатъчно убедителен конкретен урок за точното движение.

### Конкретни източници
| Код | Видео | Канал |
|---|---|---|
| bodyweight-squat | [Bodyweight Squat Tutorial - Proper Form and Technique](https://www.youtube.com/watch?v=P-yaD24bUE8) | Runna |
| goblet-squat | [How To: Goblet Squat](https://www.youtube.com/watch?v=MeIiIdhvXT4) | ScottHermanFitness |
| barbell-squat | [How To: Barbell Squat — 3 GOLDEN RULES!](https://www.youtube.com/watch?v=1oed-UmAxFs) | ScottHermanFitness |
| leg-press | [How To: Leg Press (Cybex)](https://www.youtube.com/watch?v=oujca3_Shgw) | ScottHermanFitness |
| forward-lunge | [Tutorial — Bodyweight Forward Lunge](https://www.youtube.com/watch?v=IFV10jWn1ic) | Get Right Results |
| reverse-lunge | [Bodyweight Reverse Lunge: Step-by-Step Technique](https://www.youtube.com/watch?v=PHA9tVMRsj8) | BuhoSalud |
| romanian-deadlift | [How To: Dumbbell Romanian Deadlift](https://www.youtube.com/watch?v=FQKfr1YDhEk) | ScottHermanFitness |
| calf-raise | [Bodyweight Calf Raise](https://www.youtube.com/watch?v=fQiMiUHWubw) | John Rusin |
| leg-curl | [How To: Seated Leg Curl (Cybex)](https://www.youtube.com/watch?v=ELOCsoDSmrg) | ScottHermanFitness |
| leg-extension | [How To: Leg Extension (Cybex)](https://www.youtube.com/watch?v=YyvSfVjQeL0) | ScottHermanFitness |
| glute-bridge | [How To: Glute Bridge](https://www.youtube.com/watch?v=8bbE64NuDTU) | ScottHermanFitness |
| side-leg-raise | [Sidelying Hip Abduction](https://www.youtube.com/watch?v=s6lDpy4AO6w) | E3 Rehab Exercise Library |
| push-up | [How To: Push-Up](https://www.youtube.com/watch?v=wxhNoKZlfY8) | ScottHermanFitness |
| incline-push-up | [How to do an Incline Push-Up — Proper Form & Technique — NASM](https://www.youtube.com/watch?v=0JUrOH--Kdk) | NASM |
| bench-press | [How To: Barbell Bench Press](https://www.youtube.com/watch?v=rT7DgCr-3pg) | ScottHermanFitness |
| dumbbell-bench | [How To: Dumbbell Bench Press — 3 GOLDEN RULES](https://www.youtube.com/watch?v=Y_7aHqXeCfQ) | ScottHermanFitness |
| incline-dumbbell | [How To: Dumbbell Incline Chest Press](https://www.youtube.com/watch?v=8iPEnn-ltC8) | ScottHermanFitness |
| chest-fly | [How To: Dumbbell Flys On A Flat Bench](https://www.youtube.com/watch?v=eozdVDA78K0) | ScottHermanFitness |
| lat-pulldown | [How To: Lat Pulldown (Cybex)](https://www.youtube.com/watch?v=7D2t1XnrW2s) | ScottHermanFitness |
| dumbbell-row | [How To: Dumbbell Row](https://www.youtube.com/watch?v=pYcpY20QaE8) | ScottHermanFitness |
| cable-row | [How To: Seated Low Row (LF Cable)](https://www.youtube.com/watch?v=GZbfZ033f74) | ScottHermanFitness |
| shoulder-press | [How To: Dumbbell Shoulder Press](https://www.youtube.com/watch?v=qEwKCR5JCog) | ScottHermanFitness |
| lateral-raise | [How To: Dumbbell Side Lateral Raise](https://www.youtube.com/watch?v=3VcKaXpzqRo) | ScottHermanFitness |
| reverse-fly | [Rear Delt Dumbbell Fly Form Guide](https://www.youtube.com/watch?v=9WpNWAM782Y) | Andrew Kwong (DeltaBolic) |
| face-pull | [How To: Face Pull](https://www.youtube.com/watch?v=rep-qVOkqgk) | ScottHermanFitness |
| biceps-curl | [How To: Alternating Dumbbell Curl](https://www.youtube.com/watch?v=sAq_ocpRh_I) | ScottHermanFitness |
| hammer-curl | [How To: Dumbbell Hammer Curl](https://www.youtube.com/watch?v=zC3nLlEvin4) | ScottHermanFitness |
| barbell-curl | [How To: Barbell Bicep Curl — 3 GOLDEN RULES](https://www.youtube.com/watch?v=QZEqB6wUPxQ) | ScottHermanFitness |
| triceps-pushdown | [How To: Tricep Pushdown (Life Fitness Cable)](https://www.youtube.com/watch?v=2-LAMcpzODU) | ScottHermanFitness |
| overhead-triceps | [HOW TO: Overhead Triceps Extension —— PERFECT FORM (POWERBOMB)](https://www.youtube.com/watch?v=fYqswDVbJDg) | ScottHermanFitness |
| plank | [How To: Plank — 3 GOLDEN RULES! (MADE BETTER!)](https://www.youtube.com/watch?v=fqzUlmyTpJk) | ScottHermanFitness |
| side-plank | [Side Plank](https://www.youtube.com/watch?v=eRygfYEe1hs) | E3 Rehab Exercise Library |
| dead-bug | [Dead Bug](https://www.youtube.com/watch?v=BZYaCzbP09M) | E3 Rehab Exercise Library |
| bird-dog | [Bird Dog](https://www.youtube.com/watch?v=egKWoMZ6cXM) | E3 Rehab Exercise Library |
| thoracic-rotation | [Quadruped Thoracic Rotation](https://www.youtube.com/watch?v=ADeGAWCDjhQ) | E3 Rehab Exercise Library |
| knee-side-plank | [Modified Side Plank (Bent Knee)](https://www.youtube.com/watch?v=Nb091vrUA14) | Strength & Sport |
| backpack-press | [Backpack Shoulder Press](https://www.youtube.com/watch?v=gUdNX2cwTcM) | CrossFit 1976 |
| barbell-row | [How to do the BENT-OVER BARBELL ROW! — 2 Minute Tutorial](https://www.youtube.com/watch?v=FWJR5Ve8bnQ) | Непотвърден |
| back-extension | [Back extension / Hyperextension (lower back exercises)](https://www.youtube.com/watch?v=O8d8I7-qtGg) | Непотвърден |
| hip-hinge | [How To Coach The Hip Hinge Like A Pro —— NASM-CPT Trainer Tips and Cues](https://www.youtube.com/watch?v=X-LLXtgvPRY) | The Axiom Personal Trainer Academy |
| wall-sit | [How to Do a Wall Sit Exercise — 30 Seconds — MedBridge](https://www.youtube.com/watch?v=cWTZ8Am1Ee0) | MedBridge |
| mountain-climber | [How To Do Mountain Climbers](https://www.youtube.com/watch?v=kLh-uczlPLg) | PureGym |
| elliptical | [How to use an Elliptical Machine — Planet Fitness](https://www.youtube.com/watch?v=sHMemwz_HPU) | Planet Fitness |
| stepper | [Exercise Equipment : How to Use a Stepper Machine](https://www.youtube.com/watch?v=GPpmygCGRCU) | eHowFitness |
| stationary-bike | [Nuffield Health's Exercise Tip of the Week: How to set up a spin bike](https://www.youtube.com/watch?v=C1yrJrnf_Mw) | Nuffield Health |
| chair-squat | [How to do a chair squat](https://www.youtube.com/watch?v=OViE2ghEop0) | MassGeneralHospital |
| hip-90-90 | [90/90 Hip Transitions](https://www.youtube.com/watch?v=Y-L0s5uSQ-E) | E3 Rehab Exercise Library |
| step-up | [Stair stepping - Nuffield Health](https://www.youtube.com/watch?v=LIDgBmx_GPg) | Nuffield Health |
| narrow-wall-push-up | [Close Grip Wall Push-up Young Adult Male](https://www.youtube.com/watch?v=1Yv9K95xdWE) | TruestarHealth |
| pull-up | [How To Do Pull Ups](https://www.youtube.com/watch?v=PHdHnZcbsB8) | PureGym |
| hip-thrust | [How To Do A Barbell Hip Thrust](https://www.youtube.com/watch?v=aweBS7K71l8) | PureGym |
| dips | [How To Do Chest Dips](https://www.youtube.com/watch?v=2HYAeF6o7Bc) | PureGym |
| crunch | [AB EXERCISE - How to do an abdominal crunch (ab crunch)](https://www.youtube.com/watch?v=NnVhqMQRvmM) | PureGym |
| backpack-row | [Backpack Bent Over Row](https://www.youtube.com/watch?v=Xk4BjIlBJes) | FITSOM OC |
| ankle-mobility | [Ankle Mobility Knee to Wall Exercise](https://www.youtube.com/watch?v=uippmmTdoWw) | Union Physical Therapy |

