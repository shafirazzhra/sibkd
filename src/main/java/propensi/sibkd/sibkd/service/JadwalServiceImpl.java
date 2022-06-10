package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.model.JadwalBKD;
import propensi.sibkd.sibkd.model.Semester;
import propensi.sibkd.sibkd.repository.JadwalDb;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class JadwalServiceImpl implements JadwalService {

    @Autowired
    JadwalDb jadwalDb;

    @Override
    public JadwalBKD getJadwalBKDbyId(Long idJadwalBKD) {
        Optional<JadwalBKD> jadwalBKD = jadwalDb.findByIdJadwalBKD(idJadwalBKD);
        if(jadwalBKD.isPresent()) {
            return jadwalBKD.get();
        }
        return null;
    }

    @Override
    public void addJadwalBKD(JadwalBKD jadwalBKD) {
        jadwalDb.save(jadwalBKD);
    }

    @Override
    public void updateJadwalBKD(JadwalBKD jadwalBKD) {
        jadwalDb.save(jadwalBKD);
    }

    @Override
    public boolean checkIfJadwalFormatTrue(JadwalBKD newJadwalBKD, Semester semester) {
        LocalDateTime a = newJadwalBKD.getTanggalDibukaInputLaporanBKD1();
        LocalDateTime b = newJadwalBKD.getTanggalDitutupInputLaporanBKD1();
        LocalDateTime c = newJadwalBKD.getTanggalDibukaPenilaianAsesor2();
        LocalDateTime d = newJadwalBKD.getTanggalDitutupPenilaianAsesor2();
        LocalDateTime e = newJadwalBKD.getTanggalDibukaPerbaikanLaporanBKD3();
        LocalDateTime f = newJadwalBKD.getTanggalDitutupPerbaikanLaporanBKD3();
        LocalDateTime g = newJadwalBKD.getTanggalDibukaPenilaianUlangAsesor4();
        LocalDateTime h = newJadwalBKD.getTanggalDitutupPenilaianUlangAsesor4();

        LocalDateTime i = semester.getTanggalDibukaSemester();
        LocalDateTime j = semester.getTanggalDitutupSemester();

        return a.isBefore(b) & b.isBefore(c) & c.isBefore(d) & d.isBefore(e) & e.isBefore(f) & f.isBefore(g) & g.isBefore(h)
                & h.isAfter(g) & g.isAfter(f) & f.isAfter(e) & e.isAfter(d) & d.isAfter(c) & c.isAfter(b) & b.isAfter(a)
                & a.isAfter(i) & b.isAfter(i) & c.isAfter(i) & d.isAfter(i) & e.isAfter(i) & f.isAfter(i) & g.isAfter(i) & h.isAfter(i)
                & a.isBefore(j) & b.isBefore(j) & c.isBefore(j) & d.isBefore(j) & e.isBefore(j) & f.isBefore(j) & g.isBefore(j) & h.isBefore(j);
    }

    @Override
    public String getCurrentJadwal(Semester semester) {
        JadwalBKD jadwalBKD = semester.getJadwalBKD();

        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedTime = currentDate.format(formatter);
        currentDate = LocalDateTime.parse(formattedTime);

        if(currentDate.isEqual(jadwalBKD.getTanggalDibukaInputLaporanBKD1()) || currentDate.isEqual(jadwalBKD.getTanggalDitutupInputLaporanBKD1())) {
            return "Input Laporan BKD";
        } else if(currentDate.isEqual(jadwalBKD.getTanggalDibukaPenilaianAsesor2()) || currentDate.isEqual(jadwalBKD.getTanggalDitutupPenilaianAsesor2())) {
            return "Penilaian oleh Asesor";
        } else if(currentDate.isEqual(jadwalBKD.getTanggalDibukaPerbaikanLaporanBKD3()) || currentDate.isEqual(jadwalBKD.getTanggalDitutupPerbaikanLaporanBKD3())) {
            return "Perbaikan Laporan BKD";
        } else if(currentDate.isEqual(jadwalBKD.getTanggalDibukaPenilaianUlangAsesor4()) || currentDate.isEqual(jadwalBKD.getTanggalDitutupPenilaianUlangAsesor4())) {
            return "Penilaian Ulang Asesor";
        } else {
            if(currentDate.isAfter(jadwalBKD.getTanggalDibukaInputLaporanBKD1())
                    && currentDate.isBefore(jadwalBKD.getTanggalDitutupInputLaporanBKD1())) {
                return "Input Laporan BKD";
            } else if(currentDate.isAfter(jadwalBKD.getTanggalDibukaPenilaianAsesor2())
                    && currentDate.isBefore(jadwalBKD.getTanggalDitutupPenilaianAsesor2())) {
                return "Penilaian oleh Asesor";
            } else if(currentDate.isAfter(jadwalBKD.getTanggalDibukaPerbaikanLaporanBKD3())
                    && currentDate.isBefore(jadwalBKD.getTanggalDitutupPerbaikanLaporanBKD3())) {
                return "Perbaikan Laporan BKD";
            } else if(currentDate.isAfter(jadwalBKD.getTanggalDibukaPenilaianUlangAsesor4())
                    && currentDate.isBefore(jadwalBKD.getTanggalDitutupPenilaianUlangAsesor4())) {
                return "Penilaian Ulang Asesor";
            } else {
                return "Belum ada";
            }
        }
    }
}
