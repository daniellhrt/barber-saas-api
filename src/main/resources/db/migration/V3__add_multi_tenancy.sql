-- V3: Multi-tenancy — isolamento de dados por barbeiro (dono da barbearia)
-- Cada barbeiro (ADMIN) é um tenant independente.
-- Dados de clientes, serviços e produtos são vinculados ao dono.

-- 1. Adicionar owner_barber_id em services
--    Permite que cada barbeiro tenha seu próprio catálogo de serviços
ALTER TABLE services
    ADD COLUMN IF NOT EXISTS owner_barber_id UUID REFERENCES barbers(id) ON DELETE CASCADE;

-- 2. Adicionar owner_barber_id em products
--    Permite que cada barbeiro tenha seu próprio estoque
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS owner_barber_id UUID REFERENCES barbers(id) ON DELETE CASCADE;

-- 3. Adicionar owner_barber_id em appointments
--    Agendamentos já têm barber_id, mas precisamos do dono (o admin da barbearia)
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS owner_barber_id UUID REFERENCES barbers(id) ON DELETE CASCADE;

-- 4. Adicionar owner_barber_id em service_orders
--    Ordens de serviço já têm barber_id (quem atendeu), agora precisam do dono
ALTER TABLE service_orders
    ADD COLUMN IF NOT EXISTS owner_barber_id UUID REFERENCES barbers(id) ON DELETE CASCADE;

-- Nota sobre clients: a coluna barber_id já existe e será reutilizada como
-- "barbeiro preferido" (não é o owner). Para multi-tenancy, o dono é inferido
-- pelo barber que criou o client, usando a coluna barber_id existente.
-- Se no futuro precisar separar, adicionar owner_barber_id em clients também.

-- 5. Índices para performance nas queries filtradas por owner
CREATE INDEX IF NOT EXISTS idx_services_owner ON services (owner_barber_id);
CREATE INDEX IF NOT EXISTS idx_products_owner ON products (owner_barber_id);
CREATE INDEX IF NOT EXISTS idx_appointments_owner ON appointments (owner_barber_id);
CREATE INDEX IF NOT EXISTS idx_service_orders_owner ON service_orders (owner_barber_id);
