**1. Розроблені сутності**

Проєкт працює з чотирма сутностями, які забезпечують нормалізацію даних та підтримку складних зв'язків, як-от Many-to-Many.

| Сутність | Роль | Ключові Зв'язки | Атрибути |
| :--- | :--- | :--- | :--- |
| **Пісня (Song)** | **Основна(Сутність 1)** | **Many-to-One $\rightarrow$ Artist, Album. Many-to-Many $\leftrightarrow$ Genre.** |`id`, `title`, `artist`, `duration`, `album` `release_year`, `genres` |
| **Виконавець (Artist)** | **Другорядна(Сутність 2)** | **One-to-Many $\rightarrow$ Song, Album.** | `id`, `name`, `songs`, `albums` |
| **Жанр (Genre)** | **Допоміжна** | **Many-to-Many $\leftrightarrow$ Song. (Створена для атомарності даних)** | `id`, `name`, `songs`,  |
| **Альбом (Album)** | **Допоміжна**  | **One-to-Many $\rightarrow$ Song. Many-to-One $\rightarrow$ Artist. (Запобігає дублюванню інформації)** | `id`, `name`, `songs`, `artist`|

**2. Інструкція збірки та запуску проекту**

2.1. Запуск Бази Даних (PostgreSQL у Docker)

Команда запуску сервісу бази даних:

    docker-compose up -d

2.2. Збірка та Запуск Додатка

Команда запуску додатку через Maven:

    mvn clean spring-boot:run

Додаток буде доступний на http://localhost:8080.

**3. API Ендпоінти**

| Сутність | Метод | Шлях | Опис |
| :--- | :--- | :--- | :--- |
| **Song** | POST| /api/song | Створити нову пісню. Приймає повний об'єкт SaveSongDto. |
| **Song** | GET | /api/song/{id} | Отримати детальну інформацію про пісню за її ID. |
| **Song** | PUT | /api/song/{id} | Оновити пісню за її ID. Приймає повний об'єкт SaveSongDto. |
| **Song** | DELETE | /api/song/{id} | Видалити пісню за її ID. |
| **Song** | POST | /api/song/_list | Отримати пагінований список пісень із динамічною фільтрацією (SongFilter). |
| **Song** | POST | /api/song/_report | Згенерувати та завантажити CSV-звіт на основі критеріїв фільтраці (SongFilter). |
| **Song** | POST | /api/song/upload | Масове завантаження пісень із файлу JSON (MultipartFile). Повертає статистику успішних/невдалих записів. |
| **Artist** | GET | /api/artist | Отримати список усіх виконавців. |
| **Artist** | POST | /api/artist |  Створити нового виконавця. Приймає SaveArtistDto.|
| **Artist** | PUT | /api/artist/{id} | Оновити інформацію про виконавця за його ID. Приймає SaveArtistDto. |
| **Artist** | DELETE | /api/artist/{id} | Видалити виконавця за його ID. |
| **Genre** | GET | /api/genre | Отримати список усіх жанрів. |
| **Genre** | POST | /api/genre | Створити новий жанр. Приймає об'єкт SaveGenreDto. |
| **Genre** | PUT | /api/genre/{id} | Оновити назву жанру за його ID. Приймає об'єкт SaveGenreDto. |
| **Genre** | DELETE | /api/genre/{id} | Видалити жанр за його ID. |
| **Album** | GET | /api/album/{id} | Отримати детальну інформацію про альбом за його ID, включаючи пов'язаного артиста. |
| **Album** | POST | /api/album | Створити новий альбом. Приймає SaveAlbumDto. |
| **Album** | PUT | /api/album/{id} | Оновити інформацію про альбом за його ID. Приймає SaveAlbumDto. |
| **Album** | DELETE | /api/album/{id} | Видалити альбом за його ID. |

**4. Отримання ID для JSON-запитів**

4.1. Виконайте команду для знаходження контейнеру з бд:

    docker ps | grep 5433

4.2. Підключіться до консолі psql: (Використовуйте знайдений <ID_контейнера> та облікові дані user/music_db)

    docker exec -it <ID_контейнера> psql -U user music_db

4.3. Виконайте SQL-запити для отримання ID

У консолі psql виконайте наступні команди, щоб переглянути існуючі ідентифікатори:

    -- Переглянути всі артисти та їхні ID
    SELECT id, name FROM artists; 
    
    -- Переглянути всі жанри та їхні ID
    SELECT id, name FROM genres;
    
    -- Переглянути всі альбоми та їхні ID
    SELECT id, name FROM albums;
    
    -- Вийти з консолі psql
    \q

**5. Формування JSON для масового завантаження**

Ендпоінт POST /api/song/upload очікує файл, що містить JSON-масив об'єктів. Кожен об'єкт у цьому масиві повинен відповідати структурі SaveSongDto.

    [
      {
        "title": "Назва Пісні 1",
        "artistId": "UUID_ІСНУЮЧОГО_АРТИСТА",
        "releaseYear": 2022,
        "duration": 245,
        "albumId": "UUID_ІСНУЮЧОГО_АЛЬБОМУ",
        "genresId": [
          "UUID_ЖАНРУ_1", 
          "UUID_ЖАНРУ_2"
        ]
      },
      {
        "title": "Назва Пісні 2",
        "artistId": "UUID_ІНШОГО_АРТИСТА",
        "releaseYear": 2018,
        "duration": 310,
        "albumId": "UUID_ІНШОГО_АЛЬБОМУ",
        "genresId": [
          "UUID_ЖАНРУ_3"
        ]
      }
    ]

**6. Запуск тестів**

Інтеграційні тести використовують бібліотеку Testcontainers, яка динамічно створює та запускає контейнер PostgreSQL під час самого тесту. Це означає, що обов'язково має бути запущена програма Docker Desktop (Docker Daemon), щоб Testcontainers міг керувати життєвим циклом контейнера.

**6.1. Запустіть Docker Desktop**

**6.2. Виконайте команду для запуску тестів**

    mvn test
