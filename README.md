## Projektbeschreibung

Das Smart Library Backend digitalisiert den Betrieb einer Bibliothek über eine cloud-native, microservice-basierte Architektur. Mitglieder können Bücher suchen, ausleihen und reservieren. Das System verwaltet Bestände, sendet Benachrichtigungen bei Rückgabeterminen und ermöglicht Staff-Admins den vollständigen Überblick alles über REST-APIs.

> Dieses Projekt wurde im Rahmen der Module Cloud Computing (Sommersemester 2026) an der Fachhochschule Erfurt entwickelt und implementiert.
---

## Architektur

```
                          ┌─────────────┐
                          │   Traefik   │  API Gateway :80
                          │  (Gateway)  │  Dashboard  :8080
                          └──────┬──────┘
               ┌─────────────────┼─────────────────┐
               ▼                 ▼                   ▼
    ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
    │  Book Catalog    │ │  User & Loan     │ │  Notification    │
    │    Service       │ │    Service       │ │    Service       │
    │    :8081         │ │    :8082         │ │    :8083         │
    └────────┬─────────┘ └────────┬─────────┘ └────────┬─────────┘
             │                    │                      │
             ▼                    ▼                      ▼
        ┌─────────┐          ┌─────────┐         ┌──────────────┐
        │ book-db │          │ user-db │         │notification- │
        │Postgres │          │Postgres │         │     db       │
        │  :5432  │          │  :5433  │         │   MongoDB    │
        └─────────┘          └─────────┘         └──────────────┘
                                    │
                          ┌─────────▼──────────┐
                          │      Redpanda       │
                          │  (Kafka-kompatibel) │
                          │  :9092 / :29092     │
                          └─────────────────────┘
```

### Services

| Service | Port | Datenbank | Beschreibung |
|---|---|---|---|
| `book-catalog-service` | 8081 | PostgreSQL | Bücher, ISBN, Verfügbarkeit |
| `user-loan-service` | 8082 | PostgreSQL | Nutzer, Ausleihen, Rückgaben |
| `notification-service` | 8083 | MongoDB | Event-basierte Benachrichtigungen |

---

## Tech Stack

| Bereich | Technologie |
|---|---|
| Sprache | Kotlin 2.x |
| Framework | Quarkus 3.36.1 (Reactive) |
| Datenbank | PostgreSQL 16 (Hibernate Reactive Panache) |
| Messaging | Redpanda (Kafka-kompatibel) |
| Container | Docker + Docker Compose + Kubernetes (Minikube) |
| API Gateway | Traefik v3 |
| Monitoring | Prometheus + Grafana |
| Tracing | Jaeger (OpenTelemetry) |
| CI/CD | GitLab CI/CD (`.gitlab-ci.yml`) |
| Tests | JUnit 5 + Mockito Kotlin + RestAssured |
| Code Coverage | JaCoCo |

---

## Code Coverage

Alle Services erreichen eine Instruction Coverage von über 95% (Framework-generierter Code wie Panache-Entities wird standardmäßig ausgeschlossen):

| Service | Coverage |
|---|----------|
| `book-catalog-service` | **98 %** |
| `user-loan-service` | **95 %** |
| `notification-service` | **95 %**|

Coverage-Report erzeugen:
```bash
./gradlew clean test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## Voraussetzungen

- Java 21+
- Docker Desktop (muss laufen für Tests und Docker Compose)
- Quarkus CLI (`brew install quarkusio/tap/quarkus`)
- Gradle 9+
- Minikube (für Kubernetes-Deployment)
- kubectl

---

## Bekannte Konfigurationshinweise

### settings.gradle.kts — alle 3 Services

Die `by settings`-Delegation funktioniert in neueren Gradle-Versionen nicht. Jede `settings.gradle.kts` muss so aussehen:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "book-catalog-service"  // je nach Service anpassen
```

### Notification Service — Port in Produktion

Der Notification Service läuft im Dev-Modus auf Port 8080, in Docker aber auf 8083. Daher muss in `notification-service/src/main/resources/application.properties` stehen:

```properties
%prod.quarkus.http.port=8083
```

---

## Schnellstart

### Lokale Entwicklung (Dev Mode)

Jeder Service kann einzeln im Entwicklungsmodus gestartet werden. Datenbanken starten automatisch via Docker (Quarkus Dev Services):

```bash
# Repository klonen
git clone https://git.ai.fh-erfurt.de/<group>/smart-library-backend.git
cd smart-library-backend

# Terminal 1 — Book Catalog Service (Port 8081)
cd book-catalog-service
./gradlew quarkusDev

# Terminal 2 — User & Loan Service (Port 8082)
cd ../user-loan-service
./gradlew quarkusDev

# Terminal 3 — Notification Service (Port 8083)
cd ../notification-service
./gradlew quarkusDev
```

> **Hinweis:** Docker Desktop muss laufen, da Dev Services automatisch PostgreSQL-Container starten.

### Swagger UI & Endpunkte

| Endpoint | URL |
|---|---|
| Swagger UI (Catalog) | http://localhost:8081/q/swagger-ui |
| Swagger UI (Loan) | http://localhost:8082/q/swagger-ui |
| Health (Catalog) | http://localhost:8081/q/health |
| Health (Loan) | http://localhost:8082/q/health |
| Health (Notification) | http://localhost:8083/q/health |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (PW: admin) |
| Jaeger | http://localhost:16686 |
| Traefik Dashboard | http://localhost:8080 |

### Alle Services mit Docker Compose

```bash
# Infrastruktur starten (DBs + Kafka)
cd infrastructure
docker compose up book-db user-db notification-db redpanda -d

# Alles starten inkl. Observability (nach docker build)
docker compose up -d

# Logs beobachten
docker compose logs -f
```

---

## API-Referenz

### 1. Book Catalog Service (Port 8081)

| Method | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/books` | Alle Bücher abrufen |
| `GET` | `/books/{id}` | Buch nach ID abrufen |
| `POST` | `/books` | Neues Buch anlegen |
| `PATCH` | `/books/{id}/availability` | Verfügbarkeit ändern |
| `DELETE` | `/books/{id}` | Buch löschen |

Beispiel: Buch anlegen

```bash
curl -X POST http://localhost:8081/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "available": true
  }'
```

Beispiel: Verfügbarkeit ändern (Ausleihe)

```bash
curl -X PATCH "http://localhost:8081/books/1/availability?available=false"
```

### 2. User & Loan Service (Port 8082)

| Method | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/loans` | Alle Ausleihen abrufen |
| `GET` | `/loans/{id}` | Ausleihe nach ID abrufen |
| `POST` | `/loans` | Neue Ausleihe anlegen |
| `PATCH` | `/loans/{id}/return` | Buch zurückgeben |
| `GET` | `/loans/user/{userId}` | Aktive Ausleihen eines Nutzers |
| `GET` | `/loans/book/{bookId}` | Aktive Ausleihen eines Buches |
| `DELETE` | `/loans/{id}` | Ausleihe löschen |

Beispiel: Ausleihe anlegen

```bash
curl -X POST http://localhost:8082/loans \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "bookId": 1}'
```

Beispiel: Buch zurückgeben

```bash
curl -X PATCH http://localhost:8082/loans/1/return
```

### 3. Notification Service (Port 8083)

Event-basiert via Kafka. REST-Endpunkte:

| Method | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/hello` | Lebenszeichen |
| `GET` | `/q/health` | Health Check |

---

## Projektstruktur

```
smart-library-backend/
├── book-catalog-service/          ← Amanullah
│   ├── src/main/kotlin/de/fhe/smartlibrary/catalog/
│   │   ├── api/BookResource.kt
│   │   ├── application/BookService.kt
│   │   ├── domain/Book.kt
│   │   └── infrastructure/BookEntity.kt
│   ├── src/test/kotlin/
│   │   ├── BookServiceTest.kt
│   │   ├── BookResourceTest.kt
│   │   └── infrastructure/BookEntityTest.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── Dockerfile
│
├── user-loan-service/             ← Ronald
│   ├── src/main/kotlin/de/fhe/smartlibrary/loan/
│   │   ├── api/LoanResource.kt
│   │   ├── application/LoanService.kt
│   │   ├── domain/Loan.kt
│   │   └── infrastructure/
│   │       ├── LoanEntity.kt
│   │       └── BookCatalogClient.kt
│   ├── src/test/kotlin/
│   │   ├── LoanServiceTest.kt
│   │   ├── LoanResourceTest.kt
│   │   └── infrastructure/
│   │       ├── LoanEntityTest.kt
│   │       └── BookCatalogClientTest.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── Dockerfile
│
├── notification-service/          ← Sohee
│   ├── src/main/kotlin/de/fhe/smartlibrary/
│   │   ├── GreetingResource.kt
│   │   ├── MyLivenessCheck.kt
│   │   └── notification/
│   │       ├── application/NotificationService.kt
│   │       ├── domain/Notification.kt
│   │       └── infrastructure/NotificationConsumer.kt
│   ├── src/test/kotlin/
│   │   ├── MyLivenessCheckTest.kt
│   │   ├── domain/NotificationTest.kt
│   │   └── infrastructure/NotificationConsumerTest.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── Dockerfile
│
├── infrastructure/
│   ├── docker-compose.yml
│   ├── monitoring/prometheus/prometheus.yml
│   └── kubernetes/
│       ├── kustomization.yaml
│       ├── book-catalog-service.yaml
│       ├── user-loan-service.yaml
│       ├── notification-service.yaml
│       ├── prometheus.yaml
│       ├── grafana.yaml
│       └── jaeger.yaml
│
└── .gitlab-ci.yml
```

---

## Docker Images bauen

```bash
# Book Catalog Service
cd book-catalog-service
./gradlew build -x test
docker build -t smart-library/book-catalog-service:1.0 .

# User & Loan Service
cd ../user-loan-service
./gradlew build -x test
docker build -t smart-library/user-loan-service:1.0 .

# Notification Service
cd ../notification-service
./gradlew build -x test
docker build -t smart-library/notification-service:1.0 .
```

---

## Tests ausführen

> **Hinweis:** Docker Desktop muss laufen. `quarkusDev` vorher stoppen (Tests und Dev Mode teilen denselben Port).

```bash
# Book Catalog Service
cd book-catalog-service
./gradlew clean test jacocoTestReport

# User & Loan Service
cd ../user-loan-service
./gradlew clean test jacocoTestReport

# Notification Service
cd ../notification-service
./gradlew clean test jacocoTestReport

# Coverage-Report öffnen
open build/reports/jacoco/test/html/index.html
```

---

## CI/CD — GitLab Pipeline

Pipeline läuft auf `git.ai.fh-erfurt.de` via `.gitlab-ci.yml`:

| Stage | Jobs | Beschreibung |
|---|---|---|
| `test` | test-catalog, test-loan, test-notification | Unit Tests aller Services |
| `build` | build-catalog, build-loan, build-notification | Docker Images (nur nach grünem Test) |

---

## Kubernetes Deployment (Minikube)

```bash
# 1. Images bauen und in Minikube laden
minikube image load smart-library/book-catalog-service:latest
minikube image load smart-library/user-loan-service:latest
minikube image load smart-library/notification-service:latest

# 2. Ingress aktivieren + deployen
minikube addons enable ingress
cd infrastructure/kubernetes
kubectl apply -k .

# 3. Tunnel + testen
minikube tunnel
curl http://localhost/books
```

---

## Observability

### Prometheus (http://localhost:9090)

Beispiel-Queries:
```
up                                    # Zeigt laufende Services
http_server_requests_seconds_count    # HTTP-Request-Zähler
```

### Grafana (http://localhost:3000)

1. Connections → Data Sources → Add new → Prometheus
2. URL: `http://prometheus:9090`
3. Save & Test → dann Explore → Metriken abfragen

### Jaeger (http://localhost:16686)

Traces entstehen automatisch bei HTTP-Requests. Vorher ein paar Requests schicken:

```bash
curl http://localhost:8081/books
curl http://localhost:8082/loans
```

Dann in Jaeger: Service auswählen → Find Traces.

---

## Clean Architecture

```
api/            → REST Layer
application/    → Business Logic
domain/         → Domain Model (keine Framework-Abhängigkeiten)
infrastructure/ → DB Layer, REST Clients, Kafka Consumer
```

## Hinweis zur Verwendung von KI

Für Inspiration, Fehlersuche, Formulierungshilfe und Dokumentation wurden KI-Modelle (Google Gemini Flash und Claude) unterstützend eingesetzt. Die Implementierung, Messungen und Kontrolle der Ergebnisse erfolgten vollständig eigenständig.
