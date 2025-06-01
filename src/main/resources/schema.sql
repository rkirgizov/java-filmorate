-- Для теста
DROP TABLE IF EXISTS _user CASCADE;
DROP TABLE IF EXISTS _user_friend CASCADE;
DROP TABLE IF EXISTS _friend_status CASCADE;
DROP TABLE IF EXISTS _film CASCADE;
DROP TABLE IF EXISTS _like CASCADE;
DROP TABLE IF EXISTS _mpa CASCADE;
DROP TABLE IF EXISTS _film_genre CASCADE;
DROP TABLE IF EXISTS _genre CASCADE;
DROP TABLE IF EXISTS _film_director CASCADE;
DROP TABLE IF EXISTS _director CASCADE;
DROP TABLE IF EXISTS _review CASCADE;
DROP TABLE IF EXISTS _review_rating CASCADE;

CREATE TABLE IF NOT EXISTS _user (
    id int PRIMARY KEY AUTO_INCREMENT,
    login varchar(50) UNIQUE,
    email varchar(50) UNIQUE,
    name varchar(50),
    birthday_dt date
);

CREATE TABLE IF NOT EXISTS _user_friend (
    id int PRIMARY KEY AUTO_INCREMENT,
    user_id int NOT NULL,
    friend_id int NOT NULL,
    status_id int NOT NULL
);

CREATE TABLE IF NOT EXISTS _friend_status (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS _film (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(50) NOT NULL,
    description varchar(255) NOT NULL,
    duration int NOT NULL,
    release_dt date NOT NULL,
    mpa_id int
);

CREATE TABLE IF NOT EXISTS _like (
    id int PRIMARY KEY AUTO_INCREMENT,
    user_id int NOT NULL,
    film_id int NOT NULL
);

CREATE TABLE IF NOT EXISTS _mpa (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS _film_genre (
    id int PRIMARY KEY AUTO_INCREMENT,
    film_id int NOT NULL,
    genre_id int NOT NULL
);

CREATE TABLE IF NOT EXISTS _genre (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS _director (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS _film_director (
    id int PRIMARY KEY AUTO_INCREMENT,
    film_id INT NOT NULL,
    director_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS _review (
    id int PRIMARY KEY AUTO_INCREMENT,
    content     TEXT              NOT NULL,
    is_positive BOOLEAN           NOT NULL,
    user_id     BIGINT            NOT NULL,
    film_id     BIGINT            NOT NULL,
    useful      INTEGER DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS _review_rating (
    id int PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT  NOT NULL,
    user_id   BIGINT  NOT NULL,
    is_like   BOOLEAN NOT NULL
);





--ALTER TABLE _user_friend ADD CONSTRAINT IF NOT EXISTS fk_user_friend_user_id FOREIGN KEY (user_id)
--REFERENCES _user (id);
--
--ALTER TABLE _user_friend ADD CONSTRAINT IF NOT EXISTS fk_user_friend_friend_id FOREIGN KEY (friend_id)
--REFERENCES _user (id);
--
--ALTER TABLE _user_friend ADD CONSTRAINT IF NOT EXISTS fk_user_friend_status_id FOREIGN KEY (status_id)
--REFERENCES _friend_status (id);
--
--ALTER TABLE _film ADD CONSTRAINT IF NOT EXISTS fk_film_mpa_id FOREIGN KEY (mpa_id)
--REFERENCES _mpa (id);
--
--ALTER TABLE _like ADD CONSTRAINT IF NOT EXISTS fk_like_user_id FOREIGN KEY (user_id)
--REFERENCES _user (id);
--
--ALTER TABLE _like ADD CONSTRAINT IF NOT EXISTS fk_like_film_id FOREIGN KEY (film_id)
--REFERENCES _film (id);
--
--ALTER TABLE _film_genre ADD CONSTRAINT IF NOT EXISTS fk_film_genre_film_id FOREIGN KEY (film_id)
--REFERENCES _film (id);
--
--ALTER TABLE _film_genre ADD CONSTRAINT IF NOT EXISTS fk_film_genre_genre_id FOREIGN KEY (genre_id)
--REFERENCES _genre (id);
