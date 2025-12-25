package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

//entity里面是存放的数据库，我们只需写字段，由java帮我们实现
@Entity
@Data
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),//唯一性约束
        @UniqueConstraint(columnNames = "phone")
})

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户名（唯一）
    @Column(nullable = false, unique = true)
    private String username;

    // 密码（加密后保存）
    @Column(nullable = false)
    private String password;

    private String gender;
    private Integer age;

    // 手机号码（唯一）
    @Column(unique = true)
    private String phone;
    //头像存储路径

    private String avatarPath; // 头像路径：/uploads/avatar/u1.png
}

