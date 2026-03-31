CREATE TABLE IF NOT EXISTS thread_pool_config (
    id VARCHAR(255) PRIMARY KEY COMMENT '主键，格式为 instanceId:poolName',
    instance_id VARCHAR(255) COMMENT '实例ID，格式为 applicationName@ip:port 的 MD5 值',
    pool_name VARCHAR(255) COMMENT '线程池名称',
    application_name VARCHAR(255) COMMENT '应用名称',
    core_pool_size INT COMMENT '核心线程数',
    max_pool_size INT COMMENT '最大线程数',
    queue_capacity INT COMMENT '队列容量',
    keep_alive_seconds BIGINT COMMENT '线程存活时间（秒）',
    rejected_policy VARCHAR(255) COMMENT '拒绝策略',
    ip VARCHAR(255) COMMENT '实例 IP 地址',
    port INT COMMENT '实例端口',
    register_time BIGINT COMMENT '注册时间戳',
    update_time BIGINT COMMENT '最后更新时间戳'
);

CREATE TABLE IF NOT EXISTS thread_pool_status (
    id VARCHAR(255) PRIMARY KEY COMMENT '主键，格式为 instanceId:poolName',
    instance_id VARCHAR(255) COMMENT '实例ID',
    pool_name VARCHAR(255) COMMENT '线程池名称',
    application_name VARCHAR(255) COMMENT '应用名称',
    ip VARCHAR(255) COMMENT '实例 IP 地址',
    port INT COMMENT '实例端口',
    core_pool_size INT COMMENT '核心线程数',
    max_pool_size INT COMMENT '最大线程数',
    active_count INT COMMENT '活跃线程数',
    pool_size INT COMMENT '当前线程池大小',
    task_count BIGINT COMMENT '总任务数',
    completed_task_count BIGINT COMMENT '已完成任务数',
    queue_size INT COMMENT '队列当前大小',
    queue_capacity INT COMMENT '队列容量',
    update_time BIGINT COMMENT '最后更新时间戳'
);

CREATE TABLE IF NOT EXISTS client_registry (
    id VARCHAR(255) PRIMARY KEY COMMENT '主键，instanceId',
    instance_id VARCHAR(255) COMMENT '实例ID，格式为 applicationName@ip:port 的 MD5 值',
    ip VARCHAR(255) COMMENT '实例 IP 地址',
    port INT COMMENT '实例端口',
    application_name VARCHAR(255) COMMENT '应用名称',
    status VARCHAR(20) COMMENT '客户端状态：ONLINE-在线，OFFLINE-离线',
    create_time BIGINT COMMENT '注册时间戳',
    report_time BIGINT COMMENT '最后上报时间戳'
);

CREATE TABLE IF NOT EXISTS mtp_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(100) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-未激活',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

CREATE TABLE IF NOT EXISTS mtp_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-未激活',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

CREATE TABLE IF NOT EXISTS mtp_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

CREATE TABLE IF NOT EXISTS mtp_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(100) NOT NULL COMMENT '菜单代码',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    path VARCHAR(255) COMMENT '路由路径',
    order_num INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-未激活',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

CREATE TABLE IF NOT EXISTS mtp_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

CREATE TABLE IF NOT EXISTS application_registry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    application_name VARCHAR(100) NOT NULL UNIQUE COMMENT '应用名称',
    application_info VARCHAR(500) COMMENT '应用信息',
    access_token VARCHAR(64) NOT NULL COMMENT '访问令牌',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);