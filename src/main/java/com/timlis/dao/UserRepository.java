package com.timlis.dao;

import com.timlis.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsernameAndPassword(String username,String password);

    User findByUsername(String username);
}
