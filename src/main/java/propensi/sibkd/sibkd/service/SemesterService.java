package propensi.sibkd.sibkd.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.repository.*;
import propensi.sibkd.sibkd.model.*;
import java.util.List;
import java.util.Optional;

public interface SemesterService {
    void addSemester(Semester semester);
    void updateSemester(Semester semester);
    Semester getSemesterById(Long idSemester);
    List<Semester> getSemesterList();
    Semester getCurrentSemester();
    Semester getLastSemester();
    boolean checkIfJadwalSemesterNotOverlap(JadwalBKD jadwalBKD, Semester semester);
    boolean isPeriodeInput(Semester semester);
    boolean isPeriodePerbaikan(Semester semester);
    boolean isPeriodePenilaian(Semester semester);
    boolean isPeriodePenilaianUlang(Semester semester);
    Boolean isNamaSemesterAlreadyExistWhenUpdate(Semester semester);
}
