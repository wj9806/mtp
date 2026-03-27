# MTP - 动态可监控线程池

## 项目介绍

MTP（Monitor Thread Pool）是一个基于配置中心的轻量级动态可监控线程池解决方案。后端分为配置注册中心和客户端，Java客户端启动后会向配置注册中心注册线程池的配置。在配置中心修改线程池配置后（例如核心线程数、队列长度、拒绝策略等），注册的客户端线程池配置会同步修改，无需重启应用。

## 技术栈

- **前端**：Vue3 + Element Plus + Axios + Vite
- **后端**：Java 8 + Spring Boot 2.7 + Maven + Netty
- **通信**：HTTP REST API + JSON + Netty TCP长连接

## 项目模块

```
mtp/
├── mtp-parent/              # 父POM，统一依赖管理
├── mtp-core/                # 核心模块，客户端核心代码
│   ├── api/                 # 核心API接口
│   ├── client/              # 客户端实现
│   ├── model/               # 数据模型
│   ├── netty/               # Netty通信模块
│   ├── tp/                  # 自定义线程池执行器
│   └── util/                # 工具类
├── mtp-config-center/       # 配置中心服务端
├── mtp-spring-boot-starter/ # Spring Boot Starter，开箱即用
├── mtp-example/            # 使用示例
└── mtp-ui/                 # Vue3 前端监控界面
```

## 核心架构

### 消息总线

MTP采用发布-订阅模式的消息总线架构，用于组件间的松耦合通信：

- `MessageBus` - 消息总线接口，提供消息发布与订阅功能
- `MessageListener` - 消息监听器接口，支持泛型类型
- `Message` - 消息载体，包含内容和元数据

### Netty通信

客户端与服务端之间采用Netty TCP长连接进行通信：

- `MtpClient` - 客户端Mtp连接管理
- `ClientHandler` - 客户端消息处理器
- `MtpServer` - 服务端Mtp服务器
- `ServerHandler` - 服务端消息处理器
- `MessageHandlerRegistry` - 消息处理器注册表

### 动态线程池

自定义线程池执行器 `TpThreadPoolExecutor` 继承自 `ThreadPoolExecutor`，内置：

- 任务计数统计
- 完成任务计数统计
- 线程池配置引用

## 已实现功能

### 核心功能
- [x] 线程池注册：客户端启动时自动向配置中心注册线程池
- [x] 线程池配置：支持核心线程数、最大线程数、队列容量、存活时间、拒绝策略
- [x] 动态刷新：运行时修改线程池参数，无需重启应用
- [x] 状态上报：客户端定时上报线程池运行状态
- [x] 重试机制：配置中心不可用时自动重试注册和上报
- [x] 消息总线：基于发布-订阅模式的事件通信
- [x] 配置变更推送：服务端Netty推送配置变更通知

### 配置中心
- [x] 应用管理：获取所有注册的应用列表
- [x] 配置管理：注册、更新、删除、查询线程池配置
- [x] 状态管理：接收并存储客户端上报的状态
- [x] 配置广播：向所有客户端广播配置变更

### 客户端特性
- [x] 自动注册：应用启动时自动注册线程池
- [x] 状态报告：定时向配置中心上报线程池状态
- [x] 故障恢复：配置中心恢复后自动重新注册
- [x] 拒绝策略支持：AbortPolicy、DiscardPolicy、DiscardOldestPolicy、CallerRunsPolicy
- [x] Netty长连接：与服务端保持TCP长连接，实时接收配置变更

## 快速开始

### 1. 环境要求

- JDK 8+
- Maven 3.6+
- Node.js 16+ (前端)

### 2. 启动配置中心

```bash
cd mtp-config-center
mvn clean install
mvn spring-boot:run
```

配置中心默认端口：`8080`，Netty服务端端口：`9090`

### 3. 启动示例应用

```bash
cd mtp-example
mvn clean install
mvn spring-boot:run
```

示例应用默认端口：`8081`

### 4. 启动前端

```bash
cd mtp-ui
npm install
npm run dev
```

前端默认访问地址：`http://localhost:5173`

## 配置说明

### 客户端配置（application.yml）

```yaml
mtp:
  application-name: my-app              # 应用名称
  netty-server-host: localhost          # Netty服务器地址
  netty-server-port: 9090               # Netty服务器端口
  status-report-interval: 30            # 状态上报间隔（秒）
```

## 待实现功能

### 高优先级
- [ ] 配置中心高可用：支持多实例部署
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