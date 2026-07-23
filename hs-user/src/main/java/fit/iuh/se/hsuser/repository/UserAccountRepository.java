package fit.iuh.se.hsuser.repository;

import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = "profile")
    Page<UserAccount> findAllByStatusNot(AccountStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "profile")
    Optional<UserAccount> findByIdAndStatusNot(Long id, AccountStatus status);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRoleAndStatus(UserRole role, AccountStatus status);
}
