package propensi.sibkd.sibkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.sibkd.sibkd.model.Dosen;

import java.util.Optional;

@Repository
public interface DosenDb extends JpaRepository<Dosen, Long> {
    Optional<Dosen> findByEmailDosen(String emailDosen);
    Optional<Dosen> findByIdDosen(Long idDosen);
}
