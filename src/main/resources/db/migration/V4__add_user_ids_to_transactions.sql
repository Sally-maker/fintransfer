ALTER TABLE transactions ADD COLUMN payer_user_id UUID;
ALTER TABLE transactions ADD COLUMN payee_user_id UUID;

ALTER TABLE transactions ADD CONSTRAINT fk_transaction_payer_user FOREIGN KEY (payer_user_id) REFERENCES users (id);
ALTER TABLE transactions ADD CONSTRAINT fk_transaction_payee_user FOREIGN KEY (payee_user_id) REFERENCES users (id);
