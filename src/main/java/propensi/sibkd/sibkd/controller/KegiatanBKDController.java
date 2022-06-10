package propensi.sibkd.sibkd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.model.KegiatanBKD;
import propensi.sibkd.sibkd.service.BidangService;
import propensi.sibkd.sibkd.service.KegiatanBKDService;

import java.util.List;

@Controller
@RequestMapping("/kegiatan-bkd")
public class KegiatanBKDController {

    @Qualifier("bidangServiceImpl")
    @Autowired
    private BidangService bidangService;

    @Qualifier("kegiatanBKDServiceImpl")
    @Autowired
    private KegiatanBKDService kegiatanBKDService;

    @GetMapping("/{idBidang}")
    public String listKegiatan(
            @PathVariable("idBidang") Long idBidang,
            Model model)
    {
        List<KegiatanBKD> listKegiatan = kegiatanBKDService.getListKegiatanByBidang(idBidang);
        model.addAttribute("listKegiatan", listKegiatan);
        model.addAttribute("idBidang", idBidang);
        model.addAttribute("namaBidang", bidangService.getBidangById(idBidang).getNamaBidang());
        return "viewall-kegiatan-bybidang";
    }

    @GetMapping("/{idBidang}/add")
    public String addKegiatanForm(
            @PathVariable("idBidang") Long idBidang,
            Model model)
    {
        KegiatanBKD kegiatan = new KegiatanBKD();
        kegiatan.setBidang(bidangService.getBidangById(idBidang));
        model.addAttribute("kegiatan", kegiatan);
        model.addAttribute("idBidang", idBidang);
        model.addAttribute("namaBidang", bidangService.getBidangById(idBidang).getNamaBidang());
        return "form-add-kegiatan";
    }

    @PostMapping("/{idBidang}/add")
    public String addKegiatanSubmit(
            @PathVariable("idBidang") Long idBidang,
            @ModelAttribute KegiatanBKD kegiatan,
            RedirectAttributes redirAttr)
    {
        if(kegiatanBKDService.isNamaExist(kegiatan.getNamaKegiatan()) == false){
            kegiatanBKDService.addKegiatan(kegiatan);
            redirAttr.addFlashAttribute("success", "Kegiatan dengan nama " + kegiatan.getNamaKegiatan() + " berhasil ditambah.");
            return "redirect:/kegiatan-bkd/" + idBidang;
        } else {
            redirAttr.addFlashAttribute("error", "Kegiatan tidak dapat ditambah karena nama sudah ada. Silakan masukkan nama lain.");
            return "redirect:/kegiatan-bkd/" + idBidang + "/add";
        }
    }

    @GetMapping("/{idBidang}/edit/{idKegiatan}")
    public String editKegiatanForm(
            @PathVariable("idBidang") Long idBidang,
            @PathVariable("idKegiatan") Long idKegiatan,
            Model model)
    {
        KegiatanBKD kegiatan = kegiatanBKDService.getKegiatanById(idKegiatan);
        model.addAttribute("kegiatan", kegiatan);
        model.addAttribute("idBidang", idBidang);
        model.addAttribute("namaBidang", bidangService.getBidangById(idBidang).getNamaBidang());
        return "form-edit-kegiatan";
    }

    @PostMapping("/{idBidang}/edit")
    public String editKegiatanSubmit(
            @PathVariable("idBidang") Long idBidang,
            @ModelAttribute KegiatanBKD kegiatan,
            RedirectAttributes redirAttr)
    {
        if((kegiatanBKDService.isNamaExist(kegiatan.getNamaKegiatan()) == false) || (kegiatanBKDService.isTemplateExist(kegiatan.getTemplate()) == false)){
            kegiatanBKDService.updateKegiatan(kegiatan);
            redirAttr.addFlashAttribute("success", "Kegiatan dengan nama " + kegiatan.getNamaKegiatan() + " berhasil diubah.");
            return "redirect:/kegiatan-bkd/" + idBidang;
        } else {
            redirAttr.addFlashAttribute("error", "Kegiatan tidak dapat diubah karena nama sudah ada. Silakan masukkan nama lain.");
            return "redirect:/kegiatan-bkd/" + idBidang + "/edit/" + kegiatan.getIdKegiatan();
        }
    }

    @GetMapping("/{idBidang}/delete/{idKegiatan}")
    public String deleteKegiatan(
            @PathVariable("idBidang") Long idBidang,
            @PathVariable("idKegiatan") Long idKegiatan,
            RedirectAttributes redirAttr)
    {
        KegiatanBKD kegiatan = kegiatanBKDService.getKegiatanById(idKegiatan);
        if(kegiatanBKDService.isListItemEmpty(idKegiatan)){
            kegiatanBKDService.deleteKegiatan(kegiatan);
            redirAttr.addFlashAttribute("success", "Kegiatan dengan nama " + kegiatan.getNamaKegiatan() + " berhasil dihapus.");
        } else {
            redirAttr.addFlashAttribute("error", "Kegiatan tidak dapat dihapus karena telah memiliki daftar Item BKD.");
        }
        return "redirect:/kegiatan-bkd/" + idBidang;
    }
}
