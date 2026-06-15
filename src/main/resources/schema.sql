-- ============================================================
-- Mess 数据库表结构定义脚本 (schema.sql)
-- 
-- 此脚本在应用启动时由Spring Boot自动执行。
-- 仅在spring.jpa.hibernate.ddl-auto=create-drop时使用。
-- 用于定义数据库表结构和约束。
-- 
-- 执行顺序:
-- 1. Hibernate根据实体类自动创建表（ddl-auto=create-drop）
-- 2. Spring Boot执行schema.sql（如果表已存在则跳过）
-- 3. Spring Boot执行data.sql插入初始数据
-- 
-- 注意:
-- - IF NOT EXISTS确保重复执行不会报错
-- - 生产环境建议使用Flyway或Liquibase管理数据库迁移
-- - 此脚本主要用于开发和测试环境
-- 
-- 作者: zhangyaolong.5
-- 创建时间: 2026-05-26
-- ============================================================

-- -----------------------------------------------------------
-- 用户表 (USER)
-- 存储系统用户的基本信息
-- 
-- 字段说明:
-- - ID: 主键，自增，唯一标识每个用户
-- - USERNAME: 用户名，非空且唯一，用于登录
-- - EMAIL: 电子邮箱，非空且唯一，用于通信
-- - NAME: 真实姓名，可为空，用于显示
-- - CREATED_AT: 创建时间，默认当前时间戳
-- 
-- 约束说明:
-- - PRIMARY KEY: 主键约束，确保ID唯一
-- - UNIQUE: 唯一约束，确保USERNAME和EMAIL不重复
-- - NOT NULL: 非空约束，确保必填字段不为空
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS "USER" (
    -- 主键ID，使用自增策略自动生成
    -- BIGINT类型，支持大数量级用户
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- 用户名，VARCHAR(50)限制最大长度
    -- NOT NULL确保必须有值，UNIQUE确保不重复
    USERNAME VARCHAR(50) NOT NULL UNIQUE,
    -- 电子邮箱，VARCHAR(100)限制最大长度
    -- NOT NULL确保必须有值，UNIQUE确保不重复
    EMAIL VARCHAR(100) NOT NULL UNIQUE,
    -- 真实姓名，VARCHAR(100)限制最大长度
    -- 允许为空，用户可选择不填写
    NAME VARCHAR(100),
    -- 创建时间，默认为当前时间戳
    -- 记录用户注册时间，用于审计和统计
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);