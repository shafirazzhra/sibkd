package propensi.sibkd.sibkd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.service.*;
import propensi.sibkd.sibkd.model.*;

@Controller
@RequestMapping("/user")
public class AdminController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PenggunaService penggunaService;

    @Autowired
    private DosenService dosenService;

    @Autowired
    private AsesorService asesorService;

    @GetMapping("")
    private String listUser(Model model, @RequestParam(value="keyword", required=false) String keyword, @RequestParam(value="namaRole", required=false) String namaRole){
        List<Pengguna> listUser = penggunaService.searchPengguna(keyword, namaRole);



        model.addAttribute("keyword", keyword);
        model.addAttribute("namaRole", namaRole);
        model.addAttribute("listUser", listUser);
        model.addAttribute("listRole", roleService.findAllRole());
        return "viewall-user";
    }

    @GetMapping("/{email}")
    public String detailUser(@PathVariable String email, Model model) {
        Pengguna user = penggunaService.findByEmail(email);
        model.addAttribute("user", user);
        return "detail-user";
    }

    @GetMapping("revoke/{email}")
    public String revokeUserAccess(@PathVariable String email, Model model, RedirectAttributes redirAttr) {
        Pengguna userInDb = penggunaService.findByEmail(email);
        userInDb.setRole(roleService.findByNama("Not Assigned"));
        userInDb.setDosen(null);
        userInDb.setAsesor(null);
        penggunaService.updatePengguna(userInDb);
        redirAttr.addFlashAttribute("success", "Akses pengguna dengan nama " + userInDb.getNamaPengguna() + " berhasil dihapus.");
        return "redirect:/user/";
    }

    @GetMapping("/edit/{email}")
    public String editUserForm(@PathVariable String email, Model model) {
        Pengguna user = penggunaService.findByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("listRole", roleService.findAllRole());
        return "form-edit-user";
    }

    @PostMapping("/edit")
    public String editUserSubmit(@ModelAttribute Pengguna user, @RequestParam("emailBaru") String emailBaru, Model model, RedirectAttributes redirAttr) {
        Pengguna userInDb = penggunaService.findByEmail(user.getEmailPengguna());
        userInDb.setNamaPengguna(user.getNamaPengguna());
        userInDb.setNoTelp(user.getNoTelp());
        userInDb.setAlamat(user.getAlamat());

        Role role = user.getRole();
        if (role.getNamaRole().equals("Dosen") || role.getNamaRole().equals("Asesor") || role.getNamaRole().equals("Rektor") || role.getNamaRole().equals("Kaprodi")) {
            // membuat objek dosen untuk asesor karena asesor pasti seorang dosen
            if (!dosenService.isDosenExist(user.getEmailPengguna())){
                Dosen dosenBaru = new Dosen();
                dosenBaru.setNamaDosen(user.getNamaPengguna());
                dosenBaru.setEmailDosen(emailBaru);
                dosenService.addDosen(dosenBaru);
                userInDb.setDosen(dosenBaru);
            }

            if (role.getNamaRole().equals("Asesor") && !asesorService.isAsesorExist(user.getEmailPengguna())) {
                Asesor asesorBaru = new Asesor();
                asesorBaru.setNamaAsesor(user.getNamaPengguna());
                asesorBaru.setEmailAsesor(emailBaru);
                asesorService.addAsesor(asesorBaru);
                userInDb.setAsesor(asesorBaru);
            }
            if(dosenService.isDosenExist(user.getEmailPengguna())){
                Dosen dosenLama = dosenService.getDosenByEmailDosen(user.getEmailPengguna());
                dosenLama.setNamaDosen(user.getNamaPengguna());
                dosenLama.setEmailDosen(emailBaru);
                dosenService.updateDosen(dosenLama);
            }
    
            if(asesorService.isAsesorExist(user.getEmailPengguna())){
                Asesor asesorLama = asesorService.getAsesorByEmailAsesor(user.getEmailPengguna());
                asesorLama.setNamaAsesor(user.getNamaPengguna());
                asesorLama.setEmailAsesor(emailBaru);
                asesorService.updateAsesor(asesorLama);
            }

            
        }

        
        userInDb.setEmailPengguna(emailBaru);
        userInDb.setRole(role);

        penggunaService.updatePengguna(userInDb);

        redirAttr.addFlashAttribute("success", "Data pengguna berhasil diubah.");

        return "redirect:/user/" + userInDb.getEmailPengguna();
    }
}