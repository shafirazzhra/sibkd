package propensi.sibkd.sibkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.sibkd.sibkd.model.JadwalBKD;

import java.util.List;
import java.util.Optional;

@Repository
public interface JadwalDb extends JpaRepository<JadwalBKD, Long> {
    Optional<JadwalBKD> findByIdJadwalBKD(Long idJadwalBKD);
}
