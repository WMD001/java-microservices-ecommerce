package com.wmd001.config;

import com.wmd001.domain.entity.UserCreationRequest;
import com.wmd001.service.PerformanceTestService;
import com.wmd001.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
public class PerformanceTestRunner implements CommandLineRunner {

//    @Autowired
    private PerformanceTestService performanceTestService;

    @Override
    public void run(String... args) throws Exception {
        // 生成测试数据
        List<UserCreationRequest> requests = generateTestRequests(1000);

        // 虚拟线程测试
        long startTime = System.currentTimeMillis();
        performanceTestService.batchCreateWithVirtualThreads(requests).join();
        long virtualThreadTime = System.currentTimeMillis() - startTime;

        // 传统线程池测试
        startTime = System.currentTimeMillis();
        performanceTestService.batchCreateWithFixedThreads(requests).join();
        long fixedThreadTime = System.currentTimeMillis() - startTime;

        // 输出结果
        System.out.println("=== 性能对比结果 ===");
        System.out.println("虚拟线程耗时: " + virtualThreadTime + "ms");
        System.out.println("传统线程池耗时: " + fixedThreadTime + "ms");
        System.out.println("性能提升: " +
                String.format("%.2f", (fixedThreadTime - virtualThreadTime) * 100.0 / fixedThreadTime) + "%");
    }

    private List<UserCreationRequest> generateTestRequests(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new UserCreationRequest(
                        "user_" + i + "_" + System.currentTimeMillis(),
                        "user" + i + "_" + System.currentTimeMillis() + "@example.com",
                        "password123"
                ))
                .toList();
    }
}
