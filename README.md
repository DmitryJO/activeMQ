# ActiveMQ dictionary service

Java-микросервис на Spring Boot: получает JSON из очереди `dictionaries.queue`,
сохраняет справочники в PostgreSQL и опционально отправляет их во внешний API.

## Требования

- JDK 17 или новее; переменная `JAVA_HOME` указывает на JDK.
- Docker с Docker Compose для локального ActiveMQ и PostgreSQL.
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

Ответ `202 Accepted` подтверждает отправку. Через несколько секунд проверьте запись:

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/dictionaries/CONNECTION_TEST" |
    Where-Object { $_.code -eq $code }
```

Появление записи подтверждает всю цепочку: публикация, получение из очереди и запись в БД.
