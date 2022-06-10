package propensi.sibkd.sibkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import propensi.sibkd.sibkd.model.Permission;

public interface permissionDb extends JpaRepository<Permission, String>{
    
}
