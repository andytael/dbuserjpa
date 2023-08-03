package com.example.dbuserjpa.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbUserRepoController {

    @GetMapping("/ping")
    public String ping() {
        return "Ping Pong!";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/api/pingsecureuser")
    public String pingSecureUser() {
        return "Secure User Ping Pong!";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/api/pingsecureadmin")
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
