package com.cvanalyzer.config;

import com.cvanalyzer.entity.Role;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.repository.RoleRepository;
import com.cvanalyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            roleRepository.save(Role.builder().name(Role.RoleName.ROLE_USER).build());
            log.info("Created ROLE_USER");
        }
        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(Role.builder().name(Role.RoleName.ROLE_ADMIN).build());
            log.info("Created ROLE_ADMIN");
        }
    }

    private void initAdminUser() {
        String adminEmail = "admin@cvanalyzer.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).orElseThrow();
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();

            User admin = User.builder()
                    .fullName("System Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@1234"))
                    .emailVerified(true)
                    .enabled(true)
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(admin);
            log.info("Created default admin user: {} / Admin@1234", adminEmail);
        }
    }
}
