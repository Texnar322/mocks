# WireMock Standalone + Prometheus Metrics

Standalone версия WireMock с экспортом метрик в формате Prometheus.

Проект собирается в один fat JAR и не требует Docker для запуска.

Метрики включают:

* статистику запросов к заглушкам
* latency / processing time
* JVM metrics
* process metrics

---

## Стек

| Компонент                  | Версия |
| -------------------------- | ------ |
| WireMock                   | 3.13.2 |
| wiremock-metrics extension | 1.0.4  |
| Maven                      | 3.9+   |
| Java                       | 17+    |

---

## Структура проекта

```text
wiremock-standalone-prometheus/
├── pom.xml
├── mappings/
│   └── weather.json
├── __files/
└── target/
    └── wiremock-prometheus-standalone.jar
```

---

## Сборка проекта

### Требования

Проверьте наличие Java:

```bash
java -version
```

Проверьте наличие Maven:

```bash
mvn -version
```

---

### Сборка fat JAR

```bash
mvn clean package
```

После сборки появится файл:

```text
target/wiremock-prometheus-standalone.jar
```

---

## Запуск

```bash
java -jar target/wiremock-prometheus-standalone.jar \
  --port 8080 \
  --root-dir . \
  --verbose \
  --extensions=com.rasklaad.wiremock.metrics.PrometheusMetricsExtension,com.rasklaad.wiremock.metrics.MetricsEndpointExtension
```

### Параметры

| Параметр       | Описание                         |
| -------------- | -------------------------------- |
| `--port`       | Порт WireMock                    |
| `--root-dir`   | Папка с `mappings` и `__files`   |
| `--verbose`    | Подробные логи                   |
| `--extensions` | Подключение Prometheus extension |

---

## Проверка работы

| Ресурс                | Адрес                                              |
| --------------------- | -------------------------------------------------- |
| Заглушка погоды       | `http://localhost:8080/weather/msk`                |
| Метрики Prometheus    | `http://localhost:8080/__admin/prometheus-metrics` |
| Панель администратора | `http://localhost:8080/__admin`                    |

---

## Метрики

Endpoint:

```text
http://localhost:8080/__admin/prometheus-metrics
```

Метрики WireMock:

```text
wiremock_request_totalTime_ms
wiremock_request_processingTime_ms
wiremock_request_serveTime_ms
wiremock_request_responseSendTime_ms
```

JVM метрики:

```text
jvm_memory_max_bytes
jvm_threads_peak_threads
process_cpu_usage
```

---

## Подключение к Prometheus

Добавьте в `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'wiremock'

    metrics_path: '/__admin/prometheus-metrics'

    static_configs:
      - targets:
          - 'localhost:8080'
```

> Важно: endpoint метрик нестандартный — `/__admin/prometheus-metrics`, а не `/metrics`.

---

## Добавление заглушек

Создайте `.json` файл в папке `mappings/`.

Пример:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/users"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "users": [
        {
          "id": 1,
          "name": "Alice"
        },
        {
          "id": 2,
          "name": "Bob"
        }
      ]
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

WireMock автоматически подхватывает новые mappings без перезапуска сервера.

---

## Возможность сменить библиотеку метрик

Если потребуется использовать другую реализацию метрик:

1. Создайте папку:

```text
extlib/
```

2. Положите туда нужный extension JAR.

3. Запустите WireMock через classpath:

```bash
java -cp "target/wiremock-prometheus-standalone.jar:extlib/*" \
  org.wiremock.standalone.WireMockServerRunner \
  --port 8080 \
  --root-dir . \
  --extensions=ваш.класс.расширения
```

Таким образом:

* основной standalone JAR остаётся неизменным
* библиотека метрик может заменяться отдельно
* можно подключать собственные extensions

---

## Пример mappings/weather.json

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/weather/msk"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "city": "Moscow",
      "temp": 20,
      "condition": "sunny"
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

---

## Зависимости

* WireMock — mock server
* wiremock-metrics — Prometheus metrics extension
* Micrometer
* Prometheus Java Client

---

## License

MIT
