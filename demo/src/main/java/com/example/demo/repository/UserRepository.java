package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/*
继承 JpaRepository 后，你自动获得
方法	功能
save(user)	保存或更新
findById(id)	根据主键查找
findAll()	查全部
deleteById(id)	删除
count()	计数
不用写任何 SQL，Spring Data JPA 已经帮你实现。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);
}
