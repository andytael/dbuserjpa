package oracle.spring.service.repository;

import oracle.spring.service.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);
    List<Users> findUsersByUsernameStartsWithIgnoreCase(String username);
}
