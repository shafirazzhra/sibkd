package propensi.sibkd.sibkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.sibkd.sibkd.model.ItemBKD;
import propensi.sibkd.sibkd.model.KegiatanBKD;
import propensi.sibkd.sibkd.model.Dosen;
import propensi.sibkd.sibkd.model.Semester;
import propensi.sibkd.sibkd.model.Bidang;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemDb extends JpaRepository<ItemBKD, Long> {
    List<ItemBKD> findAllByKegiatanBKD(KegiatanBKD kegiatan);
    List<ItemBKD> findAllByDosenAndSemesterAndBidang(Dosen dosen, Semester semester, Bidang bidang);
    List<ItemBKD> findAllByDosenAndSemester(Dosen dosen, Semester semester);
    Optional<ItemBKD> findItemBKDByIdItem(Long idItem);
}
