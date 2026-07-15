-- ============================================================
-- 初始数据 - 仅开发环境使用
-- ============================================================
-- 本文件在schema.sql执行后由Spring Boot自动执行，插入测试用户数据。
-- 注意：表名"USER"必须加双引号，原因同schema.sql（H2保留字）。
--
-- 这些记录与SecurityConfig中InMemoryUserDetailsManager的认证用户是独立的：
--   - SecurityConfig的user/admin是认证账号（用于登录）
--   - 这里的admin/john/jane是业务数据（/api/users接口返回）
-- 生产环境应通过迁移脚本或管理后台初始化数据，不应硬编码在SQL中。
-- ============================================================
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('admin', 'admin@example.com', 'Administrator', CURRENT_TIMESTAMP());
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('john', 'john@example.com', 'John Doe', CURRENT_TIMESTAMP());
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('jane', 'jane@example.com', 'Jane Smith', CURRENT_TIMESTAMP());