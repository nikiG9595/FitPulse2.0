# FitPulse

# EN

## Overview

**FitPulse** is a fitness management web application developed with Spring Boot as an Individual Project for the **Spring Advanced** course at SoftUni.

The system consists of two independent Spring Boot applications:

1. **FitPulse Main Application** – the main MVC application responsible for users, memberships, gym classes, bookings, security, scheduling, caching, and the user interface.
2. **FitPulse Progress Service** – a separate REST microservice responsible for storing and managing users' fitness progress records.

The Main Application communicates with the Progress Service through **Spring Cloud OpenFeign**.

---

## GitHub Repositories

- **Main Application:**  
  https://github.com/nikiG9595/FitPulse

- **Progress REST Microservice:**  
  https://github.com/nikiG9595/fitpulse-progress-service

---

## Running the Application

### Development Database Configuration

For easier local setup and project evaluation, the current `application.properties`
contains the MySQL credentials used during development.

This is a local development configuration for the educational project.
Externalized secret management (environment variables, secret managers, Docker secrets, etc.)
is outside the scope of the project requirements.

For a production environment, database credentials should be externalized and must not be
stored directly in the application's source code.

### Requirements

Before starting FitPulse, make sure you have:

- Java 21+
- Maven
- MySQL

The two applications use separate MySQL databases:

- Main Application: `fitpulse`
- Progress Service: `fitpulse_progress`

The databases can be completely empty when the applications are started for the first time.

### 1. Start the Progress Service

Open the `fitpulse-progress-service` project and start the Spring Boot application.

The Progress Service runs by default on:

```text
http://localhost:8081
```

### 2. Start the Main Application

Open the `FitPulse` project and start the Spring Boot application.

The Main Application runs by default on:

```text
http://localhost:8080
```

Open the following address in your browser:

```text
http://localhost:8080
```

> The Progress Service should also be running when using the fitness progress functionality.

---

## First Administrator Setup

A new FitPulse database does not contain a predefined administrator account.

To create the first ADMIN:

1. Start both applications.
2. Open FitPulse at `http://localhost:8080`.
3. Register a new account through the standard registration page.
4. The newly registered account will initially have the `MEMBER` role.
5. Open the `fitpulse` database in MySQL.
6. Execute:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE username = 'your_username';
```

Replace `your_username` with the username of the account you registered.

7. Log out of FitPulse and log in again.

The account will now have access to the ADMIN functionality.

The direct database change is required **only for the first administrator** when starting with an empty database.

After the first ADMIN account exists, user roles can be managed directly from:

```text
ADMIN → Users
```

An administrator can promote another user from `MEMBER` to `ADMIN` or change another administrator back to `MEMBER`.

As a safety measure, the currently logged-in administrator cannot remove their own ADMIN role.

---

## Technology Stack

### Main Application

- Java 21
- Spring Boot 3.4.0
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA
- Hibernate
- Spring Cloud OpenFeign
- Spring Cache
- Jakarta Bean Validation
- MySQL
- Maven
- Bootstrap 5
- JUnit 5
- Mockito
- MockMvc
- H2 for isolated integration testing
- JaCoCo

### Progress Microservice

- Java 21
- Spring Boot 3.4.0
- Spring Web / REST
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- MySQL
- Maven
- JUnit 5
- Mockito
- MockMvc
- H2 for testing
- JaCoCo

---

## Architecture

FitPulse uses two independent Spring Boot applications running on separate ports and using separate databases.

### Main Application

The Main Application runs on:

```text
8080
```

It provides the web interface and contains the core fitness management functionality.

### Progress Service

The Progress Service runs on:

```text
8081
```

It exposes a REST API for managing fitness progress records.

Communication between the two applications is implemented using **Spring Cloud OpenFeign**.

The Main Application invokes the Progress Service for creating, reading, updating, and deleting progress records.

---

## Domain Model

### Main Application

The Main Application contains four domain entities:

- `User`
- `Membership`
- `GymClass`
- `WorkoutBooking`

All entities use **UUID** identifiers.

The domain model contains relationships between users, memberships, gym classes, and workout bookings.

### Progress Service

The Progress microservice contains a separate domain entity:

- `ProgressRecord`

`ProgressRecord` also uses a UUID identifier and is stored in the separate Progress Service database.

---

## User Roles

FitPulse defines two application roles:

### MEMBER

A member can:

- Register and log in
- View their own profile
- Edit their own username and email
- Choose or change their membership
- Browse available gym classes
- View gym class details
- Book eligible gym classes
- Cancel their own bookings
- View their bookings
- Create fitness progress records
- Edit fitness progress records
- Delete fitness progress records
- View their fitness progress history

### ADMIN

An administrator can perform the regular authenticated operations and additionally:

- Manage their own personal fitness progress records and view their own progress history

- Create membership plans
- Edit membership plans
- Delete membership plans
- Create gym classes
- Edit gym classes
- Delete gym classes
- View application users
- Change another user's role between `MEMBER` and `ADMIN`

The currently authenticated administrator cannot demote their own ADMIN account.

---

## Membership Management

Administrators can:

- Create membership plans
- Edit membership plans
- Delete membership plans

Members can select and change their own membership.

Membership-related business rules are validated in the service layer.

---

## Gym Class Management

Administrators can:

- Create gym classes
- Edit gym classes
- Delete gym classes

Gym classes contain information such as:

- Title
- Trainer
- Start time
- Capacity
- Intensity
- Required membership level
- Description

---

## Workout Booking

Authenticated members can:

- Book eligible gym classes
- Cancel their own bookings
- View their bookings

The application prevents invalid operations such as:

- Booking without a membership
- Booking a class requiring a higher membership level
- Booking the same class more than once
- Booking a full class
- Canceling another user's booking

---

## Fitness Progress REST Microservice

Fitness progress is implemented as a separate Spring Boot REST microservice.

Repository:

https://github.com/nikiG9595/fitpulse-progress-service

The Progress Service stores `ProgressRecord` entities independently from the Main FitPulse database.

A progress record contains information including:

- User ID
- Weight
- Body fat percentage
- Recording date
- Note
- Creation timestamp
- Update timestamp

### REST API

The Progress Service exposes REST operations including:

```text
POST /api/progress
PUT /api/progress/{id}
DELETE /api/progress/{id}
GET /api/progress/user/{userId}
```

These endpoints are consumed by the Main Application through **Spring Cloud OpenFeign**.

The user performs progress operations through the Main FitPulse web interface, while the actual progress data is stored and modified by the REST microservice.

---

## Security

The Main Application uses **Spring Security**.

Implemented security features include:

- Session-based authentication
- BCrypt password hashing
- Role-based authorization
- `MEMBER` and `ADMIN` roles
- Public endpoints
- Authenticated endpoints
- ADMIN-only endpoints
- CSRF protection
- User profile access restrictions
- Administrator role management

Passwords are stored as BCrypt hashes and are never stored as plain text.

CSRF protection remains enabled for state-changing web requests.

---

## Profile Management

Authenticated users can view and edit their own profile.

Editable information includes:

- Username
- Email

Security-sensitive properties such as:

- Role
- Password
- User ID

are not exposed through the profile update form.

Membership changes are handled through the dedicated membership functionality.

---

## Validation

FitPulse applies validation at multiple application layers.

### DTO Validation

Form and API request DTOs use Jakarta Bean Validation for user input.

### Entity Validation

Domain entities use appropriate validation constraints, including:

- `@NotNull`
- `@NotBlank`
- `@Email`
- `@Size`
- `@Min`
- `@Max`
- `@DecimalMin`
- `@Future`
- `@PastOrPresent`

### Service Validation

Business rules that require application state, authentication information, or database access remain in the service layer.

Examples include:

- Duplicate username and email prevention
- Membership uniqueness
- Booking capacity
- Membership eligibility
- Booking ownership
- Duplicate booking prevention
- Role-management rules
- Resource existence checks

---

## Error Handling

Both Spring Boot applications implement centralized error handling.

The Main Application handles custom application exceptions such as:

```text
FitPulseException
```

and built-in Spring/database exceptions such as:

```text
MethodArgumentTypeMismatchException
DataIntegrityViolationException
```

Meaningful responses and custom error pages are returned instead of exposing internal stack traces, SQL messages, or database constraint details.

The Progress Service also implements validation and centralized REST exception handling.

---

## Scheduling

The Main Application contains two scheduled jobs.

### Expired Booking Cleanup

A **CRON-based scheduled job** periodically removes bookings belonging to gym classes whose start time has already passed.

### Past Class Cleanup

A **fixed-delay scheduled job** removes past gym classes once they no longer contain active bookings.

Scheduling components delegate the actual state-changing business operations to the service layer.

---

## Caching

Spring's caching mechanism is used for the membership catalogue.

`MembershipService.getAll()` uses a membership cache because membership data is shared between users, read frequently, and changes relatively infrequently.

The cache stores immutable membership view data instead of mutable JPA entities.

The cache is invalidated after:

- Membership creation
- Membership update
- Membership deletion

Selecting a membership for an individual user does not invalidate the catalogue cache because it does not modify the membership catalogue itself.

---

## Logging

Meaningful state-changing application operations are logged in the service layer.

Logging covers operations including:

- Membership creation
- Membership update
- Membership deletion
- Membership selection
- Gym class creation
- Gym class update
- Gym class deletion
- Booking creation
- Booking cancellation
- User registration
- Administrator role changes
- Profile updates
- Progress creation
- Progress update
- Progress deletion
- Scheduled cleanup operations

Sensitive information such as passwords is not logged.

---

## Testing

Both FitPulse applications contain automated tests.

The test suites include:

- Unit tests
- Integration tests
- API / MockMvc tests
- Security tests
- Validation tests
- Service-layer tests
- Repository tests

The Main Application contains a genuine Spring Boot integration test using an isolated H2 database and real services and repositories.

The Progress Service also contains unit, integration, and API tests.

**JaCoCo** is used for test coverage reporting.

The HTML coverage report is generated under:

```text
target/site/jacoco/index.html
```

Both applications are tested above the required **70% line coverage**.

Run the complete Maven verification with:

```bash
mvn clean verify
```

---

## Database

The Main Application and Progress Service use **separate MySQL databases**.

### Main Application Database

```text
fitpulse
```

### Progress Service Database

```text
fitpulse_progress
```

Both applications use:

- Spring Data JPA
- Hibernate
- UUID primary keys

Database connection credentials must be configured for the local environment before starting the applications.

---

# BG

## За проекта

**FitPulse** е уеб приложение за управление на фитнес дейности, разработено със Spring Boot като индивидуален проект за курса **Spring Advanced** в SoftUni.

Системата се състои от две независими Spring Boot приложения:

1. **FitPulse Main Application** – основното MVC приложение, което управлява потребители, абонаменти, тренировки, записвания, сигурност, scheduling, caching и потребителския интерфейс.
2. **FitPulse Progress Service** – отделен REST microservice за съхранение и управление на данните за физическия прогрес на потребителите.

Основното приложение комуникира с Progress Service чрез **Spring Cloud OpenFeign**.

---

## GitHub Repositories

- **Основно приложение:**  
  https://github.com/nikiG9595/FitPulse

- **Progress REST Microservice:**  
  https://github.com/nikiG9595/fitpulse-progress-service

---

## Стартиране на приложението

### Development конфигурация на базата данни

За по-лесно локално стартиране и оценяване на проекта текущият `application.properties`
съдържа MySQL credentials, използвани по време на разработката.

Това е локална development конфигурация за учебния проект.
Използването на environment variables, secret managers, Docker secrets и други механизми
за управление на чувствителни данни е извън обхвата на изискванията на проекта.

При production среда credentials за базата данни трябва да бъдат изнесени извън source code
и да не се съхраняват директно в конфигурационните файлове на приложението.

### Необходими инструменти

Преди стартиране на FitPulse са необходими:

- Java 21+
- Maven
- MySQL

Двете приложения използват отделни MySQL бази:

- Main Application: `fitpulse`
- Progress Service: `fitpulse_progress`

При първото стартиране базите могат да бъдат напълно празни.

### 1. Стартиране на Progress Service

Отворете проекта `fitpulse-progress-service` и стартирайте Spring Boot приложението.

Progress Service работи по подразбиране на:

```text
http://localhost:8081
```

### 2. Стартиране на Main Application

Отворете проекта `FitPulse` и стартирайте Spring Boot приложението.

Основното приложение работи по подразбиране на:

```text
http://localhost:8080
```

Отворете в браузър:

```text
http://localhost:8080
```

> Progress Service трябва също да бъде стартиран при използване на функционалността за проследяване на физическия прогрес.

---

## Създаване на първия ADMIN

При стартиране с нова база FitPulse няма предварително създаден администраторски профил.

За създаване на първия ADMIN:

1. Стартирайте и двете приложения.
2. Отворете FitPulse на `http://localhost:8080`.
3. Регистрирайте нов профил чрез стандартната регистрационна форма.
4. Новият потребител първоначално ще има роля `MEMBER`.
5. Отворете MySQL базата `fitpulse`.
6. Изпълнете:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE username = 'your_username';
```

Заменете `your_username` с потребителското име на регистрирания профил.

7. Направете Logout и Login отново във FitPulse.

Профилът вече ще има достъп до ADMIN функционалностите.

Ръчната промяна в базата е необходима **само за създаването на първия администратор**, когато приложението се стартира с празна база.

След като вече съществува ADMIN профил, ролите на останалите потребители могат да се управляват директно от:

```text
ADMIN → Users
```

Администраторът може да променя друг потребител от `MEMBER` на `ADMIN` и обратно.

Като защитна мярка текущият администратор не може сам да премахне собствената си ADMIN роля.

---

## Използвани технологии

### Основно приложение

- Java 21
- Spring Boot 3.4.0
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA
- Hibernate
- Spring Cloud OpenFeign
- Spring Cache
- Jakarta Bean Validation
- MySQL
- Maven
- Bootstrap 5
- JUnit 5
- Mockito
- MockMvc
- H2 за изолирани integration тестове
- JaCoCo

### Progress Microservice

- Java 21
- Spring Boot 3.4.0
- Spring Web / REST
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- MySQL
- Maven
- JUnit 5
- Mockito
- MockMvc
- H2 за тестове
- JaCoCo

---

## Архитектура

FitPulse използва две независими Spring Boot приложения, които работят на различни портове и използват отделни бази данни.

### Main Application

Основното приложение работи на:

```text
8080
```

То предоставя потребителския интерфейс и съдържа основната бизнес функционалност.

### Progress Service

Progress Service работи на:

```text
8081
```

Той предоставя REST API за управление на данните за физическия прогрес.

Комуникацията между двете приложения е реализирана чрез **Spring Cloud OpenFeign**.

Основното приложение извиква Progress Service за създаване, прочитане, редактиране и изтриване на progress записи.

---

## Domain Model

### Основно приложение

Основното приложение съдържа четири domain entities:

- `User`
- `Membership`
- `GymClass`
- `WorkoutBooking`

Всички използват **UUID** като primary key.

Моделът съдържа relationships между потребители, memberships, gym classes и workout bookings.

### Progress Service

Progress microservice-ът съдържа отделен domain entity:

- `ProgressRecord`

`ProgressRecord` също използва UUID и се съхранява в отделната база на Progress Service.

---

## Потребителски роли

FitPulse има две роли:

### MEMBER

Потребител с роля MEMBER може да:

- Се регистрира и влиза в системата
- Преглежда собствения си профил
- Редактира собственото си потребителско име и имейл
- Избира и променя своя membership
- Разглежда наличните тренировки
- Преглежда подробности за тренировка
- Записва се за позволени тренировки
- Отменя собствените си записвания
- Преглежда своите записвания
- Добавя progress записи
- Редактира progress записи
- Изтрива progress записи
- Преглежда историята на своя физически прогрес

### ADMIN

Администраторът има достъп до стандартните позволени функционалности и допълнително може да:

- Управлява собствените си progress записи и преглежда собствената си progress история

- Създава membership планове
- Редактира membership планове
- Изтрива membership планове
- Създава тренировки
- Редактира тренировки
- Изтрива тренировки
- Преглежда потребителите
- Променя ролята на друг потребител между `MEMBER` и `ADMIN`

Текущият администратор не може сам да понижи собствената си ADMIN роля.

---

## Управление на Memberships

Администраторите могат да:

- Създават membership планове
- Редактират membership планове
- Изтриват membership планове

Потребителите могат да избират и променят своя membership.

Membership business правилата се проверяват в service слоя.

---

## Управление на тренировки

Администраторите могат да:

- Създават тренировки
- Редактират тренировки
- Изтриват тренировки

Всяка тренировка съдържа информация като:

- Заглавие
- Треньор
- Начален час
- Капацитет
- Интензивност
- Необходимо membership ниво
- Описание

---

## Записване за тренировки

Автентикираните потребители могат да:

- Се записват за позволени тренировки
- Отменят собствените си записвания
- Преглеждат своите записвания

Приложението предотвратява:

- Записване без избран membership
- Записване при недостатъчно membership ниво
- Повторно записване за една и съща тренировка
- Записване при запълнен капацитет
- Отмяна на чуждо записване

---

## Fitness Progress REST Microservice

Функционалността за проследяване на физическия прогрес е реализирана като отделен Spring Boot REST microservice.

Repository:

https://github.com/nikiG9595/fitpulse-progress-service

Progress Service съхранява `ProgressRecord` данните независимо от основната FitPulse база.

Всеки progress запис съдържа информация, включително:

- User ID
- Тегло
- Процент телесни мазнини
- Дата на измерването
- Бележка
- Дата на създаване
- Дата на последна промяна

### REST API

Progress Service предоставя операции, включително:

```text
POST /api/progress
PUT /api/progress/{id}
DELETE /api/progress/{id}
GET /api/progress/user/{userId}
```

Основното приложение извиква тези endpoints чрез **Spring Cloud OpenFeign**.

Потребителят работи през интерфейса на основното FitPulse приложение, а Progress Service извършва реалното съхранение и промяна на progress данните.

---

## Сигурност

Основното приложение използва **Spring Security**.

Реализирани са:

- Session-based authentication
- BCrypt password hashing
- Role-based authorization
- Роли `MEMBER` и `ADMIN`
- Публични endpoints
- Endpoints за автентикирани потребители
- ADMIN-only endpoints
- CSRF защита
- Защита на потребителския профил
- Управление на роли от администратор

Паролите се съхраняват като BCrypt hash, а не като plain text.

CSRF защитата остава активна за state-changing web requests.

---

## Потребителски профил

Автентикираният потребител може да преглежда и редактира собствения си профил.

Могат да се редактират:

- Потребителско име
- Имейл

Чувствителни или защитени свойства като:

- Роля
- Парола
- User ID

не се подават през формата за редактиране на профила.

Membership-ът се променя чрез отделната membership функционалност.

---

## Валидация

FitPulse прилага validation на няколко нива.

### DTO Validation

Формите и API request DTO-тата използват Jakarta Bean Validation.

### Entity Validation

Domain entities използват подходящи ограничения, включително:

- `@NotNull`
- `@NotBlank`
- `@Email`
- `@Size`
- `@Min`
- `@Max`
- `@DecimalMin`
- `@Future`
- `@PastOrPresent`

### Service Validation

Бизнес правилата, които изискват application state, authentication информация или достъп до базата, се проверяват в service слоя.

Примери:

- Уникалност на username и email
- Уникалност на membership
- Капацитет на тренировките
- Membership eligibility
- Booking ownership
- Предотвратяване на duplicate bookings
- Правила за управление на роли
- Проверка за съществуване на ресурси

---

## Обработка на грешки

И двете Spring Boot приложения имат централизирана обработка на грешки.

Основното приложение обработва custom application exception:

```text
FitPulseException
```

както и built-in Spring/database exceptions, включително:

```text
MethodArgumentTypeMismatchException
DataIntegrityViolationException
```

Потребителят получава разбираеми съобщения и custom error pages, без да се показват stack traces, SQL съобщения или вътрешни database details.

Progress Service също има validation и централизирана обработка на REST грешки.

---

## Scheduling

Основното приложение има два scheduled jobs.

### Expired Booking Cleanup

Scheduled job с **CRON expression** периодично премахва записванията към тренировки, чието начално време вече е минало.

### Past Class Cleanup

Scheduled job с **fixed delay** премахва минали тренировки, след като вече нямат активни bookings.

Scheduled компонентите делегират реалната state-changing business логика към service слоя.

---

## Caching

Използва се Spring Cache за membership каталога.

`MembershipService.getAll()` кешира списъка с memberships, тъй като той се използва често от различни потребители и се променя сравнително рядко.

Кешът съдържа immutable membership view данни вместо mutable JPA entities.

Кешът се инвалидира след:

- Създаване на membership
- Редактиране на membership
- Изтриване на membership

Промяната на membership-а на конкретен потребител не инвалидира каталога, защото самите membership планове не се променят.

---

## Logging

Основните state-changing операции имат logging в service слоя.

Това включва:

- Създаване на membership
- Редактиране на membership
- Изтриване на membership
- Избор на membership
- Създаване на gym class
- Редактиране на gym class
- Изтриване на gym class
- Създаване на booking
- Отказване на booking
- Регистрация на потребител
- Промяна на роли
- Редактиране на профил
- Progress create
- Progress update
- Progress delete
- Scheduled cleanup операции

Чувствителни данни като пароли не се записват в логовете.

---

## Тестване

И двете приложения имат автоматизирани тестове.

Използват се:

- Unit tests
- Integration tests
- API / MockMvc tests
- Security tests
- Validation tests
- Service tests
- Repository tests

Основното приложение съдържа реален Spring Boot integration test с изолирана H2 база и реални service/repository компоненти.

Progress Service също съдържа unit, integration и API тестове.

За измерване на test coverage се използва **JaCoCo**.

HTML отчетът се генерира в:

```text
target/site/jacoco/index.html
```

И двата проекта са тествани над изисквания минимум от **70% line coverage**.

Пълната проверка може да се стартира с:

```bash
mvn clean verify
```

---

## Бази данни

Main Application и Progress Service използват **отделни MySQL бази данни**.

### Main Application Database

```text
fitpulse
```

### Progress Service Database

```text
fitpulse_progress
```

И двете приложения използват:

- Spring Data JPA
- Hibernate
- UUID primary keys

Database credentials трябва да бъдат конфигурирани за локалната среда преди стартиране на приложенията.

---

## Author

**Nikolay Ganchev**

Individual Project – Spring Advanced @ SoftUni
