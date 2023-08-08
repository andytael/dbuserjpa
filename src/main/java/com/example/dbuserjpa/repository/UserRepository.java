package com.example.dbuserjpa.repository;

import com.example.dbuserjpa.model.Users;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<Users, Long> {

    Optional<Users> findByUsername(String username);
}
