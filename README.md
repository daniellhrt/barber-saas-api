<div align="center">

# ✂️ Barber SaaS API

**Plataforma completa de gestão para barbearias — back-end robusto, seguro e pronto para produção.**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Auth0-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

</div>

---

## 📋 Sobre o Projeto

**Barber SaaS API** é uma REST API back-end completa desenvolvida para gerenciar operações de barbearias de forma moderna e escalável. A API oferece controle total sobre clientes, barbeiros, serviços, pedidos e um módulo analítico avançado com dashboard e relatórios financeiros em tempo real.

> Projeto desenvolvido com foco em **boas práticas de desenvolvimento**, **segurança** e **arquitetura limpa**, utilizando o ecossistema Spring Boot com Java 21.

---

## ✨ Funcionalidades

| Módulo | Funcionalidades |
|--------|----------------|
| 🔐 **Autenticação** | Login e registro com JWT stateless, controle de acesso por roles |
| 👤 **Clientes** | Cadastro, listagem e atualização de clientes |
| 💈 **Barbeiros** | Gestão de barbeiros com controle de acesso ADMIN |
| 🛠️ **Serviços** | Catálogo de serviços oferecidos pela barbearia |
| 📦 **Produtos** | Gestão de produtos disponíveis |
| 🧾 **Pedidos** | Criação e histórico de ordens de serviço |
| 📊 **Dashboard** | KPIs em tempo real: faturamento do dia/mês/ano, ticket médio, crescimento |
| 📈 **Relatórios** | Análise completa por período, top barbeiros, top clientes, comparativo mensal |

---

## 🚀 Stack de Tecnologias

```
Back-end
├── Java 21 (LTS)
├── Spring Boot 4.x
│   ├── Spring Web MVC        → REST API
│   ├── Spring Data JPA       → ORM / persistência
│   ├── Spring Security       → Autenticação e autorização
│   └── Spring Validation     → Validação de DTOs
├── Auth0 Java JWT 4.4.0      → Geração e validação de tokens JWT
├── Flyway                    → Versionamento e migrações do banco
├── Lombok                    → Redução de boilerplate
└── PostgreSQL                → Banco de dados relacional

Infraestrutura
├── Docker + Docker Compose   → Ambiente local reproduzível
├── Maven Wrapper             → Build sem instalação global
└── Neon (PostgreSQL Cloud)   → Banco em produção
```

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas com **separação clara de responsabilidades**:

```
src/
└── main/
    └── java/.../danbarbersaasapi/
        ├── controllers/       → Endpoints REST (recebe e responde requisições)
        ├── services/          → Regras de negócio e lógica da aplicação
        ├── repository/        → Acesso ao banco via Spring Data JPA
        ├── domain/            → Entidades JPA e DTOs de request/response
        ├── security/          → Filtros JWT, configuração Spring Security
        └── infra/exception/   → Tratamento global de exceções
```

---

## 📡 API Endpoints

### 🔐 Autenticação
```http
POST /auth/register     → Registrar novo usuário
POST /auth/login        → Autenticar e receber JWT
```

### 👤 Clientes
```http
GET    /clients         → Listar todos os clientes
POST   /clients         → Criar novo cliente
PUT    /clients/{id}    → Atualizar dados do cliente
```

### 💈 Barbeiros
```http
GET    /barbers         → Listar barbeiros
POST   /barbers         → Criar barbeiro (ADMIN)
```

### 🛠️ Serviços & Produtos
```http
GET    /services        → Listar serviços
POST   /services        → Criar serviço (ADMIN)
GET    /products        → Listar produtos
POST   /products        → Criar produto (ADMIN)
```

### 🧾 Pedidos
```http
POST   /orders                   → Criar ordem de serviço
GET    /orders/{clientId}        → Histórico do cliente
```

### 📊 Dashboard & Relatórios
```http
GET    /dashboard/stats          → KPIs básicos do dia
GET    /dashboard/advanced       → Dashboard completo com comparativos e rankings
GET    /reports?period=today     → Relatório por período (today | week | month | year)
GET    /reports/comprehensive    → Relatório com análise de barbeiros e clientes
GET    /reports/by-period        → Relatório por período customizado
```

---

## ⚙️ Como Rodar Localmente

### Pré-requisitos

- ☕ Java 21+
- 🐳 Docker e Docker Compose
- 🔧 Git

### 1. Clonar o repositório

```bash
git clone https://github.com/daniellhrt/barber-saas-api.git
cd barber-saas-api
```

### 2. Configurar variáveis de ambiente

```bash
cp .env.example .env
```

Edite o `.env` com suas configurações:

```env
DB_URL=jdbc:postgresql://localhost:5432/barbersaas_db
DB_USER=postgres
DB_PASS=sua_senha_segura
DB_NAME=barbersaas_db
JWT_SECRET=gere_uma_chave_com_64_chars_minimo
JWT_EXPIRATION_MS=86400000
SPRING_PROFILES_ACTIVE=dev
PORT=8080
```

> 💡 **Gerar JWT_SECRET seguro:**
> ```bash
> # PowerShell
> $bytes = New-Object byte[] 32; (New-Object System.Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes); [System.BitConverter]::ToString($bytes) -replace '-'
>
> # Git Bash / Linux
> openssl rand -hex 32
> ```

### 3. Subir o banco de dados

```bash
docker-compose up -d
```

### 4. Executar a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080` 🎉

### 5. Verificar funcionamento

```bash
# Health check
curl http://localhost:8080/actuator/health

# Login de teste
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## 🔒 Segurança

O projeto implementa diversas práticas de segurança:

- ✅ **Autenticação JWT stateless** — sem sessões no servidor
- ✅ **Controle de acesso por roles** — `ADMIN`, `BARBER`, `CLIENT`
- ✅ **Validação de entrada** — todos os DTOs usam `@Valid` + Bean Validation
- ✅ **Segredos externalizados** — via variáveis de ambiente (nunca no código)
- ✅ **HTTPS recomendado** em produção
- ✅ **Rotação de credenciais** — suporte a troca de JWT_SECRET e DB password sem downtime

---

## 🌍 Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/barbersaas_db` | URL de conexão PostgreSQL |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASS` | _(vazio)_ | Senha do banco |
| `DB_NAME` | `barbersaas_db` | Nome do banco |
| `JWT_SECRET` | `change-me-in-production` | Chave para assinar tokens JWT |
| `JWT_EXPIRATION_MS` | `86400000` | Expiração do token (padrão: 24h) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile ativo (`dev`, `test`, `prod`) |
| `PORT` | `8080` | Porta HTTP |
| `app.time-zone` | `America/Sao_Paulo` | Fuso horário para cálculos do dashboard |

---

## 🐳 Docker

O projeto possui suporte completo a Docker para facilitar o deploy:

```bash
# Subir apenas o banco
docker-compose up -d

# Build da imagem da aplicação
docker build -t barber-saas-api .

# Verificar containers ativos
docker ps

# Logs do banco
docker logs barbersaas_db_container
```

---

## 🧪 Testes

```bash
# Rodar todos os testes
./mvnw test

# Build completo com testes
./mvnw clean install
```

---

## 🛠️ Troubleshooting

<details>
<summary><b>❌ JAVA_HOME not set</b></summary>

```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Git Bash
export JAVA_HOME="/c/Program Files/Java/jdk-21"
```
</details>

<details>
<summary><b>❌ Database connection refused</b></summary>

- Confirme que o Docker está rodando: `docker ps`
- Verifique as credenciais no `.env`
- Reinicie o container: `docker-compose down && docker-compose up -d`
- Veja os logs: `docker logs barbersaas_db_container`
</details>

<details>
<summary><b>❌ Invalid JWT token</b></summary>

- Token pode ter expirado (padrão: 24h) — faça login novamente
- `JWT_SECRET` local pode ser diferente do usado para gerar o token
- Token foi adulterado ou corrompido
</details>

---

## 📦 CI/CD

O projeto está preparado para integração com **GitHub Actions**:

- 🔄 Build e testes automáticos a cada push/PR
- 🔍 Análise estática de código
- 🐳 Publicação de imagem Docker em releases

Configure seus secrets em: `Settings → Secrets and Variables → Actions`

Secrets necessários: `DB_URL`, `DB_USER`, `DB_PASS`, `JWT_SECRET`

---

## 📄 Licença

Distribuído sob a licença **MIT**. Veja [`LICENSE`](LICENSE) para mais detalhes.

---

<div align="center">

Desenvolvido por **Daniel** • [LinkedIn](https://linkedin.com/in/daniellhrt) • [GitHub](https://github.com/daniellhrt)

⭐ Se este projeto foi útil, deixe uma estrela!

</div>
