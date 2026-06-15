CREATE TABLE wallets (
    id       UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id  UUID           NOT NULL UNIQUE,
    balance  DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    version  BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users (id)
);
