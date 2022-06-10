package propensi.sibkd.sibkd.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.service.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pelaporan-bkd")
public class PelaporanBKDController {

    @Qualifier("dosenServiceImpl")
    @Autowired
    private DosenService dosenService;

    @Qualifier("asesorServiceImpl")
    @Autowired
    private AsesorService asesorService;

    @Qualifier("penggunaServiceImpl")
    @Autowired
    private PenggunaService penggunaService;

    @Autowired
    private SemesterService semesterService;

    @Autowired
    private JadwalService jadwalService;

    @Autowired
    private ItemBKDService itemBKDService;

    @Autowired
    private BidangService bidangService;

    @Autowired
    private FileService fileService;

    @GetMapping("/view-all")
    public String retrievePelaporan(Model model, Authentication auth, @RequestParam(value="keyword", required=false) String keyword, @RequestParam(value="status", required=false) String status) {
       
        List <String> listStatus = new ArrayList <String> ();
        listStatus.add("Belum Selesai");
        listStatus.add("Sudah Selesai");
        listStatus.add("Belum Ada Item");
        listStatus.add("Selesai Asesor 1");
        listStatus.add("Selesai Asesor 2");
        listStatus.add("Belum Ada Asesor");
        model.addAttribute("listStatus", listStatus);
        CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
        String email = oauthUser.getEmail();
        Pengguna user = penggunaService.findByEmail(email);
        String role = user.getRole().getNamaRole();
        List<Dosen> dosenList = dosenService.searchDosenByStatus(status, keyword, user);
        Semester semester = semesterService.getCurrentSemester();
        String currentJadwal = jadwalService.getCurrentJadwal(semester);
        Boolean bisaRemind = semesterService.isPeriodeInput(semester) || semesterService.isPeriodePenilaian(semester) || semesterService.isPeriodePerbaikan(semester) || semesterService.isPeriodePenilaianUlang(semester);
        Boolean bisaRemindAsesor = semesterService.isPeriodePenilaian(semester) || semesterService.isPeriodePenilaianUlang(semester);
        Boolean bisaRemindDosen = semesterService.isPeriodeInput(semester) || semesterService.isPeriodePerbaikan(semester);
        Boolean tidakBisaEditAsesorJadwal = currentJadwal.equals("Perbaikan Laporan BKD") || currentJadwal.equals("Penilaian Ulang Asesor");

        for(Dosen aDosen : dosenList) {
            dosenService.updateStatusDosen(aDosen);
        }

        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("dosenList", dosenList);
        model.addAttribute("role", role);
        model.addAttribute("bisaremind", bisaRemind);
        model.addAttribute("bisaremindasesor", bisaRemindAsesor);
        model.addAttribute("bisareminddosen", bisaRemindDosen);
        model.addAttribute("currentJadwal", currentJadwal);
        model.addAttribute("tidakBisaEditAsesorJadwal", tidakBisaEditAsesorJadwal);
        
        return "view-all-pelaporan-bkd";
    }

    @GetMapping("/edit-asesor/{emailDosen}")
    public String editAsesorForm(
            @PathVariable String emailDosen,
            Model model
    ) {
        Dosen dosen = dosenService.getDosenByEmailDosen(emailDosen);
        List<Asesor> listAsesor1 = asesorService.getListAllAsesor();
        List<Asesor> listAsesor2 = asesorService.getListAllAsesor();
        model.addAttribute("dosen", dosen);
        model.addAttribute("idDosen", dosen.getIdDosen());

        // Buat objek baru biar kalau pengen asesor 1 aja
        Asesor newAsesor2 = new Asesor((long) -1, "-- Pilih Asesor 2 --");
        listAsesor2.add(0, newAsesor2);
        model.addAttribute("listAsesor1", listAsesor1);
        model.addAttribute("listAsesor2", listAsesor2);

        return "form-edit-asesor";
    }

    @PostMapping("/edit-asesor/{emailDosen}")
    public String editAsesorSubmit(
            @PathVariable String emailDosen,
            @ModelAttribute Dosen dosen,
            RedirectAttributes redirAttr
   ) {

        if(dosen.getListAsesor().get(1) == null) {
            dosen.getListAsesor().remove(1);
        }

        boolean isAsesorAssignTheirSelf = asesorService.isAsesorAssignTheirSelf(dosen.getEmailDosen(), dosen.getListAsesor());

        if(isAsesorAssignTheirSelf == false) {
            if(dosen.getListAsesor().size() == 1) {
                dosenService.updateDosen(dosen);
                redirAttr.addFlashAttribute("success", "Berhasil meng-assign asesor ke dosen bernama " + dosen.getNamaDosen() + " dengan email: " + dosen.getEmailDosen());
                dosenService.updateStatusDosen(dosen);
                return "redirect:/pelaporan-bkd/view-all";
            } else {
                boolean isAsesorSame = asesorService.isAsesorSame(dosen.getListAsesor().get(0), dosen.getListAsesor().get(1));
                if(isAsesorSame == false) {
                    dosenService.updateDosen(dosen);
                    dosenService.updateStatusDosen(dosen);
                    redirAttr.addFlashAttribute("success", "Berhasil meng-assign asesor ke dosen bernama " + dosen.getNamaDosen() + " dengan email: " + dosen.getEmailDosen());
                    return "redirect:/pelaporan-bkd/view-all";
                } else {
                    redirAttr.addFlashAttribute("error", "Asesor 1 dan Asesor 2 tidak bisa sama");
                    return "redirect:/pelaporan-bkd/edit-asesor/" + dosen.getEmailDosen();
                }
            }
        } else {
            redirAttr.addFlashAttribute("error", "Asesor dari seorang dosen harus merupakan dosen lain");
            return "redirect:/pelaporan-bkd/edit-asesor/" + dosen.getEmailDosen();
        }


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
        // Default kalo baru buka yg ditampilin all bidang dan semester sekarang
        if ((idSemester == null || idSemester == currentSemester.getIdSemester()) && (namaBidang == null || namaBidang.equals("All"))) {
            listItemDosen = itemBKDService.getListByDosenAndSemester(dosen, currentSemester);
        }
        // Kalo udah pake filter buat milih bidang dan semester
        else {
            listItemDosen = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, selectedSemester, bidangService.getBidangByNama(namaBidang));
        }

        model.addAttribute("foto", fotoId);
        model.addAttribute("dosen", dosen);
        model.addAttribute("semesterId", idSemester);
        model.addAttribute("namaBidang", namaBidang);
        model.addAttribute("semesterList", semesterList);
        model.addAttribute("namaBidangList", namaBidangList);
        model.addAttribute("selectedSemester", selectedSemester);
        model.addAttribute("currentSemester", currentSemester);
        model.addAttribute("listItem", listItemDosen);

        return "view-detail-pelaporan";



    }



}
