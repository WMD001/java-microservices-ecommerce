package top.wmd001.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.wmd001.domain.FileTaskRequest;

import java.util.concurrent.CompletableFuture;

/**
 * 文件任务处理服务
 * 演示不同任务类型使用不同的线程池执行器
 */
@Service
public class FileTaskService {

    private static final Logger log = LoggerFactory.getLogger(FileTaskService.class);

    /**
     * 处理文件批量解析任务（使用文件解析专用线程池）
     * @Async 指定使用 fileParsingTaskExecutor
     */
    @Async("fileParsingTaskExecutor")
    public CompletableFuture<String> processFileParsingTask(FileTaskRequest request) {
        log.info("文件解析任务开始: {} (线程: {})", request.fileName(), Thread.currentThread().getName());

        // 模拟文件解析过程（I/O密集型）
        try {
            // 模拟读取文件、解析内容
            Thread.sleep(200 + (int)(Math.random() * 300));

            // 模拟解析结果
            String result = String.format("文件解析完成: %s，提取到 %d 条记录",
                    request.fileName(), 100 + (int)(Math.random() * 900));

            log.info("文件解析任务完成: {}", request.fileName());
            return CompletableFuture.completedFuture(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("文件解析任务被中断: {}", request.fileName());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 处理文件字数分析任务（使用字数分析专用线程池）
     * @Async 指定使用 wordCountTaskExecutor
     */
    @Async("wordCountTaskExecutor")
    public CompletableFuture<String> processWordCountTask(FileTaskRequest request) {
        log.info("字数分析任务开始: {} (线程: {})", request.fileName(), Thread.currentThread().getName());

        // 模拟字数分析过程（CPU密集型）
        long start = System.currentTimeMillis();
        int wordCount = 0;

        // 模拟文本处理
        for (int i = 0; i < 500000 + (int)(Math.random() * 500000); i++) {
            wordCount += String.valueOf(i).length();
        }

        long duration = System.currentTimeMillis() - start;
        String result = String.format("字数分析完成: %s，总字数: %d，耗时: %d ms",
                request.fileName(), wordCount, duration);

        log.info("字数分析任务完成: {}，耗时: {} ms", request.fileName(), duration);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 使用虚拟线程处理任务（如果启用）
     * @Async 指定使用 virtualThreadTaskExecutor
     */
    @Async("virtualThreadTaskExecutor")
    public CompletableFuture<String> processWithVirtualThread(FileTaskRequest request) {
        log.info("虚拟线程任务开始: {} (线程: {})", request.fileName(), Thread.currentThread().getName());

        // 模拟任务处理
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String result = String.format("虚拟线程任务完成: %s，线程类型: %s",
                request.fileName(), Thread.currentThread().isVirtual() ? "虚拟线程" : "平台线程");

        log.info("虚拟线程任务完成: {}", request.fileName());
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 使用默认线程池处理任务
     * @Async 不指定，使用默认执行器
     */
    @Async
    public CompletableFuture<String> processWithDefaultExecutor(FileTaskRequest request) {
        log.info("默认线程池任务开始: {} (线程: {})", request.fileName(), Thread.currentThread().getName());

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String result = String.format("默认线程池任务完成: %s", request.fileName());
        log.info("默认线程池任务完成: {}", request.fileName());
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 批量提交任务，演示不同线程池的隔离效果
     */
    public String submitBatchTasks() {
        StringBuilder report = new StringBuilder("批量任务提交报告:\n");

        // 提交文件解析任务
        for (int i = 1; i <= 5; i++) {
            FileTaskRequest request = new FileTaskRequest(
                    "data-file-" + i + ".csv",
                    FileTaskRequest.TaskType.FILE_PARSING,
                    i
            );
            processFileParsingTask(request);
            report.append("  ✓ 提交文件解析任务: ").append(request.fileName()).append("\n");
        }

        // 提交字数分析任务
        for (int i = 1; i <= 5; i++) {
            FileTaskRequest request = new FileTaskRequest(
                    "text-doc-" + i + ".txt",
                    FileTaskRequest.TaskType.WORD_COUNT,
                    i
            );
            processWordCountTask(request);
            report.append("  ✓ 提交字数分析任务: ").append(request.fileName()).append("\n");
        }

        // 提交虚拟线程任务（如果启用）
        try {
            FileTaskRequest request = new FileTaskRequest(
                    "virtual-task.md",
                    FileTaskRequest.TaskType.FILE_PARSING,
                    1
            );
            processWithVirtualThread(request);
            report.append("  ✓ 提交虚拟线程演示任务\n");
        } catch (Exception e) {
            report.append("  ⓘ 虚拟线程未启用或配置有误\n");
        }

        return report.toString();
    }
}