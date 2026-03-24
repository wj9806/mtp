package com.mtp.core.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtil {

    /**
     * 计算线程池中阻塞队列容量
     */
    public static Integer blockingQueueCapacity(Executor executor) {
        ThreadPoolExecutor threadPoolExecutor = null;
        if (executor instanceof ThreadPoolExecutor) {
            threadPoolExecutor = (ThreadPoolExecutor) executor;
        } else if (executor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor e = (ThreadPoolTaskExecutor) executor;
            return e.getQueueCapacity();
        } else {
            return 0;
        }
        BlockingQueue<Runnable> queue = threadPoolExecutor.getQueue();
        return queue.size() + queue.remainingCapacity();
    }

}
