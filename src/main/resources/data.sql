-- ============================================================
-- Mess 数据库初始数据脚本 (data.sql)
-- 
-- 此脚本在应用启动时由Spring Boot自动执行。
-- 在schema.sql执行完毕后运行，用于插入初始数据。
-- 
-- 执行条件:
-- - spring.jpa.hibernate.ddl-auto=create-drop时每次启动都执行
-- - spring.jpa.defer-datasource-initialization=true时确保顺序正确
-- 
-- 初始数据说明:
-- - admin: 管理员账户，拥有系统管理权限
-- - john: 普通用户账户，用于演示和测试
-- - jane: 普通用户账户，用于演示和测试
-- 
-- 注意:
-- - 此脚本仅在开发环境使用，生产环境不应使用
-- - 密码由Spring Security配置管理，不在此表中存储
-- - CURRENT_TIMESTAMP()自动填充创建时间
-- 
-- 作者: zhangyaolong.5
-- 创建时间: 2026-05-26
-- ============================================================

-- 插入管理员用户
-- 用户名: admin，邮箱: admin@example.com，姓名: Administrator
-- 对应Spring Security配置中的admin用户（密码: admin，角色: ADMIN）
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('admin', 'admin@example.com', 'Administrator', CURRENT_TIMESTAMP());

-- 插入普通用户1
-- 用户名: john，邮箱: john@example.com，姓名: John Doe
-- 对应Spring Security配置中的user用户（密码: password，角色: USER）
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('john', 'john@example.com', 'John Doe', CURRENT_TIMESTAMP());

-- 插入普通用户2
-- 用户名: jane，邮箱: jane@example.com，姓名: Jane Smith
-- 用于演示和测试的额外用户数据
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('jane', 'jane@example.com', 'Jane Smith', CURRENT_TIMESTAMP());