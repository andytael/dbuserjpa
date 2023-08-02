package com.example.dbuserjpa;

import com.example.dbuserjpa.model.User;
import com.example.dbuserjpa.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DbuserrepoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbuserrepoApplication.class, args);
    }


    @Bean
    CommandLineRunner commandLineRunner(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            users.save(new User("user", encoder.encode("password"), "ROLE_USER"));
            users.save(new User("admin", encoder.encode("password"), "ROLE_USER,ROLE_ADMIN"));
        };
    }
}
