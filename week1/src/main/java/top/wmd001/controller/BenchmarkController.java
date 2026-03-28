package top.wmd001.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wmd001.domain.BenchmarkResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能压测控制器
 * 演示Java 21新特性（虚拟线程、结构化并发）进行性能测试
 */
@RestController
@RequestMapping("/api/benchmark")
@Tag(name = "性能压测", description = "线程池性能对比测试")
public class BenchmarkController {

    /**
     * 执行简单压测，对比不同配置下的性能
     */
    @GetMapping("/simple")
    @Operation(summary = "简单压测对比", description = "对比默认线程池、虚拟线程、专用线程池的性能")
    public ResponseEntity<List<BenchmarkResult>> runSimpleBenchmark(
            @RequestParam(defaultValue = "100") int totalRequests,
            @RequestParam(defaultValue = "10") int concurrentThreads) {

        List<BenchmarkResult> results = new ArrayList<>();

        // 测试1：默认线程池（平台线程）
        results.add(runBenchmarkTest("默认线程池", totalRequests, concurrentThreads,
                Executors.newFixedThreadPool(concurrentThreads)));

        // 测试2：虚拟线程（如果支持）
        try {
            results.add(runBenchmarkTest("虚拟线程", totalRequests, concurrentThreads,
                    Executors.newVirtualThreadPerTaskExecutor()));
        } catch (Exception e) {
            System.out.println("虚拟线程测试跳过: " + e.getMessage());
        }

        // 测试3：专用线程池（较小核心数）
        results.add(runBenchmarkTest("专用线程池(核心4)", totalRequests, concurrentThreads,
                Executors.newFixedThreadPool(4)));

        // 测试4：缓存线程池
        results.add(runBenchmarkTest("缓存线程池", totalRequests, concurrentThreads,
                Executors.newCachedThreadPool()));

        return ResponseEntity.ok(results);
    }

    /**
     * 执行单个压测配置
     */
    private BenchmarkResult runBenchmarkTest(String configName, int totalRequests,
                                             int concurrentThreads, ExecutorService executor) {

        System.out.println("开始压测: " + configName);
        long startTime = System.currentTimeMillis();
        AtomicLong completedRequests = new AtomicLong(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        List<Long> responseTimes = new ArrayList<>();

        try {
            // 使用结构化并发（Java 21预览）提交任务
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < totalRequests; i++) {
                final int requestId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    long requestStart = System.currentTimeMillis();

                    // 模拟任务处理：混合CPU和I/O
                    try {
                        // 模拟I/O等待（大部分请求）
                        if (requestId % 10 != 0) {
                            Thread.sleep(50 + (int)(Math.random() * 100));
                        } else {
                            // 10%的请求是CPU密集型
                            long sum = 0;
                            for (int j = 0; j < 10000; j++) {
                                sum += j * j;
                            }
                        }

                        long responseTime = System.currentTimeMillis() - requestStart;
                        responseTimes.add(responseTime);
                        totalResponseTime.addAndGet(responseTime);
                        completedRequests.incrementAndGet();

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, executor);

                futures.add(future);

                // 控制并发度
                if (futures.size() >= concurrentThreads) {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    futures.clear();
                }
            }

            // 等待剩余任务完成
            if (!futures.isEmpty()) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double throughput = totalTime > 0 ? (completedRequests.get() * 1000.0) / totalTime : 0;
        double avgResponseTime = completedRequests.get() > 0 ?
                totalResponseTime.get() / (double) completedRequests.get() : 0;

        // 计算P95响应时间
        double p95ResponseTime = calculateP95(responseTimes);

        System.out.printf("压测完成 %s: 吞吐量=%.2f req/s, 平均响应=%.2f ms, P95=%.2f ms%n",
                configName, throughput, avgResponseTime, p95ResponseTime);

        return new BenchmarkResult(
                configName,
                completedRequests.get(),
                throughput,
                avgResponseTime,
                p95ResponseTime,
                concurrentThreads,
                100 // 模拟队列容量
        );
    }

    /**
     * 计算响应时间列表的P95值
     */
    private double calculateP95(List<Long> responseTimes) {
        if (responseTimes.isEmpty()) {
            return 0.0;
        }

        List<Long> sorted = new ArrayList<>(responseTimes);
        sorted.sort(Long::compareTo);

        int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));

        return sorted.get(index);
    }

    /**
     * 模拟Tomcat不同配置下的性能对比
     */
    @GetMapping("/tomcat-comparison")
    @Operation(summary = "Tomcat配置对比", description = "对比不同Tomcat线程池配置的性能")
    public ResponseEntity<String> tomcatConfigurationComparison() {
        StringBuilder report = new StringBuilder();
        report.append("Tomcat线程池配置性能对比分析（基于8核CPU服务器）\n");
        report.append("=".repeat(60)).append("\n\n");

        report.append("1. 配置A: 最大线程=200 (推荐I/O密集型)\n");
        report.append("   - 适用场景: 电商系统、高并发API服务\n");
        report.append("   - 优点: 能处理大量并发连接，减少连接拒绝\n");
        report.append("   - 风险: 线程上下文切换开销增加\n");
        report.append("   - 建议队列容量: 50-100\n\n");

        report.append("2. 配置B: 最大线程=50 (CPU密集型优化)\n");
        report.append("   - 适用场景: 计算密集型服务、批处理任务\n");
        report.append("   - 优点: 减少线程竞争，提高CPU利用率\n");
        report.append("   - 风险: 高并发时容易排队，响应时间变长\n");
        report.append("   - 建议队列容量: 20-50\n\n");

        report.append("3. 配置C: 虚拟线程 (Java 21+)\n");
        report.append("   - 适用场景: 高并发I/O密集型，需要同步编程模型\n");
        report.append("   - 优点: 百万级并发，极低内存开销\n");
        report.append("   - 限制: synchronized块可能导致线程固定\n");
        report.append("   - 建议载体线程数: CPU核心数\n\n");

        report.append("单机多应用部署建议:\n");
        report.append("- 每个应用独立线程池，避免相互干扰\n");
        report.append("- 根据应用类型分配资源: \n");
        report.append("  • I/O密集型: 较多线程，较小队列\n");
        report.append("  • CPU密集型: 较少线程，较大队列\n");
        report.append("- 监控指标: 线程池活跃度、队列长度、拒绝任务数\n");
        report.append("- 动态调整: 根据实际负载周期性优化参数\n");

        return ResponseEntity.ok(report.toString());
    }

    /**
     * 获取最佳实践配置清单
     */
    @GetMapping("/best-practices")
    @Operation(summary = "最佳实践配置清单", description = "返回针对不同场景的线程池配置建议")
    public ResponseEntity<String> getBestPracticeConfigurations() {
        String configs = """
                # 线程池最佳实践配置清单
                
                ## 1. Tomcat连接池 (8核CPU服务器)
                
                ### 场景A: 电商API服务 (I/O密集型)
                server.tomcat:
                  max-threads: 200
                  min-spare-threads: 20
                  max-connections: 10000
                  accept-count: 100
                  connection-timeout: 30000
                
                ### 场景B: 计算服务 (CPU密集型)
                server.tomcat:
                  max-threads: 50
                  min-spare-threads: 10
                  max-connections: 1000
                  accept-count: 50
                  connection-timeout: 10000
                
                ## 2. Spring Boot异步任务池
                
                ### 默认异步池 (通用场景)
                spring.task.execution.default:
                  core-size: 8          # CPU核心数
                  max-size: 20
                  queue-capacity: 50
                  keep-alive-seconds: 60
                
                ### 文件解析专用池 (I/O密集型，慢任务)
                spring.task.execution.file-parsing:
                  core-size: 4
                  max-size: 10
                  queue-capacity: 100   # 慢任务需要较大队列
                  keep-alive-seconds: 120
                
                ### 字数分析专用池 (CPU密集型)
                spring.task.execution.word-count:
                  core-size: 2
                  max-size: 6
                  queue-capacity: 50
                  keep-alive-seconds: 60
                
                ## 3. 虚拟线程配置 (Java 21+)
                spring.threads.virtual.enabled: true
                
                ## 4. 单机多应用部署策略
                
                ### 资源分配公式:
                - 总线程数限制 = CPU核心数 × 200 (经验值)
                - 每个应用分配 = (应用重要性权重 × 总线程数) / 总权重
                - 监控指标阈值:
                  • 线程使用率 > 80% → 考虑扩容
                  • 队列长度 > 队列容量×80% → 优化或增加线程
                  • 任务拒绝率 > 1% → 调整参数
                
                ### 示例: 3个应用部署 (电商API、批处理、管理后台)
                1. 电商API (权重: 5): 最大线程=120, 队列=80
                2. 批处理 (权重: 3): 最大线程=50, 队列=150
                3. 管理后台 (权重: 2): 最大线程=30, 队列=30
                """;

        return ResponseEntity.ok(configs);
    }
}