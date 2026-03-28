package top.wmd001.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.wmd001.domain.FileTaskRequest;
import top.wmd001.domain.ThreadPoolStats;
import top.wmd001.service.FileTaskService;
import top.wmd001.service.ThreadPoolService;

import java.util.concurrent.CompletableFuture;

/**
 * 线程池管理控制器
 * 提供线程池状态查询、任务提交等REST接口
 */
@RestController
@RequestMapping("/api/thread-pools")
@Tag(name = "线程池管理", description = "线程池配置、状态监控和任务管理")
public class ThreadPoolController {

    private final ThreadPoolService threadPoolService;
    private final FileTaskService fileTaskService;

    public ThreadPoolController(ThreadPoolService threadPoolService,
                                FileTaskService fileTaskService) {
        this.threadPoolService = threadPoolService;
        this.fileTaskService = fileTaskService;
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @ApiResponse(responseCode = "200", description = "服务正常")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Thread Pool Service is running at " + System.currentTimeMillis());
    }

    @GetMapping("/stats")
    @Operation(summary = "获取所有线程池状态", description = "返回默认、文件解析、字数分析三个线程池的实时统计信息")
    public ResponseEntity<ThreadPoolStats[]> getAllThreadPoolStats() {
        return ResponseEntity.ok(threadPoolService.getAllPoolStats());
    }

    @GetMapping("/stats/{poolName}")
    @Operation(summary = "获取指定线程池状态")
    public ResponseEntity<ThreadPoolStats> getThreadPoolStats(
            @Parameter(description = "线程池名称: default, file-parsing, word-count")
            @PathVariable String poolName) {

        ThreadPoolStats stats = switch (poolName.toLowerCase()) {
            case "default" -> threadPoolService.getDefaultPoolStats();
            case "file-parsing" -> threadPoolService.getFileParsingPoolStats();
            case "word-count" -> threadPoolService.getWordCountPoolStats();
            default -> throw new IllegalArgumentException("未知的线程池名称: " + poolName);
        };

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/tasks/file-parsing")
    @Operation(summary = "提交文件解析任务", description = "使用文件解析专用线程池处理任务")
    public CompletableFuture<ResponseEntity<String>> submitFileParsingTask(
            @RequestBody FileTaskRequest request) {

        // 验证任务类型
        if (request.taskType() != FileTaskRequest.TaskType.FILE_PARSING) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body("任务类型必须是 FILE_PARSING")
            );
        }

        return fileTaskService.processFileParsingTask(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError()
                        .body("任务处理失败: " + e.getMessage()));
    }

    @PostMapping("/tasks/word-count")
    @Operation(summary = "提交字数分析任务", description = "使用字数分析专用线程池处理任务")
    public CompletableFuture<ResponseEntity<String>> submitWordCountTask(
            @RequestBody FileTaskRequest request) {

        if (request.taskType() != FileTaskRequest.TaskType.WORD_COUNT) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body("任务类型必须是 WORD_COUNT")
            );
        }

        return fileTaskService.processWordCountTask(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError()
                        .body("任务处理失败: " + e.getMessage()));
    }

    @PostMapping("/tasks/virtual-thread")
    @Operation(summary = "提交虚拟线程任务", description = "使用虚拟线程执行器处理任务（如果启用）")
    public CompletableFuture<ResponseEntity<String>> submitVirtualThreadTask(
            @RequestBody FileTaskRequest request) {

        return fileTaskService.processWithVirtualThread(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError()
                        .body("虚拟线程任务失败: " + e.getMessage()));
    }

    @PostMapping("/tasks/batch")
    @Operation(summary = "批量提交任务", description = "同时提交多个不同类型的任务，演示线程池隔离效果")
    public ResponseEntity<String> submitBatchTasks() {
        String report = fileTaskService.submitBatchTasks();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/demo/cpu-intensive")
    @Operation(summary = "演示CPU密集型任务", description = "执行一个模拟的CPU密集型计算任务")
    public ResponseEntity<String> demoCpuIntensiveTask() {
        String result = threadPoolService.simulateCpuIntensiveTask();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/demo/io-intensive")
    @Operation(summary = "演示I/O密集型任务", description = "执行一个模拟的I/O密集型任务")
    public ResponseEntity<String> demoIoIntensiveTask() {
        String result = threadPoolService.simulateIoIntensiveTask();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tomcat/config")
    @Operation(summary = "查看Tomcat配置", description = "返回当前Tomcat线程池的配置信息")
    public ResponseEntity<String> getTomcatConfig() {
        // 从系统属性或环境变量获取Tomcat配置
        String config = String.format("""
                Tomcat 线程池配置:
                - server.tomcat.max-threads: %s
                - server.tomcat.min-spare-threads: %s
                - server.tomcat.max-connections: %s
                - server.tomcat.accept-count: %s
                - server.tomcat.connection-timeout: %s ms
                """,
                System.getProperty("server.tomcat.max-threads", "200"),
                System.getProperty("server.tomcat.min-spare-threads", "20"),
                System.getProperty("server.tomcat.max-connections", "10000"),
                System.getProperty("server.tomcat.accept-count", "100"),
                System.getProperty("server.tomcat.connection-timeout", "30000")
        );

        return ResponseEntity.ok(config);
    }
}