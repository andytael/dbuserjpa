package com.example.dbuserjpa.controller;

import com.example.dbuserjpa.model.Users;
import com.example.dbuserjpa.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class DbUserRepoController {

    final UserRepository userRepository;

    public DbUserRepoController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public record Userinfo (String username, String password) {
    }

    @GetMapping("/ping")
    public String ping() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        System.out.println("DEBUG: /users Username : " + securityContext.getAuthentication().getName());
        System.out.println("DEBUG: /users Authorities : " + securityContext.getAuthentication().getAuthorities());
        System.out.println("DEBUG: /users Details : " + securityContext.getAuthentication().getDetails());
        return "Ping Pong!";
    }

    /* Connect
    curl -i -u obaas-admin:password  http://localhost:8080/connect

    http -a obaas-user:password :8080/connect
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/connect")
    public ResponseEntity connect () {
        return new ResponseEntity(HttpStatus.OK);
    }

    /* Get all users
    curl -s -u obaas-admin:password  http://localhost:8080/users | jq

    http -a obaas-admin:password :8080/users
    */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public List<Users> getAllUsers() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return userRepository.findAll();
    }

    /* Create user
    curl -u obaas-admin:password  -i -X POST \
      -H 'Content-Type: application/json' \
      -d '{"username": "Nisse", "password": "howdy", "roles" : "USER_ROLE"}' \
      http://localhost:8080/user

    http -a obaas-admin:password POST :8080/user username=anna password=bruno roles=USER_ROLE,ADMIN_ROLE
    */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/user")
    public ResponseEntity<Users> createUser(@RequestBody Users user, StandardPasswordEncoder encoder) {
        try {
            Users _user = userRepository.save(new Users(
                    user.getUsername(),
                    encoder.encode(user.getPassword()),
                    user.getRoles()
            ));
            return new ResponseEntity<>(_user, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* Change Password - Admin user can change the password on anyone, user can only change it's own password

    curl -u obaas-admin:password  -i -X PUT \
      -H 'Content-Type: application/json' \
      -d '{"username": "obaas-admin", "password": "newpassword"}' \
      http://localhost:8080/userpwd

    http -a obaas-admin:password PUT :8080/userpwd username=obaas-admin password=andy
    */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/userpwd")
    public ResponseEntity<Users> changePassword(@RequestBody Userinfo userInfo, StandardPasswordEncoder encoder) {

        // Check if the user is a user with ROLE_ADMIN
        SecurityContext securityContext = SecurityContextHolder.getContext();
        boolean isAdminUser = false;
        for (GrantedAuthority role : securityContext.getAuthentication().getAuthorities()) {
            if (role.getAuthority().contains("ROLE_ADMIN")) {
                isAdminUser = true;
            }
        }

        // If the username of the authenticated user matches the requestbody username or if it is an user with ROLE_ADMIN
        if ((userInfo.username().compareTo(securityContext.getAuthentication().getName()) == 0) || isAdminUser) {
            try {
                Optional<Users> _user = userRepository.findByUsername(userInfo.username());
                _user.get().setPassword(encoder.encode(userInfo.password()));
                userRepository.saveAndFlush(_user.get());
                return new ResponseEntity(HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }


    /* Delete a User By Id
    curl -u obaas-admin:password -i -X DELETE http://localhost:8080/userid/{id}

    http -a obaas-admin:password DELETE :8080/userid/4
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

}
