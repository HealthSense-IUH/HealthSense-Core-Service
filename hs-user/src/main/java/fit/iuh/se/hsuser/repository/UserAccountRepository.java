package fit.iuh.se.hsuser.repository;

import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> findByIdForUpdate(Long id);

    boolean existsByEmail(String email);

    @Query("""
        select u
        from UserAccount u
        join fetch u.profile
        where u.email = :email
    """)
    Optional<UserAccount> findUserByEmail(String email);

    @EntityGraph(attributePaths = "profile")
    @Query("""
        select u
        from UserAccount u
        where u.role = :role
          and u.id <> :excludedId
          and (:status is null or u.status = :status)
    """)
    Page<UserAccount> findUsers(
            UserRole role,
            AccountStatus status,
            Long excludedId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "profile")
    @Query("""
        select u
        from UserAccount u
        where u.role = :role
          and u.id <> :excludedId
          and (:status is null or u.status = :status)
          and (
              cast(u.id as string) like concat('%', :keyword, '%')
              or lower(u.email) like lower(concat('%', :keyword, '%'))
              or u.profile.phone like concat('%', :keyword, '%')
          )
    """)
    Page<UserAccount> searchUsers(
            UserRole role,
            AccountStatus status,
            Long excludedId,
            String keyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "profile")
    Optional<UserAccount> findByIdAndStatusNot(Long id, AccountStatus status);

    @EntityGraph(attributePaths = "profile")
    @Query("""
        select u
        from UserAccount u
        where u.role = :role
          and u.status = :status
    """)
    Page<UserAccount> findDoctors(
            UserRole role,
            AccountStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "profile")
    @Query("""
        select u
        from UserAccount u
        where u.role = :role
          and u.status = :status
          and (
              cast(u.id as string) like concat('%', :keyword, '%')
              or lower(u.email) like lower(concat('%', :keyword, '%'))
              or lower(u.profile.displayName) like lower(concat('%', :keyword, '%'))
              or u.profile.phone like concat('%', :keyword, '%')
          )
    """)
    Page<UserAccount> searchDoctors(
            UserRole role,
            AccountStatus status,
            String keyword,
            Pageable pageable
    );

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRoleAndStatus(UserRole role, AccountStatus status);

    List<UserAccount> findAllByRoleAndStatus(UserRole role, AccountStatus status);
}
