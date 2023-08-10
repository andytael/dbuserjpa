package oracle.spring.service;

import oracle.spring.service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import oracle.spring.service.model.Users;

@SpringBootApplication
public class DbUserRepoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbUserRepoApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            users.save(new Users("obaas-user", encoder.encode("password"), "ROLE_USER"));
            users.save(new Users("obaas-admin", encoder.encode("password"), "ROLE_USER,ROLE_ADMIN"));
        };
        //TODO: Get pwd from configuration mgmt (ansible) images_build playbook supplied by user/installer
    }
}
