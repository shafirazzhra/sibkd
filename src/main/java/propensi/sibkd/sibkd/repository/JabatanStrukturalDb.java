package propensi.sibkd.sibkd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import propensi.sibkd.sibkd.model.JabatanStruktural;

@Repository
public interface JabatanStrukturalDb extends JpaRepository<JabatanStruktural, Long>{
    Optional<JabatanStruktural> findByNamaJabatanStrukturalIgnoreCase(String nama);
    Optional<JabatanStruktural> findByIdJabatanStruktural(Long idJabatanStruktural);
}
