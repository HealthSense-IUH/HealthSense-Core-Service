package fit.iuh.se.hsuser.repository;

import fit.iuh.se.hsuser.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    Optional<UserAccount> findByEmail(String email);
}
