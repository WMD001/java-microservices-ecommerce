package com.wmd001.repository;

import com.wmd001.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);

}
