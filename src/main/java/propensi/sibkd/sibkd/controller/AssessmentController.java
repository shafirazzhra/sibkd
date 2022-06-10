package propensi.sibkd.sibkd.controller;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.service.*;
import propensi.sibkd.sibkd.model.*;

@Controller
@RequestMapping("/assessment")
public class AssessmentController {

    @Autowired
    private PenggunaService penggunaService;
    @Autowired
    private SemesterService semesterService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ItemBKDService itemBKDService;
    @Autowired
    private BidangService bidangService;
    @Qualifier("dosenServiceImpl")
    @Autowired
    private DosenService dosenService;

    @GetMapping("/list")
    private String listAssessment(Model model, Authentication auth, @RequestParam(value="keyword", required=false) String keyword, @RequestParam(value="status", required=false) String status){
        List<String> listStatus = new ArrayList<String>();
        CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
        String email = oauthUser.getEmail();
        Pengguna user = penggunaService.findByEmail(email);
        Semester selectedSemester = semesterService.getCurrentSemester();
        boolean bisaAssess = false;
        LocalDateTime currentDate = LocalDateTime.now();

        if (currentDate.isAfter(selectedSemester.getJadwalBKD().getTanggalDibukaPenilaianAsesor2()) &&
                currentDate.isBefore(selectedSemester.getJadwalBKD().getTanggalDitutupPenilaianAsesor2())||
                currentDate.isAfter(selectedSemester.getJadwalBKD().getTanggalDibukaPenilaianUlangAsesor4()) &&
                currentDate.isBefore(selectedSemester.getJadwalBKD().getTanggalDitutupPenilaianUlangAsesor4())) {
                    bisaAssess = true;
        }


        listStatus.add("Belum Selesai");
        listStatus.add("Sudah Selesai");
        listStatus.add("Belum Ada Item");
        model.addAttribute("bisaAssess", bisaAssess);
        model.addAttribute("listStatus", listStatus);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("dosenService", dosenService);
        model.addAttribute("user", user);
        model.addAttribute("semester", semesterService.getCurrentSemester());
        List<Dosen> listUser = dosenService.searchDosen(keyword, status, semesterService.getCurrentSemester(), user);
        model.addAttribute("listUser", listUser);
        Integer jumlahAssess = 0;
        for (Dosen d : listUser){
            if (dosenService.cekStatusAssesment(d, user, semesterService.getCurrentSemester()) == false){
                jumlahAssess++;
            }
        }

        model.addAttribute("jumlahAssess", jumlahAssess);


        model.addAttribute("itemBKDService", itemBKDService);
        return "viewall-assesment";
    }

    @GetMapping("/detail/{emailDosen}")
    private String detailAssessmentDosen(
            Authentication auth,
            @PathVariable("emailDosen") String emailDosen,
            @RequestParam(value = "semesterId", required = false) Long idSemester,
            @RequestParam(value = "namaBidang", required = false) String namaBidang,
            Model model
    ){
        CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
        String emailUser = oauthUser.getEmail();
        Pengguna user = penggunaService.findByEmail(emailUser);
        Dosen dosen = penggunaService.findByEmail(emailDosen).getDosen();

        // Cek role apakah asesor1, asesor2, atau kaprodi
        String roleAssess = "";
        if (user.getRole().getNamaRole().equals("Asesor")){
            if (dosen.getListAsesor().indexOf(user.getAsesor()) == 0){
                roleAssess = "asesor1";
            } else if (dosen.getListAsesor().indexOf(user.getAsesor()) == 1){
                roleAssess = "asesor2";
            }
        } else if (user.getRole().getNamaRole().equals("Kaprodi") && user.getDosen().getProdi().equalsIgnoreCase(dosen.getProdi())){
            roleAssess = "kaprodi";
        }

        if (roleAssess.equals("")){
            return "error/403"; // jika tidak memiliki role assess maka menampilkan error 403 forbidden page
        } else {
            String fotoId = "";
            if (dosen.getUrlFoto() != null) {
                fotoId = "/viewFile/" + fileService.getFileByUrl(dosen.getUrlFoto()).getId();
            }

            List<String> namaBidangList = new ArrayList<String>();
            namaBidangList.add("All");
            for(Bidang bidang : bidangService.getListBidang()){
                namaBidangList.add(bidang.getNamaBidang());
            }

            List<Semester> semesterList = semesterService.getSemesterList();
            Semester selectedSemester = semesterService.getSemesterById(idSemester);
            Semester currentSemester = semesterService.getCurrentSemester();

            List<ItemBKD> listItemDosen;
            // Default jika baru buka halaman: yang ditampilin all bidang dan semester sekarang
            if ((idSemester == null || idSemester == currentSemester.getIdSemester()) && (namaBidang == null || namaBidang.equals("All"))) {
                listItemDosen = itemBKDService.getListByDosenAndSemester(dosen, currentSemester);
            }
            // Setelah pakai filter untuk pilih bidang dan semester
            else {
                listItemDosen = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, selectedSemester, bidangService.getBidangByNama(namaBidang));
            }

            boolean bisaAssess = false;
            LocalDateTime currentDate = LocalDateTime.now();

            if (currentDate.isAfter(currentSemester.getJadwalBKD().getTanggalDibukaPenilaianAsesor2()) &&
                    currentDate.isBefore(currentSemester.getJadwalBKD().getTanggalDitutupPenilaianAsesor2())||
                    currentDate.isAfter(currentSemester.getJadwalBKD().getTanggalDibukaPenilaianUlangAsesor4()) &&
                            currentDate.isBefore(currentSemester.getJadwalBKD().getTanggalDitutupPenilaianUlangAsesor4())) {
                bisaAssess = true;
            }

            model.addAttribute("roleAssess", roleAssess);
            model.addAttribute("foto", fotoId);
            model.addAttribute("dosen", dosen);
            model.addAttribute("semesterId", idSemester);
            model.addAttribute("namaBidang", namaBidang);
            model.addAttribute("semesterList", semesterList);
            model.addAttribute("namaBidangList", namaBidangList);
            model.addAttribute("selectedSemester", selectedSemester);
            model.addAttribute("currentSemester", currentSemester);
            model.addAttribute("listItem", listItemDosen);
            model.addAttribute("bisaAssess", bisaAssess);

            return "view-detail-assessment";
        }

    }

    @GetMapping("/{emailDosen}/edit/{idItem}")
    private String editAssessmentFormPage(
            Authentication auth,
            @PathVariable("emailDosen") String emailDosen,
            @PathVariable("idItem") Long idItem,
            Model model
    ){
        CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
        String emailUser = oauthUser.getEmail();
        Pengguna user = penggunaService.findByEmail(emailUser);
        Dosen dosen = penggunaService.findByEmail(emailDosen).getDosen();

        // Cek role apakah asesor1, asesor2, atau kaprodi
        String roleAssess = "";
        if (user.getRole().getNamaRole().equals("Asesor")){
            if (dosen.getListAsesor().indexOf(user.getAsesor()) == 0){
                roleAssess = "asesor1";
            } else if (dosen.getListAsesor().indexOf(user.getAsesor()) == 1){
                roleAssess = "asesor2";
            }
        } else if (user.getRole().getNamaRole().equals("Kaprodi") && user.getDosen().getProdi().equalsIgnoreCase(dosen.getProdi())){
            roleAssess = "kaprodi";
        }

        if (roleAssess.equals("")) {
            return "error/403"; // jika tidak memiliki role assess maka menampilkan error 403 forbidden page
        } else {
            List<String> listRekomendasi = new ArrayList<String>();
            listRekomendasi.add("Selesai");
            listRekomendasi.add("Lanjutkan");
            listRekomendasi.add("Gagal");
            listRekomendasi.add("Beban Lebih");
            listRekomendasi.add("Lainnya");

            ItemBKD item = itemBKDService.getItemById(idItem);

            model.addAttribute("item", item);
            model.addAttribute("emailDosen", emailDosen);
            model.addAttribute("roleAssess", roleAssess);
            model.addAttribute("listRekomendasi", listRekomendasi);

            return "form-edit-assessment";
        }
    }

    @PostMapping("/{emailDosen}/edit")
    private String editAssessmentSubmitPage(
            @PathVariable("emailDosen") String emailDosen,
            @ModelAttribute ItemBKD item,
            RedirectAttributes redirAttr
    ){
        Semester currentSemester = semesterService.getCurrentSemester();
        LocalDateTime currentDate = LocalDateTime.now();

        if (currentDate.isAfter(currentSemester.getJadwalBKD().getTanggalDibukaPenilaianAsesor2()) &&
                currentDate.isBefore(currentSemester.getJadwalBKD().getTanggalDitutupPenilaianAsesor2())||
                currentDate.isAfter(currentSemester.getJadwalBKD().getTanggalDibukaPenilaianUlangAsesor4()) &&
                        currentDate.isBefore(currentSemester.getJadwalBKD().getTanggalDitutupPenilaianUlangAsesor4())) {
            itemBKDService.updateItemBKD(item);
            Dosen dosen = item.getDosen();
            dosenService.updateStatusDosen(dosen);
            redirAttr.addFlashAttribute("success","Berhasil mengubah Catatan dan Rekomendasi untuk Item BKD " + item.getDeskripsi());
        } else {
            redirAttr.addFlashAttribute("error","Tidak dapat mengubah Catatan dan Rekomendasi karena belum memasuki jadwal Penilaian.");
        }

        return "redirect:/assessment/detail/" + emailDosen;
    }
}
