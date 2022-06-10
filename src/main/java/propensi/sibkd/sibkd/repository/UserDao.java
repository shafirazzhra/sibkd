package propensi.sibkd.sibkd.repository;

import propensi.sibkd.sibkd.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<Pengguna, String> {
    Pengguna findByEmailPengguna(String email);
    
}