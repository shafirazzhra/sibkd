package propensi.sibkd.sibkd.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.repository.*;
import propensi.sibkd.sibkd.model.*;
import java.util.List;


public interface RoleService {
    Role findByNama(String namaRole);
    Role findById(Long idRole);
    List<Role> findAllRole();
    

}
