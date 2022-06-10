package propensi.sibkd.sibkd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.pdfbox.multipdf.PDFMergerUtility; 
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.service.*;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
@RequestMapping("/export-laporan")
public class ExportLaporanController {

    @Qualifier("itemBKDServiceImpl")
    @Autowired
    private ItemBKDService itemBKDService;

    @Autowired
    private DosenService dosenService;

    @Autowired
    private SemesterService semesterService;
    
    @Autowired
    private BidangService bidangService;

    @Autowired
    private ExportPdfService exportPdfService;

// modified from : https://stackoverflow.com/questions/13808090/create-pdf-and-merge-with-pdfbox
    @GetMapping("/{idDosen}")
    private Object exportMerged(@PathVariable(value="idDosen") Long idDosen, RedirectAttributes redirAttr) throws IOException {

        // Load file dari database
        Dosen chosenDosen = dosenService.getDosenByIdDosen(idDosen);
        Semester currentSemester = semesterService.getCurrentSemester();

        // constraint : dosen harus punya jabatan struktural
        if(chosenDosen.getJabstruk() == null) {
            redirAttr.addFlashAttribute("error", "Laporan tidak dapat dibuat karena dosen dengan nama " + chosenDosen.getNamaDosen() + " belum memiliki jabatan struktural.");
            return "redirect:/pelaporan-bkd/view-all";
        }

        // constraint : status pelapran dosen harus sudah selesai
        if(!"Sudah Selesai".equals(chosenDosen.getStatus())){
            redirAttr.addFlashAttribute("error", "Laporan tidak dapat dibuat karena status pelaporan dosen dengan nama " + chosenDosen.getNamaDosen() + " belum selesai.");
            return "redirect:/pelaporan-bkd/view-all";
        }

        // membuat utility dan PDDocument
        PDDocument documentToExport = new PDDocument();
        PDFMergerUtility mergePdf = new PDFMergerUtility();

        // tabel rangkuman
        ByteArrayInputStream rangkumanData = exportPdfService.exportLaporanPdf("laporan-bkd-rangkuman", buatRangkuman(chosenDosen, currentSemester));
        PDDocument rangkumanPdf = PDDocument.load(rangkumanData);
        mergePdf.appendDocument(documentToExport, rangkumanPdf);
        rangkumanPdf.close();

        // Membuat laporan untuk setiap bidang
        for (Bidang bidang : bidangService.getListBidang()) {
            List<ItemBKD> listItemBKD = itemBKDService.getListByDosenAndSemesterAndBidang(chosenDosen, currentSemester, bidang);
            List<File> listFile = new ArrayList<File>();

            for (ItemBKD item : listItemBKD) {
                listFile.addAll(item.getListFileBuktiPenugasan());
                listFile.addAll(item.getListFileBukti());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("listItem", listItemBKD);
            data.put("semester", currentSemester);
            data.put("bidang", bidang);

            // tabel item
            ByteArrayInputStream tableData = exportPdfService.exportLaporanPdf("laporan-bkd", data);
            PDDocument tablePdf = PDDocument.load(tableData);
            mergePdf.appendDocument(documentToExport, tablePdf);
            tablePdf.close();

            // merge dokumen bukti
            if (listFile.size() > 0) {
                for (File file : listFile) {
                    if (file.getType().equals("image/png") || file.getType().equals("image/jpeg")) {
    
                        PDPage page = new PDPage(PDRectangle.A4);
                        documentToExport.addPage(page);
    
                        // resizing setup
                        byte[] image = file.getData();
                        ByteArrayInputStream bais = new ByteArrayInputStream(image);
                        BufferedImage bim = ImageIO.read(bais);
                        PDImageXObject imageObject = LosslessFactory.createFromImage(documentToExport, bim);
                        PDPageContentStream contentStream = new PDPageContentStream(documentToExport, page);
    
                        // scale image to A4 if width/height > A4 sized page
                        float scaleDownRatio = 1;
                        if (bim.getWidth() > 0.9*PDRectangle.A4.getWidth() || bim.getHeight() > 0.9*PDRectangle.A4.getHeight()) {
                            float widthRatio = (float) 0.9*PDRectangle.A4.getWidth() / bim.getWidth();
                            float heightRatio = (float) 0.9*PDRectangle.A4.getHeight() / bim.getHeight();
                            scaleDownRatio = (widthRatio<heightRatio)? widthRatio:heightRatio;
                        }
                        contentStream.drawImage(imageObject, 25, page.getMediaBox().getHeight() - bim.getHeight() * scaleDownRatio - 25, bim.getWidth() * scaleDownRatio, bim.getHeight() * scaleDownRatio);
                        // tulis nama file di kiri bawah halaman
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.setNonStrokingColor(26, 21, 0, 45);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(10, 10);
                        contentStream.showText(file.getName());
                        contentStream.endText();
    
                        contentStream.close();
    
                    } else if (file.getType().equals("application/pdf")){ // untuk type application/pdf

                        PDDocument loadedFile = PDDocument.load(file.getData());
    
                        // untuk tulis nama file
                        for (PDPage page : loadedFile.getPages()) {
                            PDPageContentStream contentStream = new PDPageContentStream(loadedFile, page,PDPageContentStream.AppendMode.APPEND, true, true);
                            contentStream.setFont(PDType1Font.HELVETICA, 10);
                            contentStream.setNonStrokingColor(26, 21, 0, 45);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(10, 10);
                            contentStream.showText(file.getName());
                            contentStream.endText();
                            contentStream.close();
                        }
    
                        mergePdf.appendDocument(documentToExport, loadedFile);
                        loadedFile.close();
                    }
                }
            }
        }

        // memberi nama file yang akan di-download
        ByteArrayOutputStream pdfDocOutputstream = new ByteArrayOutputStream();
        mergePdf.setDestinationFileName(chosenDosen.getNamaDosen() + "_" + currentSemester.getNamaSemester() +"_report.pdf");
        documentToExport.save(pdfDocOutputstream);
        documentToExport.close();

        // membuat response object
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        // set nama dokumen yang di download
        headers.setContentDispositionFormData(mergePdf.getDestinationFileName(), mergePdf.getDestinationFileName());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<>(pdfDocOutputstream.toByteArray(), headers, HttpStatus.OK);

        return response;
    }

    // modifikasi rangkumanBKDController untuk export rangkuman ke laporan
    public Map<String, Object> buatRangkuman(Dosen dosen, Semester semester){
        
            List<ItemBKD> listItemBKDPendidikan = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, semester, bidangService.getBidangByNama("Pendidikan"));
            List<ItemBKD> listItemBKDPenelitian = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, semester, bidangService.getBidangByNama("Penelitian"));
            List<ItemBKD> listItemBKDPengabdian = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, semester, bidangService.getBidangByNama("Pengabdian"));
            List<ItemBKD> listItemBKDPenunjang = itemBKDService.getListByDosenAndSemesterAndBidang(dosen, semester, bidangService.getBidangByNama("Penunjang"));

            
            String [][] listSksKinerja = new String[7][2];
            String [][] listPenilaianAsesor1 = new String[7][2];
            String [][] listPenilaianAsesor2 = new String[7][2];
            String[] listStatusAkhir = new String[7];
            int sksPendidikan, sksPenelitian, sksPengabdian, sksPenunjang, sksPendidikanAsesor1, sksPenelitianAsesor1, sksPengabdianAsesor1,
                sksPenunjangAsesor1, sksPendidikanAsesor2, sksPenelitianAsesor2, sksPengabdianAsesor2, sksPenunjangAsesor2;
            
            sksPendidikan = sksPenelitian = sksPengabdian = sksPenunjang = sksPendidikanAsesor1 = sksPenelitianAsesor1 = sksPengabdianAsesor1 =
            sksPenunjangAsesor1 = sksPendidikanAsesor2 = sksPenelitianAsesor2 = sksPengabdianAsesor2 = sksPenunjangAsesor2 = 0;

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
                listPenilaianAsesor1[0][1] = (sksPendidikanAsesor1 > 0 ) ? "M" : "T";
            }
            else{
                listPenilaianAsesor2[0][0] = Integer.toString(0);
                listPenilaianAsesor2[0][1] = "-";
            }

            // Status akhir
            listStatusAkhir[0] = ((listSksKinerja[0][1].equals("M")) && listPenilaianAsesor1[0][1].equals("M")
            && ("M".equals(listPenilaianAsesor2[0][1])|| "-".equals(listPenilaianAsesor2[0][1]))) ? "M" : "T";

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
            && (listPenilaianAsesor2[4][1].equals("M") || listPenilaianAsesor2[4][1].equals("-"))) ? "M" : "T";

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

            Map<String, Object> dataRangkuman = new HashMap<>();
            dataRangkuman.put("dosen", dosen);
            dataRangkuman.put("semester", semester);
            dataRangkuman.put("listSksKinerja", listSksKinerja);
            dataRangkuman.put("listPenilaianAsesor1", listPenilaianAsesor1);
            dataRangkuman.put("listPenilaianAsesor2", listPenilaianAsesor2);
            dataRangkuman.put("listStatusAkhir", listStatusAkhir);

            return dataRangkuman;
            
    }
}
