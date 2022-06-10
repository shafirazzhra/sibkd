package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.model.Bidang;
import propensi.sibkd.sibkd.model.ItemBKD;
import propensi.sibkd.sibkd.model.KegiatanBKD;
import propensi.sibkd.sibkd.repository.BidangDb;
import propensi.sibkd.sibkd.repository.ItemDb;
import propensi.sibkd.sibkd.repository.KegiatanDb;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class KegiatanBKDServiceImpl implements KegiatanBKDService{

    @Autowired
    KegiatanDb kegiatanDb;

    @Autowired
    BidangDb bidangDb;

    @Autowired
    ItemDb itemDb;

    @Override
    public void addKegiatan(KegiatanBKD kegiatan){
        kegiatanDb.save(kegiatan);
    }

    @Override
    public void updateKegiatan(KegiatanBKD kegiatan){
        kegiatanDb.save(kegiatan);
    }

    @Override
    public void deleteKegiatan(KegiatanBKD kegiatan) { kegiatanDb.delete(kegiatan); }

    @Override
    public KegiatanBKD getKegiatanById(Long idKegiatan){
        Optional<KegiatanBKD> kegiatan = kegiatanDb.findByIdKegiatan(idKegiatan);
        if(kegiatan.isPresent()) return kegiatan.get();
        else return null;
    }

    @Override
    public KegiatanBKD getKegiatanByNama(String namaKegiatan){
        Optional<KegiatanBKD> kegiatan = kegiatanDb.findByNamaKegiatanIgnoreCase(namaKegiatan);
        if(kegiatan.isPresent()) return kegiatan.get();
        else return null;
    }

    @Override
    public boolean isListItemEmpty(Long idKegiatan){
        List<ItemBKD> listItem = itemDb.findAllByKegiatanBKD(getKegiatanById(idKegiatan));
        if (listItem.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<KegiatanBKD> getListAllKegiatan(){
        return kegiatanDb.findAll();
    }

    @Override
    public List<KegiatanBKD> getListKegiatanByBidang(Long idBidang){
        Optional<Bidang> bidang = bidangDb.findByIdBidang(idBidang);
        if(bidang.isPresent()){
            return kegiatanDb.findAllByBidang(bidang);
        }
        else return null;
    }

    @Override
    public boolean isNamaExist(String namaInput){
        for(KegiatanBKD kegiatan: getListAllKegiatan()){
            if(namaInput.equalsIgnoreCase(kegiatan.getNamaKegiatan())){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTemplateExist(String template){
        for(KegiatanBKD kegiatan: getListAllKegiatan()){
            if(template.equals(kegiatan.getTemplate())){
                return true;
            }
        }
        return false;
    }
}
