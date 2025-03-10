DROP TABLE IF EXISTS dbuser.TRANSACTION CASCADE;
CREATE TABLE dbuser.TRANSACTION (
    id                  SERIAL PRIMARY KEY,
    sender_account_id   VARCHAR(50)    NOT NULL,
    receiver_account_id VARCHAR(50)    NOT NULL,
    amount              DECIMAL(19, 4) NOT NULL,
    currency            VARCHAR(10)    NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);