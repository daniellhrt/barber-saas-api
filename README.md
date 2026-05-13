# Barber SaaS API

REST API backend para o sistema de gestão de barbearias (Barber SaaS). Construído com Spring Boot, PostgreSQL e JWT para autenticação.

## Tecnologias

- **Framework:** Spring Boot 3.x
- **Linguagem:** Java 17+
- **Banco de Dados:** PostgreSQL (Neon em produção, Docker Compose para local)
- **Autenticação:** JWT (JSON Web Tokens)
- **Migrações:** Flyway
- **Build:** Maven
- **Containerização:** Docker

## Pré-requisitos

- Java 17+ instalado
- Maven 3.8+ (ou use `./mvnw`)
- Docker e Docker Compose (para rodar PostgreSQL localmente)
- Git

## Configuração Local

### 1. Clonar o repositório

```bash
git clone https://github.com/daniellhrt/barber-saas-api.git
cd barber-saas-api
```

### 2. Configurar variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto. Você pode usar `.env.example` como referência:

```bash
cp .env.example .env
```

Editea `.env` com seus valores:

```env
DB_URL=jdbc:postgresql://localhost:5432/barbersaas_db
DB_USER=postgres
DB_PASS=seu_password_seguro
DB_NAME=barbersaas_db
JWT_SECRET=gere_uma_chave_forte_com_openssl_rand_hex_32
JWT_EXPIRATION_MS=86400000
SPRING_PROFILES_ACTIVE=dev
PORT=8080
```

#### Gerar JWT_SECRET seguro

No PowerShell ou Git Bash:

```bash
# PowerShell
$bytes = New-Object byte[] 32; (New-Object System.Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes); [System.BitConverter]::ToString($bytes) -replace '-'

# Git Bash / OpenSSL
openssl rand -hex 32
```

### 3. Iniciar o banco de dados

Com Docker Compose:

```bash
docker-compose up -d
```

Isso iniciará um container PostgreSQL. Confirme que está rodando:

```bash
docker ps
```

### 4. Compilar e rodar a aplicação

**Opção A: Usando Maven Wrapper**

```bash
./mvnw spring-boot:run
```

**Opção B: Usando Maven instalado globalmente**

```bash
mvn spring-boot:run
```

A aplicação iniciará em `http://localhost:8080`.

### 5. Validar a aplicação

Teste um endpoint público (login):

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

Ou verifique o health check:

```bash
curl http://localhost:8080/actuator/health
```

## Rodar Testes

```bash
./mvnw test
```

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/com/daniel/danbarbersaasapi/
│   │   ├── controllers/          # REST endpoints
│   │   ├── domain/               # Entidades e DTOs
│   │   ├── repository/           # Data access (Spring Data JPA)
│   │   ├── services/             # Lógica de negócio
│   │   ├── security/             # JWT, authentication, authorization
│   │   ├── infra/exception/      # Global exception handlers
│   │   └── DanbarberSaasApiApplication.java
│   └── resources/
│       ├── application.yml       # Configuração (usa env vars)
│       └── db/migration/         # Flyway migrations
└── test/                         # Testes unitários/integração
```

## API Endpoints Principais

### Autenticação

- `POST /auth/login` - Fazer login (público)
- `POST /auth/register` - Registrar novo usuário (público)

### Clientes

- `GET /clients` - Listar clientes (autenticado)
- `POST /clients` - Criar cliente (autenticado)
- `PUT /clients/{id}` - Atualizar cliente (autenticado)

### Barbeiros

- `GET /barbers` - Listar barbeiros (autenticado)
- `POST /barbers` - Criar barbeiro (autenticado, ADMIN)

### Serviços

- `GET /services` - Listar serviços (autenticado)
- `POST /services` - Criar serviço (autenticado, ADMIN)

### Pedidos/Comendas

- `POST /orders` - Criar pedido/comenda (autenticado)
- `GET /orders/{clientId}` - Histórico de comendas do cliente (autenticado)

## Segurança

### Gerenciamento de Segredos

**IMPORTANTE:** Nunca commite segredos (senhas, chaves JWT) no repositório.

1. **Localmente**: Use arquivo `.env` (ignorado por `.gitignore`).
2. **CI/CD (GitHub Actions)**: Configure secrets em Settings → Secrets and Variables → Actions:
   - `DB_URL`
   - `DB_USER`
   - `DB_PASS`
   - `JWT_SECRET`
3. **Produção**: Use Secret Manager do seu provider (AWS Secrets Manager, GCP Secret Manager, etc.).

### Rotacionar Credenciais

Quando houver suspeita de vazamento:

1. **JWT Secret:**
   - Gere uma nova chave: `openssl rand -hex 32`
   - Atualize em `.env` (local) e em GitHub Secrets/Secret Manager (prod)
   - Usuários com tokens antigos precisarão fazer login novamente.

2. **DB Password:**
   - Altere a senha no painel do provedor (Neon, AWS RDS, etc.)
   - Atualize em `.env` e em GitHub Secrets/Secret Manager
   - Reinicie o container Docker se usar docker-compose localmente: `docker-compose down && docker-compose up -d`

### Boas Práticas de Segurança

- Sempre use HTTPS em produção.
- Mantenha JWT_SECRET com pelo menos 32 caracteres aleatórios.
- Use roles (`ADMIN`, `BARBER`, `CLIENT`) para controlar acesso.
- Valide entrada (todos os DTOs usam `@Valid`).
- Inspecione logs para atividades suspeitas.

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/barbersaas_db` | URL de conexão PostgreSQL |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASS` | (vazio) | Senha do banco |
| `DB_NAME` | `barbersaas_db` | Nome do banco |
| `JWT_SECRET` | `change-me-in-production` | Chave para assinar JWT |
| `JWT_EXPIRATION_MS` | `86400000` | Expiração do token em ms (24h padrão) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile ativo (dev, test, prod) |
| `PORT` | `8080` | Porta HTTP |

## Troubleshooting

### Erro: "JAVA_HOME not set"

Configure JAVA_HOME:

**PowerShell:**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

**Git Bash:**
```bash
export JAVA_HOME="/c/Program Files/Java/jdk-17"
```

### Erro: "Database connection refused"

- Confirme que Docker Compose está rodando: `docker ps`
- Confirme as credenciais em `.env`
- Verifique logs do container: `docker logs barbersaas_db_container`

### Erro: "Invalid JWT token"

- Token expirou (aguarde 24h ou faça login novamente).
- JWT_SECRET local não corresponde ao usado para gerar o token.
- Token foi alterado ou corrompido.

## CI/CD

Este projeto inclui suporte para GitHub Actions (configure em `.github/workflows/`):

- Build e testes em cada push/PR
- Análise estática de código
- Publicação de imagem Docker em caso de release

## Licença

MIT

## Contato

Para dúvidas ou contribuições, abra uma issue ou pull request no repositório.

---

**Última atualização:** Maio 2026

