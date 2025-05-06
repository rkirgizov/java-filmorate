## Схема данных проекта java-filmorate

![java-filmorate-DB](./images/java-filmorate-DB.png)
[* ссылка на файл](./images/java-filmorate-DB.png)

## Примеры SQL-запросов

### Выгрузка фильмов по определенному жанру
```
SELECT f.*
FROM Film f
JOIN FilmGenre fg ON f.id = fg.film_id
JOIN Genre g ON g.id = fg.genre_id
WHERE g.name = '&genre';
```

### Выгрузка фильмов по определенному рейтингу
```
SELECT f.*
FROM Film f
JOIN Rating r ON f.id = r.rating_id
WHERE r.name = '&rating';
```

### Подсчёт лайков у фильма по его названию
```
SELECT COUNT(*) AS like_count
FROM FilmUserLike fl
JOIN Film f ON fl.id = f.film_id
WHERE f.name = '&name';
```

### Выгрузка фильмов, которые понравились определенному пользователю
```
SELECT f.*
FROM Film f
JOIN FilmUserLike fl ON f.id = fl.film_id
JOIN User u ON fl.user_id = u.id
WHERE u.login = '&login';
```

### Выгрузка друзей пользователя
```
SELECT u.id, u.name
FROM User u
JOIN UserFriend uf ON u.id = uf.friend_id
WHERE uf.user_id = &userId;
```

### Список общих друзей &user1 и &user2
```
SELECT u.id, u.name
FROM User u
JOIN UserFriend uf ON u.id = uf.friend_id
WHERE uf.user_id IN (&user1Id, &user2Id)
GROUP BY u.id, u.name
HAVING COUNT(uf.user_id) = 2;
```
## Описание таблиц

```
### User
id PK int 
email varchar(50) UNIQUE 
login varchar(50) UNIQUE 
name varchar(50) 
birthday_dt date

### UserFriend
id PK int 
user_id int FK >- User.id 
friend_id int FK >- User.id 
status_id int FK >- FriendshipStatus.id

### FriendshipStatus
id PK int 
name varchar(20)

### Film
id PK int 
rating_id int FK >- Rating.id 
name varchar(50) 
description varchar(255) 
release_dt date

### FilmUserLike
id PK int 
user_id int FK >- User.id 
film_id int FK >- Film.id

### Rating
id PK int 
name varchar(20)

### FilmGenre
id PK int 
film_id int FK >- Film.id 
genre_id int FK >- Genre.id

### Genre
id PK int 
name varchar(20)
```