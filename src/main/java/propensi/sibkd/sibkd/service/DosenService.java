package propensi.sibkd.sibkd.service;

import propensi.sibkd.sibkd.model.*;

import java.util.List;

public interface DosenService {
    void addDosen(Dosen dosen);
    void updateDosen(Dosen dosen);
    List<Dosen> getListAllDosen();
    Dosen getDosenByIdDosen(Long idDosen);
    Dosen getDosenByEmailDosen(String emailDosen);
    boolean isDosenExist(String emailDosen); 
    boolean cekStatusAssesment(Dosen dosen, Pengguna user, Semester semester);
    List <Dosen> findAllPenggunaByInProdi(String prodi, Dosen dosen);
    List <Dosen> searchDosen(String keyword, String status, Semester semester, Pengguna user);
    void updateStatusDosen(Dosen dosen);
    List <Dosen> findAllDosenByStatus(String status, Pengguna user);
    List<Dosen> searchDosenByStatus(String status, String keyword, Pengguna user);
}