package propensi.sibkd.sibkd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
@RequestMapping("/item")
public class ItemBKDController {

    @Qualifier("penggunaServiceImpl")
    @Autowired
    private PenggunaService penggunaService;

    @Qualifier("itemBKDServiceImpl")
    @Autowired
    private ItemBKDService itemBKDService;

    @Autowired
    private SemesterService semesterService;

    @Autowired
    private KegiatanBKDService kegiatanService;
    
    @Autowired
    private BidangService bidangService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DosenService dosenService;


    @GetMapping("/{bidang}") // redirect otomatis ke halaman current semester
    private String viewDaftarItemDefault(@PathVariable(value="bidang") String namaBidang, Model model){

        Long currentSemesterId = semesterService.getCurrentSemester().getIdSemester();

        return "redirect:/item/" + namaBidang + "/semester/" + currentSemesterId;
    }

    @GetMapping("/{bidang}/semester/{idSemester}") // halaman dengan id semester
    private String viewDaftarItem(@PathVariable(value="bidang") String namaBidang, @PathVariable(value="idSemester") Long idSemester, Authentication auth, Model model){
        if(auth != null){
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Pengguna user = penggunaService.findByEmail(email);
            Dosen dosen = user.getDosen();

            List<Semester> semesterList = semesterService.getSemesterList(); 
            Semester selectedSemester = semesterService.getSemesterById(idSemester);
            Bidang selectedBidang = bidangService.getBidangByNama(namaBidang);

            List<ItemBKD> listItem = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, selectedSemester, selectedBidang);

            boolean bukaInput = (semesterService.isPeriodeInput(selectedSemester) || semesterService.isPeriodePerbaikan(selectedSemester));
            
            List<Asesor> listAsesor = new ArrayList<Asesor>();
            listAsesor.addAll(dosen.getListAsesor());

            model.addAttribute("bidang", selectedBidang.getNamaBidang());
            model.addAttribute("listAsesor", listAsesor);
            model.addAttribute("bukaInput", bukaInput);
            model.addAttribute("semesterList", semesterList);
            model.addAttribute("currentSemester", selectedSemester);
            model.addAttribute("listItem", listItem);

        }
        return "viewall-itembkd";
    }

    @PostMapping("/{bidang}/semester/{idSemester}") // lihat semester pilihan
    private String viewDaftarItemSemester(@PathVariable(value="bidang") String namaBidang, @RequestParam("semesterId") Long idSemester, Model model){

        return "redirect:/item/" + namaBidang + "/semester/" + idSemester;
    }

    @GetMapping("/{bidang}/add") 
    private String addItemForm(@PathVariable(value="bidang") String namaBidang, Model model){
        Bidang bidang = bidangService.getBidangByNama(namaBidang);

        List<KegiatanBKD> listKegiatan = kegiatanService.getListKegiatanByBidang(bidang.getIdBidang());
        ItemBKD newItem = new ItemBKD();

        model.addAttribute("namaBidang", namaBidang);
        model.addAttribute("item", newItem);
        model.addAttribute("listKegiatan", listKegiatan);

        return "form-add-item";
    }

    @PostMapping("/{bidang}/add")
    private String addItem(@PathVariable(value="bidang") String namaBidang, 
                            @RequestParam(value = "filePenugasan") MultipartFile[] filePenugasan,
                            @RequestParam(value = "fileKinerja") MultipartFile[] fileKinerja,
                            @ModelAttribute ItemBKD newItem, 
                            RedirectAttributes redirAttr,
                            Authentication auth, 
                            Model model) throws IOException {

        Semester selectedSemester = semesterService.getCurrentSemester();

        if(auth != null && (semesterService.isPeriodeInput(selectedSemester) || semesterService.isPeriodePerbaikan(selectedSemester))){
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Pengguna user = penggunaService.findByEmail(email);
            Dosen dosen = user.getDosen();
            Bidang bidang = bidangService.getBidangByNama(namaBidang);

            List<File> newFilePenugasanList = new ArrayList<File>();
            Arrays.asList(filePenugasan).stream().forEach(file -> {
                try {
                    File newFile = fileService.storeFileWithLink(file);
                    if (newFile != null) {
                        newFilePenugasanList.add(newFile);
                    }
                } catch (IOException e) {}
            });
            newItem.setListFileBuktiPenugasan(newFilePenugasanList);

            List<File> newFileKinerjaList = new ArrayList<File>();
            Arrays.asList(fileKinerja).stream().forEach(file -> {
                try {
                    File newFile = fileService.storeFileWithLink(file);
                    if (newFile != null) {
                        newFileKinerjaList.add(newFile);
                    }
                } catch (IOException e) {}
            });
            newItem.setListFileBukti(newFileKinerjaList);

            newItem.setBidang(bidang);
            newItem.setDosen(dosen);
            newItem.setSemester(semesterService.getCurrentSemester());

            itemBKDService.addItemBKD(newItem);
            dosenService.updateStatusDosen(dosen);

            redirAttr.addFlashAttribute("success", "Item berhasil ditambahkan.");

        } else {
            redirAttr.addFlashAttribute("error", "Item gagal ditambahkan karena bukan masa input atau perbaikan. Periksa kembali jadwal BKD.");
        }
        return "redirect:/item/" + namaBidang + "/semester/" + selectedSemester.getIdSemester();
    }

    @PostMapping("/{idItem}/{jenisFile}") 
    private String addFileBukti(@PathVariable(value="idItem") Long idItem,
                                @PathVariable(value="jenisFile") String jenisFile,
                                @RequestParam(value="file") MultipartFile file,
                                Model model, RedirectAttributes redirAttr) throws IOException {
                                    
        ItemBKD item = itemBKDService.getItemById(idItem);
        Semester selectedSemester = item.getSemester();

        boolean asesor2ExistAndRekomendasiSelesai = false;
        if(item.getDosen().getListAsesor().size() == 2) {
            if ("Selesai".equals(item.getRekomendasiAsesor2())) {
                asesor2ExistAndRekomendasiSelesai = true;
            }
        } else asesor2ExistAndRekomendasiSelesai = true;

        if ("Selesai".equals(item.getRekomendasiAsesor1()) && asesor2ExistAndRekomendasiSelesai) {
            redirAttr.addFlashAttribute("error", "Dokumen gagal ditambahkan karena rekomendasi asesor sudah 'Selesai'");

        } else if (semesterService.isPeriodeInput(selectedSemester) || semesterService.isPeriodePerbaikan(selectedSemester)) {
            File fileUploaded = fileService.storeFileWithLink(file);
            // kategori 1 penugasan, kategori 2 kinerja
            if (jenisFile.equals("addFilePenugasan")) {
                itemBKDService.addFileToItem(idItem, 1, fileUploaded);
            } else {
                itemBKDService.addFileToItem(idItem, 2, fileUploaded);
            }
            // Jika item diedit pada periode perbaikan, rekomendasinya akan berubah jadi menunggu penilaian ulang
            if (semesterService.isPeriodePerbaikan(selectedSemester)) {
                if (item.getRekomendasiAsesor1() != null) item.setRekomendasiAsesor1("Menunggu Penilaian Ulang");
                if (item.getRekomendasiAsesor2() != null) item.setRekomendasiAsesor2("Menunggu Penilaian Ulang");
                itemBKDService.updateItemBKD(item);
                Dosen dosen = item.getDosen();
                dosenService.updateStatusDosen(dosen);
            }
            redirAttr.addFlashAttribute("success", "Dokumen berhasil ditambahkan.");

        } else {
            redirAttr.addFlashAttribute("error", "Dokumen gagal ditambahkan karena bukan masa input atau perbaikan. Periksa kembali jadwal BKD.");
        }
        

        return "redirect:/item/" + item.getBidang().getNamaBidang().toLowerCase() + "/semester/" + item.getSemester().getIdSemester();
    }

    @GetMapping("/{idItem}/{jenisFile}/delete/{idFile}") 
    private String deleteFileBukti(@PathVariable(value="idItem") Long idItem,
                                    @PathVariable(value="jenisFile") String jenisFile,
                                    @PathVariable(value="idFile") String idFile,
                                    Model model, RedirectAttributes redirAttr) throws IOException {

        ItemBKD item = itemBKDService.getItemById(idItem);
        Semester selectedSemester = item.getSemester();

        boolean asesor2ExistAndRekomendasiSelesai = false;
        if(item.getDosen().getListAsesor().size() == 2) {
            if ("Selesai".equals(item.getRekomendasiAsesor2())) {
                asesor2ExistAndRekomendasiSelesai = true;
            }
        } else asesor2ExistAndRekomendasiSelesai = true;

        if ("Selesai".equals(item.getRekomendasiAsesor1()) && asesor2ExistAndRekomendasiSelesai) {
            redirAttr.addFlashAttribute("error", "Dokumen gagal dihapus karena rekomendasi asesor sudah 'Selesai'");

        } else if (semesterService.isPeriodeInput(selectedSemester) || semesterService.isPeriodePerbaikan(selectedSemester)) {
            File fileDeleted = fileService.getFile(idFile);

            if (jenisFile.equals("penugasan")) {
                itemBKDService.deleteFileFromItem(idItem, 1, fileDeleted);
            } else {
                itemBKDService.deleteFileFromItem(idItem, 2, fileDeleted);
            }
            fileService.deleteFile(fileDeleted);

            // Jika item diedit pada periode perbaikan, rekomendasinya akan berubah jadi menunggu penilaian ulang
            if (semesterService.isPeriodePerbaikan(selectedSemester)) {
                if (item.getRekomendasiAsesor1() != null) item.setRekomendasiAsesor1("Menunggu Penilaian Ulang");
                if (item.getRekomendasiAsesor2() != null) item.setRekomendasiAsesor2("Menunggu Penilaian Ulang");
                itemBKDService.updateItemBKD(item);
                Dosen dosen = item.getDosen();
                dosenService.updateStatusDosen(dosen);
            }
            redirAttr.addFlashAttribute("success", "Dokumen berhasil dihapus.");

        } else {
            redirAttr.addFlashAttribute("error", "Dokumen gagal dihapus karena bukan masa input atau perbaikan. Periksa kembali jadwal BKD.");
        }
        

        return "redirect:/item/" + item.getBidang().getNamaBidang().toLowerCase() + "/semester/" + item.getSemester().getIdSemester();
    }
    
    @GetMapping("/{bidang}/{idItem}/edit") 
    private String editItemForm(@PathVariable(value="bidang") String namaBidang, @PathVariable(value="idItem") Long idItem, Model model){
        Bidang bidang = bidangService.getBidangByNama(namaBidang);

        List<KegiatanBKD> listKegiatan = kegiatanService.getListKegiatanByBidang(bidang.getIdBidang());
        ItemBKD selectedItem = itemBKDService.getItemById(idItem);

        model.addAttribute("namaBidang", namaBidang);
        model.addAttribute("item", selectedItem);
        model.addAttribute("listKegiatan", listKegiatan);

        return "form-edit-item";
    }

    @PostMapping("/{bidang}/{idItem}/edit") 
    private String editItemSubmit(@ModelAttribute ItemBKD item,
                                @PathVariable(value="bidang") String namaBidang, 
                                RedirectAttributes redirAttr, Model model){

        
        ItemBKD itemEdited = itemBKDService.getItemById(item.getIdItem());
        Semester selectedSemester = itemEdited.getSemester();
        boolean bukaInput = (semesterService.isPeriodeInput(selectedSemester) || semesterService.isPeriodePerbaikan(selectedSemester));

        boolean asesor2ExistAndRekomendasiSelesai = false;
        if(itemEdited.getDosen().getListAsesor().size() == 2) {
            if ("Selesai".equals(itemEdited.getRekomendasiAsesor2())) {
                asesor2ExistAndRekomendasiSelesai = true;
            }
        } else asesor2ExistAndRekomendasiSelesai = true;

        if ("Selesai".equals(itemEdited.getRekomendasiAsesor1()) && asesor2ExistAndRekomendasiSelesai) {
            redirAttr.addFlashAttribute("error", "Item gagal disimpan karena rekomendasi asesor sudah 'Selesai'");

        } else if (bukaInput){
            itemEdited.setKegiatanBKD(item.getKegiatanBKD());
            itemEdited.setDeskripsi(item.getDeskripsi());
            itemEdited.setNamaBuktiPenugasan(item.getNamaBuktiPenugasan());
            itemEdited.setSks(item.getSks());
            itemEdited.setMasaPenugasan(item.getMasaPenugasan());
            itemEdited.setNamaBukti(item.getNamaBukti());
            itemEdited.setSksTerpenuhi(item.getSksTerpenuhi());
            // Jika item diedit pada periode perbaikan, rekomendasinya akan berubah jadi menunggu penilaian ulang
            if (semesterService.isPeriodePerbaikan(selectedSemester)) {
                if (itemEdited.getRekomendasiAsesor1() != null) itemEdited.setRekomendasiAsesor1("Menunggu Penilaian Ulang");
                if (itemEdited.getRekomendasiAsesor2() != null) itemEdited.setRekomendasiAsesor2("Menunggu Penilaian Ulang");
                Dosen dosen = itemEdited.getDosen();
                dosenService.updateStatusDosen(dosen);
            }

            itemBKDService.updateItemBKD(itemEdited);

            redirAttr.addFlashAttribute("success", "Item BKD berhasil diubah.");

        } else {
            redirAttr.addFlashAttribute("error", "Item BKD gagal diubah karena bukan periode input atau perbaikan. Periksa kembali jadwal BKD.");
        }
        return "redirect:/item/" + namaBidang + "/semester/"+ selectedSemester.getIdSemester();
    }

    @GetMapping("/{bidang}/{idItem}/delete")
    private String deleteItem(@PathVariable(value="bidang") String namaBidang, @PathVariable(value="idItem") Long idItem, RedirectAttributes redirAttr){
        ItemBKD item = itemBKDService.getItemById(idItem);
        Semester selectedSemester = item.getSemester();
        boolean bukaInput = (semesterService.isPeriodeInput(selectedSemester) || semesterService.isPeriodePerbaikan(selectedSemester));

        boolean asesor2ExistAndRekomendasiSelesai = false;
        if(item.getDosen().getListAsesor().size() == 2) {
            if ("Selesai".equals(item.getRekomendasiAsesor2())) {
                asesor2ExistAndRekomendasiSelesai = true;
            }
        } else asesor2ExistAndRekomendasiSelesai = true;

        if ("Selesai".equals(item.getRekomendasiAsesor1()) && asesor2ExistAndRekomendasiSelesai) {
            redirAttr.addFlashAttribute("error", "Item gagal dihapus karena rekomendasi asesor sudah 'Selesai'");

        } else if (bukaInput){
            for (File f : item.getListFileBukti()){
                fileService.deleteFile(f);
            }
    
            for (File f : item.getListFileBuktiPenugasan()){
                fileService.deleteFile(f);
            }
            itemBKDService.deleteItemBKD(item);

            redirAttr.addFlashAttribute("success", "Item BKD berhasil dihapus.");
        }

        else{
            redirAttr.addFlashAttribute("error", "Item BKD gagal dihapus karena bukan periode input atau perbaikan. Periksa kembali jadwal BKD.");
        }
        return "redirect:/item/" + namaBidang + "/semester/"+ selectedSemester.getIdSemester();
    }
    
}
