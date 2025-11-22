# Russian Sentiment Analysis API


Микросервис для анализа тональности русскоязычных текстов с использованием машинного обучения и развертыванием в Kubernetes кластере.


##  Возможности

- **Анализ тональности** - автоматическое определение эмоциональной окраски текста (positive/negative/neutral)
- **Русский язык** - специализированная модель для анализа русскоязычного контента
- **REST API** - стандартизированные HTTP endpoints для интеграции
- **Контейнеризация** - полная поддержка Docker и Kubernetes
- **Мониторинг** - встроенные метрики и health checks через Spring Boot Actuator
- **Балансировка нагрузки** - готовность к горизонтальному масштабированию
- **Производительность** - низкая задержка и высокая пропускная способность


### Технологический стек

- **Backend**: Java 17, Spring Boot 3.2.0
- **Build Tool**: Maven 3.6+
- **Контейнеризация**: Docker, Docker Compose
- **Оркестрация**: Kubernetes, Minikube
- **Мониторинг**: Spring Boot Actuator, Prometheus metrics
- **API**: RESTful JSON API

## ⚡ Быстрый старт

### Требования

- Java 17 или выше
- Maven 3.6 или выше
- Docker (опционально)
- Minikube (опционально)

### Локальный запуск за 5 минут

```bash
# Клонирование репозитория
git clone <repository-url>
cd sentiment-app

# Сборка приложения
mvn clean package

# Запуск
java -jar target/sentiment-app-1.0.0.jar

# Проверка работы
curl "http://localhost:8080/api/health"
```

##  API Документация

### Базовый URL
```
http://localhost:8080/api
```

### Endpoints

#### 1. Анализ тональности
```http
GET /sentiment?text={текст}
```

**Параметры:**
- `text` (обязательный) - текст для анализа

**Пример запроса:**
```bash
curl -G "http://localhost:8080/api/sentiment" \
  --data-urlencode "text=Это отлично и прекрасно работает"
```

**Пример ответа:**
```json
{
  "sentiment": "positive",
  "confidence": 0.92,
  "text": "Это отлично и прекрасно работает",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

#### 2. Проверка здоровья
```http
GET /health
```

**Ответ:**
```json
{
  "status": "UP",
  "service": "Russian Sentiment Analysis API",
  "version": "1.0.0",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

#### 3. Информация о модели
```http
GET /model/info
```

**Ответ:**
```json
{
  "positive_words": 36,
  "negative_words": 35,
  "neutral_words": 32,
  "total_words": 103,
  "model_loaded": true
}
```

#### 4. Отладочная информация
```http
GET /debug?text={текст}
```

**Ответ:**
```json
{
  "original_text": "Отлично но плохо",
  "cleaned_text": "отлично но плохо",
  "words": ["отлично", "но", "плохо"],
  "matched_words": [
    {
      "word": "отлично",
      "category": "positive",
      "weight": 1.0
    },
    {
      "word": "плохо", 
      "category": "negative",
      "weight": 0.8
    }
  ],
  "analysis_result": {
    "sentiment": "positive",
    "confidence": 0.6,
    "text": "Отлично но плохо",
    "timestamp": "2024-01-15T10:30:00.000Z"
  }
}
```

#### 5. Тестовые сценарии
```http
GET /test
```

##  Локальная разработка

### Настройка окружения

1. **Установите Java 17:**
```bash
# На macOS с Homebrew
brew install openjdk@17

# На Ubuntu
sudo apt install openjdk-17-jdk

# Проверка установки
java -version
```

2. **Установите Maven:**
```bash
# На macOS
brew install maven

# На Ubuntu
sudo apt install maven

# Проверка установки
mvn -version
```

### Запуск в IDE

1. Откройте проект в IntelliJ IDEA
2. Убедитесь, что используете JDK 17
3. Запустите `SentimentApplication.java`
4. Приложение будет доступно на `http://localhost:8080`

### Тестирование

```bash
# Запуск unit-тестов
mvn test

# Запуск с генерацией отчета о покрытии
mvn jacoco:report

# Интеграционное тестирование
mvn verify
```

##  Docker

### Сборка образа

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/sentiment-app-1.0.0.jar app.jar

RUN groupadd -r spring && useradd -r -g spring spring
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

```bash
# Сборка образа
docker build -t sentiment-app:1.0.0 .

# Запуск контейнера
docker run -d -p 8080:8080 --name sentiment-api sentiment-app:1.0.0

# Просмотр логов
docker logs -f sentiment-api
```

### Docker Compose

Создайте `docker-compose.yml`:

```yaml
version: '3.8'

services:
  sentiment-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
```

Запуск:
```bash
docker-compose up -d
```

##  Kubernetes развертывание

### Требования

- Minikube 1.20+
- kubectl 1.21+

### Настройка Minikube

```bash
# Запуск Minikube кластера
minikube start --memory=4096 --cpus=2

# Включение необходимых аддонов
minikube addons enable ingress
minikube addons enable metrics-server

# Настройка Docker environment
eval $(minikube docker-env)
```

### Манифесты Kubernetes

#### deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sentiment-app
  labels:
    app: sentiment-app
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: sentiment-app
  template:
    metadata:
      labels:
        app: sentiment-app
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
      - name: sentiment-app
        image: sentiment-app:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: JAVA_OPTS
          value: "-Xmx512m -Xms256m"
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        securityContext:
          runAsNonRoot: true
          runAsUser: 1000
```

#### service.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: sentiment-service
  labels:
    app: sentiment-app
spec:
  selector:
    app: sentiment-app
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  type: LoadBalancer
```

#### ingress.yaml
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: sentiment-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  rules:
  - http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: sentiment-service
            port:
              number: 80
```

#### horizontal-pod-autoscaler.yaml
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: sentiment-app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: sentiment-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### Развертывание

```bash
# Сборка Docker образа в Minikube environment
docker build -t sentiment-app:1.0.0 .

# Применение манифестов
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/horizontal-pod-autoscaler.yaml

# Мониторинг развертывания
kubectl get pods -w
kubectl get services
kubectl get ingress

# Получение URL для доступа
minikube service sentiment-service --url
```

##  Модель данных

### Структура словаря

Модель использует словарный подход с весами для каждого слова.


### Алгоритм анализа

1. **Предобработка текста:**
    - Приведение к нижнему регистру
    - Удаление знаков препинания
    - Токенизация

2. **Анализ слов:**
    - Поиск слов в словаре
    - Суммирование весов по категориям
    - Нормализация scores

3. **Классификация:**
    - Выбор категории с максимальным score
    - Расчет confidence уровня
