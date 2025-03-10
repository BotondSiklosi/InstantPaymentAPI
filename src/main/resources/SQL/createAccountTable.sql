DROP TABLE IF EXISTS dbuser.ACCOUNT CASCADE;
CREATE TABLE dbuser.ACCOUNT (
    ID                  VARCHAR(50) PRIMARY KEY,
    AMOUNT              DECIMAL(19, 4) NOT NULL,
    CURRENCY            VARCHAR(10)    NOT NULL,
);