
# AI Fact-Checker Agent

Проект представляет собой ИИ-агента для факт-чека новостей. Используется **Koog** для создания ИИ-агента и **Spring Boot** для REST API.

---

## 🔧 Требования

- Java 17+
- Gradle
- Docker & Docker Compose

---

## 🔑 Настройки окружения

Для работы ИИ агента создайте файл `.env` в корне проекта со следующими переменными:

```dotenv
# Ключ для Koog / ИИ агента
AGENTS_API_KEY=sk-proj-xxx-xxx
```

Веб-поиск выполняется через MCP-сервис `web-search-mcp` (SearXNG), поднимаемый в Docker Compose, — отдельных ключей не требует.

---

## 🚀 Запуск

1. Сборка проекта:

    ```bash
    ./gradlew build -x test
    ````

2. Запуск через Docker Compose:

    ```bash
    docker compose up --build -d
    ```

   API будет доступно по адресу: `http://127.0.0.1:80`

---

## 📡 Взаимодействие с API

### Факт-чек новости

**Endpoint:**

```
GET /fact/check?news=новость
```

**Параметры:**

| Параметр | Описание                   |
|----------|----------------------------|
| news     | Текст новости для проверки |

**Пример запроса:**

```bash
curl "http://127.0.0.1:80/fact/check?news=Some news text"
```

**Ответ:**

```json
{
  "isReliable": true,
  "explanation": "This news is verified based on multiple sources.",
  "sources": [
    "https://example.com/source1",
    "https://example.com/source2"
  ]
}
```

---

## ⚙️ Структура проекта

```
src/main/kotlin/com/yooshyasha/factcheckerpet/
 ├─ FactCheckerPetApplication.kt      # main
 ├─ agent/
 │   ├─ common/                       # провайдеры и общие инструменты
 │   │   └─ AgentProvider.kt
 │   └─ fact/checking/                # ИИ агент для факт-чекинга
 │       ├─ FactCheckingAgentProvider.kt
 │       └─ FactCheckingTools.kt
 ├─ controller/CheckController.kt     # REST контроллер
 ├─ dto/                              # модели данных
 │   ├─ FactCheckResult.kt
 │   ├─ News.kt
 │   └─ RequestFactCheck.kt
 └─ service/                          # сервисы
     └─ FactCheckingService.kt

```

* `FactCheckController` — REST API endpoint
* `FactCheckService` — логика взаимодействия с Koog ИИ агентом

---

## 📝 Примечания

* Для корректной работы Koog требуется подключение к интернету.
* API может возвращать статус `uncertain`, если ИИ не может определить достоверность.
