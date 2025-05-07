-- V1_3__Add_FK_To_Maut_Users.sql
ALTER TABLE maut_users
ADD CONSTRAINT fk_maut_users_client_application
FOREIGN KEY (client_application_id)
REFERENCES client_applications(id)
ON DELETE RESTRICT; -- Or CASCADE / SET NULL depending on desired behavior

COMMENT ON CONSTRAINT fk_maut_users_client_application ON maut_users
IS 'Foreign key linking maut_users(client_application_id) to client_applications(id). Ensures referential integrity.';
