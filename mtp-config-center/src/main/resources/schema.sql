CREATE TABLE IF NOT EXISTS thread_pool_config (
    id VARCHAR(255) PRIMARY KEY COMMENT '主键，格式为 instanceId:poolName',
    instance_id VARCHAR(255) COMMENT '实例ID，格式为 applicationName@ip:port 的 MD5 值',
    pool_name VARCHAR(255) COMMENT '线程池名称',
    application_name VARCHAR(255) COMMENT '应用名称',
    core_pool_size INT COMMENT '核心线程数',
    max_pool_size INT COMMENT '最大线程数',
    queue_capacity INT COMMENT '队列容量',
    keep_alive_seconds INT COMMENT '线程存活时间（秒）',
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