package com.example.securitydemo.services;

import com.example.securitydemo.dto.Role;
import com.example.securitydemo.dto.User;
import com.example.securitydemo.repositories.RoleReopository;
import com.example.securitydemo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService
{

    @Autowired
    private final RoleReopository roleReopository;
    @Autowired
    private final UserRepository userRepository;
 //   @Autowired
 //   private final PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(s);
        if (user == null) throw new UsernameNotFoundException("User Not Found In Database");

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });

        return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(),authorities);
    }


    @Override
    public User saveUser(User user) {
        //   user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public Role saveRole(Role role) {
        return roleReopository.save(role) ;
    }

    @Override
    public void addRoleToUser(String username, String roleName)
    {
        User user = userRepository.findByUsername(username);
        Role role = roleReopository.findByName(roleName);
        user.getRoles().add(role);

    }

    @Override
    public List<User> getAllUser() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);
        if (users.isEmpty()) throw new RuntimeException("not found in method ");
        return users  ;
    }

    @Override
    public User getUser(String username)
    {
        return userRepository.findByUsername(username);
    }


}
