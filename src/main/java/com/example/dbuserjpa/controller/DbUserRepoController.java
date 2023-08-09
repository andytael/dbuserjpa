package com.example.dbuserjpa.controller;

import com.example.dbuserjpa.model.Users;
import com.example.dbuserjpa.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DbUserRepoController {

    final UserRepository userRepository;

    public DbUserRepoController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Ping Pong!";
    }

    // Get all users
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    /* Create user
    curl -u obaas-admin:password  -i -X POST \
      -H 'Content-Type: application/json' \
      -d '{"username": "Nisse", "password": "howdy", "roles" : "USER_ROLE"}' \
      http://localhost:8080/user
    */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/user")
    public ResponseEntity<Users> createUser(@RequestBody Users user) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        try {
            Users _user =userRepository.save(new Users(
                    user.getUsername(),
                    encoder.encode(user.getPassword()),
                    user.getRoles()
            ));
            return new ResponseEntity<>(_user, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* Delete a User By Id
    curl -u obaas-admin:password -i -X DELETE http://localhost:8080/user/{id}
    */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/userid/{id}")
    public ResponseEntity<HttpStatus> deleteUserById(@PathVariable("id") long id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/api/pinguser")
    public String pingSecureUser() {
        return "Secure User Ping Pong!";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/api/pingadmin")
    public String pingSecureAdmin() {
        return "Secure Admin Ping Pong!";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/user")
    public String user() {
        return "Hello, User!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin")
    public String admin() {
        return "Hello, Admin!";
    }

}
