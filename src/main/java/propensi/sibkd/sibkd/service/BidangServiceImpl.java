package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.model.Bidang;
import propensi.sibkd.sibkd.repository.BidangDb;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BidangServiceImpl implements BidangService {

    @Autowired
    BidangDb bidangDb;

    @Override
    public Bidang getBidangByNama(String nama){
        Optional<Bidang> bidang = bidangDb.findByNamaBidangIgnoreCase(nama);
        if(bidang.isPresent()) return bidang.get();
        else return null;
    }

    @Override
    public Bidang getBidangById(Long id){
        Optional<Bidang> bidang = bidangDb.findByIdBidang(id);
        if(bidang.isPresent()) return bidang.get();
        else return null;
    }

    @Override
    public List<Bidang> getListBidang(){
        return bidangDb.findAll();
    }

}
