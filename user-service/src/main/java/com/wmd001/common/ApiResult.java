package com.wmd001.common;

import java.util.Map;

public sealed interface ApiResult<T> permits ApiResult.ValidationError, ApiResult.Error, ApiResult.Success {
    record Success<T>(T data, String traceId) implements ApiResult<T> {}
    record Error(String code, String message, Map<String, String> details) implements ApiResult<Void> {}
    record ValidationError(String field, String message, Object rejectedValue) implements ApiResult<Void> {}
}





