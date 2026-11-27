package vn.rikkei.cinemabox.showtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Showtime> findWithLockById(Long id);
}
