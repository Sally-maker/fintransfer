CREATE TABLE transactions (
    id                   UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    payer_id             UUID           NOT NULL,
    payee_id             UUID           NOT NULL,
    amount               DECIMAL(19, 2) NOT NULL,
    status               VARCHAR(20)    NOT NULL,
    authorization_reason TEXT,
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_transaction_payer FOREIGN KEY (payer_id) REFERENCES wallets (id),
    CONSTRAINT fk_transaction_payee FOREIGN KEY (payee_id) REFERENCES wallets (id)
);
