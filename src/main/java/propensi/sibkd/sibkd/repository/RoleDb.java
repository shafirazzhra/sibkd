package propensi.sibkd.sibkd.repository;
import propensi.sibkd.sibkd.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
public interface RoleDb extends JpaRepository <Role, Long> {
    Role findByIdRole (Long idRole);
    Role findByNamaRoleIgnoreCase(String namaRole);
    
}
