## Схема данных проекта java-filmorate

[Quick Database Diagrams](https://app.quickdatabasediagrams.com/#/)
![java-filmorate-DB](./images/java-filmorate-DB.png)
[* ссылка на файл в проекте](./images/java-filmorate-DB.png)

## Примеры SQL-запросов
### Пользователи
```
Поиск по id         = "SELECT * FROM _user WHERE id = ?";
Поиск всех          = "SELECT * FROM _user";
Добавление          = "INSERT INTO _user (login,email,name,birthday_dt) VALUES (?,?,?,?)";
Обновление          = "UPDATE _user SET login = ?, email = ?, name = ?, birthday_dt = ? WHERE id = ?";
Удаление            = "DELETE FROM _user WHERE id = ?";
Добавление друга    = "INSERT INTO _user_friend (user_id, friend_id, status_id) SELECT ?, ?, ?";
Удаление друга      = "DELETE FROM _user_friend WHERE (user_id = ? AND friend_id = ?)";
Поиск друзей        = "SELECT u.*, fs.name AS friendship_status FROM _user u " +
                        "JOIN _user_friend uf ON u.id = uf.friend_id " +
                        "JOIN _friend_status fs ON uf.status_id = fs.id " +
                        "WHERE uf.user_id  = ?";
```
### Фильмы
```
Поиск по id         = " SELECT * FROM _film WHERE id = ?";
Поиск всех          = "SELECT * FROM _film";
Добавление          = "INSERT INTO _film (name,description,duration,release_dt,mpa_id) VALUES (?,?,?,?,?)";
Добавление          = "INSERT INTO _film_genre (film_id, genre_id) VALUES (?,?)";
Удаление            = "DELETE FROM _film_genre WHERE film_id = ?";
Обновление          = "UPDATE _film SET name = ?, description = ?, duration = ?, release_dt = ?, mpa_id = ? WHERE id = ?";
Добавление лайка    = "INSERT INTO _like (user_id, film_id) VALUES (?,?)";
Удаление лайка      = "DELETE FROM _like WHERE user_id = ? AND film_id = ?";
Популярные фильмы   = "SELECT f.* FROM _film f
                        LEFT JOIN _like l ON f.id = l.film_id
                        GROUP BY f.id
                        HAVING COUNT(l.user_id) > 0
                        ORDER BY COUNT(l.user_id) DESC
                        LIMIT ?";
```
## Используемые таблицы
```
_user
----
id PK int 
login varchar(50) UNIQUE 
email varchar(50) UNIQUE 
name varchar(50) 
birthday_dt date

_user_friend
----
id PK int 
user_id int FK >- _user.id 
friend_id int FK >- _user.id 
status_id int FK >- _friend_status.id

_friend_status
----
id PK int 
name varchar(20)

_film
----
id PK int 
name varchar(50) 
description varchar(255) 
duration int
release_dt date
mpa_id int FK >- _mpa.id

_like
----
id PK int 
user_id int FK >- _user.id 
film_id int FK >- _film.id

_mpa
----
id PK int 
name varchar(20)

_film_genre
----
id PK int 
film_id int FK >- _film.id 
genre_id int FK >- _genre.id

_genre
----
id PK int 
name varchar(20)
```