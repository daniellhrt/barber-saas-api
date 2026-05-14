# Guia de Teste da Aplicação com Nova JWT Secret

## Status Atual
✅ Arquivo `.env` criado com:
- Nova JWT Secret: `63F1B2E03A7883AAC2EF9D2862FCF4B1919C410B750098316762003E2D8BEBCC`
- Credenciais Neon: `neondb_owner` / `npg_kMoGY1Rse4Iz`
- Banco de dados apontando para Neon (produção)

⚠️ Para testar, você tem duas opções:

## Opção A: Testar com Docker (Recomendado)

### Pré-requisitos
- Docker Desktop instalado e rodando
- docker-compose disponível

### Passos

1. **Iniciar Docker Desktop** (se não estiver rodando)
   - Windows: Procure por "Docker Desktop" no menu iniciar e clique para iniciar

2. **Iniciar o banco de dados PostgreSQL com docker-compose**
   ```powershell
   docker-compose up -d
   # Aguarde alguns segundos para o Postgres estar pronto
   docker ps  # Deve mostrar container "barbersaas_db_container" rodando
   ```

3. **Buildar a imagem Docker da aplicação**
   ```powershell
   docker build -t barber-saas-api:latest -f Dockerfile .
   # Isso vai compilar o projeto com Maven dentro do container
   # Primeira execução pode levar 5-10 minutos (download de dependências)
   ```

4. **Executar a aplicação com environment do .env**
   ```powershell
   # Windows PowerShell - carregar variáveis do .env
   $env:DB_URL = "jdbc:postgresql://ep-flat-voice-acnlajh4-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require"
   $env:DB_USER = "neondb_owner"
   $env:DB_PASS = "npg_kMoGY1Rse4Iz"
   $env:JWT_SECRET = "63F1B2E03A7883AAC2EF9D2862FCF4B1919C410B750098316762003E2D8BEBCC"
   
   # Rodar container da aplicação
   docker run -it --rm `
     -e DB_URL=$env:DB_URL `
     -e DB_USER=$env:DB_USER `
     -e DB_PASS=$env:DB_PASS `
     -e JWT_SECRET=$env:JWT_SECRET `
     -p 8080:8080 `
     barber-saas-api:latest
   ```

5. **Testar a aplicação** (em outra aba PowerShell)
   ```powershell
   # Health check
   curl http://localhost:8080/actuator/health
   # Deve retornar: {"status":"UP"}
   
   # Testar login (sem autenticação)
   curl -X POST http://localhost:8080/auth/login `
     -H "Content-Type: application/json" `
     -d '{"email":"test@example.com","password":"password123"}'
   # Se falharem os dados, tudo bem - o importante é que não dê 500 error
   ```

---

## Opção B: Instalar Java Localmente (Alternativa)

### Pré-requisitos
- Fazer download de JDK 17+ (https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
  - Ou usar OpenJDK (https://jdk.java.net/17/)

### Passos

1. **Instalar Java**
   - Windows: Execute o instalador `.exe` (recomendado: adicionar ao PATH durante instalação)

2. **Verificar instalação**
   ```powershell
   java -version
   javac -version
   ```

3. **Compilar e rodar testes**
   ```powershell
   # Na pasta do projeto
   ./mvnw clean compile
   ./mvnw test  # Rodar testes unitários
   ```

4. **Executar a aplicação**
   ```powershell
   # Opção 1: Spring Boot
   ./mvnw spring-boot:run
   
   # Opção 2: Compilar JAR e rodar
   ./mvnw clean package -DskipTests
   java -jar target/danbarber-saas-api-*.jar
   ```

   A aplicação iniciará em `http://localhost:8080`

5. **Testar** (como na Opção A)

---

## Testes Manuais (curl)

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```
**Esperado:** `{"status":"UP"}`

### 2. Listar Clientes (requer autenticação)
```bash
# Primeiro, faça login (substitua email/password por dados reais)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"seu_email@example.com","password":"sua_senha"}'

# Resposta exemplo:
# {"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}

# Use o token em requisições subsequentes:
curl -H "Authorization: Bearer <token_aqui>" \
  http://localhost:8080/clients
```

### 3. Registrar Novo Usuário
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"novo@example.com",
    "password":"Senha123!",
    "role":"CLIENT"
  }'
```

### 4. Criar Cliente
```bash
# Com token obtido de login:
curl -X POST http://localhost:8080/clients \
  -H "Authorization: Bearer <token_aqui>" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"João Silva",
    "phone":"11999999999",
    "email":"joao@example.com"
  }'
```

---

## Troubleshooting

### Docker não inicia
- Verifique se Docker Desktop está instalado: https://www.docker.com/products/docker-desktop
- Reinicie Docker Desktop
- Verifique recurso WSL 2 no Windows (Docker usa WSL2)

### Aplicação não conecta ao banco
- Verifique se URL do Neon está correta no `.env`
- Confirme credenciais (usuário/senha) no painel do Neon
- Teste conexão com `psql` ou pgAdmin:
  ```powershell
  # Se tiver psql instalado: Teste a conectividade diretamente com Neon
  # A URL de conexão está em: .env DB_URL
  ```

### JWT Secret não funciona
- Verifique se o valor foi copiado corretamente
- Certifique-se de que não há espaços extras: 
  ```powershell
  Get-Content '.env' | Select-String 'JWT_SECRET'
  ```

### Tokens antigos não funcionam
- ✅ Isso é esperado! Quando você troca JWT_SECRET, tokens antigos ficam inválidos
- Usuários precisam fazer login novamente
- Isso é mecanismo de segurança (revogação instantânea)

---

## Próximos Passos Após Testes Bem-Sucedidos

1. **Fazer commit** da branch `fix/security-hardening` para o repositório principal
2. **Deploy em produção** com a nova JWT Secret configurada em GitHub Secrets/Secret Manager
3. **Comunicar aos usuários** que precisam fazer login novamente (nova chave JWT)
4. **(Opcional) Rotacionar senha do Neon** no painel (para evitar comprometimentos anteriores)

---

**Data:** May 14, 2026
**Status:** Pronto para teste
**JWT Secret Gerado:** `63F1B2E03A7883AAC2EF9D2862FCF4B1919C410B750098316762003E2D8BEBCC`

