package com.example.dbuserjpa.userdetailservice;

import com.example.dbuserjpa.model.SecurityUser;
import com.example.dbuserjpa.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SecurityUser user = userRepository
                .findByUsername(username)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
        System.out.println("DEBUG: " + user.getUsername() + " " + user.getPassword() + " " + user.getAuthorities());
        return user;
    }
}
