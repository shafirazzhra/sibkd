package propensi.sibkd.sibkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.sibkd.sibkd.model.Bidang;
import propensi.sibkd.sibkd.model.KegiatanBKD;

import java.util.List;
import java.util.Optional;

@Repository
public interface KegiatanDb extends JpaRepository<KegiatanBKD, Long> {
    List<KegiatanBKD> findAllByBidang(Optional<Bidang> bidang);
    Optional<KegiatanBKD> findByIdKegiatan(Long idKegiatan);
    Optional<KegiatanBKD> findByNamaKegiatanIgnoreCase(String namaKegiatan);
}
