# ActiveMQ dictionary service

Java-микросервис на Spring Boot: получает JSON из очереди `dictionaries.queue`,
сохраняет справочники в PostgreSQL и опционально отправляет их во внешний API.

## Требования

- JDK 17 или новее; переменная `JAVA_HOME` указывает на JDK.
- Доступный ActiveMQ Classic. Docker с Docker Compose нужен только для запуска
  локального брокера и PostgreSQL из `docker-compose.yml`.
- Maven Wrapper включён в проект; отдельная установка Maven не требуется.
  Для первого запуска нужен доступ к Maven Central.

## Сборка и тесты

В PowerShell из папки проекта:

```powershell
.\mvnw.cmd clean verify
```

Тесты используют встроенный ActiveMQ и H2, поэтому Docker для них не нужен.
Исполняемый JAR: `target/activeMQ-0.0.1-SNAPSHOT.jar`.

## Запуск

### Только ActiveMQ, без БД

Включите профиль `amq-only`. Он отключает подключение к БД, JPA и внешний API.
Слушатель выводит тело каждого входящего текстового сообщения в лог; формат JSON
справочника не обязателен. Сообщения подтверждаются и удаляются из очереди после
обработки — используйте отдельную тестовую очередь, если нужно сохранить рабочие данные.
Этот профиль не сохраняет сообщения в БД для последующей обработки.

Для запуска из IDEA откройте **Run → Edit Configurations**, выберите конфигурацию
`ActiveMqApplication` и добавьте в **Program arguments** (при необходимости через
**Modify options**):

```text
--spring.profiles.active=amq-only
```

В `application.yml` укажите `spring.activemq.broker-url`, `user`, `password` и
`app.jms.dictionary-queue`. Значения `spring.datasource` при этом профиле не используются.
Для удалённого брокера вместо `localhost` нужен его адрес.
Реальные учётные данные удобнее задавать через переменные окружения конфигурации
запуска IDEA: `SPRING_ACTIVEMQ_BROKER_URL`, `SPRING_ACTIVEMQ_USER`,
`SPRING_ACTIVEMQ_PASSWORD`, `APP_JMS_DICTIONARY_QUEUE`.

Запуск через PowerShell с теми же переменными окружения:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=amq-only"
```

Либо после сборки:

```powershell
java -jar target/activeMQ-0.0.1-SNAPSHOT.jar --spring.profiles.active=amq-only
```

Для проверки используйте POST из раздела «Проверка обмена сообщениями» ниже.
В консоли приложения должна появиться строка:

```text
Received ActiveMQ message (amq-only): {"type":"CONNECTION_TEST",...}
```

`POST /api/dictionaries/publish` доступен в обоих режимах и по-прежнему принимает
JSON с полями `type`, `code`, `name`. GET-эндпоинты чтения справочников в режиме
`amq-only` отключены и возвращают 404. Сам запуск приложения ещё не доказывает
подключение: если брокер недоступен, слушатель будет повторять попытки подключения.

### ActiveMQ и PostgreSQL

Без профиля `amq-only` приложение работает в прежнем режиме с записью в БД:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Либо после сборки:

```powershell
java -jar target/activeMQ-0.0.1-SNAPSHOT.jar
```

На Linux/macOS используйте `./mvnw` вместо `.\mvnw.cmd`.
При установленном Maven также можно использовать `mvn`.

- Приложение: http://localhost:8081
- ActiveMQ: `tcp://localhost:61616`
- Консоль брокера: http://localhost:8161/admin/ (локальные данные: `admin/admin`).
- PostgreSQL: `localhost:5434`, база `dictionaries`, пользователь/пароль `postgres/postgres`.

Настройки находятся в `src/main/resources/application.yml`.
Чтобы включить дополнительный HTTP POST во внешний API, перед запуском задайте
`$env:EXTERNAL_API_URL="http://host:port/path"`.

## Проверка обмена сообщениями

В другом окне PowerShell:

```powershell
$code = "TEST-" + [guid]::NewGuid().ToString("N")
$body = @{
    type = "CONNECTION_TEST"
    code = $code
    name = "Connection test"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/api/dictionaries/publish" `
    -Method Post -ContentType "application/json" -Body $body -UseBasicParsing
```

Ответ `202 Accepted` подтверждает отправку. В профиле `amq-only` проверьте лог
`Received ActiveMQ message (amq-only)` с вашим кодом.
В режиме с БД через несколько секунд проверьте запись:

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/dictionaries/CONNECTION_TEST" |
    Where-Object { $_.code -eq $code }
```

Появление записи подтверждает всю цепочку: публикация, получение из очереди и запись в БД.
