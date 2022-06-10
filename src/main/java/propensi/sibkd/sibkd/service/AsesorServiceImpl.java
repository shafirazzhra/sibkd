package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.model.Asesor;
import propensi.sibkd.sibkd.repository.AsesorDb;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AsesorServiceImpl implements AsesorService{

    @Autowired
    AsesorDb asesorDb;

    @Override
    public void addAsesor(Asesor Asesor){
        asesorDb.save(Asesor);
    }

    @Override
    public void updateAsesor(Asesor Asesor){
        asesorDb.save(Asesor);
    }

    @Override
    public Asesor getAsesorByIdAsesor(Long idAsesor){
        Optional<Asesor> Asesor = asesorDb.findByIdAsesor(idAsesor);
        if(Asesor.isPresent()) return Asesor.get();
        else return null;
    }

    @Override
    public Asesor getAsesorByEmailAsesor(String email){
        Optional<Asesor> Asesor = asesorDb.findByEmailAsesor(email);
        if(Asesor.isPresent()) return Asesor.get();
        else return null;
    }

    @Override
    public List<Asesor> getListAllAsesor(){
        return asesorDb.findAll();
    }

    @Override
    public boolean isAsesorExist(String emailAsesor){
        Optional<Asesor> asesor = asesorDb.findByEmailAsesor(emailAsesor);
        if(!asesor.isPresent()) return false;
        else return true;
    }

    @Override
    public boolean isAsesorSame(Asesor asesor1, Asesor asesor2) {
        return asesor1.getEmailAsesor().equals(asesor2.getEmailAsesor());
    }

    @Override
    public boolean isAsesorAssignTheirSelf(String emailDosen, List<Asesor> listAsesor) {
        boolean flag = false;
        for(Asesor asesor : listAsesor) {
            if(asesor.getEmailAsesor().equals(emailDosen)) {
                flag = true;
                break;
            }
        }
        return flag;
    }


}
