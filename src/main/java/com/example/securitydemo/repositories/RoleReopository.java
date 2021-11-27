package com.example.securitydemo.repositories;

import com.example.securitydemo.dto.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleReopository extends JpaRepository<Role, Long>
{
    Role findByName(String name );

}
