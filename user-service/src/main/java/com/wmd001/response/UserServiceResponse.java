package com.wmd001.response;

import com.wmd001.common.ApiResult;
import com.wmd001.domain.entity.UserEntity;

import java.util.List;

public sealed interface UserServiceResponse
        permits UserServiceResponse.InternalError,
        UserServiceResponse.UserCreated,
        UserServiceResponse.UserFound,
        UserServiceResponse.UserNotFound,
        UserServiceResponse.ValidationFailed {

    default String getResponseType() {
        return this.getClass().getSimpleName();
    }

    // 成功响应：用户创建成功
    record UserCreated(Long userId, String username, String email)
            implements UserServiceResponse {
    }

    // 成功响应：用户查询成功
    record UserFound(UserEntity.UserDto user) implements UserServiceResponse {
    }

    // 业务错误：用户不存在
    record UserNotFound(String message) implements UserServiceResponse {
    }

    // 验证错误：请求参数验证失败
    record ValidationFailed(List<ApiResult.ValidationError> errors)
            implements UserServiceResponse {
    }

    // 系统错误：内部服务器错误
    record InternalError(String code, String details)
            implements UserServiceResponse {
    }

}
