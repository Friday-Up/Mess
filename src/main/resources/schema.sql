-- ============================================================
-- 数据库表结构定义
-- ============================================================
-- 本文件在Spring Boot启动时由schema.sql机制自动执行，先于data.sql。
-- 需配合 application.yml 中 spring.jpa.defer-datasource-initialization=true，
-- 否则JPA的ddl-auto会先删表再建表，导致本文件执行时表已存在或被覆盖。
--
-- 表名"USER"加双引号的原因：USER是H2/SQL标准保留字，不加引号会语法报错。
-- 生产环境建议改用 t_user 等非保留字表名，或使用Flyway/Liquibase管理迁移。
--
-- 字段与User实体(@Entity)一一对应，JPA通过@Table(name="USER")映射。
-- ============================================================
CREATE TABLE IF NOT EXISTS "USER" (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,        -- 主键，自增，对应User.id
    USERNAME VARCHAR(50) NOT NULL UNIQUE,        -- 用户名，非空唯一，对应User.username
    EMAIL VARCHAR(100) NOT NULL UNIQUE,          -- 邮箱，非空唯一，对应User.email
    NAME VARCHAR(100),                           -- 真实姓名，可为空，对应User.name
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间，自动填充，对应User.createdAt
);