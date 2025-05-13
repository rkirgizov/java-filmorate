## Схема данных проекта java-filmorate

[Quick Database Diagrams](https://app.quickdatabasediagrams.com/#/)
![java-filmorate-DB](./images/java-filmorate-DB.png)
[* ссылка на файл в проекте](./images/java-filmorate-DB.png)

## Примеры SQL-запросов
### Выгрузка фильмов по определенному жанру
```
SELECT f.*
FROM film f
JOIN film_genre fg ON f.id = fg.film_id
JOIN genre g ON g.id = fg.genre_id
WHERE g.name = '&genre';
```
### Выгрузка фильмов по определенному рейтингу
```
SELECT f.*
FROM film f
JOIN rating r ON f.id = r.rating_id
WHERE r.name = '&rating';
```
### Подсчёт лайков у фильма по его названию
```
SELECT COUNT(*) AS like_count
FROM film_like fl
JOIN film f ON fl.id = f.film_id
WHERE f.name = '&name';
```
### Выгрузка фильмов, которые понравились определенному пользователю
```
SELECT f.*
FROM film f
JOIN film_like fl ON f.id = fl.film_id
JOIN user u ON fl.user_id = u.id
WHERE u.login = '&login';
```
### Выгрузка друзей пользователя
```
SELECT u.id, u.name
FROM user u
JOIN user_friend uf ON u.id = uf.friend_id
WHERE uf.user_id = &userId;
```
### Список общих друзей &user1 и &user2
```
SELECT u.id, u.name
FROM user u
JOIN user_friend uf ON u.id = uf.friend_id
WHERE uf.user_id IN (&user1Id, &user2Id)
GROUP BY u.id, u.name
HAVING COUNT(uf.user_id) = 2;
```
## Описание таблиц
```
user
----
id PK int 
login varchar(50) UNIQUE 
email varchar(50) UNIQUE 
name varchar(50) 
birthday_dt date

user_friend
----
id PK int 
user_id int FK >- user.id 
friend_id int FK >- user.id 
status_id int FK >- friend_status.id

friend_status
----
id PK int 
name varchar(20)

film
----
id PK int 
name varchar(50) 
description varchar(255) 
duration int
release_dt date
mpa_rating_id int FK >- mpa_rating.id

like
----
id PK int 
user_id int FK >- user.id 
film_id int FK >- film.id

mpa_rating
----
id PK int 
name varchar(20)

film_genre
----
id PK int 
film_id int FK >- film.id 
genre_id int FK >- genre.id

genre
----
id PK int 
name varchar(20)
```