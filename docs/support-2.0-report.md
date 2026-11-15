# FitPulse 2.0 — Помощ и обратна връзка

Добавени са публична форма `/support` и секция `/admin/messages` в съществуващата администрация. Формата има трите категории, тема, описание, имейл, една незадължителна снимка и потвърждение след запис. За логнат потребител имейлът се взема от профила на сървъра. Гостите изпращат без регистрация.

Администраторът вижда страницирани съобщения, филтрира по категория/статус, отваря описание и снимка и задава „Ново“, „В процес“ или „Решено“. Няма автоматично изпращане на имейли.

## Променени съществуващи файлове

- `pom.xml` — ImageIO WebP decoder, TwelveMonkeys 3.13.0.
- `src/main/java/com/fitpulse/config/SecurityConfig.java` — публичен `/support`; `/admin` и `/admin/**` само за ADMIN; CSRF остава включена.
- `src/main/resources/application.properties` — максимум 5 MB на файл, 6 MB на заявка, настройка за защитеното хранилище.
- `src/main/resources/templates/fragments/nav.html` — помощ вместо „Предложи фитнес“ в менюто за логнати потребители.
- `src/main/resources/templates/fragments/head.html` — включване на ограничени до новите екрани CSS правила.
- `src/main/resources/templates/home.html` — публичен footer линк.
- `src/main/resources/templates/auth/login.html` и `auth/register.html` — текст и линк за помощ под формите.
- `src/main/resources/templates/admin/dashboard.html` — бутон „Съобщения“.

## Нови файлове

Под `src/main/java/com/fitpulse/`:

- `config/SupportUploadFilter.java` — лимит по IP преди обработка на файла, безопасно обработване на multipart грешки и освобождаване на временните файлове.
- `controller/SupportController.java` — публична форма, сървърна валидация, имейл от профила, потвърждение след запис.
- `controller/AdminSupportController.java` — списък, филтри, подробности, статуси и защитен достъп до снимките.
- `controller/SupportUploadExceptionAdvice.java` — обработване на multipart грешки преди избора на контролер.
- `model/dto/SupportForm.java` — разрешени полета и ограничения за входа.
- `model/entity/SupportMessage.java` — съобщение, статус, време и ключ към снимката.
- `model/enums/SupportCategory.java` и `model/enums/SupportStatus.java` — категории и статуси.
- `repository/SupportMessageRepository.java` — филтриране и странициране.
- `service/SupportService.java` — запис и промяна на статус; изчистване на снимката при rollback.
- `service/SupportImageStorage.java` — реално декодиране на JPG/PNG/WebP, ограничение на размерите, повторно PNG кодиране без метаданни и безопасни генерирани имена.
- `service/SupportRateLimiter.java` — атомарно ограничение в базата: 5 опита за 15 минути на IP и допълнително на логнат профил. Ключовете са SHA-256; сурови IP адреси не се записват. Старите записи се почистват при следващи заявки.

Под `src/main/resources/`:

- `templates/support.html`.
- `templates/admin/messages.html`.
- `templates/admin/message-details.html`.
- `static/css/support.css` — тъмен дизайн с оранжеви акценти и адаптивно представяне.
- `db/migration/V16__support_messages.sql` — новите таблици `support_messages` и `support_rate_limits` и индекс за филтрите. Миграцията е подготвена, не е изпълнявана.

## Съхранение и защити

По подразбиране снимките са в `${user.home}/.fitpulse/support`, извън static директориите. Пътят може да се зададе чрез `FITPULSE_SUPPORT_STORAGE`; трябва да сочи към частна постоянна директория. При няколко инстанции хранилището трябва да е споделено. Снимките се предоставят само през ADMIN маршрут по ID на съобщението, с PNG content type, `nosniff` и `no-store`. Имената и MIME типът от браузъра не се използват за доверие. Допълнително се ограничават до 10 мегапиксела и 8192 пиксела на страна.

Текстовете се показват чрез `th:text`, без изпълнение на HTML/JavaScript. POST формите използват съществуващата CSRF защита. IP идентичността използва адреса на връзката, а не непроверен `X-Forwarded-For`; при reverse proxy трябва да се конфигурира доверено предаване на реалния клиентски адрес.

Бутонът „Предложи фитнес“ в каталога е запазен. В тази задача не са променяни тренировките, храненето, каталогът, профилите или останалите административни действия.

Извършен е само преглед на променения код и diff. Не са изпълнявани общ одит, Maven/build, тестове, стартиране, commit или push. Новата зависимост не е изтегляна и поведението не е потвърдено в работещо приложение.
