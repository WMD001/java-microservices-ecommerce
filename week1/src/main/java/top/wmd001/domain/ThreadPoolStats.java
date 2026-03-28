package top.wmd001.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 线程池统计信息（Java 21 Record 类示例）
 * 不可变数据模型，用于返回线程池状态
 */
public record ThreadPoolStats(
        String poolName,
        int corePoolSize,
        int maxPoolSize,
        int activeThreads,
        int queueSize,
        long completedTaskCount,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
    public ThreadPoolStats {
        // 紧凑构造器，可添加验证逻辑
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize 不能为负数");
        }
    }

    /**
     * 工厂方法，快速创建当前时间戳的记录
     */
    public static ThreadPoolStats of(String poolName, int corePoolSize, int maxPoolSize,
                                     int activeThreads, int queueSize, long completedTaskCount) {
        return new ThreadPoolStats(poolName, corePoolSize, maxPoolSize,
                activeThreads, queueSize, completedTaskCount, LocalDateTime.now());
    }
}
