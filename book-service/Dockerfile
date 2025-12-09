# --- Aşama 1: Build (Derleme) ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Sadece pom.xml'i kopyala
COPY pom.xml .

# 2. Bağımlılıkları indir (Bu katman CACHE'lenecek)
# Kodun değişse bile, pom.xml değişmediği sürece burası çalışmaz, hafızadan gelir.
RUN mvn dependency:go-offline

# 3. Kaynak kodları kopyala ve derle
COPY src ./src
RUN mvn clean package -DskipTests

# --- Aşama 2: Run (Çalıştırma) ---
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]