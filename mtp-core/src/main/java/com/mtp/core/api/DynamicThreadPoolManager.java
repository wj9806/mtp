package com.mtp.core.api;

import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池管理器接口，提供线程池的注册、注销、刷新等功能
 */
public interface DynamicThreadPoolManager {

    void registerPool(String poolName, ThreadPoolConfig config);

    void registerPool(String poolName, ThreadPoolExecutor executor);

    void unregisterPool(String poolName);

    Executor getExecutor(String poolName);

    void refreshPool(String poolName, ThreadPoolConfig config);

    void reportStatus();

    List<ThreadPoolStatus> getAllStatuses();

    List<String> getAllPoolNames();

    ThreadPoolStatus getPoolStatus(String poolName);
}