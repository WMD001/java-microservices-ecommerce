package com.wmd001.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    // 业务方法：用户信息摘要生成
    public String generateSummary() {
        return String.format("用户[%d]: %s (%s) - 状态: %s",
                id, username, email, isActive ? "活跃" : "冻结");
    }

    // 使用Record作为值对象（DTO转换）
    public record UserDto(Long id, String username, String email, Boolean isActive) {}

    public UserDto toDto() {
        return new UserDto(id, username, email, isActive);
    }

    // 静态验证方法：使用Record模式验证用户数据
    public static boolean validateUser(Object obj) {
        return switch (obj) {
            case UserDto(Long id, String uname, String email, Boolean active)
                    when active && !uname.isEmpty() && email.contains("@") -> true;
            default -> false;
        };
    }
}
