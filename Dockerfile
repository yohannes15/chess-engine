# === Stage 1: Backend (sbt assembly) ===
FROM eclipse-temurin:21-jdk AS backend-builder
WORKDIR /build

# Install sbt 1.12.8
RUN curl -fsSL https://github.com/sbt/sbt/releases/download/v1.12.8/sbt-1.12.8.tgz \
    | tar xz -C /usr/local --strip-components=1

# Cache dependencies (replay only when project/* or build.sbt changes)
COPY project/ project/
COPY build.sbt ./
RUN sbt update

# Build the fat JAR
COPY src/ src/
RUN sbt assembly

# === Stage 2: Frontend (Vite build) ===
FROM node:22-alpine AS frontend-builder
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# === Stage 3: Runtime image ===
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy artifacts from build stages
COPY --from=backend-builder /build/target/scala-3.8.3/*.jar app.jar
COPY --from=frontend-builder /build/dist/ frontend-dist/

ENV FRONTEND_DIST=/app/frontend-dist

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
