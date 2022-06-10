package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.repository.DosenDb;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DosenServiceImpl implements DosenService{

    @Autowired
    DosenDb dosenDb;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    AsesorService asesorService;

    @Autowired
    ItemBKDService itemBKDService;

    @Autowired
    SemesterService semesterService;

    @Override
    public void addDosen(Dosen Dosen){
        dosenDb.save(Dosen);
    }

    @Override
    public void updateDosen(Dosen Dosen){
        dosenDb.save(Dosen);
    }

    @Override
    public Dosen getDosenByIdDosen(Long idDosen){
        Optional<Dosen> Dosen = dosenDb.findByIdDosen(idDosen);
        if(Dosen.isPresent()) return Dosen.get();
        else return null;
    }

    @Override
    public Dosen getDosenByEmailDosen(String emailDosen) {
        Optional<Dosen> dosen = dosenDb.findByEmailDosen(emailDosen);
        if(dosen.isPresent()) return dosen.get();
        else return null;
    }

    @Override
    public List<Dosen> getListAllDosen(){
        return dosenDb.findAll();
    }

    @Override
    public boolean isDosenExist(String emailDosen){
        Optional<Dosen> dosen = dosenDb.findByEmailDosen(emailDosen);
        if(!dosen.isPresent()) return false;
        else return true;
    }

    @Override
    public boolean cekStatusAssesment(Dosen dosen, Pengguna user, Semester semester) {
        // Cek role apakah asesor1, asesor2, atau kaprodi
        String roleAssess = "";
        if (user.getRole().getNamaRole().equals("Asesor")){
           if (dosen.getListAsesor().indexOf(user.getAsesor()) == 0){
                roleAssess = "asesor1";
            } else if (dosen.getListAsesor().indexOf(user.getAsesor()) == 1){
                roleAssess = "asesor2";
            }
        } else if (user.getRole().getNamaRole().equals("Kaprodi")){
            roleAssess = "kaprodi";
        }

       
        
        for (ItemBKD i : itemBKDService.getListByDosenAndSemester(dosen, semester)){
            if (roleAssess.equals("asesor1")){
                if (i.getRekomendasiAsesor1() == null || !( i.getRekomendasiAsesor1().equalsIgnoreCase("Gagal") || i.getRekomendasiAsesor1().equalsIgnoreCase("Selesai"))){
                    return false;
                }
            } 
            if (roleAssess.equals("asesor2")){
                if ( i.getRekomendasiAsesor2() == null || !( i.getRekomendasiAsesor2().equalsIgnoreCase("Gagal") || i.getRekomendasiAsesor2().equalsIgnoreCase("Selesai"))){
                    return false;
                }
            }
            if (roleAssess.equals("kaprodi")){
                if (i.getKomentarKaprodi() == null || (i.getKomentarKaprodi().equals("")) || (i.getKomentarKaprodi().equals(" "))){
                    return false;
                }
            } 
        } 
        
        return true;
        


        
    }

    @Override
    public List <Dosen> findAllPenggunaByInProdi(String prodi, Dosen dosen){
        List<Dosen> hasil = new ArrayList<Dosen> ();
        if (dosen.getProdi() == null){
            return hasil;
        }

        for (Dosen d : getListAllDosen()){
            if (d.getProdi() != null && d.getProdi().equalsIgnoreCase(prodi)){
                hasil.add(d);
            }
        }
        if (dosen != null){
            hasil.remove(dosen);
        }
        
        return hasil;
    }

   

    @Override
    public List <Dosen> searchDosen(String keyword, String status, Semester semester, Pengguna user){
        List<Dosen> hasil = new ArrayList<Dosen> ();
        List<Dosen> allDosen = new ArrayList<Dosen> ();
        if (user.getRole().getNamaRole().equals("Asesor")){
            allDosen = asesorService.getAsesorByEmailAsesor(user.getEmailPengguna()).getListDosen();
        }
        if (user.getRole().getNamaRole().equals("Kaprodi")){
            allDosen = findAllPenggunaByInProdi(getDosenByEmailDosen(user.getEmailPengguna()).getProdi(), getDosenByEmailDosen(user.getEmailPengguna()));
        }

        if (status != null && status != "All" ){
            for (Dosen d : allDosen){
                if (status.equalsIgnoreCase("Belum ada item") && itemBKDService.getListByDosenAndSemester(d, semester).size()==0){
                    hasil.add(d);                    
                }
                if (status.equalsIgnoreCase("Sudah selesai") && cekStatusAssesment(d, user, semester) == true && itemBKDService.getListByDosenAndSemester(d, semester).size()!=0){
                    hasil.add(d);
                }
                if (status.equalsIgnoreCase("Belum selesai") && cekStatusAssesment(d, user, semester) == false){
                    hasil.add(d);
                }
            }
        }

        if (keyword != null && keyword != "" && keyword != " "){
            keyword = keyword.toLowerCase();

            if (status == null || status.equals("All")){
                for (Dosen d: allDosen){
                    if (d.getNamaDosen().toLowerCase().contains(keyword)){
                        hasil.add(d);
                    }
                }
            } else {
                for (Dosen d : allDosen){
                    if (d.getNamaDosen().toLowerCase().contains(keyword)){
                        String st = "";
                        if (itemBKDService.getListByDosenAndSemester(d, semester).size()==0){
                            st = "Belum Ada Item"  ;             
                        }
                        if (cekStatusAssesment(d, user, semester) == true && itemBKDService.getListByDosenAndSemester(d, semester).size()!=0){
                            st = "Sudah Selesai"  ;  
                        }
                        if (cekStatusAssesment(d, user, semester) == false){
                            st = "Belum Selesai";
                        }
                        if (st.equalsIgnoreCase(status) && !hasil.contains(d)){
                            hasil.add(d);
                        }
                        if (!st.equalsIgnoreCase(status))  {
                            hasil.remove(d);
                        }

                    }
                    else {
                        hasil.remove(d);
                    }
                }
                
            }

        }

        if ((keyword == null || keyword == "" || keyword == " ") && (status == null || status.equals("All")) ){
                hasil = allDosen;
        }
                
            



        return hasil;
    }

    @Override
    public void updateStatusDosen(Dosen dosen) {
        // TODO Auto-generated method stub
        /*
        status = belum selesai / belum ada item / belum ada asesor / sudah selesai / selesai asesor 1 / selesai asesor 2
        jd suatu item bkd itu statusnya selesai kalo rekomendasi asesornya 2 2nya selesai/gagal.
        jd ini caranya ngeloop semua item bkdnya dosen, terus:

        - belum selesai = minimal ada 1 aja item bkd yg rekomendasi asesor 1 dan asesor 2nya belum selesai/gagal
        - belum ada item = list item bkdnya 0
        - belum ada asesor = list asesor 0 
        - sudah selesai = SEMUA item bkd rekomendasi asesor 1 dan 2 nya udah (selesai/gagal)
        - selesai asesor 1 = semua rekomendasi item bkd dari dosen statusnya udah selesai/gagal untuk asesor 1, tapi asesor 2 nya engga
        - selesai asesor 2 = semua rekomendasi item bkd dari dosen statusnya udah selesai/gagal untuk asesor 2, tapi asesor 1 nya engga
          
        */

        List<ItemBKD> itemBKDListDosenCurrentSemester = itemBKDService.getListByDosenAndSemester(dosen, semesterService.getCurrentSemester());

        String status = "";
        if(dosen.getListAsesor() == null || dosen.getListAsesor().size() == 0) {
            status = "Belum Ada Asesor";
        } else {
            if(itemBKDListDosenCurrentSemester.size() == 0) {
                status = "Belum Ada Item";
            } else {
                if(dosen.getListAsesor().size() == 2) {
                    boolean isAsesor1DoneAses = isAsesor1DoneAses(itemBKDListDosenCurrentSemester);
                    boolean isAsesor2DoneAses = isAsesor2DoneAses(itemBKDListDosenCurrentSemester);

                    if(isAsesor1DoneAses == true && isAsesor2DoneAses == true) {
                        status = "Sudah Selesai";
                    } else if(isAsesor1DoneAses == true && isAsesor2DoneAses == false) {
                        status = "Selesai Asesor 1";
                    } else if(isAsesor1DoneAses == false && isAsesor2DoneAses == true) {
                        status = "Selesai Asesor 2";
                    } else {
                        status = "Belum Selesai";
                    }
                } else {
                    boolean isAsesorDoneAses = isAsesor1DoneAses(itemBKDListDosenCurrentSemester);

                    status = (isAsesorDoneAses == true) ? "Sudah Selesai" : "Belum Selesai";
                }
            }
        }

        dosen.setStatus(status);
        updateDosen(dosen);
        
    }

    public boolean isAsesor1DoneAses(List<ItemBKD> itemBKDListDosenCurrentSemester) {
        boolean flag = true;
        for(ItemBKD itemBKD : itemBKDListDosenCurrentSemester) {
            String rekomendasiAsesor1 = itemBKD.getRekomendasiAsesor1();
            if(rekomendasiAsesor1 == null) {
                flag = false;
                break;
            } else {
                if(!(rekomendasiAsesor1.equals("Selesai") | rekomendasiAsesor1.equals("Gagal"))) {
                    flag = false;
                    break;
                }
            }

        }
        return flag;
    }

    public boolean isAsesor2DoneAses(List<ItemBKD> itemBKDListDosenCurrentSemester) {
        boolean flag = true;
        for(ItemBKD itemBKD : itemBKDListDosenCurrentSemester) {
            String rekomendasiAsesor2 = itemBKD.getRekomendasiAsesor2();
            if(rekomendasiAsesor2 == null) {
                flag = false;
                break;
            } else {
                if(!(rekomendasiAsesor2.equals("Selesai") | rekomendasiAsesor2.equals("Gagal"))) {
                    flag = false;
                    break;
                }
            }

        }
        return flag;
    }

        @Override
        public List <Dosen> findAllDosenByStatus(String status, Pengguna user){
            List <Dosen> hasil = new ArrayList<Dosen>();
            List<Dosen> allDosen = new ArrayList<Dosen> ();
            if (user.getRole().getNamaRole().equals("Staff SDM") || user.getRole().getNamaRole().equals("Rektor")){
                allDosen = getListAllDosen();
            }
            if (user.getRole().getNamaRole().equals("Kaprodi")){
                allDosen = findAllPenggunaByInProdi(getDosenByEmailDosen(user.getEmailPengguna()).getProdi(), getDosenByEmailDosen(user.getEmailPengguna()));
            }
            for (Dosen dosen : allDosen){
                if (status != null && dosen.getStatus().equalsIgnoreCase(status)){
                    hasil.add(dosen);
                }
            }
            return hasil;
        }

        @Override
        public List<Dosen> searchDosenByStatus(String status, String keyword, Pengguna user){
            List<Dosen> hasil = new ArrayList<Dosen>();
            List<Dosen> allDosen = new ArrayList<Dosen> ();
            if (user.getRole().getNamaRole().equals("Staff SDM") ||user.getRole().getNamaRole().equals("Rektor") ){
                allDosen = getListAllDosen();
            }
            if (user.getRole().getNamaRole().equals("Kaprodi")){
                allDosen = findAllPenggunaByInProdi(getDosenByEmailDosen(user.getEmailPengguna()).getProdi(), getDosenByEmailDosen(user.getEmailPengguna()));
            }
        
            if (status != null || status != "All"){
                hasil = findAllDosenByStatus(status, user);
            }
            
            if (keyword != null && keyword != "" && keyword != " ") {
                keyword = keyword.toLowerCase();
    
                if (status == null || status.equals("All")){
                    for (Dosen p : allDosen){
                        if (p.getNamaDosen().toLowerCase().contains(keyword)){
                            hasil.add(p); 
                        }                
                    }
                } else {
                    for (Dosen p : allDosen){
                        if (p.getNamaDosen().toLowerCase().contains(keyword)){
                            String nama = p.getStatus();
                            if (nama.equals(status) && !hasil.contains(p)){
                                hasil.add(p);
                            }
                            if (!nama.equals(status))  {
                                hasil.remove(p);
                            }
                        }
                        else {
                            hasil.remove(p);
                        }       
                             
                    }
    
                } 
    
    
                }
                
                if ((keyword == null || keyword == "" || keyword == " ") && (status == null || status.equals("All")) ){
                    hasil = allDosen;
                }
        
                return hasil;
            }


        }


