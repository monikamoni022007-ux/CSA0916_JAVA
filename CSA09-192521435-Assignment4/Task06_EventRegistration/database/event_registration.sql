CREATE DATABASE IF NOT EXISTS event_registration_db;
USE event_registration_db;

CREATE TABLE IF NOT EXISTS events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(120) NOT NULL UNIQUE,
    event_date DATE,
    venue VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS registrations (
    registration_id INT PRIMARY KEY AUTO_INCREMENT,
    registration_number VARCHAR(30) NOT NULL,
    student_name VARCHAR(120) NOT NULL,
    event_id INT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(event_id),
    CONSTRAINT uq_registration UNIQUE (registration_number, event_id)
);

INSERT INTO events (event_name, event_date, venue) VALUES
('Campus Tech Expo', '2026-12-12', 'Main Auditorium'),
('Robotics Workshop', '2026-12-15', 'Innovation Lab'),
('Design Thinking Hackathon', '2026-12-20', 'Innovation Hall');
