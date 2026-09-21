CREATE TABLE ledger_entries(
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            account_id UUID NOT NULL REFERENCES accounts(id),
            type VARCHAR(20) NOT NULL CHECK ( type IN ('DEPOSIT','WITHDRAW')),
            amount NUMERIC(19,2) NOT NULL CHECK (amount>0),
            balance_after NUMERIC(19,2) NOT NULL,
            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id)

