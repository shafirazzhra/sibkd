package propensi.sibkd.sibkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.sibkd.sibkd.model.Bidang;

import java.util.Optional;

@Repository
public interface BidangDb extends JpaRepository<Bidang, Long> {
    Optional<Bidang> findByNamaBidangIgnoreCase(String namaBidang);
    Optional<Bidang> findByIdBidang(Long idBidang);
}
