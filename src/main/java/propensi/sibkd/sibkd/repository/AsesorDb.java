package propensi.sibkd.sibkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.sibkd.sibkd.model.Asesor;

import java.util.Optional;

@Repository
public interface AsesorDb extends JpaRepository<Asesor, Long> {
    Optional<Asesor> findByEmailAsesor(String emailDosen);
    Optional<Asesor> findByIdAsesor(Long idDosen);
}
