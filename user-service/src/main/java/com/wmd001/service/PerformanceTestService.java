package com.wmd001.service;

import com.wmd001.domain.entity.UserCreationRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PerformanceTestService {

    private final UserService userService;

    private final ExecutorService virtualThreadExecutor =
            Executors.newVirtualThreadPerTaskExecutor();
    private final ExecutorService fixedThreadExecutor =
            Executors.newFixedThreadPool(20);

    public PerformanceTestService(UserService userService) {
        this.userService = userService;
    }

    // 使用虚拟线程批量创建用户
    public CompletableFuture<Void> batchCreateWithVirtualThreads(List<UserCreationRequest> requests) {
        return CompletableFuture.allOf(
                requests.stream()
                        .map(request -> CompletableFuture.runAsync(
                                () -> userService.createUser(
                                        request.username(),
                                        request.email(),
                                        request.password()
                                ), virtualThreadExecutor))
                        .toArray(CompletableFuture[]::new)
        );
    }

    // 使用传统线程池批量创建用户
    public CompletableFuture<Void> batchCreateWithFixedThreads(List<UserCreationRequest> requests) {
        return CompletableFuture.allOf(
                requests.stream()
                        .map(request -> CompletableFuture.runAsync(
                                () -> userService.createUser(
                                        request.username(),
                                        request.email(),
                                        request.password()
                                ), fixedThreadExecutor))
                        .toArray(CompletableFuture[]::new)
        );
    }
}
