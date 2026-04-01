package com.wmd001.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "用户创建请求", example = "{\"username\":\"john_doe\",\"email\":\"john@example.com\",\"password\":\"secure123\"}")
public record UserCreationRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度需在3-20字符之间")
        @Schema(description = "用户名", example = "john")
        String username,
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Schema(description = "邮箱", example = "john@example.com")
        String email,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 30, message = "密码长度需在6-30字符之间")
        @Schema(description = "密码", example = "secure123")
        String password
) {}
