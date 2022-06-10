package propensi.sibkd.sibkd.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.service.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;

@Controller
public class BiodataController{
    @Autowired
    private PenggunaService penggunaService;

    @Autowired
    private DosenService dosenService;

    @Autowired
    private FileService fileService;

    @Autowired
    private JabatanStrukturalService jabatanStrukturalService;

    @GetMapping("/biodata")
    private String viewBiodata(Authentication auth, Model model, RedirectAttributes redirAttr){
        if(auth != null){
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Pengguna user = penggunaService.findByEmail(email);
            Dosen dosen = user.getDosen();

            String fotoId = "";
            if (dosen.getUrlFoto() != null) {
                fotoId = "/viewFile/" + fileService.getFileByUrl(dosen.getUrlFoto()).getId();
            }
            model.addAttribute("foto", fotoId);
            model.addAttribute("user", user);
            model.addAttribute("dosen", dosen);
            redirAttr.addFlashAttribute("success", "Foto berhasil diupload");
            
        }
        return "view-biodata";
    }

    @GetMapping("/biodata/edit")
    private String editBiodata(Authentication auth, Model model){
        if(auth != null){
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Pengguna user = penggunaService.findByEmail(email);
            List<JabatanStruktural> listJabstruk = jabatanStrukturalService.getListJabatanStruktural();

            String fotoId = "";
            if (user.getDosen().getUrlFoto() != null) {
                fotoId = "/viewFile/" + fileService.getFileByUrl(user.getDosen().getUrlFoto()).getId();
            }

            model.addAttribute("foto", fotoId);
            model.addAttribute("user", user);
            model.addAttribute("listJabstruk", listJabstruk);
        }
        return "form-edit-biodata";
    }

    @PostMapping("/biodata/edit")
    public String editBiodataSubmit(@ModelAttribute Pengguna user, 
                                    @RequestParam(value="jabstruk", required=false) Long idJabatanStruktural,
                                    Model model, RedirectAttributes redirAttr) {
        Pengguna userInDb = penggunaService.findByEmail(user.getEmailPengguna());
        userInDb.setNamaPengguna(user.getNamaPengguna());
        userInDb.setNoTelp(user.getNoTelp());
        userInDb.setAlamat(user.getAlamat());

        JabatanStruktural jabstruk = jabatanStrukturalService.getJabstrukByIdJabstruk(idJabatanStruktural);
        
        Dosen dosenInDb = userInDb.getDosen();
        Dosen dosenToUpdate = user.getDosen();
        dosenInDb.setNidn(dosenToUpdate.getNidn());
        if (jabstruk != null){
            System.out.println(jabstruk.getNamaJabatanStruktural());
            System.out.println(jabstruk.getIdJabatanStruktural());
            dosenInDb.setJabstruk(jabstruk);
        }
        
        dosenInDb.setNoSerdos(dosenToUpdate.getNoSerdos());
        dosenInDb.setPerguruanTinggi(dosenToUpdate.getPerguruanTinggi());
        dosenInDb.setFakultas(dosenToUpdate.getFakultas());
        dosenInDb.setProdi(dosenToUpdate.getProdi());
        dosenInDb.setJabatanFungsional(dosenToUpdate.getJabatanFungsional());
        dosenInDb.setSkripsi(dosenToUpdate.getSkripsi());
        dosenInDb.setTesis(dosenToUpdate.getTesis());
        dosenInDb.setPtS1(dosenToUpdate.getPtS1());
        dosenInDb.setPtS2(dosenToUpdate.getPtS2());

        penggunaService.updatePengguna(userInDb);
        dosenService.updateDosen(dosenInDb);
        redirAttr.addFlashAttribute("success", "Biodata berhasil diubah.");

        return "redirect:/biodata";
    }

    @GetMapping("/biodata/{idDosen}/{doc}/delete")
    public String deleteBioFile(
            @ModelAttribute File fileModel,
            @PathVariable(value="idDosen") Long idDosen,
            @PathVariable(value="doc") String doc,
            Model model,
            RedirectAttributes redirAttr) {

        Dosen dosen = dosenService.getDosenByIdDosen(idDosen);
        if (doc.equals("nidn")) {
            File file = fileService.getFileByUrl(dosen.getUrlNidn());
            fileService.deleteFile(file);
            dosen.setUrlNidn(null);
            redirAttr.addFlashAttribute("success", "File Kartu NIDN berhasil dihapus.");

        } else if (doc.equals("serdos")) {
            File file = fileService.getFileByUrl(dosen.getUrlSerdos());
            fileService.deleteFile(file);
            dosen.setUrlSerdos(null);
            redirAttr.addFlashAttribute("success", "File Sertifikat Dosen berhasil dihapus.");

        } else if (doc.equals("jastruk")) {
            File file = fileService.getFileByUrl(dosen.getUrlJastruk());
            fileService.deleteFile(file);
            dosen.setUrlJastruk(null);
            redirAttr.addFlashAttribute("success", "File SK Jabatan Struktural berhasil dihapus.");

        } else if (doc.equals("jafung")) {
            File file = fileService.getFileByUrl(dosen.getUrlJafung());
            fileService.deleteFile(file);
            dosen.setUrlJafung(null);
            redirAttr.addFlashAttribute("success", "File SK Jabatan Fungsional berhasil dihapus.");

        } else if (doc.equals("ijazah1")) {
            File file = fileService.getFileByUrl(dosen.getUrlijazah1());
            fileService.deleteFile(file);
            dosen.setUrlijazah1(null);
            redirAttr.addFlashAttribute("success", "File Ijazah S1 berhasil dihapus.");

        } else if (doc.equals("ijazah2")) {
            File file = fileService.getFileByUrl(dosen.getUrlijazah2());
            fileService.deleteFile(file);
            dosen.setUrlijazah2(null);
            redirAttr.addFlashAttribute("success", "File Ijazah S2 berhasil dihapus.");
        }

        dosenService.updateDosen(dosen);
        return "redirect:/biodata";
    }
}