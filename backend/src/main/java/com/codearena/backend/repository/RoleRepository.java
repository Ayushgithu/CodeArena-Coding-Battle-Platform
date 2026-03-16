package com.codearena.backend.repository;

import com.codearena.backend.entity.Role;
import com.codearena.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,String> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
