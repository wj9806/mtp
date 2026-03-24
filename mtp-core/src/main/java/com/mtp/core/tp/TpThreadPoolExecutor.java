package com.mtp.core.tp;

import com.mtp.core.model.ThreadPoolConfig;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义线程池执行器，扩展ThreadPoolExecutor以支持任务计数和完成计数
 */
public class TpThreadPoolExecutor extends ThreadPoolExecutor {

    private final ThreadPoolConfig config;
    private final AtomicLong taskCounter;
    private final AtomicLong completedTaskCounter;

    public TpThreadPoolExecutor(ThreadPoolConfig config,
                                TimeUnit unit,
                                BlockingQueue<Runnable> workQueue,
                                ThreadFactory threadFactory) {
        super(config.getCorePoolSize(), config.getMaxPoolSize(), config.getKeepAliveSeconds(), unit,
                workQueue, threadFactory, config.getRejectedExecutionHandler());
        this.config = config;
        this.taskCounter = new AtomicLong(0);
        this.completedTaskCounter = new AtomicLong(0);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }


    @Override
    public void execute(Runnable command) {
        taskCounter.incrementAndGet();
        super.execute(command);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        completedTaskCounter.incrementAndGet();
    }

    public ThreadPoolConfig getConfig() {
        return config;
    }

    public long getTaskCount() {
        return taskCounter.get();
    }

    public long getCompletedTaskCount() {
        return completedTaskCounter.get();
    }
}