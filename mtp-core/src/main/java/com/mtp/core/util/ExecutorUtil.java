package com.mtp.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ClassUtils;

import java.util.concurrent.*;

@Slf4j
public class ExecutorUtil {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("executor-util-thread");
        return thread;
    });

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

    public static boolean isExecutor(Class<?> type) {
        return Executor.class.isAssignableFrom(type);
    }

    public static boolean isExecutor(String className, ClassLoader cl) throws ClassNotFoundException {
        return isExecutor(ClassUtils.forName(className, cl));
    }

    public static boolean isExecutor(Object bean) {
        return bean instanceof Executor;
    }

    public static void gracefulShutdown(ExecutorService threadPool,
                                        int timeout,
                                        TimeUnit unit) {

        executorService.submit(() -> {
            // 1. 禁止提交新任务
            threadPool.shutdown();

            try {
                // 2. 等待现有任务完成
                if (!threadPool.awaitTermination(timeout, unit)) {
                    // 3. 超时后尝试强制关闭
                    threadPool.shutdownNow();

                    // 4. 等待强制关闭完成
                    if (!threadPool.awaitTermination(timeout, unit)) {
                        log.warn("threadPool terminate timeout");
                    }
                }
            } catch (InterruptedException e) {
                // 5. 中断时也尝试强制关闭
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }
}
