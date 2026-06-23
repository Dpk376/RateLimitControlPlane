ALTER TABLE tenants ADD COLUMN downstream_url VARCHAR(255) NOT NULL DEFAULT 'http://localhost:8080/api/service';
