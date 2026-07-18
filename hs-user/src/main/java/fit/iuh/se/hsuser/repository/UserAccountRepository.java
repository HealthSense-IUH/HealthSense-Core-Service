package fit.iuh.se.hsuser.repository;

import fit.iuh.se.hsuser.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    @Query("""
        select u
        from UserAccount u
        join fetch u.profile
        where u.email = :email
    """)
    Optional<UserAccount> findUserByEmail(String email);
}
