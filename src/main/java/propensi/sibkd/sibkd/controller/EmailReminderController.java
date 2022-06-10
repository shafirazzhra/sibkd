package propensi.sibkd.sibkd.controller;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.service.AsesorService;
import propensi.sibkd.sibkd.service.DosenService;
import propensi.sibkd.sibkd.service.EmailService;
import propensi.sibkd.sibkd.service.PenggunaService;
import propensi.sibkd.sibkd.service.SemesterService;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;
@Controller
public class EmailReminderController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private SemesterService semesterService;

    @Autowired
    private DosenService dosenService;

    @Autowired
    private AsesorService asesorService;

    @Autowired
    private PenggunaService penggunaService;

    @GetMapping("/reminder/all")
    private String remindAll(HttpServletRequest req,
    RedirectAttributes redir,
    Model model){
        try {
            Semester semester = semesterService.getCurrentSemester();
            if (semesterService.isPeriodeInput(semester) || semesterService.isPeriodePerbaikan(semester)){
                //send email ke all dosen
                
                String todo = "";
                if (semesterService.isPeriodeInput(semester)){
                    todo = "input";
                } else if (semesterService.isPeriodePerbaikan(semester)){
                    todo = "memperbaiki";
                }
                String url1 = ServletUriComponentsBuilder.fromCurrentContextPath().path("/").toUriString();
               
                for (Dosen dosen : dosenService.getListAllDosen()){
                    String emailMessage = "<p>Selamat pagi/siang/sore, "+dosen.getNamaDosen()+"!</p>"
                    + "<p>Mohon untuk segera " + todo + " item BKD"+ " </p>"
                    + "<p>Tekan link di bawah ini untuk membuka sistem SI-BKD.</p>"
                    + "<p><a href=\"" + url1 + "\">SI-BKD</a></p>"
                    + "<br>";

                    emailService.sendEmail(dosen.getEmailDosen(),emailMessage,"Reminder untuk "+ todo + " item BKD");


                }

                redir.addFlashAttribute("success", "Email berhasil terkirim ke semua dosen untuk "+ todo + " item BKD.");

            } 
            else if (semesterService.isPeriodePenilaian(semester) || semesterService.isPeriodePenilaianUlang(semester)){
                //send email ke all asesor
                String todo = "";
                if (semesterService.isPeriodePenilaian(semester)){
                    todo = "menilai";
                } else if (semesterService.isPeriodePenilaianUlang(semester)){
                    todo = "menilai ulang";
                }

                String url = ServletUriComponentsBuilder.fromCurrentContextPath().path("/").toUriString();
                for (Asesor dosen : asesorService.getListAllAsesor()){
                
                    String emailMessage = "<p>Selamat pagi/siang/sore, "+dosen.getNamaAsesor()+"!</p>"
                        + "<p>Mohon untuk segera " + todo + " item BKD dari dosen yang Anda nilai."+ " </p>"
                        + "<p>Tekan link di bawah ini untuk membuka sistem SI-BKD.</p>"
                        + "<p><a href=\"" + url + "\">SI-BKD</a></p>"
                        + "<br>";

                    emailService.sendEmail(dosen.getEmailAsesor(), emailMessage,"Reminder untuk "+ todo + " item BKD");
                
                }
                redir.addFlashAttribute("success", "Email berhasil terkirim kepada semua asesor untuk "+ todo + " item BKD.");
               
            }

            boolean jadwal = semesterService.isPeriodeInput(semester) || semesterService.isPeriodePenilaian(semester) || semesterService.isPeriodePenilaianUlang(semester) || semesterService.isPeriodePerbaikan(semester);
            if (!jadwal){
                redir.addFlashAttribute("error", "Email tidak berhasil terkirim karena tidak ada jadwal.");

            }
            
            
        } catch (Exception e) {
            redir.addFlashAttribute("error", "Email tidak berhasil terkirim.");

        }

        

        return "redirect:/pelaporan-bkd/view-all";
    }



    @GetMapping("/reminder/{email}")
    private String remindDosenAsesor(HttpServletRequest req,
    RedirectAttributes redir,
    Model model, @PathVariable(value="email") String email){
        try{
            Dosen dosen = dosenService.getDosenByEmailDosen(email);
            Asesor asesor1 = dosen.getListAsesor().get(0);
            Asesor asesor2 = null;
            
            if (dosen.getListAsesor().size() == 2){
                asesor2 = dosen.getListAsesor().get(1);
            } 
            
            Semester semester = semesterService.getCurrentSemester();
            Boolean remindDosen = dosen.getStatus().equalsIgnoreCase("Belum Selesai") || dosen.getStatus().equalsIgnoreCase("Belum Ada Item");
            Boolean tidakBisaRemindByStatus = dosen.getStatus().equalsIgnoreCase("Sudah selesai") || dosen.getStatus().equalsIgnoreCase("Belum Ada Asesor");
            Boolean remindAsesor1 = dosen.getStatus().equalsIgnoreCase("Selesai Asesor 2");
            Boolean remindAsesor2 = dosen.getStatus().equalsIgnoreCase("Selesai Asesor 1") && (asesor2 != null);
            Boolean bisaRemindDosen = semesterService.isPeriodeInput(semester) || semesterService.isPeriodePerbaikan(semester);
            Boolean bisaRemindAsesor = semesterService.isPeriodePenilaian(semester) || semesterService.isPeriodePenilaianUlang(semester);
            Boolean tidakBisaRemindByTanggal = !(bisaRemindDosen || bisaRemindAsesor);

           
            
            if (tidakBisaRemindByStatus || tidakBisaRemindByTanggal){
                redir.addFlashAttribute("error", "Reminder tidak dapat dikirimkan karena status dosen atau belum masuk periode reminder.");
            }
            else if (remindDosen && bisaRemindDosen){
                // kirim email ke dosen jika periode sedang input/perbaikan dan status dosen belum selesai/belum ada item
                String todo = "";
                if (semesterService.isPeriodeInput(semester)){
                    todo = "input";
                } else if (semesterService.isPeriodePerbaikan(semester)){
                    todo = "memperbaiki";
                }
                String url1 = ServletUriComponentsBuilder.fromCurrentContextPath().path("/").toUriString();
               
                String emailMessage = "<p>Selamat pagi/siang/sore, "+dosen.getNamaDosen()+"!</p>"
                    + "<p>Mohon untuk segera " + todo + " item BKD"+ " </p>"
                    + "<p>Tekan link di bawah ini untuk membuka sistem SI-BKD.</p>"
                    + "<p><a href=\"" + url1 + "\">SI-BKD</a></p>"
                    + "<br>";

                emailService.sendEmail(dosen.getEmailDosen(),emailMessage,"Reminder untuk "+ todo + " item BKD");
                
                redir.addFlashAttribute("success", "Email berhasil terkirim kepada dosen dengan nama "+ dosen.getNamaDosen() +".");
            
            }
            else if (bisaRemindAsesor){
                // kirim email ke asesor jika periode sedang penilaian/penilaian ulang dan status dosen belum selesai/selesai asesor1/selesai asesor 2
               
                String todo = "";
                if (semesterService.isPeriodePenilaian(semester)){
                    todo = "menilai";
                } else if (semesterService.isPeriodePenilaianUlang(semester)){
                    todo = "menilai ulang";
                }
                String url = ServletUriComponentsBuilder.fromCurrentContextPath().path("/").toUriString();
                if (remindAsesor1){
                    String emailMessage = "<p>Selamat pagi/siang/sore, "+asesor1.getNamaAsesor()+"!</p>"
                            + "<p>Mohon untuk segera " + todo + " item BKD dari dosen bernama "+ dosen.getNamaDosen() +" ."+ " </p>"
                            + "<p>Tekan link di bawah ini untuk membuka sistem SI-BKD.</p>"
                            + "<p><a href=\"" + url + "\">SI-BKD</a></p>"
                            + "<br>";
    
                    emailService.sendEmail(asesor1.getEmailAsesor(), emailMessage,"Reminder untuk "+ todo + " item BKD");
                    redir.addFlashAttribute("success", "Email berhasil terkirim kepada asesor 1 dengan nama " + asesor1.getNamaAsesor() + " dari dosen bernama "+ dosen.getNamaDosen() +".");
            
                }
                else if (remindAsesor2){
                    String emailMessage = "<p>Selamat pagi/siang/sore, "+asesor2.getNamaAsesor()+"!</p>"
                    + "<p>Mohon untuk segera " + todo + " item BKD dari dosen bernama "+ dosen.getNamaDosen() +" ."+ " </p>"
                            + "<p>Tekan link di bawah ini untuk membuka sistem SI-BKD.</p>"
                            + "<p><a href=\"" + url + "\">SI-BKD</a></p>"
                            + "<br>";
    
                    emailService.sendEmail(asesor2.getEmailAsesor(), emailMessage,"Reminder untuk "+ todo + " item BKD");
                    redir.addFlashAttribute("success", "Email berhasil terkirim kepada asesor 2 dengan nama " + asesor2.getNamaAsesor() + " dari dosen bernama "+ dosen.getNamaDosen() +".");
                   
                }
                else if (dosen.getStatus().equalsIgnoreCase("belum selesai")){
                    for (Asesor asesor : dosen.getListAsesor()){
                        String emailMessage = "<p>Selamat pagi/siang/sore, "+ asesor.getNamaAsesor()+"!</p>"
                            + "<p>Mohon untuk segera " + todo + " item BKD dari dosen bernama "+ dosen.getNamaDosen() +" ."+ " </p>"
                            + "<p>Tekan link di bawah ini untuk membuka sistem SI-BKD.</p>"
                            + "<p><a href=\"" + url + "\">SI-BKD</a></p>"
                            + "<br>";
    
                        emailService.sendEmail(asesor.getEmailAsesor(), emailMessage,"Reminder untuk "+ todo + " item BKD");
                   
                    }

                    if (asesor2 != null){
                        redir.addFlashAttribute("success", "Email berhasil terkirim kepada asesor 1 dengan nama " + asesor1.getNamaAsesor() + " dan asesor 2 bernama " + asesor2.getNamaAsesor() + " dari dosen bernama "+ dosen.getNamaDosen() +".");
                    } else {
                        redir.addFlashAttribute("success", "Email berhasil terkirim kepada asesor 1 dengan nama " + asesor1.getNamaAsesor() + " dari dosen bernama "+ dosen.getNamaDosen() +".");
               
                    }
                    
                    
                }

            
                
                }
                else {
                    redir.addFlashAttribute("error", "Email tidak berhasil terkirim.");   
            
                }
               
            

              




        }
        catch (Exception e){
            redir.addFlashAttribute("error", "Email tidak berhasil terkirim.");   
             

        }
        return "redirect:/pelaporan-bkd/view-all";

    }
}


