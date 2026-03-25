
MERGE INTO mtp_role (id, role_code, role_name, description, status) KEY(role_code) VALUES (1, 'ADMIN', '管理员', '系统管理员', 'ACTIVE');
MERGE INTO mtp_role (id, role_code, role_name, description, status) KEY(role_code) VALUES (2, 'USER', '普通用户', '普通用户', 'ACTIVE');

MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (1, '应用列表', 'applications', 0, '/applications', 1, 'ACTIVE');
MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (2, '配置管理', 'configs', 0, '/configs', 2, 'ACTIVE');
MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (3, '状态监控', 'status', 0, '/status', 3, 'ACTIVE');
MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (8, '应用注册', 'app-registry', 0, '/app-registry', 4, 'ACTIVE');
MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (4, '系统管理', 'system', 0, '', 5, 'ACTIVE');

MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (5, '用户管理', 'users', 4, '/users', 1, 'ACTIVE');
MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (6, '角色管理', 'roles', 4, '/roles', 2, 'ACTIVE');
MERGE INTO mtp_menu (id, menu_name, menu_code, parent_id, path, order_num, status) KEY(menu_code) VALUES (7, '菜单管理', 'menus', 4, '/menus', 3, 'ACTIVE');

MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (1, 1, 1);
MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (2, 1, 2);
MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (3, 1, 3);
MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (4, 1, 4);
MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (5, 1, 5);
MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (6, 1, 6);
MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (7, 1, 7);
MERGE INTO mtp_role_menu (id, role_id, menu_id) KEY(role_id, menu_id) VALUES (8, 1, 8);