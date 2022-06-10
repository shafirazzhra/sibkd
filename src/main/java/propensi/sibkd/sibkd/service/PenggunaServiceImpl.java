package propensi.sibkd.sibkd.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.repository.*;

@Service
@Transactional
public class PenggunaServiceImpl implements PenggunaService {
    @Autowired
    private UserDao repo;

    @Autowired
    private RoleService roleService;


    @Override
    public Pengguna findByEmail(String email){
        Optional<Pengguna> user = Optional.ofNullable(repo.findByEmailPengguna(email));
        if(user.isPresent()) return user.get();
        else return null;
    }

    @Override
    public void processOAuthPostLogin(String email, Authentication auth) {
        //modified from https://www.codejava.net/frameworks/spring-boot/oauth2-login-with-google-example
        Pengguna existUser = repo.findByEmailPengguna(email);

        if (existUser == null) {
            Pengguna newUser = new Pengguna();
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            String nama = oauthUser.getName();
            newUser.setEmailPengguna(email);
            newUser.setNamaPengguna(nama);
            Role role = roleService.findByNama("Not Assigned");
            newUser.setRole(role);

            repo.save(newUser);
        }

    }
    @Override
    public List<Pengguna> findAllPengguna(){
        return repo.findAll();
    }

    @Override
    public List<Pengguna> searchPengguna(String keyword, String namaRole){
        List<Pengguna> hasil = new ArrayList<Pengguna>();
        
        if (namaRole != null || namaRole != "All"){
            hasil = findAllPenggunaByRole(namaRole);
        }
        
        if (keyword != null && keyword != "" && keyword != " ") {
            keyword = keyword.toLowerCase();

            if (namaRole == null || namaRole.equals("All")){
                for (Pengguna p : findAllPengguna()){
                    if ((p.getNamaPengguna().toLowerCase().contains(keyword) || (p.getEmailPengguna().toLowerCase().contains(keyword)) || (p.getRole().getNamaRole().toLowerCase().contains(keyword)))){
                        hasil.add(p); 
                    }                
                }
            } else {
                for (Pengguna p : findAllPengguna()){
                    if ((p.getNamaPengguna().toLowerCase().contains(keyword) || (p.getEmailPengguna().toLowerCase().contains(keyword)) || (p.getRole().getNamaRole().toLowerCase().contains(keyword)))){
                        String nama = p.getRole().getNamaRole();
                        if (nama.equals(namaRole) && !hasil.contains(p)){
                            hasil.add(p);
                        }
                        if (!nama.equals(namaRole))  {
                            hasil.remove(p);
                        }
                    }
                    else {
                        hasil.remove(p);
                    }       
                         
                }

            } 


            }
            
            if ((keyword == null || keyword == "" || keyword == " ") && (namaRole == null || namaRole.equals("All")) ){
                hasil = findAllPengguna();
            }
    
            return hasil;
        }

        

        

     
        
    
    @Override
    public List <Pengguna> findAllPenggunaByRole(String namaRole){
        List<Pengguna> hasil = new ArrayList<Pengguna>();
        for (Pengguna p : findAllPengguna()){
            if (p.getRole().getNamaRole().equalsIgnoreCase(namaRole)){
                hasil.add(p);
            }
        }
        return hasil;

    }

    @Override
    public void updatePengguna(Pengguna pengguna){
        repo.save(pengguna);
    }



    
}
