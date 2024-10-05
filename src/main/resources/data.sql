SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE FILMS_GENRES;

TRUNCATE TABLE FRIENDS;

TRUNCATE TABLE LIKES;

TRUNCATE TABLE FILMS RESTART IDENTITY;

TRUNCATE TABLE USERS RESTART IDENTITY;


TRUNCATE TABLE GENRES RESTART IDENTITY;

TRUNCATE TABLE RATINGS RESTART IDENTITY;

TRUNCATE TABLE STATUSES RESTART IDENTITY;

SET REFERENTIAL_INTEGRITY TRUE;


INSERT INTO RATINGS (RATING_NAME, DESCRIPTION)
VALUES ('G','У фильма нет возрастных ограничений'),
    ('PG','Детям рекомендуется смотреть фильм с родителями'),
    ('PG-13','Детям до 13 лет просмотр не желателен'),
    ('R','Лицам до 17 лет просматривать фильм можно только в присутствии взрослого'),
    ('NC-17','Лицам до 18 лет просмотр запрещён');
ALTER TABLE RATINGS ALTER COLUMN RATING_ID RESTART WITH 6;

INSERT INTO GENRES (GENRE_NAME)
VALUES ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');
    ALTER TABLE GENRES ALTER COLUMN GENRE_ID RESTART WITH 7;

    INSERT INTO STATUSES (STATUS_NAME)
    VALUES ('Подтверждённая'),
        ('Неподтверждённая');
    ALTER TABLE STATUSES ALTER COLUMN STATUS_ID RESTART WITH 3;