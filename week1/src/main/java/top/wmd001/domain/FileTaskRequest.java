package top.wmd001.domain;

/**
 * 文件任务请求（Java 21 Record 类示例）
 * 使用模式匹配的潜在对象
 */
public record FileTaskRequest(
        String fileName,
        TaskType taskType,
        int priority
) {
    public enum TaskType {
        // 文件批量解析
        FILE_PARSING,
        // 文件字数分析
        WORD_COUNT
    }

    /**
     * 使用模式匹配的示例方法（Java 21预览功能）
     * @return string
     */
    public String processWithPatternMatching() {
        return switch (taskType) {
            case FILE_PARSING -> "文件解析任务：" + fileName + " (优先级: " + priority + ")";
            case WORD_COUNT -> "字数分析任务：" + fileName + " (优先级: " + priority + ")";
        };
    }
}