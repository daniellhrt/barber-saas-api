# Estágio 1: Build (Construção do .jar)
# Usando o Maven com Java 17 (se você usar o Java 21, basta trocar o 17 por 21 abaixo)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml e baixa as dependências (ajuda no cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e compila ignorando os testes para ser mais rápido
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução na Nuvem)
# Usando uma imagem JRE super leve para economizar memória
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia apenas o arquivo .jar gerado no Estágio 1
COPY --from=build /app/target/*.jar app.jar

# Libera a porta e executa a aplicação
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]