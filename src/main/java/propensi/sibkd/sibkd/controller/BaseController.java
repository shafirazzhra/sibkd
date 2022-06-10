package propensi.sibkd.sibkd.controller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.RedirectView;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import propensi.sibkd.sibkd.service.*;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.security.*;




@Controller
public class BaseController {

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    SemesterService semesterService;
    
    @Autowired
    FileService fileService;

    @Autowired
    JadwalService jadwalService;

    @GetMapping("/")
    private String home(Model model, Authentication auth){

        if (auth != null){
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Pengguna user = penggunaService.findByEmail(email);
            String nama = user.getNamaPengguna();
            String role = user.getRole().getNamaRole();
            if (user.getRole().getNamaRole().equals("Not Assigned")){
                return "redirect:/wait";
            }

            model.addAttribute("nama", nama);
            model.addAttribute("email", email);
            model.addAttribute("role", role);



            Semester currentSemester = semesterService.getCurrentSemester();
            String currentJadwal = jadwalService.getCurrentJadwal(currentSemester);

            model.addAttribute("currentSemester", currentSemester);
            model.addAttribute("jadwal", currentSemester.getJadwalBKD());
            model.addAttribute("currentJadwal", currentJadwal);

//            // Untuk tabel jadwal
//            model.addAttribute("listJadwalBKD", jadwalBKDList);
//
//            // Untuk pilihan dropdown
//            model.addAttribute("currentSemester", currentSemester);

//            // Untuk jadwal saat ini
//            model.addAttribute("currentJadwal", semesterService.getCurrentJadwal(currentSemester).getNamaKegiatan());

            return"home";
        }
        
        return "login";
    }

    
    @GetMapping("/wait")
    private String tunggu(Authentication auth) {
        CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Pengguna user = penggunaService.findByEmail(email);
            if (!user.getRole().getNamaRole().equals("Not Assigned")){
                return "redirect:/";
            }
        return "tunggu";
    }


    @GetMapping("auth")
    @ResponseBody
    public Authentication auth(Authentication auth) {
        return auth;
    }

    @GetMapping("/design-system")
    private String designSystem() {
        return "design-system";
    }

    

    @GetMapping("/downloadFile/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        // Load file from database
        File dbFile = fileService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getName() + "\"")
                .body(new ByteArrayResource(dbFile.getData()));
    }

    @GetMapping("/viewFile/{fileId}")
    public ResponseEntity<Resource> dviewFile(@PathVariable String fileId, HttpServletRequest request) {
        // Load file from database
        File dbFile = fileService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getType()))
                .body(new ByteArrayResource(dbFile.getData()));
    }
}
