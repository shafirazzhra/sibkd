package propensi.sibkd.sibkd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.service.*;

import java.io.IOException;

@Controller
public class FileController {
    @Autowired
    FileService fileService;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    DosenService dosenService;

    @PostMapping("/biodata/{idDosen}/{doc}/upload")
    public String uploadFile(
            @ModelAttribute File fileModel,
            @PathVariable(value="idDosen") Long idDosen,
            @PathVariable(value="doc") String doc,
            @RequestParam(value="file") MultipartFile file,
            Model model) throws IOException {
        File fileUploaded = fileService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/viewFile/")
                .path(fileUploaded.getId())
                .toUriString();

        fileUploaded.setUrlView(fileDownloadUri);
        fileService.updateFile(fileUploaded);

        Dosen dosen = dosenService.getDosenByIdDosen(idDosen);

        if (doc.equals("nidn")) {
            dosen.setUrlNidn(fileDownloadUri);

        } else if (doc.equals("serdos")) {
            dosen.setUrlSerdos(fileDownloadUri);

        } else if (doc.equals("jastruk")) {
            dosen.setUrlJastruk(fileDownloadUri);

        } else if (doc.equals("jafung")) {
            dosen.setUrlJafung(fileDownloadUri);

        } else if (doc.equals("ijazah1")) {
            dosen.setUrlijazah1(fileDownloadUri);

        } else if (doc.equals("ijazah2")) {
            dosen.setUrlijazah2(fileDownloadUri);

        } else if (doc.equals("foto")) {
            if (dosen.getUrlFoto() != null) {
                File foto = fileService.getFileByUrl(dosen.getUrlFoto());
                fileService.deleteFile(foto);
            }
            dosen.setUrlFoto(fileDownloadUri);
        }

        dosenService.updateDosen(dosen);
        
        return "redirect:/biodata";
    }
}
