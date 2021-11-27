package com.example.securitydemo.services;


import com.example.securitydemo.dto.Role;
import com.example.securitydemo.dto.User;

import java.util.List;

public interface UserService
{
    User saveUser(User user);
    Role saveRole(Role role);
    void  addRoleToUSer(String username, String roleName);
    List<User> getAllUser();
    User getUser(String username);
}
