package propensi.sibkd.sibkd.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import propensi.sibkd.sibkd.repository.*;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.security.*;


public interface PenggunaService {

   void processOAuthPostLogin(String email, Authentication auth);
   Pengguna findByEmail(String email);
   List <Pengguna> findAllPengguna();
   List <Pengguna> findAllPenggunaByRole(String namaRole);
   List <Pengguna> searchPengguna(String keyword, String namaRole);
   void updatePengguna(Pengguna pengguna);
   

}