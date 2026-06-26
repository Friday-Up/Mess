-- 数据库表结构定义 - 仅在ddl-auto=create-drop时使用，生产环境建议用Flyway/Liquibase
CREATE TABLE IF NOT EXISTS "USER" (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,        -- 主键，自增
    USERNAME VARCHAR(50) NOT NULL UNIQUE,        -- 用户名，非空唯一
    EMAIL VARCHAR(100) NOT NULL UNIQUE,          -- 邮箱，非空唯一
    NAME VARCHAR(100),                           -- 真实姓名，可为空
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间，自动填充
);