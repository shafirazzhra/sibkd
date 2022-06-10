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

@Controller
@RequestMapping("/jadwal-bkd")
public class JadwalBKDController {

    @Qualifier("jadwalServiceImpl")
    @Autowired
    private JadwalService jadwalService;

    @Qualifier("semesterServiceImpl")
    @Autowired
    private SemesterService semesterService;

    @GetMapping("/edit/{idSemester}")
    public String editJadwalForm(@PathVariable Long idSemester, Model model) {
        Semester semester = semesterService.getSemesterById(idSemester);
        JadwalBKD jadwalBKD = semester.getJadwalBKD();
        model.addAttribute("jadwalBKD", jadwalBKD);
        model.addAttribute("semester", semester);
        return "form-edit-jadwal";
    }

    @PostMapping("/edit")
    public String editJadwalSubmit(
            @ModelAttribute JadwalBKD jadwalBKD,
            RedirectAttributes redirAttr
    ) {
        if(jadwalService.checkIfJadwalFormatTrue(jadwalBKD, jadwalBKD.getSemester())) {
            jadwalService.updateJadwalBKD(jadwalBKD);
            redirAttr.addFlashAttribute("success", "Jadwal Semester " + jadwalBKD.getSemester().getNamaSemester() + " berhasil diubah.");
            return "redirect:/semester/view-all";
        } else {
            redirAttr.addFlashAttribute("error", "Jadwal Semester " + jadwalBKD.getSemester().getNamaSemester() + " tidak berhasil diubah karena kesalahan tanggal. Silakan masukkan kembali.");
            return "redirect:/jadwal-bkd/edit/" + jadwalBKD.getSemester().getIdSemester();
        }
    }
}
