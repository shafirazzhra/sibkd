package propensi.sibkd.sibkd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.model.JadwalBKD;
import propensi.sibkd.sibkd.model.Semester;
import propensi.sibkd.sibkd.service.JadwalService;
import propensi.sibkd.sibkd.service.SemesterService;

import java.util.List;

@Controller
@RequestMapping("/semester")
public class SemesterController {

    @Qualifier("semesterServiceImpl")
    @Autowired
    private SemesterService semesterService;

    @Qualifier("jadwalServiceImpl")
    @Autowired
    private JadwalService jadwalService;

    @GetMapping("/view-all")
    public String listSemester(Model model) {
        List<Semester> semesterList = semesterService.getSemesterList();
        model.addAttribute("listSemester", semesterList);
        model.addAttribute("namaCurrentSemester", semesterService.getCurrentSemester().getNamaSemester());
        return "viewall-semester";
    }

    @GetMapping("/tambah")
    public String tambahSemesterForm( Model model) {
        Semester semester = new Semester();
        JadwalBKD jadwalBKD = new JadwalBKD();
        jadwalBKD.setSemester(semester);
        model.addAttribute("jadwalBKD", jadwalBKD);
        model.addAttribute("semester", semester);
        return "form-add-semester";
    }

    @PostMapping("/tambah")
    public String tambahSemesterSubmit(
            @ModelAttribute JadwalBKD jadwalBKD,
            @ModelAttribute Semester semester,
            RedirectAttributes redirAttr
    ) {
        if(jadwalService.checkIfJadwalFormatTrue(jadwalBKD, semester) && semesterService.checkIfJadwalSemesterNotOverlap(jadwalBKD, semester)) {
            semesterService.addSemester(semester);
            jadwalBKD.setSemester(semester);
            jadwalService.addJadwalBKD(jadwalBKD);
            redirAttr.addFlashAttribute("success", "Semester " + jadwalBKD.getSemester().getNamaSemester() + " berhasil ditambah.");
            return "redirect:/semester/view-all";
        } else {
            redirAttr.addFlashAttribute("error", "Semester " + semester.getNamaSemester() + " tidak berhasil ditambah karena ada kesalahan tanggal pada jadwal. Silakan masukkan kembali.");
            return "redirect:/semester/tambah";
        }
    }

    @GetMapping("/edit/{idSemester}")
    public String editSemesterForm(@PathVariable Long idSemester, Model model) {
        Semester semester = semesterService.getSemesterById(idSemester);
        JadwalBKD jadwalBKD = semester.getJadwalBKD();
        model.addAttribute("semester", semester);
//        model.addAttribute("jadwalBKD", jadwalBKD);
        return "form-edit-semester";
    }

    @PostMapping("/edit")
    public String editSemesterSubmit(
            @ModelAttribute Semester semester,
//            @ModelAttribute JadwalBKD jadwalBKD,
            RedirectAttributes redirAttr
    ) {
        if (jadwalService.checkIfJadwalFormatTrue(semester.getJadwalBKD(), semester) && semesterService.checkIfJadwalSemesterNotOverlap(semester.getJadwalBKD(), semester)) {

            if(semesterService.isNamaSemesterAlreadyExistWhenUpdate(semester) == true) {
                redirAttr.addFlashAttribute("error", "Nama Semester sudah ada. Silakan masukkan kembali.");
                return "redirect:/semester/edit/" + semester.getIdSemester();
            } else {
                semesterService.updateSemester(semester);
                redirAttr.addFlashAttribute("success", "Semester " + semester.getNamaSemester() + " berhasil diubah.");
                return "redirect:/semester/view-all";
            }
        } else {
            redirAttr.addFlashAttribute("error", "Semester " + semester.getNamaSemester() + " tidak berhasil diubah karena kesalahan tanggal. Silakan masukkan kembali.");
            return "redirect:/semester/edit/" + semester.getIdSemester();
        }
    }

}
