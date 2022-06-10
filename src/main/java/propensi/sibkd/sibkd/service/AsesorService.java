package propensi.sibkd.sibkd.service;

import propensi.sibkd.sibkd.model.Asesor;

import java.util.List;

public interface AsesorService {
    void addAsesor(Asesor asesor);
    void updateAsesor(Asesor asesor);
    List<Asesor> getListAllAsesor();
    Asesor getAsesorByIdAsesor(Long idAsesor);
    Asesor getAsesorByEmailAsesor(String email);
    boolean isAsesorExist(String emailAsesor);
    boolean isAsesorSame(Asesor asesor1, Asesor asesor2);
    boolean isAsesorAssignTheirSelf(String emailDosen, List<Asesor> listAsesor);
    
}