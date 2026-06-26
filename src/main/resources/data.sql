-- 初始数据 - 仅开发环境使用，对应SecurityConfig中的用户配置
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('admin', 'admin@example.com', 'Administrator', CURRENT_TIMESTAMP());
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('john', 'john@example.com', 'John Doe', CURRENT_TIMESTAMP());
INSERT INTO "USER" (USERNAME, EMAIL, NAME, CREATED_AT) VALUES ('jane', 'jane@example.com', 'Jane Smith', CURRENT_TIMESTAMP());