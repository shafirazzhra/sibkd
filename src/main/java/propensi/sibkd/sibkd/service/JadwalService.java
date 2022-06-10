package propensi.sibkd.sibkd.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.repository.*;
import propensi.sibkd.sibkd.model.*;
import java.util.List;
import java.util.Optional;

public interface JadwalService {
    JadwalBKD getJadwalBKDbyId(Long idJadwalBKD);
    void addJadwalBKD(JadwalBKD jadwalBKD);
    void updateJadwalBKD(JadwalBKD jadwalBKD);
    boolean checkIfJadwalFormatTrue(JadwalBKD newJadwalBKD, Semester newSemester);
    String getCurrentJadwal(Semester semester);
}
