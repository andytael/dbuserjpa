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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user/api/v1")
public class DbUserRepoController {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    final UserRepository userRepository;

    public DbUserRepoController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record Userinfo(String username, String password) {
    }

    /* Connect
    curl -i -u obaas-admin:password  http://localhost:8080/user/api/v1/connect

    http -a obaas-user:password :8080/user/api/v1/connect
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/connect")
    public ResponseEntity connect() {
        return new ResponseEntity(HttpStatus.OK);
    }

    /* Find all users or users containing a username. If no param is provided all users are returned

    curl -i -u obaas-admin:password  http://localhost:8080/user/api/v1/findUser
    curl -i -u obaas-admin:password  http://localhost:8080/user/api/v1/findUser\?username\=obaas-admin

    http -a obaas-admin:password :8080/user/api/v1/findUser
    http -a obaas-admin:password :8080/user/api/v1/findUser username==obaas-admin
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/findUser")
    public ResponseEntity<List<Users>> getUsers(@RequestParam(required = false) String username) {
        try {
            List<Users> _users = new ArrayList<>();
            if (username == null)
                _users.addAll(userRepository.findAll());
            else
                _users.addAll(userRepository.findPhoneBooksByUsernameStartsWithIgnoreCase(username));
            if (_users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(_users, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* Create user
    curl -u obaas-admin:password  -i -X POST \
      -H 'Content-Type: application/json' \
      -d '{"username": "Nisse", "password": "howdy", "roles" : "USER_ROLE"}' \
      http://localhost:8080/user/api/v1/createUser

    http -a obaas-admin:password POST :8080/user/api/v1/createUser username=anna password=bruno roles=USER_ROLE,ADMIN_ROLE
    */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/createUser")
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
      http://localhost:8080/user/api/v1/updatePassword

    http -a obaas-admin:password PUT :8080/user/api/v1/updatePassword username=obaas-admin password=andy
    */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/updatePassword")
    public ResponseEntity<Users> changePassword(@RequestBody Userinfo userInfo, StandardPasswordEncoder encoder) {

        // Check if the user is a user with ROLE_ADMIN
        SecurityContext securityContext = SecurityContextHolder.getContext();
        boolean isAdminUser = false;
        for (GrantedAuthority role : securityContext.getAuthentication().getAuthorities()) {
            if (role.getAuthority().contains(ROLE_ADMIN)) {
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

    /* Delete a User by username
    curl -u obaas-admin:password -i -X DELETE http://localhost:8080/user/api/v1/deleteUsername/{username}

    http -a obaas-admin:password DELETE :8080/user/api/v1/deleteUsername/{username}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/deleteUsername/{username}")
    public ResponseEntity<HttpStatus> deleteUserByUsername(@PathVariable("username") String username) {
        try {
            Optional<Users> _user = userRepository.findByUsername(username);
            userRepository.deleteById(_user.get().getUserId());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /* Delete a User By Id
    curl -u obaas-admin:password -i -X DELETE http://localhost:8080/user/api/v1/deleteId/{id}

    http -a obaas-admin:password DELETE :8080/user/api/v1/deleteId/{id}
    */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/deleteId/{id}")
    public ResponseEntity<HttpStatus> deleteUserById(@PathVariable("id") long id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Test method to see if a user has ROLE_USER
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/pinguser")
    public String pingSecureUser() {
        return "Secure User Ping Pong!";
    }

    // Test method to see if a user has ROLE_ADMIN
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/pingadmin")
    public String pingSecureAdmin() {
        return "Secure Admin Ping Pong!";
    }

    // Open Method e.g no auth
    @GetMapping("/ping")
    public String ping() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        System.out.println("DEBUG: /users Username : " + securityContext.getAuthentication().getName());
        System.out.println("DEBUG: /users Authorities : " + securityContext.getAuthentication().getAuthorities());
        System.out.println("DEBUG: /users Details : " + securityContext.getAuthentication().getDetails());
        return "Ping Pong!";
    }
}
