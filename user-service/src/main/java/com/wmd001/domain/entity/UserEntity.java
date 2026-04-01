package com.wmd001.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    public record UserDto(
            @NotNull(message = "用户ID不能为空")
            Long id,

            @NotBlank(message = "用户名不能为空")
            @Size(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
            String username,

            @NotBlank(message = "用户邮箱不能为空")
            @Email(message = "邮箱格式不正确")
            String email,

            @NotNull(message = "激活状态不能为空")
            Boolean isActive) {
        public UserDto {
            if (isActive == null) {
                isActive = false;
                log.warn("用户激活状态未指定，默认为false");
            }
        }
    }

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
