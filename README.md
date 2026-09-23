# DoPlus

Курсовая работа по дисциплине "Технологии разработки качественного программного обеспечения"

Было создано клиент-серверное приложение для управления проектами и отслеживания задач.
## Стек технологий
* Java - используемый язык программирования для серверной части
* Java Spring - фреймворк для написания бэкенда
* JavaScript - используемый язык программирования для клиентской части
* ReactJS - фреймворк для написания фронтенда
* Axios - HTTP клиент для связи фронтенда с бэкэндом
* SQLite - база данных
* JUnit, Moсkito, Jest, Cypress - тестирование

## Архитектура приложения

![Image](https://github.com/AlexStpm/DoPlus/raw/main/images/images/Arch.png)

## Схема базы данных

В базе данных имеются таблицы:
* Tasks - таблица, содержащая информацию о всех задачах
* TaskTags - таблица для реализации связи Many-to-Many между задачами и их тэгами
* Tags - таблица, содержащая информацию о всех тэгах
* Boards - таблица, содержащая информацию о всех досках
* BoardMembers - таблица, содержащая информацию о пользователях доски
* Users - таблица, содержащая информацию о всех пользователях
* Roles - таблица, содержащая информацию о всех ролях
* Notifications - таблица, содержащая информацию о всех уведомлениях
* TimeManagment - таблица, содержащая информацию о всех записях временного учёта


![Image](https://github.com/AlexStpm/DoPlus/raw/main/images/images/DB.png)

## Общая схема бэкенда приложения

Controller - контроллер, предназначенный для обработки REST запросов.

Service - сервис, предназначенный для обслуживания бизнес-логики.

Dao - DAO

Классы, описывающие базу данных и методы для работы с ней (с помощью Spring Data):
* User - таблица пользователей
* Board - таблица досок
* Task - таблица задач
* Tag - таблица тэгов
* Notification - таблица, хранящая уведомления
* TimeManagmant - таблица с записями о затраченном времени

Классы, описывающие содержание форм запросов и используемые для десериализации JSON:
* UpdateTimeManagmentRequest - описывает форму обновления записи затраченного времени
* CreateTimeManagmentRequest - описывает форму создания записи затраченного времени
* CreateNotificationRequest - описывает форму создания уведомления при назначении задачи пользователю
* CreateTagRequest - описывает форму добавления тэга
* UpdateTaskRequest - описывает форму обновления задачи
* CreateTaskRequest - описывает форму создания задачи
* UpdateUserRequest - описывает форму обновления пользователя
* CreateUserRequest - описывает форму создания пользователя

Классы, описывающие содержание каждой отдельной страницы приложения и используемые для сериализация этих данных в JSON:
* AdminUserView - описывает AdminUserView
* UserView - описывает UserView
* BoardView - описывает BoardView
* TaskView - описывает TaskView
* TagView - описывает TagView
* NotificationView - описывает NotificationView
* TimeManagmentView - описывает TimeManagmentView

![Image](https://github.com/AlexStpm/DoPlus/raw/main/images/images/ALL.png)

### DAO Layer

![Image](https://github.com/AlexStpm/DoPlus/raw/main/images/images/DAO.png)

### Service Layer

![Image](https://github.com/AlexStpm/DoPlus/raw/main/images/images/Service.png)

### Controller Layer

![Image](https://github.com/AlexStpm/DoPlus/raw/main/images/images/Controller.png)
