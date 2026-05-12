-- V1__create_initial_schema.sql

-- 1. Tabela de Usuários (Administradores e Barbeiros)
CREATE TABLE users
(
    id            UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(50)         NOT NULL CHECK (role IN ('ADMIN', 'BARBER')),
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Barbeiros (Vinculada ao Usuário para autenticação)
CREATE TABLE barbers
(
    id              UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    user_id         UUID UNIQUE  NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    specialty       VARCHAR(100),
    commission_rate DECIMAL(5, 2)            DEFAULT 0.00,
    is_active       BOOLEAN                  DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabela de Clientes
CREATE TABLE clients
(
    id         UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    whatsapp   VARCHAR(20),
    email      VARCHAR(255) UNIQUE,
    cpf        VARCHAR(14) UNIQUE,
    birth_date DATE,
    notes      TEXT,
    address    TEXT,
    barber_id  UUID         REFERENCES barbers (id) ON DELETE SET NULL, -- Barbeiro preferido/carteira
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabela de Serviços
CREATE TABLE services
(
    id                         UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name                       VARCHAR(100)   NOT NULL,
    price                      DECIMAL(10, 2) NOT NULL,
    estimated_duration_minutes INTEGER        NOT NULL,
    description                TEXT,
    created_at                 TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tabela de Produtos (Estoque)
CREATE TABLE products
(
    id             UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name           VARCHAR(100)   NOT NULL,
    category       VARCHAR(50),
    brand          VARCHAR(50),
    price          DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER                  DEFAULT 0,
    sku            VARCHAR(50) UNIQUE,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Tabela de Agendamentos (Agenda)
CREATE TABLE appointments
(
    id             UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    client_id      UUID                     NOT NULL REFERENCES clients (id) ON DELETE CASCADE,
    barber_id      UUID                     NOT NULL REFERENCES barbers (id) ON DELETE CASCADE,
    scheduled_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status         VARCHAR(50)              NOT NULL CHECK (status IN ('CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED')),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tabela de Ordens de Serviço (Fechamento e Financeiro)
CREATE TABLE service_orders
(
    id             UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    client_id      UUID           NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    barber_id      UUID           NOT NULL REFERENCES barbers (id) ON DELETE RESTRICT,
    appointment_id UUID           REFERENCES appointments (id) ON DELETE SET NULL,
    total_amount   DECIMAL(10, 2) NOT NULL  DEFAULT 0.00,
    payment_method VARCHAR(50),
    status         VARCHAR(50)    NOT NULL CHECK (status IN ('OPEN', 'PAID', 'CANCELED')),
    notes          TEXT,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Tabela de Itens da Ordem de Serviço (Serviços e Produtos na mesma OS)
CREATE TABLE order_items
(
    id               UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    service_order_id UUID           NOT NULL REFERENCES service_orders (id) ON DELETE CASCADE,
    reference_id     UUID           NOT NULL, -- ID flexível para serviço ou produto
    type             VARCHAR(50)    NOT NULL CHECK (type IN ('SERVICE', 'PRODUCT')),
    quantity         INTEGER        NOT NULL  DEFAULT 1,
    unit_price       DECIMAL(10, 2) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Índices para otimização de consultas frequentes e dashboards
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_clients_barber_id ON clients (barber_id);
CREATE INDEX idx_appointments_barber_time ON appointments (barber_id, scheduled_time);
CREATE INDEX idx_service_orders_client_id ON service_orders (client_id);
CREATE INDEX idx_service_orders_barber_id ON service_orders (barber_id);
CREATE INDEX idx_service_orders_status ON service_orders (status);