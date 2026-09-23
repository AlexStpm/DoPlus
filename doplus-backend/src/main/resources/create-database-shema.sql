--
-- Файл сгенерирован с помощью SQLiteStudio v3.4.4 в Чт ноя 23 21:38:43 2023
--
-- Использованная кодировка текста: UTF-8
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Таблица: BoardMembers
CREATE TABLE IF NOT EXISTS BoardMembers (
    user_id       INTEGER REFERENCES Users (user_id) 
                          NOT NULL,
    board_id      INTEGER REFERENCES Boards (board_id) 
                          NOT NULL,
    role_on_board TEXT    NOT NULL
);


-- Таблица: Boards
CREATE TABLE IF NOT EXISTS Boards (
    board_id    INTEGER PRIMARY KEY AUTOINCREMENT
                        UNIQUE
                        NOT NULL,
    name        TEXT    NOT NULL,
    is_archived INTEGER NOT NULL
                        DEFAULT (0) 
                        CHECK (is_archived IN (0, 1) ) 
);


-- Таблица: Notifications
CREATE TABLE IF NOT EXISTS Notifications (
    notification_id INTEGER PRIMARY KEY AUTOINCREMENT
                            UNIQUE
                            NOT NULL,
    user_id         INTEGER REFERENCES Users (user_id) 
                            NOT NULL,
    task_id         INTEGER REFERENCES Tasks (task_id) ON DELETE CASCADE
                            NOT NULL,
    is_seen         INTEGER NOT NULL
                            CHECK (is_seen IN (0, 1) ) 
                            DEFAULT (0) 
);


-- Таблица: Roles
CREATE TABLE IF NOT EXISTS Roles (
    role_id INTEGER PRIMARY KEY AUTOINCREMENT
                    UNIQUE
                    NOT NULL,
    name    TEXT    NOT NULL
                    UNIQUE
);


-- Таблица: Tags
CREATE TABLE IF NOT EXISTS Tags (
    tag_id    INTEGER PRIMARY KEY AUTOINCREMENT
                      NOT NULL
                      UNIQUE,
    board_id  INTEGER REFERENCES Boards (board_id) 
                      NOT NULL,
    name      TEXT    NOT NULL,
    is_active INTEGER NOT NULL
                      DEFAULT (1) 
                      CHECK (is_active IN (0, 1) ) 
);


-- Таблица: Tasks
CREATE TABLE IF NOT EXISTS Tasks (
    task_id     INTEGER PRIMARY KEY ASC AUTOINCREMENT
                        NOT NULL
                        UNIQUE,
    assignee_id INTEGER REFERENCES Users (user_id),
    reporter_id INTEGER REFERENCES Users (user_id) 
                        NOT NULL,
    board_id    INTEGER NOT NULL
                        REFERENCES Boards (board_id),
    priority    TEXT,
    severity    TEXT,
    estimation  INTEGER,
    status      TEXT    NOT NULL,
    title       TEXT    NOT NULL,
    description TEXT,
    creation_time TEXT    NOT NULL
);


-- Таблица: TaskTags
CREATE TABLE IF NOT EXISTS TaskTags (
    task_id INTEGER NOT NULL
                    REFERENCES Tasks (task_id) ON DELETE CASCADE,
    tag_id  INTEGER NOT NULL
                    REFERENCES Tags (tag_id) 
);


-- Таблица: TimeManagment
CREATE TABLE IF NOT EXISTS TimeManagement (
    TimeManagement_id INTEGER PRIMARY KEY AUTOINCREMENT
                             UNIQUE
                             NOT NULL,
    user_id          INTEGER REFERENCES Users (user_id) 
                             NOT NULL,
    date_time        TEXT    NOT NULL,
    task_id          INTEGER REFERENCES Tasks (task_id) ON DELETE CASCADE
                             NOT NULL,
    description      TEXT    NOT NULL
);


-- Таблица: Users
CREATE TABLE IF NOT EXISTS Users (
    user_id    INTEGER PRIMARY KEY AUTOINCREMENT
                       UNIQUE
                       NOT NULL,
    username   TEXT    NOT NULL
                       UNIQUE,
    password   TEXT    NOT NULL,
    first_name TEXT    NOT NULL,
    last_name  TEXT    NOT NULL,
    role_id    INTEGER NOT NULL
                       REFERENCES Roles (role_id),
    is_active  INTEGER NOT NULL
                       CHECK (is_active IN (0, 1) ) 
                       DEFAULT (1) 
);


-- Таблица: Tokens
CREATE TABLE IF NOT EXISTS Tokens (
    token_id    INTEGER PRIMARY KEY AUTOINCREMENT
                    UNIQUE
                    NOT NULL,
    token   TEXT    NOT NULL
                    UNIQUE,
    user_id    INTEGER NOT NULL
                    REFERENCES Users (user_id)
    );

INSERT INTO Roles (role_id, name) VALUES (1, 'ROOTADMIN');
INSERT INTO Roles (role_id, name) VALUES (2, 'USER');

INSERT INTO Users (username, password, first_name, last_name, role_id, is_active)
VALUES ('rootadmin', '$2a$10$vfDJbR0IkujK3dJHchfDlO9h4U1W8zqLUFSExIEoLy2WHQqVBZE3C', 'Root', 'Admin', 1, 1); --password 123

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
