# TestTaskBot

## Описание проекта
TestTaskBot - это Telegram-бот, который использует PostgreSQL в качестве базы данных и написан на Java с использованием Spring Boot. Проект запускается в контейнерах Docker и использует `docker-compose` для управления сервисами.

## Стек технологий
- **Java 17**
- **Spring Boot**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Telegram API**

## Требования для запуска
Перед запуском убедитесь, что у вас установлены:
- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Сборка и развертывание проекта

1. **Клонировать репозиторий:**
   ```sh
   git clone https://github.com/fiodop/TestTaskBot.git
   cd TestTaskBot
   ```

2. **Создать `.env` файл (если необходимо)**
   Если в будущем понадобится хранить переменные окружения отдельно, можно создать `.env` файл.

3. **Запустить контейнеры Docker:**
   ```sh
   docker-compose up --build
   ```
   Флаг `--build` необходим при первом запуске или при изменениях в коде.

4. **Проверить запущенные контейнеры:**
   ```sh
   docker ps
   ```
   Должны быть запущены два контейнера: `pg_bot_db` (PostgreSQL) и `app` (Spring Boot приложение).

5. **Бот должен быть доступен в Telegram после старта контейнеров.**

## Остановка контейнеров

Для остановки запущенных контейнеров выполните:
```sh
   docker-compose down
```

## Возможные проблемы и их решения

1. **Ошибка: "invalid mount path: 'pg_bot_db' mount path must be absolute"**
   - Убедитесь, что `volumes` в `docker-compose.yml` настроены правильно (именованный volume, а не bind mount).
   - Если нужно использовать папку в проекте, укажите полный путь.

2. **Ошибка подключения к БД:**
   - Проверьте логи контейнера PostgreSQL:
     ```sh
     docker logs pg_bot_db
     ```
   - Убедитесь, что переменные `POSTGRES_USER`, `POSTGRES_PASS`, `POSTGRES_DB` указаны без пробелов (`=` без пробелов!).

