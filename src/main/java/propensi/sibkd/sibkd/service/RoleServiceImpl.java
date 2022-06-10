package propensi.sibkd.sibkd.service;

import java.util.Optional;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import propensi.sibkd.sibkd.model.Role;
import propensi.sibkd.sibkd.repository.RoleDb;
@Service
@Transactional
public class RoleServiceImpl implements RoleService{

    @Autowired
    RoleDb roleDb;
    @Override
    public Role findByNama(String namaRole) {
        Optional<Role> role = Optional.ofNullable(roleDb.findByNamaRoleIgnoreCase(namaRole));
        if(role.isPresent()) return role.get();
        else return null;
    }

    @Override
    public List<Role> findAllRole() {
        return roleDb.findAll();
    }

    @Override
    public Role findById(Long idRole) {
        return roleDb.findByIdRole(idRole);
    }
}