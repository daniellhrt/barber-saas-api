# Guia Rápido - Testando a Fase 2

## 🚀 Quick Start para Fase 2

### Pré-requisitos
- ✅ Projeto compilado (`./mvnw clean compile`)
- ✅ PostgreSQL rodando (Docker Compose ou Neon)
- ✅ Token JWT válido (faça login antes)

---

## 📥 Passo 1: Preparar o Ambiente

```powershell
# 1. Navegar até o projeto
cd C:\Users\ferna\IdeaProjects\danbarber-saas-api

# 2. Iniciar o banco de dados (se usando Docker)
docker-compose up -d

# 3. Verificar container rodando
docker ps
```

---

## 🔑 Passo 2: Obter um Token JWT

```powershell
# Registrar um novo usuário
$body = @{
    email = "admin@barbersaas.com"
    password = "Admin@123456"
    role = "ADMIN"
} | ConvertTo-Json

curl -X POST http://localhost:8080/auth/register `
  -H "Content-Type: application/json" `
  -d $body

# Fazer login
$loginBody = @{
    email = "admin@barbersaas.com"
    password = "Admin@123456"
} | ConvertTo-Json

$response = curl -X POST http://localhost:8080/auth/login `
  -H "Content-Type: application/json" `
  -d $loginBody

# Copiar o token da resposta
# Exemplo de resposta: {"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
```

---

## 📊 Passo 3: Testar os Novos Endpoints

### 3.1 Dashboard Básico (Fase 1)
```powershell
$token = "YOUR_TOKEN_HERE"

curl -X GET http://localhost:8080/dashboard/stats `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json"

# Esperado: 
# {
#   "kpis": {...},
#   "chartData": [...]
# }
```

### 3.2 Dashboard Avançado (NOVO - Fase 2) ⭐
```powershell
curl -X GET http://localhost:8080/dashboard/advanced `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json"

# Esperado: 
# {
#   "kpis": {
#     "faturamentoDia": X,
#     "faturamentoMes": X,
#     "faturamentoAno": X,
#     "atendimentosHoje": X,
#     "atendimentosMes": X,
#     "ticketMedio": X,
#     "growthPercentage": X,
#     "totalClientes": X,
#     "totalBarbeiros": X
#   },
#   "chartData": [...],
#   "monthlyComparisonData": [...],
#   "topBarbers": [...],
#   "topClients": [...],
#   "lastUpdated": "2026-05-15T14:30:00"
# }
```

### 3.3 Relatório Simples (Fase 1)
```powershell
curl -X GET "http://localhost:8080/reports?period=today" `
  -H "Authorization: Bearer $token"

# Períodos suportados: today, yesterday, week, month
```

### 3.4 Relatório Abrangente (NOVO - Fase 2) ⭐
```powershell
curl -X GET "http://localhost:8080/reports/comprehensive?period=month" `
  -H "Authorization: Bearer $token"

# Períodos suportados: week, month, year

# Esperado:
# {
#   "summary": {...},
#   "barberAnalysis": [
#     {
#       "barberId": "uuid",
#       "barberName": "João Silva",
#       "totalServices": 45,
#       "totalRevenue": 2250.00,
#       "averageTicket": 50.00,
#       "commissionRate": 15.00,
#       "estimatedCommission": 337.50
#     }
#   ],
#   "topClients": [
#     {
#       "clientId": "uuid",
#       "clientName": "Maria Santos",
#       "totalVisits": 12,
#       "totalSpent": 600.00,
#       "averageTicket": 50.00,
#       "lastVisit": "2026-05-14",
#       "preferredBarber": "João Silva"
#     }
#   ],
#   "paymentMethods": [...],
#   "dailyTrends": [...],
#   "period": "month"
# }
```

---

## 📝 Passo 4: Criar Dados de Teste (Opcional)

Se quiser testar com dados mais realistas:

```powershell
# Criar alguns clientes
$client1 = @{
    name = "João Silva"
    phone = "11999999999"
    email = "joao@example.com"
} | ConvertTo-Json

curl -X POST http://localhost:8080/clients `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d $client1

# Repetir para mais clientes com dados variados
```

---

## 🔍 Passo 5: Validar Respostas

### Checklist para Dashboard Avançado
- [ ] `kpis.faturamentoDia` ≥ 0
- [ ] `kpis.faturamentoMes` ≥ `kpis.faturamentoDia`
- [ ] `kpis.faturamentoAno` ≥ `kpis.faturamentoMes`
- [ ] `kpis.ticketMedio` > 0 (se houver atendimentos)
- [ ] `kpis.growthPercentage` é um número válido
- [ ] `kpis.totalClientes` > 0
- [ ] `kpis.totalBarbeiros` > 0
- [ ] `chartData.length` === 7 (dias da semana)
- [ ] `monthlyComparisonData.length` === 12 (últimos 12 meses)
- [ ] `topBarbers.length` ≤ 5
- [ ] `topClients.length` ≤ 5
- [ ] `lastUpdated` é um timestamp válido

### Checklist para Relatório Abrangente
- [ ] `summary.totalAmount` ≥ 0
- [ ] `summary.totalOrders` ≥ 0
- [ ] `summary.avgTicket` > 0 (se houver ordens)
- [ ] `barberAnalysis` contém todos os barbeiros ativos
- [ ] `barberAnalysis` ordenado por `totalRevenue` DESC
- [ ] `topClients.length` ≤ 10
- [ ] `topClients` ordenado por `totalSpent` DESC
- [ ] `paymentMethods[].percentage.sum()` ≈ 100%
- [ ] `dailyTrends` ordenado por `date` ASC
- [ ] `period` retorna o período solicitado

---

## 🐛 Troubleshooting

### Erro: "Error 500" ou "No Data"
```powershell
# Verificar logs da aplicação
docker logs danbarber-saas-api

# Verificar conexão com banco
docker logs barbersaas_db_container
```

### Erro: "Unauthorized" ou "401"
```powershell
# Token expirou ou é inválido
# Fazer login novamente e obter novo token
```

### Erro: "Method Not Allowed" ou "404"
```powershell
# Verificar se URL está correta
# Dashboard: GET /dashboard/advanced
# Relatório: GET /reports/comprehensive
```

### Erro: "Bad Request" ou "400"
```powershell
# Verificar se o token foi enviado no header Authorization
# Verificar se Content-Type é application/json
```

---

## ✅ Validação Completa

Após completar todos os testes, a Fase 2 está pronta para deploy:

```powershell
# 1. Compilar
./mvnw clean compile

# 2. Executar testes (quando Java estiver configurado)
./mvnw test

# 3. Buildar JAR
./mvnw clean package -DskipTests

# 4. Verificar artefato
ls target/danbarber-saas-api-*.jar

# 5. Executar (opcional, localmente)
java -jar target/danbarber-saas-api-*.jar
```

---

## 📊 Exemplos de Resposta

### Dashboard Avançado (Exemplo)
```json
{
  "kpis": {
    "faturamentoDia": 500.00,
    "faturamentoMes": 8500.00,
    "faturamentoAno": 75000.00,
    "atendimentosHoje": 10,
    "atendimentosMes": 180,
    "ticketMedio": 50.00,
    "growthPercentage": 12.50,
    "totalClientes": 250,
    "totalBarbeiros": 5
  },
  "chartData": [
    {"name": "Seg", "total": 1200.00},
    {"name": "Ter", "total": 950.00},
    {"name": "Qua", "total": 1100.00},
    {"name": "Qui", "total": 1050.00},
    {"name": "Sex", "total": 1400.00},
    {"name": "Sab", "total": 1800.00},
    {"name": "Dom", "total": 0.00}
  ],
  "monthlyComparisonData": [
    {"name": "Jun/2025", "total": 70000.00},
    {"name": "Jul/2025", "total": 71000.00},
    // ... 10 mais
    {"name": "Mai/2026", "total": 75000.00}
  ],
  "topBarbers": [
    {
      "barberId": "uuid-1",
      "barberName": "João Silva",
      "totalServices": 45,
      "totalRevenue": 2250.00,
      "averageTicket": 50.00,
      "commissionRate": 15.00,
      "estimatedCommission": 337.50
    }
  ],
  "topClients": [
    {
      "clientId": "uuid-1",
      "clientName": "Maria Santos",
      "totalVisits": 12,
      "totalSpent": 600.00,
      "averageTicket": 50.00,
      "lastVisit": "2026-05-14",
      "preferredBarber": "João Silva"
    }
  ],
  "lastUpdated": "2026-05-15T14:30:00.123456"
}
```

---

## 🚀 Próximos Passos

1. **Deploy em Staging**: Validar com dados reais
2. **Ajustes e Feedback**: Coletar feedback dos usuários
3. **Deploy em Produção**: Rollout para usuários
4. **Monitoramento**: Acompanhar performance e uso
5. **Fase 3**: Agendamentos + Notificações

---

## 📚 Documentação Referência

- `PHASE_2_DOCUMENTATION.md` - Documentação completa da Fase 2
- `PHASE_2_CHANGES.md` - Detalhes técnicos de mudanças
- `README.md` - Documentação geral do projeto
- `TESTING_GUIDE.md` - Guia de teste geral

---

**Última Atualização:** 15 de Maio de 2026
**Status:** Pronto para Teste e Deploy

