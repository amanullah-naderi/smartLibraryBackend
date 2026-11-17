# Kubernetes Deployment Guide (Smart Library Backend)

This directory contains the Kubernetes manifests required to run the Smart Library Backend on a local or cloud-based Kubernetes cluster.

## Architecture in Kubernetes

- **Databases**:
  - `book-db`: PostgreSQL for catalog storage.
  - `user-db`: PostgreSQL for user/loan storage.
  - `notification-db`: MongoDB for notification storage.
- **Messaging**:
  - `redpanda`: Redpanda (Kafka-compatible) for event streaming.
- **Microservices**:
  - `book-catalog-service`: REST API on port `8081`.
  - `user-loan-service`: REST API on port `8082`.
  - `notification-service`: REST API on port `8083`.
- **Ingress Route**:
  - Exposes `/books`, `/loans`, and `/hello` on a single external port.

---

## Local Setup & Deployment

### 1. Build Docker Images Locally

Before deploying, compile the services and build the container images:

```bash
# Book Catalog Service
cd book-catalog-service
./gradlew build -x test
docker build -t smart-library/book-catalog-service:latest .

# User & Loan Service
cd ../user-loan-service
./gradlew build -x test
docker build -t smart-library/user-loan-service:latest .

# Notification Service
cd ../notification-service
./gradlew build -x test
docker build -t smart-library/notification-service:latest .
```

### 2. Import Images to Your Local Cluster

#### Using k3d (Recommended):
Create a cluster and load the locally built images:
```bash
# Create k3d cluster with Ingress port mapping
k3d cluster create smart-library -p "8080:80@loadbalancer"

# Import images
k3d image import smart-library/book-catalog-service:latest -c smart-library
k3d image import smart-library/user-loan-service:latest -c smart-library
k3d image import smart-library/notification-service:latest -c smart-library
```

#### Using Minikube:
If you are using Minikube, load them via:
```bash
minikube image load smart-library/book-catalog-service:latest
minikube image load smart-library/user-loan-service:latest
minikube image load smart-library/notification-service:latest
```

---

### 3. Deploy to Kubernetes

Deploy all components in one command using `kubectl` with Kustomization:

```bash
cd infrastructure/kubernetes
kubectl apply -k .
```

### 4. Verify the Deployment

Check the status of all pods, services, and ingress routes:

```bash
# View pod status
kubectl get pods

# View services
kubectl get services

# View ingress configuration
kubectl get ingress
```

To test the services through the Ingress gateway (if running on `k3d` with port mapping to `localhost:8080`):

```bash
# Test book-catalog-service
curl http://localhost:8080/books

# Test user-loan-service
curl http://localhost:8080/loans

# Test notification-service
curl http://localhost:8080/hello
```
