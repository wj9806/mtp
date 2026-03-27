package com.mtp.example.controller;

import com.mtp.core.api.DynamicThreadPoolManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;

/**
 * 示例控制器，用于演示动态线程池的使用
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

/*    private final DynamicThreadPoolManager threadPoolManager;

    public DemoController(DynamicThreadPoolManager threadPoolManager) {
        this.threadPoolManager = threadPoolManager;
    }

    @GetMapping("/execute")
    public String executeTask() {
        Executor executor = threadPoolManager.getExecutor("business-pool");
        if (executor != null) {
            executor.execute(() -> {
                try {
                    Thread.sleep(getRandomNumber() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return "Task submitted to business-pool";
        }
        return "Pool not found";
    }

    public static int getRandomNumber() {
        return (int) (Math.random() * 5) + 1;
    }

    @GetMapping("/status")
    public String getStatus() {
        return threadPoolManager.getAllPoolNames().toString();
    }*/
}