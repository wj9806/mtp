package com.mtp.core.mtp;

import java.util.concurrent.*;

public class MtpThreadPoolExecutorProxy extends ThreadPoolExecutor {

    public MtpThreadPoolExecutorProxy(ThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS,
                executor.getQueue(), executor.getThreadFactory(),
                executor.getRejectedExecutionHandler());

        allowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
    }
}
