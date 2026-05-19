BEGIN;

-- 1) Barber user + barber profile (idempotent by unique email/user_id)
WITH upsert_user AS (
    INSERT INTO users (email, password_hash, role)
    VALUES (
        'barbeiro.seed@barbersaas.local',
        '$2a$10$8K1p/a0dQ4V6xM8K3kPjQO5twjO9C9f2k5QhM8d5xS0P3f5Yv9Q6i',
        'BARBER'
    )
    ON CONFLICT (email)
    DO UPDATE SET
        password_hash = EXCLUDED.password_hash,
        role = EXCLUDED.role,
        updated_at = CURRENT_TIMESTAMP
    RETURNING id
), selected_user AS (
    SELECT id FROM upsert_user
    UNION ALL
    SELECT id FROM users WHERE email = 'barbeiro.seed@barbersaas.local'
    LIMIT 1
), upsert_barber AS (
    INSERT INTO barbers (user_id, name, phone, specialty, commission_rate, is_active)
    SELECT
        su.id,
        'Carlos Mendes',
        '(11) 98888-1000',
        'Corte e barba tradicional',
        40.00,
        TRUE
    FROM selected_user su
    ON CONFLICT (user_id)
    DO UPDATE SET
        name = EXCLUDED.name,
        phone = EXCLUDED.phone,
        specialty = EXCLUDED.specialty,
        commission_rate = EXCLUDED.commission_rate,
        is_active = EXCLUDED.is_active,
        updated_at = CURRENT_TIMESTAMP
    RETURNING id
), selected_barber AS (
    SELECT id FROM upsert_barber
    UNION ALL
    SELECT b.id
    FROM barbers b
    JOIN users u ON u.id = b.user_id
    WHERE u.email = 'barbeiro.seed@barbersaas.local'
    LIMIT 1
)

-- 2) 10 clients linked to the seeded barber (idempotent by unique email)
INSERT INTO clients (name, phone, whatsapp, email, cpf, birth_date, notes, address, barber_id)
SELECT
    'Cliente Seed ' || LPAD(gs::text, 2, '0') AS name,
    '(11) 97777-' || LPAD((1000 + gs)::text, 4, '0') AS phone,
    '(11) 97777-' || LPAD((1000 + gs)::text, 4, '0') AS whatsapp,
    'cliente.seed' || LPAD(gs::text, 2, '0') || '@barbersaas.local' AS email,
    LPAD((10000000000 + gs)::text, 11, '0') AS cpf,
    (DATE '1990-01-01' + (gs * 30))::date AS birth_date,
    'Cliente de teste gerado automaticamente' AS notes,
    'Rua Teste, ' || gs || ' - Sao Paulo/SP' AS address,
    sb.id AS barber_id
FROM generate_series(1, 10) gs
CROSS JOIN selected_barber sb
ON CONFLICT (email)
DO UPDATE SET
    name = EXCLUDED.name,
    phone = EXCLUDED.phone,
    whatsapp = EXCLUDED.whatsapp,
    cpf = EXCLUDED.cpf,
    birth_date = EXCLUDED.birth_date,
    notes = EXCLUDED.notes,
    address = EXCLUDED.address,
    barber_id = EXCLUDED.barber_id,
    updated_at = CURRENT_TIMESTAMP;

-- 3) 5 services (replace fixed seed names)
DELETE FROM services
WHERE name IN (
    'Corte Masculino Seed',
    'Barba Seed',
    'Corte + Barba Seed',
    'Pigmentacao Seed',
    'Sobrancelha Seed'
);

INSERT INTO services (name, price, estimated_duration_minutes, description)
VALUES
    ('Corte Masculino Seed', 45.00, 40, 'Corte social/degrade para massa de teste'),
    ('Barba Seed', 30.00, 25, 'Modelagem e acabamento de barba para teste'),
    ('Corte + Barba Seed', 70.00, 60, 'Combo de corte com barba para teste'),
    ('Pigmentacao Seed', 55.00, 35, 'Pigmentacao capilar para teste'),
    ('Sobrancelha Seed', 20.00, 15, 'Design de sobrancelha para teste');

-- 4) 5 products (idempotent by unique SKU)
INSERT INTO products (name, category, brand, price, stock_quantity, sku)
VALUES
    ('Pomada Modeladora Seed', 'Finalizacao', 'BarberPro', 39.90, 25, 'SEED-PRD-001'),
    ('Shampoo Anticaspa Seed', 'Higiene', 'BarberPro', 34.90, 18, 'SEED-PRD-002'),
    ('Oleo para Barba Seed', 'Barba', 'BarberPro', 29.90, 30, 'SEED-PRD-003'),
    ('Gel Pos-Barba Seed', 'Barba', 'BarberPro', 24.90, 20, 'SEED-PRD-004'),
    ('Tonico Capilar Seed', 'Tratamento', 'BarberPro', 49.90, 15, 'SEED-PRD-005')
ON CONFLICT (sku)
DO UPDATE SET
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    brand = EXCLUDED.brand,
    price = EXCLUDED.price,
    stock_quantity = EXCLUDED.stock_quantity,
    updated_at = CURRENT_TIMESTAMP;

COMMIT;

-- Summary
SELECT 'barbers' AS table_name, COUNT(*) AS total FROM barbers
UNION ALL
SELECT 'clients', COUNT(*) FROM clients
UNION ALL
SELECT 'services', COUNT(*) FROM services
UNION ALL
SELECT 'products', COUNT(*) FROM products;

