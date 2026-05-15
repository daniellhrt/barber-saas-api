# 🚀 PHASE 2 - Guia de Próximos Passos

---

## ✅ CHECKLIST PRÉ-DEPLOY

### 1️⃣ Validação de Código
```
☐ Compilar o projeto
  └─ Comando: ./mvnw clean compile
  └─ Esperado: Build SUCCESS

☐ Executar testes unitários
  └─ Comando: ./mvnw test
  └─ Esperado: Todos os testes passando

☐ Build do JAR
  └─ Comando: ./mvnw clean package -DskipTests
  └─ Esperado: target/danbarber-saas-api-*.jar criado
```

### 2️⃣ Testes Manuais
```
☐ Iniciar aplicação localmente
  └─ Comando: java -jar target/danbarber-saas-api-*.jar
  └─ Esperado: Application started on port 8080

☐ Testar Health Check
  └─ GET http://localhost:8080/actuator/health
  └─ Esperado: {"status":"UP"}

☐ Obter JWT Token
  └─ POST /auth/login com credenciais válidas
  └─ Esperado: {"token":"eyJ..."}

☐ Testar Dashboard Avançado
  └─ GET /dashboard/advanced com token
  └─ Esperado: Response com kpis, chartData, etc

☐ Testar Relatório Abrangente
  └─ GET /reports/comprehensive?period=month com token
  └─ Esperado: Response com todas as análises
```

### 3️⃣ Validar Response Format
```
☐ Dashboard /advanced
  ├─ kpis (com todos 9 campos)
  ├─ chartData (7 itens para dias da semana)
  ├─ monthlyComparisonData (12 itens para meses)
  ├─ topBarbers (até 5 items)
  ├─ topClients (até 5 items)
  └─ lastUpdated (ISO timestamp)

☐ Reports /comprehensive
  ├─ summary
  ├─ barberAnalysis (todos barbeiros ativos)
  ├─ topClients (até 10 items, ordenados por gasto)
  ├─ paymentMethods (soma % = 100%)
  ├─ dailyTrends (ordenados por data)
  └─ period (confirma período solicitado)
```

### 4️⃣ Verificar Segurança
```
☐ Endpoint sem JWT retorna 401
  └─ curl GET /dashboard/advanced sem header
  └─ Esperado: Unauthorized

☐ Token inválido retorna 401
  └─ curl GET /dashboard/advanced com token falso
  └─ Esperado: Unauthorized

☐ Dados sensíveis não expostos
  └─ Validar que senhas/tokens não aparecem em responses
  └─ Esperado: ✅ Seguro

☐ Operações monetárias precisas
  └─ Somar manualmente e comparar com resultado
  └─ Esperado: Valores corretos (BigDecimal)
```

---

## 📊 TESTES INICIAIS RECOMENDADOS

### Teste 1: Dashboard Básico (Validar compatibilidade)
```bash
# Pré-requisito: Token JWT válido
TOKEN="seu_token_aqui"

# Request
curl -X GET http://localhost:8080/dashboard/stats \
  -H "Authorization: Bearer $TOKEN"

# Validar campos
# ✓ kpis.faturamentoDia
# ✓ kpis.faturamentoMes
# ✓ kpis.atendimentosHoje
# ✓ kpis.ticketMedio
# ✓ chartData (7 items)
```

### Teste 2: Dashboard Avançado (NOVO)
```bash
# Request
curl -X GET http://localhost:8080/dashboard/advanced \
  -H "Authorization: Bearer $TOKEN"

# Validar novos campos
# ✓ kpis.faturamentoAno
# ✓ kpis.atendimentosMes
# ✓ kpis.growthPercentage
# ✓ kpis.totalClientes
# ✓ kpis.totalBarbeiros
# ✓ monthlyComparisonData (12 items)
# ✓ topBarbers (array)
# ✓ topClients (array)
```

### Teste 3: Relatório Simples (Validar compatibilidade)
```bash
# Request
curl -X GET "http://localhost:8080/reports?period=month" \
  -H "Authorization: Bearer $TOKEN"

# Validar campos
# ✓ summary (total, orders, avgTicket)
# ✓ orders (array com detalhes)
```

### Teste 4: Relatório Abrangente (NOVO)
```bash
# Request
curl -X GET "http://localhost:8080/reports/comprehensive?period=month" \
  -H "Authorization: Bearer $TOKEN"

# Validar novos campos
# ✓ barberAnalysis (todos barbeiros ativos)
# ✓ topClients (top 10)
# ✓ paymentMethods (distribuição)
# ✓ dailyTrends (série temporal)
```

### Teste 5: Validar Períodos
```bash
# Testar diferentes períodos
# GET /reports/comprehensive?period=week
# GET /reports/comprehensive?period=month
# GET /reports/comprehensive?period=year

# Validar que period aparece na response
```

---

## 🐛 TROUBLESHOOTING COMUM

### Problema 1: "No Data" ou Response Vazia
```
Causa Provável:
└─ Nenhuma ordem de serviço no banco para o período

Solução:
1. Criar dados de teste (clientes, barbeiros, serviços)
2. Criar ordens de serviço com datas recentes
3. Tentar novamente

Script para criar dados:
  1. POST /auth/register para criar usuário ADMIN
  2. POST /barbers para criar barbeiros
  3. POST /clients para criar clientes
  4. POST /services para criar serviços
  5. POST /orders para criar pedidos
```

### Problema 2: "Error 401 Unauthorized"
```
Causa Provável:
└─ JWT token inválido ou expirado

Solução:
1. Fazer login novamente: POST /auth/login
2. Copiar novo token da response
3. Usar novo token no header Authorization

Comando:
  curl -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"seu_email@example.com","password":"senha"}'
```

### Problema 3: "Error 500 Internal Server Error"
```
Causa Provável:
└─ Erro em lógica de service ou database connection

Solução:
1. Verificar logs da aplicação
2. Verificar conexão com banco de dados
3. Verificar se PostgreSQL está rodando

Comandos:
  # Ver logs
  docker logs danbarber-saas-api
  
  # Verificar banco
  docker logs barbersaas_db_container
  
  # Reconectar ao banco
  docker-compose down && docker-compose up -d
```

### Problema 4: "BadRequest" com Período Inválido
```
Causa Provável:
└─ Período não suportado no query parameter

Solução:
1. Usar períodos válidos:
   - "today" (padrão)
   - "yesterday"
   - "week"
   - "month"
   - "year"

Exemplo:
  GET /reports/comprehensive?period=month ✓
  GET /reports/comprehensive?period=invalid ✗
```

---

## 📊 DADOS DE TESTE SUGERIDOS

### Dataset Mínimo para Testar
```
Clientes (3):
├─ João Silva (11 visitas, gasto total R$550)
├─ Maria Santos (8 visitas, gasto total R$400)
└─ Pedro Costa (5 visitas, gasto total R$250)

Barbeiros (2):
├─ Barbeiro A (comissão 15%, 15 serviços)
└─ Barbeiro B (comissão 12%, 9 serviços)

Métodos de Pagamento:
├─ CARTAO_CREDITO (15 transações, R$750)
├─ DINHEIRO (7 transações, R$350)
└─ PIX (2 transações, R$100)

Período: Últimos 30 dias (com dados variados por dia)
```

---

## 📈 VALIDAÇÕES DE CÁLCULO

### Validação 1: Faturamento Total
```
Operação:
  SUM(all service_orders.total_amount) BETWEEN startDate AND endDate

Exemplo:
  Se temos 3 pedidos de R$100, R$150, R$200
  Esperado: R$450
  
Código:
  BigDecimal total = orders.stream()
    .map(ServiceOrder::getTotalAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

### Validação 2: Ticket Médio
```
Operação:
  totalAmount / totalOrders

Exemplo:
  R$450 / 3 pedidos = R$150 por pedido
  
Código:
  avgTicket = totalAmount
    .divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP);
```

### Validação 3: Comissão Estimada
```
Operação:
  totalRevenue * (commissionRate / 100)

Exemplo:
  R$1000 * (15% / 100) = R$150

Código:
  estimatedCommission = totalRevenue
    .multiply(barber.getCommissionRate())
    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
```

### Validação 4: Percentual de Método
```
Operação:
  (methodAmount / totalAmount) * 100

Exemplo:
  R$750 / R$1200 * 100 = 62.5%

Código:
  percentage = methodAmount
    .divide(totalAmount, 4, RoundingMode.HALF_UP)
    .multiply(new BigDecimal(100))
    .doubleValue();
```

### Validação 5: Crescimento MoM
```
Operação:
  ((currentMonth - lastMonth) / lastMonth) * 100

Exemplo:
  ((R$1000 - R$890) / R$890) * 100 = 12.36%

Código:
  growthPercentage = faturamentoMes
    .subtract(faturamentoLastMonth)
    .divide(faturamentoLastMonth, 4, RoundingMode.HALF_UP)
    .multiply(new BigDecimal(100));
```

---

## 🔍 POINTS TO VERIFY

### Database Consistency
- [ ] Todas service_orders têm client_id válido
- [ ] Todas service_orders têm barber_id válido
- [ ] created_at timestamps são válidos
- [ ] total_amount é sempre >= 0
- [ ] Relacionamentos integrity constraints funcionando

### API Response Format
- [ ] Content-Type: application/json
- [ ] HTTP Status codes corretos (200, 401, 400, 500)
- [ ] Error messages são informativas
- [ ] Timestamps em formato ISO 8601
- [ ] Números BigDecimal com precisão correta

### Business Logic
- [ ] Growthpercentage > 0 quando crescimento positivo
- [ ] Growthpercentage < 0 quando decrescimento
- [ ] Sum percentuais = 100% (com margem de erro)
- [ ] Top items realmente estão ordenados corretamente
- [ ] Data ranges respeitam bordas (startDate, endDate)

---

## 📋 DEPLOY CHECKLIST

### Pré-Deploy
```
☐ Código compilado sem erros
☐ Testes unitários passando
☐ Testes manuais validados
☐ Dados de teste criados
☐ Documentação revisada
☐ Logs limpos/configurados
☐ Security validado
☐ Performance testada
```

### Deploy
```
☐ Docker image built
☐ Environment variables configuradas
☐ Database migrations executadas
☐ Backup do banco feito
☐ Rollback plan documentado
☐ Monitoramento ativo
```

### Pós-Deploy
```
☐ Health Check passando
☐ Endpoints respondendo
☐ Dados em produção validados
☐ Logs monitorados
☐ Performance aceitável
☐ Usuários notificados
```

---

## 📚 DOCUMENTOS DE REFERÊNCIA

| Documento | Quando Consultar | Link |
|-----------|------------------|------|
| PHASE_2_DOCUMENTATION.md | Para entender completamente | Completo |
| PHASE_2_QUICK_START.md | Para começar a testar | Quick |
| PHASE_2_CHANGES.md | Para detalhes técnicos | Technical |
| PHASE_2_SUMMARY.md | Para resumo executivo | Overview |
| INDEX_PHASE_2.md | Para localizar mudanças | Index |
| PHASE_2_VISUAL_SUMMARY.md | Para visão visual | Visual |

---

## 🎯 TIMELINE SUGERIDO

```
DIA 1: Compilação e Testes Iniciais
  - Compilar projeto
  - Executar testes unitários
  - Validar estrutura de código

DIA 2: Testes Manuais
  - Testar endpoints básicos
  - Testar novos endpoints
  - Validar respostas

DIA 3: Dados Reais
  - Criar dataset de teste
  - Validar cálculos
  - Testar edge cases

DIA 4: Security e Performance
  - Testar autenticação
  - Validar SQL injection protection
  - Teste de carga

DIA 5: Documentação e Deploy
  - Revisar documentação
  - Preparar release notes
  - Deploy em staging

DIA 6-7: Validação em Staging
  - Teste com dados reais
  - Validação de performance
  - Coleta de feedback

DIA 8: Deploy em Produção
  - Deploy com rollback plan
  - Monitoramento contínuo
  - Suporte ao usuário
```

---

## 📞 CONTATO E SUPORTE

### Se encontrar problemas:

1. **Erro de Compilação**
   - Verificar versão Java (21+)
   - Verificar Maven (3.8+)
   - Limpar cache: `./mvnw clean`

2. **Erro em Runtime**
   - Verificar logs: `docker logs danbarber-saas-api`
   - Verificar banco: `docker logs barbersaas_db_container`
   - Verificar connectivity: `ping localhost:5432`

3. **Dados Incorretos**
   - Validar dados de entrada
   - Verificar período solicitado
   - Compilar manualmente cálculos

4. **Performance Lenta**
   - Verificar índices do banco
   - Monitorar queries lentas
   - Considerar paginação

---

## ✅ CONCLUSÃO

Todos os passos acima garantirão uma implementação de sucesso da Fase 2.

**Próximo Passo:** Começar pelo CHECKLIST PRÉ-DEPLOY acima uma vez que Java esteja configurado.

---

**Última Atualização:** 15 de Maio de 2026  
**Status:** Pronto para Implementação  
**Versão:** 2.0.0

