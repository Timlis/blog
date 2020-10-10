package com.timlis.service.impl;

import com.timlis.dao.UserRepository;
import com.timlis.pojo.User;
import com.timlis.service.UserService;
import com.timlis.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User checkUser(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, MD5Utils.code(password));
    }


}
