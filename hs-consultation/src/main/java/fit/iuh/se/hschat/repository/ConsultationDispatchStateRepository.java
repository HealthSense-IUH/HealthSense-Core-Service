package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationDispatchState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConsultationDispatchStateRepository extends JpaRepository<ConsultationDispatchState, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from ConsultationDispatchState state where state.id = 1")
    Optional<ConsultationDispatchState> findSingletonForUpdate();
}
