-- V2: Adiciona campos para as funcionalidades do microSaaS
-- Executado após V1__create_initial_schema.sql

-- 1. Adiciona intervalo de retorno esperado nos clientes
--    O barbeiro define quantos dias o cliente costuma demorar para retornar
--    Usado para gerar alertas em GET /clients/overdue
ALTER TABLE clients
    ADD COLUMN IF NOT EXISTS return_interval_days INTEGER DEFAULT NULL;

-- 2. Adiciona duração personalizável nos agendamentos
--    Padrão 30 minutos, mas o barbeiro pode personalizar por atendimento
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS duration_minutes INTEGER NOT NULL DEFAULT 30;

-- 3. Adiciona vínculo de serviço ao agendamento
--    Permite saber qual serviço foi agendado (para calcular duração automática)
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS service_id UUID REFERENCES services(id) ON DELETE SET NULL;

-- 4. Adiciona campo de observações ao agendamento
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- 5. Índice para busca de agendamentos por período (calendário)
CREATE INDEX IF NOT EXISTS idx_appointments_scheduled_time ON appointments (scheduled_time);

-- 6. Índice para busca de clientes por intervalo de retorno
CREATE INDEX IF NOT EXISTS idx_clients_return_interval ON clients (return_interval_days) WHERE return_interval_days IS NOT NULL;
