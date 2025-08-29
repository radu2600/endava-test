-- OWNERS
INSERT INTO owner (name, email) VALUES ('Ana Pop', 'ana.pop@example.com');
INSERT INTO owner (name, email) VALUES ('Bogdan Ionescu', 'bogdan.ionescu@example.com');

-- CARS
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN001', 'Dacia', 'Logan', 2022, 1);
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN002', 'Ford', 'Puma', 2023, 2);

-- INSURANCE POLICIES
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiration_notified) VALUES (1, 'Allianz', '2024-01-01', '2024-12-31', false);
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiration_notified) VALUES (1, 'Groupama', '2025-01-01', '2026-01-01', false);
