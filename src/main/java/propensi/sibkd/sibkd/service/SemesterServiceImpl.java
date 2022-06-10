package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.model.JadwalBKD;
import propensi.sibkd.sibkd.model.Semester;
import propensi.sibkd.sibkd.repository.RoleDb;
import propensi.sibkd.sibkd.repository.SemesterDB;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class SemesterServiceImpl implements SemesterService {

    @Autowired
    SemesterDB semesterDB;

    @Override
    public void addSemester(Semester semester) {
        semesterDB.save(semester);
    }

    @Override
    public void updateSemester(Semester semester) {
        semesterDB.save(semester);
    }

    @Override
    public Semester getSemesterById(Long idSemester) {
        Optional<Semester> semester = semesterDB.findByIdSemester(idSemester);
        if(semester.isPresent()) {
            return semester.get();
        }
        return null;
    }

    @Override
    public List<Semester> getSemesterList() {
        return semesterDB.findAll(Sort.by(Sort.Direction.DESC, "tanggalDibukaSemester"));
    }

    @Override
    public Semester getCurrentSemester() {
        LocalDateTime currentDate = LocalDateTime.now();
        List<Semester> semesterList = getSemesterList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedTime = currentDate.format(formatter);
        currentDate = LocalDateTime.parse(formattedTime);

        Semester currentSemester = null;
        for(Semester semester : semesterList) {
            LocalDateTime tanggalDibukaSemester = semester.getTanggalDibukaSemester();
            LocalDateTime tanggalDitutupSemester = semester.getTanggalDitutupSemester();

            if(currentDate.isAfter(tanggalDibukaSemester) && currentDate.isBefore(tanggalDitutupSemester)) {
                currentSemester = semester;
                break;
            }
        }
        if(currentSemester == null) {
            return getLastSemester();
        } else {
            return currentSemester;
        }
    }

    @Override
    public Semester getLastSemester() {
        LocalDateTime currentDate = LocalDateTime.now();
        List<Semester> semesterList = getSemesterList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedTime = currentDate.format(formatter);
        currentDate = LocalDateTime.parse(formattedTime);

        Semester lastSemester = null;
        for(int i = semesterList.size()-1; i >= 0; i--) {
            if(i != 0) {
                if(currentDate.isAfter(semesterList.get(i).getJadwalBKD().getTanggalDitutupPenilaianUlangAsesor4())
                && currentDate.isBefore(semesterList.get(i-1).getJadwalBKD().getTanggalDibukaInputLaporanBKD1())) {
                    lastSemester = semesterList.get(i);
                    break;
                }
            } else {
                lastSemester = semesterList.get(0);
                break;
            }
        }
        return lastSemester;
    }

    @Override
    public boolean checkIfJadwalSemesterNotOverlap(JadwalBKD jadwalBKD, Semester semester) {
        LocalDateTime a = semester.getTanggalDibukaSemester();
        LocalDateTime b = semester.getTanggalDitutupSemester();

        LocalDateTime jadwalBuka = jadwalBKD.getTanggalDibukaInputLaporanBKD1();
        LocalDateTime jadwalTutup = jadwalBKD.getTanggalDitutupPenilaianUlangAsesor4();

        boolean flag = false;
        boolean flag2 = false;
        boolean flag3 = false;
        for(Semester smt : getSemesterList()) {
            LocalDateTime x = smt.getTanggalDibukaSemester();
            LocalDateTime y = smt.getTanggalDitutupSemester();

            if(a.isAfter(x) & a.isAfter(y) & b.isAfter(x) & b.isAfter(y)) {
                flag = true;
                        
            }
            if(a.isBefore(x) & a.isBefore(y) & b.isBefore(x) & b.isBefore(y)) {
                flag = true;
            }
        }
        if(flag == true) {
            if(a.isBefore(b)) {
                flag2 = true;
            }
            if(flag2 == true) {
                if(a.isBefore(jadwalBuka) & b.isAfter(jadwalTutup)) {
                    flag3 = true;
                }
            }
        }


        return flag & flag2 & flag3;
    }

    @Override
    public boolean isPeriodeInput(Semester semester) {
        LocalDateTime currentDate = LocalDateTime.now();

        return currentDate.isAfter(semester.getJadwalBKD().getTanggalDibukaInputLaporanBKD1()) &&
                currentDate.isBefore(semester.getJadwalBKD().getTanggalDitutupInputLaporanBKD1());
    }

    @Override
    public boolean isPeriodePerbaikan(Semester semester) {
        LocalDateTime currentDate = LocalDateTime.now();

        return currentDate.isAfter(semester.getJadwalBKD().getTanggalDibukaPerbaikanLaporanBKD3()) &&
                currentDate.isBefore(semester.getJadwalBKD().getTanggalDitutupPerbaikanLaporanBKD3());
    }

    @Override
    public boolean isPeriodePenilaian(Semester semester) {
        LocalDateTime currentDate = LocalDateTime.now();

        return currentDate.isAfter(semester.getJadwalBKD().getTanggalDibukaPenilaianAsesor2())&&
                currentDate.isBefore(semester.getJadwalBKD().getTanggalDitutupPenilaianAsesor2());
    }

    @Override
    public boolean isPeriodePenilaianUlang(Semester semester) {
        LocalDateTime currentDate = LocalDateTime.now();

        return currentDate.isAfter(semester.getJadwalBKD().getTanggalDibukaPenilaianUlangAsesor4()) &&
                currentDate.isBefore(semester.getJadwalBKD().getTanggalDitutupPenilaianUlangAsesor4());
    }

    @Override
    public Boolean isNamaSemesterAlreadyExistWhenUpdate(Semester semester) {
        List<Semester> semesterList = getSemesterList();
        Boolean flag = false;

        for(Semester semesterInDB : semesterList) {
            if(semester.getNamaSemester().equals(semesterInDB.getNamaSemester())) {
                flag = true;
                break;
            }
        }

        return flag;
    }
}
