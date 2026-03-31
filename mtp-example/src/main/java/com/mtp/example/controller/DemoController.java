package com.mtp.example.controller;

import com.mtp.starter.Mtp;
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

    //@Mtp("business-pool")
    private Executor executor;

    @Mtp("test-spring-pool")
    public void setA (Executor executor) {
        this.executor = executor;
    }

    @GetMapping("/execute")
    public String executeTask() {
        if (executor != null) {
            executor.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " Executing task...");
                    Thread.sleep(getRandomNumber() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println(executor);
            });
            return "Task submitted to business-pool";
        }
        return "Pool not found";
    }

    public static int getRandomNumber() {
        return (int) (Math.random() * 5) + 1;
    }
}