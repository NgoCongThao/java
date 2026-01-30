# --- GIAI ĐOẠN 1: BUILD (Dùng Maven để đóng gói code thành file .jar) ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy file cấu hình thư viện trước (để tận dụng cache)
COPY pom.xml .
# Tải thư viện về (Bước này tốn thời gian nhất nên làm riêng)
RUN mvn dependency:go-offline -B

# Copy toàn bộ code nguồn vào
COPY src ./src

# Chạy lệnh build (bỏ qua test để nhanh hơn)
RUN mvn clean package -DskipTests

# --- GIAI ĐOẠN 2: RUN (Dùng bản Java nhẹ để chạy) ---
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy file .jar đã build ở Giai đoạn 1 sang đây
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Lệnh chạy App khi khởi động Container
ENTRYPOINT ["java", "-jar", "app.jar"]