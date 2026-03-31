package com.mtp.example.config;

import com.mtp.starter.Mtp;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@Mtp("my-pool")
public class MyPool extends ThreadPoolExecutor {

    public MyPool() {
        super(1, 5, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }
}
