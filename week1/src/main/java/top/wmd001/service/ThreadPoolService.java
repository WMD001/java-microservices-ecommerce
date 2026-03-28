package top.wmd001.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import top.wmd001.domain.ThreadPoolStats;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控服务
 * 提供线程池状态查询和统计功能
 */
@Service
public class ThreadPoolService {

    private final ThreadPoolTaskExecutor defaultTaskExecutor;
    private final ThreadPoolTaskExecutor fileParsingTaskExecutor;
    private final ThreadPoolTaskExecutor wordCountTaskExecutor;

    public ThreadPoolService(ThreadPoolTaskExecutor defaultTaskExecutor,
                             ThreadPoolTaskExecutor fileParsingTaskExecutor,
                             ThreadPoolTaskExecutor wordCountTaskExecutor) {
        this.defaultTaskExecutor = defaultTaskExecutor;
        this.fileParsingTaskExecutor = fileParsingTaskExecutor;
        this.wordCountTaskExecutor = wordCountTaskExecutor;
    }

    /**
     * 获取默认异步任务线程池状态
     */
    public ThreadPoolStats getDefaultPoolStats() {
        ThreadPoolExecutor executor = defaultTaskExecutor.getThreadPoolExecutor();
        return ThreadPoolStats.of(
                "default-async-pool",
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
    }

    /**
     * 获取文件解析线程池状态
     */
    public ThreadPoolStats getFileParsingPoolStats() {
        ThreadPoolExecutor executor = fileParsingTaskExecutor.getThreadPoolExecutor();
        return ThreadPoolStats.of(
                "file-parsing-pool",
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
    }

    /**
     * 获取字数分析线程池状态
     */
    public ThreadPoolStats getWordCountPoolStats() {
        ThreadPoolExecutor executor = wordCountTaskExecutor.getThreadPoolExecutor();
        return ThreadPoolStats.of(
                "word-count-pool",
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
    }

    /**
     * 获取所有线程池状态
     */
    public ThreadPoolStats[] getAllPoolStats() {
        return new ThreadPoolStats[]{
                getDefaultPoolStats(),
                getFileParsingPoolStats(),
                getWordCountPoolStats()
        };
    }

    /**
     * 模拟CPU密集型任务
     */
    public String simulateCpuIntensiveTask() {
        long start = System.currentTimeMillis();
        // 模拟计算
        long result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += i * i;
        }
        long duration = System.currentTimeMillis() - start;
        return String.format("CPU密集型任务完成，结果: %d，耗时: %d ms", result, duration);
    }

    /**
     * 模拟I/O密集型任务
     */
    public String simulateIoIntensiveTask() {
        long start = System.currentTimeMillis();
        try {
            // 模拟I/O等待
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "任务被中断";
        }
        long duration = System.currentTimeMillis() - start;
        return String.format("I/O密集型任务完成，耗时: %d ms", duration);
    }
}