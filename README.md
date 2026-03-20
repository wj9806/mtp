# MTP - 动态可监控线程池

## 项目介绍

MTP（Monitor Thread Pool）是一个基于配置中心的轻量级动态可监控线程池解决方案。后端分为配置注册中心和客户端，Java客户端启动后会向配置注册中心注册线程池的配置。在配置中心修改线程池配置后（例如核心线程数、队列长度、拒绝策略等），注册的客户端线程池配置会同步修改，无需重启应用。

## 技术栈

- **前端**：Vue3 + Element Plus + Axios + Vite
- **后端**：Java 8 + Spring Boot 2.7 + Maven
- **
- 通信**：HTTP REST API + JSON

## 项目模块

```
mtp/
├── mtp-parent/              # 父POM，统一依赖管理
├── mtp-core/                # 核心模块，客户端核心代码
├── mtp-config-center/       # 配置中心服务端
├── mtp-spring-boot-starter/ # Spring Boot Starter，开箱即用
├── mtp-example/            # 使用示例
└── mtp-ui/                 # Vue3 前端监控界面
```

## 已实现功能

### 核心功能
- [x] 线程池注册：客户端启动时自动向配置中心注册线程池
- [x] 线程池配置：支持核心线程数、最大线程数、队列容量、存活时间、拒绝策略
- [x] 动态刷新：运行时修改线程池参数，无需重启应用
- [x] 状态上报：客户端定时上报线程池运行状态
- [x] 重试机制：配置中心不可用时自动重试注册和上报

### 配置中心
- [x] 应用管理：获取所有注册的应用列表
- [x] 配置管理：注册、更新、删除、查询线程池配置
- [x] 状态管理：接收并存储客户端上报的状态

### 前端界面
- [x] 应用列表：展示所有注册的应用
- [x] 配置管理：查看和编辑线程池配置
- [x] 状态监控：实时展示线程池运行状态

### 客户端特性
- [x] 自动注册：应用启动时自动注册线程池
- [x] 状态报告：定时向配置中心上报线程池状态
- [x] 故障恢复：配置中心恢复后自动重新注册
- [x] 拒绝策略支持：AbortPolicy、DiscardPolicy、DiscardOldestPolicy、CallerRunsPolicy

## 待实现功能

### 高优先级
- [ ] 配置中心高可用：支持多实例部署
- [ ] 配置变更推送：配置中心支持主动推送配置变更，而非客户端轮询
- [ ] 配置版本管理：支持配置历史记录和回滚
- [ ] 告警功能：线程池负载过高时发送告警通知

### 中优先级
- [ ] 监控指标：增加更多监控指标，如任务等待时间、线程平均执行时间等
- [ ] 分布式追踪：支持跨多个实例的线程池监控
- [ ] 配置校验：前端对配置参数进行合法性校验
- [ ] 批量操作：支持批量修改多个线程池配置

### 低优先级
- [ ] 性能优化：减少状态上报的网络开销
- [ ] 安全认证：增加认证和授权机制
- [ ] 数据持久化：配置中心支持数据库存储
- [ ] 集群支持：支持客户端多实例部署的场景

## 快速开始

### 1. 启动配置中心

```bash
cd mtp-config-center
mvn clean install
mvn spring-boot:run
```

配置中心默认端口：`8080`

### 2. 启动示例应用

```bash
cd mtp-example
mvn clean install
mvn spring-boot:run
```

示例应用默认端口：`8081`

### 3. 启动前端

```bash
cd mtp-ui
npm install
npm run dev
```

前端默认端口：`3000`

### 4. 客户端集成

在您的Spring Boot项目中添加依赖：

```xml
<dependency>
    <groupId>com.mtp</groupId>
    <artifactId>mtp-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

配置application.properties：

```properties
mtp.application-name=your-app-name
mtp.config-center-url=http://localhost:8080
mtp.enabled=true
mtp.status-report-interval=30
```

## API 接口

### 配置中心API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/applications` | GET | 获取所有应用列表 |
| `/api/config/register` | POST | 注册线程池配置 |
| `/api/config/unregister` | DELETE | 取消注册线程池 |
| `/api/config/update` | PUT | 更新线程池配置 |
| `/api/config/get` | GET | 获取单个线程池配置 |
| `/api/config/list` | GET | 获取应用的所有线程池配置 |
| `/api/status/report` | POST | 上报线程池状态 |
| `/api/status/list` | GET | 获取线程池状态列表 |

## 配置说明

### 线程池配置参数

| 参数 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| poolName | String | 线程池名称 | - |
| corePoolSize | Integer | 核心线程数 | - |
| maxPoolSize | Integer | 最大线程数 | - |
| queueCapacity | Integer | 队列容量 | - |
| keepAliveSeconds | Integer | 存活时间(秒) | - |
| rejectedPolicy | String | 拒绝策略 | abort |

### 拒绝策略

| 策略 | 说明 |
|------|------|
| abort | 抛出RejectedExecutionException异常（默认） |
| discard | 直接丢弃任务 |
| discard-oldest | 丢弃队列最前面的任务 |
| caller-runs | 由调用线程执行任务 |

## License

MIT
