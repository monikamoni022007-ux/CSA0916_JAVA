CREATE DATABASE IF NOT EXISTS csa09_assign5_test;
USE csa09_assign5_test;

CREATE TABLE IF NOT EXISTS student (
    reg_no     VARCHAR(15)  NOT NULL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    department VARCHAR(50)  NOT NULL,
    semester   INT          NOT NULL,
    cgpa       DOUBLE       NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
