package com.mtp.core.mtp;

import com.mtp.core.model.ThreadPoolConfig;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * 自定义线程池执行器，扩展ThreadPoolExecutor以支持任务计数和完成计数
 */
public class MtpThreadPoolExecutor extends ThreadPoolExecutor {

    private final ThreadPoolConfig config;

    public MtpThreadPoolExecutor(ThreadPoolConfig config,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory) {
        super(config.getCorePoolSize(), config.getMaxPoolSize(), config.getKeepAliveSeconds(), unit,
                workQueue, threadFactory, config.getRejectedExecutionHandler());
        this.config = config;
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }


    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
    }

    public ThreadPoolConfig getConfig() {
        return config;
    }
}