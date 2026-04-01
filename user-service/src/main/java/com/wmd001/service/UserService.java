package com.wmd001.service;

import com.wmd001.domain.entity.UserCreationRequest;
import com.wmd001.domain.entity.UserEntity;
import com.wmd001.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ExecutorService virtualThreadExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEntity createUser(String username, String email, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hashed_" + password); // 实际使用BCrypt
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        return userRepository.save(user);
    }

    // 虚拟线程示例：批量用户创建
    public void batchCreateUsers(List<UserCreationRequest> requests) {
        requests.forEach(request ->
                virtualThreadExecutor.submit(() -> {
                    createUser(request.username(), request.email(), request.password());
                    System.out.println("用户创建完成: " + request.username());
                })
        );
    }

    // Record模式应用：用户信息摘要（基于DTO）
    public String getUserSummary(UserEntity.UserDto userDto) {
        return switch (userDto) {
            case UserEntity.UserDto(Long id, String username, String email, Boolean active) ->
                    String.format("用户[%d]: %s (%s) - 状态: %s",
                            id, username, email, active ? "活跃" : "冻结");
            default -> "无效用户";
        };
    }

}
