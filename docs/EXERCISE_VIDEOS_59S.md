# Кратки видеа към упражненията — 11.11.2026

## Резултат
- Общо упражнения: **57**.
- Активни видеа с потвърдена продължителност до 59 секунди: **47**.
- Подходящи съществуващи адреси, запазени: **21**.
- Съществуващи адреси, заменени с потвърден кратък клип: **24**.
- Добавени адреси за упражнения, които преди нямаха видео: **2**.
- Стари адреси, деактивирани без потвърден заместител: **10**.
- Остават без потвърден подходящ клип: **10**.

Продължителността е прочетена от публичните YouTube player метаданни на същия videoId. Не е извеждана от заглавие, Shorts формат или таймер на плейъра. От 55-те стари адреса при първата проверка 21 са в лимита, 19 са над него, а за 15 не е получена продължителност. Последващият опит за тези 15 адреса върна HTTP 429. Директните проверки бяха прекратени; непотвърдени стойности не са попълвани.

Съответствието е прегледано по заглавията, описанията, варианта на движението и оборудването. **Целите клипове не са гледани визуално и не се твърди пълна проверка на техниката.** Полето durationConfirmed потвърждава само продължителността. Активните клипове са с продължителност 8–56 секунди; предпочетени са 15–45 секунди, когато има подходящ потвърден избор. Кратките подходящи стари клипове са запазени.

## Без потвърден подходящ клип
- **Клек с дъмбел** (`goblet-squat`): Кратки кандидати са намерени, но дъмбелът не е потвърден; демонстрацията на Onnit е с пудовка. Не е подменено оборудването.
- **Обратен напад** (`reverse-lunge`): Старият клип е 97 сек. Новият намерен OPEX кандидат няма потвърдена продължителност след ограничаването на заявките.
- **Качване на платформа** (`step-up`): Старият клип е 186 сек. Намерени са кандидати за качване на платформа със собствено тегло, но продължителността им не е потвърдена.
- **Глутеус мост** (`glute-bridge`): Не е получена продължителност за стария адрес. Намерени са OPEX кандидати; оставени са за допълване след ограничението на YouTube.
- **Разгъване за трицепс над глава** (`overhead-triceps`): Краткият OPEX кандидат е с два дъмбела, а упражнението тук е с един дъмбел и две ръце. Другият кратък кандидат не е достатъчно потвърден за този вариант.
- **Велоергометър** (`stationary-bike`): Продължителността на стария адрес не е потвърдена. Намерените общи тренировки не са подходящи кратки демонстрации.
- **Степер** (`stepper`): Продължителността на стария адрес не е потвърдена; намерените цели тренировки не заместват кратка демонстрация.
- **Кростренажор** (`elliptical`): Продължителността на стария адрес и на намерените алтернативи не е потвърдена.
- **Планински катерач** (`mountain-climber`): Продължителността на стария адрес не е получена. Намерени са алтернативни демонстрации, но не са включени без потвърдена продължителност.
- **Седеж до стена** (`wall-sit`): Старият адрес е намерен, но продължителността не е потвърдена. Не е приета стойност от заглавието като реална продължителност.

## Реализация
- ExerciseVideos допуска само конкретни валидни watch/shorts адреси със записана потвърдена продължителност 1–59 секунди и описан източник/обхват на съпоставянето.
- Общ Thymeleaf фрагмент показва адаптивен iframe в детайла на упражнението и в публичните и личните тренировъчни програми, свързан по съществуващия exercise.code.
- Видеото се разгъва по избор. Плейърът е от youtube-nocookie.com, loading=lazy, autoplay=0, без автоматично възпроизвеждане и без параметър end или друга подмяна на реалната продължителност.
- Има връзка към YouTube при проблем с вграждането. Наличността и разрешението за вграждане могат да бъдат променени от автора впоследствие.
- При липса на потвърден клип се показва кратко съобщение, без неработещ плейър.
- Упражненията, мускулните групи, предписанията, програмите, сесиите и потребителските данни не са променяни. Няма промяна в база или миграция.

## Файлове
Нови: `templates/fragments/exercise-video.html`, `static/css/exercise-videos.css`, `tools/verify_exercise_video_metadata.py`, този отчет и `docs/exercise-video-review.json`.

Редактирани: `service/ExerciseVideos.java`, `controller/ProgramController.java`, `content/exercise-videos.json`, `templates/exercises/details.html`, `templates/programs/preview.html`, `templates/programs/details.html`, `templates/fragments/head.html`.

Java файловете са под src/main/java/com/fitpulse; шаблоните, CSS и съдържанието са под src/main/resources. Подробното съпоставяне на стария и новия адрес е в [машинно четимия отчет](exercise-video-review.json). Инструментът в tools само чете публични метаданни; не изтегля видео и спира нови заявки при HTTP 429.

Не са стартирани Maven, build, автоматични тестове, приложението или браузърна проверка. Няма commit/push. **Плейърите не са проверени в работещ браузър.**

## Активни видеа и източници
| Упражнение | Секунди | Видео | Канал |
|---|---:|---|---|
| Клек със собствено тегло | 47 | [Bodyweight Squat Tutorial - Proper Form and Technique](https://www.youtube.com/watch?v=P-yaD24bUE8) | Runna |
| Клек с щанга | 25 | [Barbell Back Squat — Exercise Demo](https://www.youtube.com/watch?v=KB_r0K3Ce2E) | Red Dot Fitness |
| Лег преса | 21 | [Leg Press Correct Form — Best Leg Workout for Muscle Growth #shorts](https://www.youtube.com/watch?v=23WGAVw7c9g) | Fit_hub03 |
| Напад напред | 40 | [Tutorial — Bodyweight Forward Lunge](https://www.youtube.com/watch?v=IFV10jWn1ic) | Get Right Results |
| Румънска тяга с дъмбели | 16 | [Dumbbell Romanian Deadlift](https://www.youtube.com/watch?v=xAL7lHwj30E) | Onnit Academy |
| Повдигане за прасци | 10 | [Bodyweight Calf Raise](https://www.youtube.com/watch?v=fQiMiUHWubw) | John Rusin |
| Сгъване за задно бедро | 49 | [Seated Leg Curl Machine Tutorial](https://www.youtube.com/watch?v=EqCsK_vlnfE) | Morgan, M.S., CPT, WFS, FNS |
| Разгъване за предно бедро | 15 | [How to Do Leg Extensions Correctly — Gym Guide #shorts #fitness #gym #legs](https://www.youtube.com/watch?v=SeQnSX63mUQ) | KC FITNESS |
| Хип тръст | 16 | [How To Do A Barbell Hip Thrust](https://www.youtube.com/watch?v=aweBS7K71l8) | PureGym |
| Странично отвеждане на крак | 19 | [Sidelying Hip Abduction](https://www.youtube.com/watch?v=s6lDpy4AO6w) | E3 Rehab Exercise Library |
| Лицеви опори | 8 | [Push Up - OPEX Exercise Library](https://www.youtube.com/watch?v=Ql8PKKsDE70) | OPEX Fitness |
| Лицеви опори от висока опора | 16 | [How to do an Incline Push-Up — Proper Form & Technique — NASM](https://www.youtube.com/watch?v=0JUrOH--Kdk) | National Academy of Sports Medicine (NASM) |
| Лежанка с щанга | 14 | [Barbell Bench Press](https://www.youtube.com/watch?v=oYvoluJha9E) | E3 Rehab Exercise Library |
| Лежанка с дъмбели | 42 | [The Dumbbell Bench Press: Gym Shorts (How To)](https://www.youtube.com/watch?v=ufl6HV5NN9g) | Barbell Logic |
| Полулег с дъмбели | 16 | [Incline dumbbell press #shorts](https://www.youtube.com/watch?v=MnwXr08clSA) | Fit_with_anshu |
| Флайс с дъмбели | 23 | [Dumbbell Chest Fly: Demonstration](https://www.youtube.com/watch?v=8feKOJ5LQag) | StrengthLog |
| Кофички на успоредка | 12 | [How To Do Chest Dips](https://www.youtube.com/watch?v=2HYAeF6o7Bc) | PureGym |
| Набирания | 11 | [How To Do Pull Ups](https://www.youtube.com/watch?v=PHdHnZcbsB8) | PureGym |
| Горен скрипец | 15 | [Lat Pulldown](https://www.youtube.com/watch?v=sxbFnGt-EFc) | E3 Rehab Exercise Library |
| Гребане с дъмбел | 9 | [Single-Arm Dumbbell Row](https://www.youtube.com/watch?v=65-9uajZmzk) | Onnit Academy |
| Гребане с щанга | 11 | [Bent-over Row](https://www.youtube.com/watch?v=rodN4lgcNxc) | Onnit Academy |
| Хоризонтален скрипец | 17 | [Seated Cable Row](https://www.youtube.com/watch?v=UCXxvVItLoM) | Renaissance Periodization |
| Хиперекстензия | 48 | [Hyperextensions (Back Extensions) - Core / Back Exercise - Bodybuilding.com](https://www.youtube.com/watch?v=qtjJUWCnDyE) | Bodybuilding.com |
| Раменна преса с дъмбели | 53 | [How To Do Dumbbell Shoulder Press — Exercise Demo](https://www.youtube.com/watch?v=5WwbDVpm2u4) | OriGym |
| Странично повдигане с дъмбели | 12 | [Onnit Tutorials — Dumbbell Lateral Raise Exercise](https://www.youtube.com/watch?v=4q-TZNT3eHU) | Onnit |
| Обратен флайс | 11 | [Rear Delt Dumbbell Fly Form Guide](https://www.youtube.com/watch?v=9WpNWAM782Y) | Andrew Kwong (DeltaBolic) |
| Дърпане към лице на скрипец | 26 | [Face Pull (Cables) — Exercise Demo](https://www.youtube.com/watch?v=ikO6xiIsnTU) | Red Dot Fitness |
| Бицепсово сгъване с дъмбели | 27 | [Dumbbell Bicep Curl — Exercise Demo](https://www.youtube.com/watch?v=_HF3957vEeE) | Red Dot Fitness |
| Чуково сгъване | 19 | [How to do a Dumbbell Hammer Curl](https://www.youtube.com/watch?v=CFBZ4jN1CMI) | National Academy of Sports Medicine (NASM) |
| Бицепсово сгъване с щанга | 42 | [Gym Shorts (How To): The Barbell Curl](https://www.youtube.com/watch?v=RK47T0F276I) | Barbell Logic |
| Трицепсово разгъване на скрипец | 14 | [This Is How You're SUPPOSED TO Do Triceps Cable Pushdowns ft. CBum 💪](https://www.youtube.com/watch?v=Fht4XiXhilg) | Smartgainsdaily |
| Планк | 44 | [How To Do Forearm Plank — Exercise Demo](https://www.youtube.com/watch?v=F_T41yzsXJ8) | OriGym |
| Страничен планк | 11 | [Side Plank](https://www.youtube.com/watch?v=eRygfYEe1hs) | E3 Rehab Exercise Library |
| Коремни преси | 20 | [AB EXERCISE - How to do an abdominal crunch (ab crunch)](https://www.youtube.com/watch?v=NnVhqMQRvmM) | PureGym |
| Повдигане на крака от лег | 17 | [Lying Leg Raise: Demonstration](https://www.youtube.com/watch?v=HPXqFqBK728) | StrengthLog |
| Мъртъв бръмбар | 19 | [Dead Bug](https://www.youtube.com/watch?v=BZYaCzbP09M) | E3 Rehab Exercise Library |
| Птица-куче | 19 | [Bird Dog](https://www.youtube.com/watch?v=egKWoMZ6cXM) | E3 Rehab Exercise Library |
| Бързо ходене | 47 | [🚶‍♀️Here’s a step-by-step guide on how to do brisk walking:Brisk Walking Technique 🚶‍♀️](https://www.youtube.com/watch?v=ludLE7q8Zas) | Dr Ram Physio |
| Тазобедрено сгъване | 20 | [Bodyweight Hip Hinge – Master the Foundation of Strength](https://www.youtube.com/watch?v=vauqqYoLKvw) | Train With Cuz |
| Ротации на гръден отдел | 15 | [Quadruped Thoracic Rotation](https://www.youtube.com/watch?v=ADeGAWCDjhQ) | E3 Rehab Exercise Library |
| Преходи 90/90 за таз | 19 | [90/90 Hip Transitions](https://www.youtube.com/watch?v=Y-L0s5uSQ-E) | E3 Rehab Exercise Library |
| Мобилизация на глезен | 49 | [Ankle Mobility   Knee to Wall Exercise](https://www.youtube.com/watch?v=uippmmTdoWw) | Union Physical Therapy |
| Клек до стол | 56 | [How to do a chair squat](https://www.youtube.com/watch?v=OViE2ghEop0) | MassGeneralHospital |
| Гребане с раница | 8 | [Backpack Bent Over Row](https://www.youtube.com/watch?v=Xk4BjIlBJes) | FITSOM OC |
| Раменна преса с раница | 27 | [Backpack Shoulder Press](https://www.youtube.com/watch?v=gUdNX2cwTcM) | CrossFit 1976 |
| Страничен планк от колене | 50 | [Modified Side Plank (Bent Knee)](https://www.youtube.com/watch?v=Nb091vrUA14) | Strength & Sport |
| Тясна лицева опора от стена | 27 | [Close Grip Wall Push-up Young Adult Male](https://www.youtube.com/watch?v=1Yv9K95xdWE) | TruestarHealth |
