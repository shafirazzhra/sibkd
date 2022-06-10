package propensi.sibkd.sibkd.service;

import java.util.Optional;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import propensi.sibkd.sibkd.model.JabatanStruktural;
import propensi.sibkd.sibkd.repository.JabatanStrukturalDb;

@Service
@Transactional
public class JabatanStrukturalServiceImpl implements JabatanStrukturalService{
    @Autowired
    JabatanStrukturalDb jabatanStrukturalDb;

    @Override
    public JabatanStruktural getJabstrukByNamaJabstruk(String nama){
        Optional<JabatanStruktural> jabstruk = jabatanStrukturalDb.findByNamaJabatanStrukturalIgnoreCase(nama);
        if(jabstruk.isPresent()) return jabstruk.get();
        else return null;
       
    }

    @Override
    public JabatanStruktural getJabstrukByIdJabstruk(Long id){
        Optional<JabatanStruktural> jabstruk = jabatanStrukturalDb.findByIdJabatanStruktural(id);
        if(jabstruk.isPresent()) return jabstruk.get();
        else return null;
    }

    @Override
    public List<JabatanStruktural> getListJabatanStruktural(){
        return jabatanStrukturalDb.findAll();
    }
}
