# Estágio 1: Build (Onde o Maven trabalha)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia apenas o pom.xml primeiro para aproveitar o cache das dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Agora copia o código fonte e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Run (Imagem final, leve, apenas para rodar o app)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o arquivo .jar gerado no estágio anterior
# Verifique se o nome do arquivo no target é exatamente esse
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]