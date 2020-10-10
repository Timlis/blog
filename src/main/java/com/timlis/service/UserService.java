package com.timlis.service;

import com.timlis.pojo.User;

public interface UserService {
    User checkUser(String username,String password);
}
