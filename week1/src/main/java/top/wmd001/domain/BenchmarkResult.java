package top.wmd001.domain;

public record BenchmarkResult(
        String configName,
        long totalRequests,
        double throughput,
        double avgResponseTime,
        double p95ResponseTime,
        int maxThreads,
        int queueCapacity
) {
    public String summary() {
        return String.format("配置: %s | 吞吐量: %.2f req/s | 平均响应: %.2f ms | P95: %.2f ms",
                configName, throughput, avgResponseTime, p95ResponseTime);
    }

    /**
     * 判断配置是否优于另一个结果
     */
    public boolean isBetterThan(BenchmarkResult other) {
        // 比较吞吐量和响应时间的综合得分
        double score1 = throughput / avgResponseTime;
        double score2 = other.throughput() / other.avgResponseTime();
        return score1 > score2;
    }
}
