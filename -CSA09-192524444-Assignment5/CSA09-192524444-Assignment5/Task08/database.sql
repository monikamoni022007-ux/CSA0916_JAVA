CREATE DATABASE IF NOT EXISTS csa09_assign5_test;
USE csa09_assign5_test;

DROP TABLE IF EXISTS bus_pass;
CREATE TABLE bus_pass (
    pass_id  VARCHAR(20)  NOT NULL PRIMARY KEY,
    reg_no   VARCHAR(20)  NOT NULL,
    name     VARCHAR(100) NOT NULL,
    route_no INT          NOT NULL,
    amount   DOUBLE       NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
