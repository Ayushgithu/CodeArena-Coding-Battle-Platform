package com.codearena.backend.dataInitializer;

import com.codearena.backend.entity.Role;
import com.codearena.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        insertRoleIfNotExists("PLAYER");
        insertRoleIfNotExists("INTERVIEWER");
        // Check if admin already exists
        }
    private void insertRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            System.out.println("✅ Role '" + roleName + "' inserted.");
        }
    }
}
