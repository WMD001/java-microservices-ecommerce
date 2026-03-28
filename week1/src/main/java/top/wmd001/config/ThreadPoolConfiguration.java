package top.wmd001.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 线程池配置类
 * 包含：Tomcat虚拟线程配置、默认异步任务池、文件解析专用池、字数分析专用池
 *
 * @author WMD001
 */
@Configuration
@EnableAsync
@Slf4j
public class ThreadPoolConfiguration {

    /**
     * 配置Tomcat使用虚拟线程处理请求
     * 当spring.threads.virtual.enabled=true时生效
     */
    @Bean
    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true")
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            // 使用虚拟线程执行Tomcat请求
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            log.info("✅ Tomcat虚拟线程已启用 - 使用虚拟线程处理HTTP请求");
        };
    }

    /**
     * 默认异步任务执行器（@Async无指定时使用）
     * 配置来自spring.task.execution.default
     */
    @Bean(name = "defaultTaskExecutor")
    public ThreadPoolTaskExecutor defaultTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // CPU核心数
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("default-async-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        log.info("✅ 默认异步任务线程池初始化完成: {}", executor.getThreadPoolExecutor());
        return executor;
    }

    /**
     * 文件批量解析任务专用线程池
     * I/O密集型，核心线程数较少，队列容量较大
     */
    @Bean(name = "fileParsingTaskExecutor")
    public ThreadPoolTaskExecutor fileParsingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("file-parsing-");
        executor.setKeepAliveSeconds(120);
        executor.initialize();
        log.info("✅ 文件解析专用线程池初始化完成: {}", executor.getThreadPoolExecutor());
        return executor;
    }

    /**
     * 文件字数分析任务专用线程池
     * CPU密集型，核心线程数较少
     */
    @Bean(name = "wordCountTaskExecutor")
    public ThreadPoolTaskExecutor wordCountTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("word-count-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        log.info("✅ 字数分析专用线程池初始化完成: {}", executor.getThreadPoolExecutor());
        return executor;
    }

    /**
     * 虚拟线程异步执行器（Java 21预览功能）
     * 用于演示虚拟线程执行异步任务
     */
    @Bean(name = "virtualThreadTaskExecutor")
    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true")
    public Executor virtualThreadTaskExecutor() {
        // 使用TaskExecutorAdapter包装虚拟线程执行器
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

}