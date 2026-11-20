# CSA09 – Programming in Java: Assignment 5 (CO5)
## Data Persistence Engineering – Files and Databases

- **Student Name:** [STUDENT NAME]
- **Register Number:** [REGISTER NUMBER]
- **Department:** [DEPARTMENT]
- **Course:** CSA09 – Programming in Java (Course Outcome CO5)
- **Java Version:** [INSERT JDK VERSION, e.g., OpenJDK 21.0.x]
- **Required Dependencies:** MySQL Community Server 8.0+, MySQL Connector/J (`mysql-connector-j-8.x.x.jar`)

---

## 1. MySQL Database Setup (For Tasks 03, 05, 07, and 08)

Run the SQL setup scripts in your local MySQL 8.0 instance before executing the JDBC tasks:

```bash
mysql -u root -p < Task03/database.sql
mysql -u root -p < Task05/setup_users_test_db.sql
mysql -u root -p < Task07/database.sql
```

> **Note:** By default, the JDBC runner classes connect to `jdbc:mysql://localhost:3306/csa09_assign5_test` with user `root` and password `root` (or via environment variables `DB_URL`, `DB_USER`, `DB_PASS`). Set `export DB_PASS="your_mysql_password"` before running if your local root password differs.

---

## 2. Compilation and Execution Commands

| Task Folder | Compilation Command | Execution Command | Short Result Summary |
| :--- | :--- | :--- | :--- |
| **Task01** | `javac Task01/*.java` | `java -cp Task01 PassStore && java -cp Task01 PassStoreExtended` | `[INSERT SHORT RESULT NOTE]` |
| **Task02** | `javac Task02/*.java` | `java -cp Task02 LineCountBenchmark` | `[INSERT SHORT RESULT NOTE]` |
| **Task03** | `javac Task03/*.java` | `java -cp .:mysql-connector-j.jar:Task03 StudentDaoDemo` | `[INSERT SHORT RESULT NOTE]` |
| **Task04** | `javac Task04/*.java` | `java -cp Task04 SerializationDemo` | `[INSERT SHORT RESULT NOTE]` |
| **Task05** | `javac Task05/*.java` | `java -cp .:mysql-connector-j.jar:Task05 AdversarialSecurityTest` | `[INSERT SHORT RESULT NOTE]` |
| **Task06** | `javac Task06/*.java` | `java -cp Task06 PassStore && java -cp Task06 PassSearchGrowthBenchmark` | `[INSERT SHORT RESULT NOTE]` |
| **Task07** | `javac Task07/*.java` | `java -cp .:mysql-connector-j.jar:Task07 JdbcInsertionBenchmarkAndVerify` | `[INSERT SHORT RESULT NOTE]` |
| **Task08** | `javac Task08/*.java` | `java -cp .:mysql-connector-j.jar:Task08 Migration` | `[INSERT SHORT RESULT NOTE]` |
