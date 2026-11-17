-- Il proprietario di un acquisto è ricavato dalla transazione di credito:
-- package_purchase -> credit_transaction -> credit_wallet -> user.
ALTER TABLE package_purchases
    DROP CONSTRAINT fk_package_purchase_user;

ALTER TABLE package_purchases
    DROP COLUMN user_id;
