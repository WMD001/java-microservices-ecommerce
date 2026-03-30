package com.wmd001.controller;

import com.wmd001.domain.entity.UserEntity;
import com.wmd001.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "电商系统用户管理API")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(
            summary = "创建用户",
            description = "创建新用户账户，返回用户ID和基本信息"
    )
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "用户创建请求", required = true)
            @RequestBody UserCreationRequest request) {

        UserEntity user = userService.createUser(
                request.username(),
                request.email(),
                request.password()
        );

        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getIsActive()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户信息")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        // 模拟从数据库获取
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("demo_user");
        user.setEmail("demo@example.com");
        user.setPasswordHash("hashed_pass");
        user.setFirstName("Demo");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getIsActive()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "获取用户摘要", description = "使用Record模式生成用户摘要信息")
    public ResponseEntity<Map<String, String>> getUserSummary(@PathVariable Long id) {
        // 创建示例用户实体并转换为DTO
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setIsActive(true);

        UserEntity.UserDto userDto = user.toDto();
        String summary = userService.getUserSummary(userDto);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    // DTO定义（与UserService内部Record保持一致）
    public record UserCreationRequest(String username, String email, String password) {}
    public record UserResponse(Long id, String username, String email, Boolean isActive) {}
}
