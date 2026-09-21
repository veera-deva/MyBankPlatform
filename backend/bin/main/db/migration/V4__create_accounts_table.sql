CREATE TABLE accounts(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
customer_id UUID NOT NULL UNIQUE REFERENCES customers(id),
account_number VARCHAR(20) NOT NULL UNIQUE, status VARCHAR(20) DEFAULT 'ACTIVE' CHECK ( status IN ('ACTIVE','CLOSED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now());