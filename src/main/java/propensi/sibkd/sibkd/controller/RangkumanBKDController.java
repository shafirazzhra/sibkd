package propensi.sibkd.sibkd.controller;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.service.*;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;


@Controller
public class RangkumanBKDController {
    @Qualifier("penggunaServiceImpl")
    @Autowired
    private PenggunaService penggunaService;

    @Qualifier("itemBKDServiceImpl")
    @Autowired
    private ItemBKDService itemBKDService;

    @Autowired
    private SemesterService semesterService;

    @Autowired
    private BidangService bidangService;

    @GetMapping("/rangkuman-bkd")
    public String viewRangkuman(Authentication auth, @RequestParam(value = "semesterId", required = false) Long idSemester, Model model){
        if(auth != null){
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Pengguna user = penggunaService.findByEmail(email);
            Dosen dosen = user.getDosen();
            Semester currentSemester = semesterService.getCurrentSemester();
            List<Semester> semesterList = semesterService.getSemesterList(); 
            Semester selectedSemester = semesterService.getSemesterById(idSemester);

            //get list item bkd per bidang
            List<ItemBKD> listItemBKDPendidikan = Collections.emptyList();
            List<ItemBKD> listItemBKDPenelitian = Collections.emptyList();
            List<ItemBKD> listItemBKDPengabdian = Collections.emptyList();
            List<ItemBKD> listItemBKDPenunjang = Collections.emptyList();

            if(idSemester == null || idSemester == currentSemester.getIdSemester()){
                listItemBKDPendidikan = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, currentSemester, bidangService.getBidangByNama("Pendidikan"));
                listItemBKDPenelitian = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, currentSemester, bidangService.getBidangByNama("Penelitian"));
                listItemBKDPengabdian = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, currentSemester, bidangService.getBidangByNama("Pengabdian"));
                listItemBKDPenunjang = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, currentSemester, bidangService.getBidangByNama("Penunjang"));
            }
            else{
                listItemBKDPendidikan = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, selectedSemester, bidangService.getBidangByNama("Pendidikan"));
                listItemBKDPenelitian = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, selectedSemester, bidangService.getBidangByNama("Penelitian"));
                listItemBKDPengabdian = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, selectedSemester, bidangService.getBidangByNama("Pengabdian"));
                listItemBKDPenunjang = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, selectedSemester, bidangService.getBidangByNama("Penunjang"));
            }

            if (dosen.getJabstruk() != null){
                String [][] listSksKinerja = new String[7][2];
                String [][] listPenilaianAsesor1 = new String[7][2];
                String [][] listPenilaianAsesor2= new String[7][2];
                String[] listStatusAkhir = new String[7];
                int sksPendidikan = 0;
                int sksPenelitian = 0;
                int sksPengabdian = 0;
                int sksPenunjang = 0;
                int sksPendidikanAsesor1 = 0;
                int sksPenelitianAsesor1 = 0;
                int sksPengabdianAsesor1 = 0;
                int sksPenunjangAsesor1 = 0;
                int sksPendidikanAsesor2 = 0;
                int sksPenelitianAsesor2 = 0;
                int sksPengabdianAsesor2 = 0;
                int sksPenunjangAsesor2 = 0;

                //get total semester kinerja per bidang
                if(listItemBKDPendidikan.size() != 0 || listItemBKDPenelitian.size() != 0 
                    || listItemBKDPengabdian.size() != 0 || listItemBKDPenunjang.size() != 0){
                        for(ItemBKD item : listItemBKDPendidikan){ 
                            sksPendidikan += item.getSksTerpenuhi();
                            listSksKinerja[0][0] = Integer.toString(sksPendidikan);
                            if(item.getRekomendasiAsesor1() != null && dosen.getListAsesor().size() == 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPendidikanAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[0][0] = Integer.toString(sksPendidikanAsesor1);
                                }
                            }
                            else if (item.getRekomendasiAsesor1() != null && item.getRekomendasiAsesor2() != null && dosen.getListAsesor().size() > 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPendidikanAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[0][0] = Integer.toString(sksPendidikanAsesor1);
                                }
                                if(item.getRekomendasiAsesor2().equals("Selesai")){
                                    sksPendidikanAsesor2 += item.getSksTerpenuhi();
                                    listPenilaianAsesor2[0][0] = Integer.toString(sksPendidikanAsesor2);
                                }
                            }
                        }
            
                        for(ItemBKD item : listItemBKDPenelitian){
                            sksPenelitian += item.getSksTerpenuhi();
                            listSksKinerja[1][0] = Integer.toString(sksPenelitian);
                            if(item.getRekomendasiAsesor1() != null && dosen.getListAsesor().size() == 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPenelitianAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[1][0] = Integer.toString(sksPenelitianAsesor1);
                                }
                            }
                            else if (item.getRekomendasiAsesor1() != null && item.getRekomendasiAsesor2() != null && dosen.getListAsesor().size() > 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPenelitianAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[1][0] = Integer.toString(sksPenelitianAsesor1);
                                }
                                if(item.getRekomendasiAsesor2().equals("Selesai")){
                                    sksPenelitianAsesor2 += item.getSksTerpenuhi();
                                    listPenilaianAsesor2[1][0] = Integer.toString(sksPenelitianAsesor2);
                                }
                            }
                        }
            
                        for(ItemBKD item : listItemBKDPengabdian){
                            sksPengabdian += item.getSksTerpenuhi();
                            listSksKinerja[2][0] = Integer.toString(sksPengabdian);
                            if(item.getRekomendasiAsesor1() != null && dosen.getListAsesor().size() == 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPengabdianAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[2][0] = Integer.toString(sksPengabdianAsesor1);
                                }
                            }
                            else if (item.getRekomendasiAsesor1() != null && item.getRekomendasiAsesor2() != null &&dosen.getListAsesor().size() > 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPengabdianAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[2][0] = Integer.toString(sksPengabdianAsesor1);
                                }
                                if(item.getRekomendasiAsesor2().equals("Selesai")){
                                    sksPengabdianAsesor2 += item.getSksTerpenuhi();
                                    listPenilaianAsesor2[2][0] = Integer.toString(sksPengabdianAsesor2);
                                }
                            }
                        }
            
                        for(ItemBKD item : listItemBKDPenunjang){
                            sksPenunjang += item.getSksTerpenuhi();
                            listSksKinerja[3][0] = Integer.toString(sksPenunjang);
                            if(item.getRekomendasiAsesor1() != null && dosen.getListAsesor().size() == 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPenunjangAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[3][0] = Integer.toString(sksPenunjangAsesor1);
                                }
                            }
                            else if (item.getRekomendasiAsesor1() != null && item.getRekomendasiAsesor2() != null && dosen.getListAsesor().size() > 1){
                                if(item.getRekomendasiAsesor1().equals("Selesai")){
                                    sksPenunjangAsesor1 += item.getSksTerpenuhi();
                                    listPenilaianAsesor1[3][0] = Integer.toString(sksPenunjangAsesor1);
                                }
                                if(item.getRekomendasiAsesor2().equals("Selesai")){
                                    sksPenunjangAsesor2 += item.getSksTerpenuhi();
                                    listPenilaianAsesor2[3][0] = Integer.toString(sksPenunjangAsesor2);
                                }
                            }
                        }
                }

                // Cek kondisi memenuhi/tidak sesuai ketentuan dinas perguruan tinggi

                // Pendidikan
                // dosen
                listSksKinerja[0][1] = (sksPendidikan > 0 ) ? "M" : "T";

                // asesor 1
                listPenilaianAsesor1[0][1] = (sksPendidikanAsesor1 > 0 ) ? "M" : "T";

                // asesor 2
    
                if(dosen.getListAsesor().size() > 1){
                    listPenilaianAsesor2[0][1] = (sksPendidikanAsesor2 > 0 ) ? "M" : "T";
                    
                }
                else{
                    listPenilaianAsesor2[0][0] = Integer.toString(0);
                    listPenilaianAsesor2[0][1] = "-";
                }

                // Status akhir
                listStatusAkhir[0] = ((listSksKinerja[0][1].equals("M")) && listPenilaianAsesor1[0][1].equals("M")
                && (listPenilaianAsesor2[0][1].equals("M") || listPenilaianAsesor2[0][1].equals("-"))) ? "M" : "T";

                // <------------------------------------------------------------------>

                // Penelitian
                // dosen
                listSksKinerja[1][1] = "M";

                // asesor 1
                listPenilaianAsesor1[1][1] = "M";

                // asesor 2
                if(dosen.getListAsesor().size() > 1){
                    listPenilaianAsesor1[1][1] = "M";
                }
                else{
                    listPenilaianAsesor2[1][0] = Integer.toString(0);
                    listPenilaianAsesor2[1][1] = "-";
                }

                // Status akhir
                listStatusAkhir[1] = "M";

                // <------------------------------------------------------------------>

                // Pengabdian
                // dosen
                listSksKinerja[2][1] = "M";

                // asesor 1
                listPenilaianAsesor1[2][1] = "M";

                // asesor 2
                if(dosen.getListAsesor().size() > 1){
                    listPenilaianAsesor1[2][1] = "M";
                }
                else{
                    listPenilaianAsesor2[2][0] = Integer.toString(0);
                    listPenilaianAsesor2[2][1] = "-";
                }

                // Status akhir
                listStatusAkhir[2] = "M";

                // <------------------------------------------------------------------>

                // Penunjang
                // dosen
                listSksKinerja[3][1] = "M";

                // asesor 1
                listPenilaianAsesor1[3][1] = "M";

                // asesor 2
                if(dosen.getListAsesor().size() > 1){
                    listPenilaianAsesor1[3][1] = "M";
                }
                else{
                    listPenilaianAsesor2[3][0] = Integer.toString(0);
                    listPenilaianAsesor2[3][1] = "-";
                }

                // Status akhir
                listStatusAkhir[3] = "M";

                // <------------------------------------------------------------------>

                // Pendidikan + Penelitian
                listSksKinerja[4][0] = Integer.toString((sksPendidikan + sksPenelitian));
                listPenilaianAsesor1[4][0] = Integer.toString((sksPendidikanAsesor1 + sksPenelitianAsesor1));
                listPenilaianAsesor2[4][0] = Integer.toString((sksPendidikanAsesor2 + sksPenelitianAsesor2));
                
                // dosen
                listSksKinerja[4][1] = ((sksPendidikan + sksPenelitian) >= dosen.getJabstruk().getMinPdPl()) ? "M" : "T";
                
                //asesor 1
                listPenilaianAsesor1[4][1] = ((sksPendidikanAsesor1 + sksPenelitianAsesor1) >= dosen.getJabstruk().getMinPdPl()) ? "M" : "T";

                //asesor 2
                if(dosen.getListAsesor().size() > 1){
                    listPenilaianAsesor2[4][1] = ((sksPendidikanAsesor2 + sksPenelitianAsesor2) >= dosen.getJabstruk().getMinPdPl()) ? "M" : "T";
                }
                else{
                    listPenilaianAsesor2[4][0] = Integer.toString(0);
                    listPenilaianAsesor2[4][1] = "-";
                }

                // status akhir
                listStatusAkhir[4] = ((listSksKinerja[4][1].equals("M")) && listPenilaianAsesor1[4][1].equals("M")
                && ("M".equals(listPenilaianAsesor2[4][1]) || "-".equals(listPenilaianAsesor2[4][1]))) ? "M" : "T";

                // <------------------------------------------------------------------>

                // Pengabdian + Penunjang
                listSksKinerja[5][0] = Integer.toString((sksPengabdian + sksPenunjang));
                listPenilaianAsesor1[5][0] = Integer.toString((sksPengabdianAsesor1 + sksPenunjangAsesor1));
                listPenilaianAsesor2[5][0] = Integer.toString((sksPengabdianAsesor2 + sksPenunjangAsesor2));

                // dosen
                listSksKinerja[5][1] = ((sksPengabdian + sksPenunjang) >= dosen.getJabstruk().getMinPgPk()) ? "M" : "T";

                //asesor 1
                listPenilaianAsesor1[5][1] = ((sksPengabdianAsesor1 + sksPenunjangAsesor1) >= dosen.getJabstruk().getMinPgPk()) ? "M" : "T";

                //asesor 2
                if(dosen.getListAsesor().size() > 1){
                    listPenilaianAsesor2[5][1] = ((sksPengabdianAsesor2 + sksPenunjangAsesor2) >= dosen.getJabstruk().getMinPgPk()) ? "M" : "T";
                }
                else{
                    listPenilaianAsesor2[5][0] = Integer.toString(0);
                    listPenilaianAsesor2[5][1] = "-";
                }

                // status akhir
                listStatusAkhir[5] = ((listSksKinerja[5][1].equals("M")) && listPenilaianAsesor1[5][1].equals("M")
                && (listPenilaianAsesor2[5][1].equals("M") || listPenilaianAsesor2[5][1].equals("-"))) ? "M" : "T";

                // <------------------------------------------------------------------>

                // Total kinerja
                listSksKinerja[6][0] = Integer.toString((sksPendidikan + sksPenelitian + sksPengabdian + sksPenunjang));
                listPenilaianAsesor1[6][0] = Integer.toString((sksPendidikanAsesor1 + sksPenelitianAsesor1 + sksPengabdianAsesor1 + sksPenunjangAsesor1));
                listPenilaianAsesor2[6][0] = Integer.toString((sksPendidikanAsesor2 + sksPenelitianAsesor2 + sksPengabdianAsesor2 + sksPenunjangAsesor2));

                // dosen
                listSksKinerja[6][1] = ((sksPendidikan + sksPenelitian + sksPengabdian + sksPenunjang) <= dosen.getJabstruk().getMaxTotal()) ? "M" : "T";

                // asesor 1
                listPenilaianAsesor1[6][1] = ((sksPendidikanAsesor1 + sksPenelitianAsesor1 + sksPengabdianAsesor1 + sksPenunjangAsesor1) <= dosen.getJabstruk().getMaxTotal()) ? "M" : "T";

                // asesor 2
                if(dosen.getListAsesor().size() > 1){
                    listPenilaianAsesor2[6][1] = ((sksPendidikanAsesor2 + sksPenelitianAsesor2 + sksPengabdianAsesor2 + sksPenunjangAsesor2) <= dosen.getJabstruk().getMaxTotal()) ? "M" : "T";
                }
                else{
                    listPenilaianAsesor2[6][0] = Integer.toString(0);
                    listPenilaianAsesor2[6][1] = "-";
                }

                // status akhir
                listStatusAkhir[6] = ((listSksKinerja[6][1].equals("M")) && listPenilaianAsesor1[6][1].equals("M")
                && (listPenilaianAsesor2[6][1].equals("M") || listPenilaianAsesor2[6][1].equals("-"))) ? "M" : "T";

                JabatanStruktural jabstruk = dosen.getJabstruk();

                model.addAttribute("selectedSemester", selectedSemester);
                model.addAttribute("semesterList", semesterList);
                model.addAttribute("currentSemester", currentSemester);
                model.addAttribute("semesterId", idSemester);
                model.addAttribute("user", user);
                model.addAttribute("dosen", dosen);
                model.addAttribute("jabstruk", jabstruk);
                model.addAttribute("listSksKinerja", listSksKinerja);
                model.addAttribute("listPenilaianAsesor1", listPenilaianAsesor1);
                model.addAttribute("listPenilaianAsesor2", listPenilaianAsesor2);
                model.addAttribute("listStatusAkhir", listStatusAkhir);
                return "rangkuman-bkd";
            }
            if (dosen.getJabstruk() == null) {
                model.addAttribute("dosen", dosen);
                model.addAttribute("currentSemester", currentSemester);
                return "rangkuman-bkd-fail";
            }
            }
            return "403";    
    }
}
