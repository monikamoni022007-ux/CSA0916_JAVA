CREATE DATABASE IF NOT EXISTS csa09_assign5_test;
USE csa09_assign5_test;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id   VARCHAR(50)  NOT NULL PRIMARY KEY,
    pwd  VARCHAR(100) NOT NULL,
    role VARCHAR(30)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (id, pwd, role) VALUES
('admin', 'Str0ng#Admin$2026', 'TRANSPORT_HEAD'),
('192311001', 'Arun@Pass99', 'STUDENT');

DROP TABLE IF EXISTS hostel_wallet;
CREATE TABLE hostel_wallet (
    reg_no  VARCHAR(20) NOT NULL PRIMARY KEY,
    balance DOUBLE      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS canteen_wallet;
CREATE TABLE canteen_wallet (
    reg_no  VARCHAR(20) NOT NULL PRIMARY KEY,
    balance DOUBLE      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO hostel_wallet VALUES ('192311001', 2000.0);
INSERT INTO canteen_wallet VALUES ('192311001', 500.0);
