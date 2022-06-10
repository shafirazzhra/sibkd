package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.sibkd.sibkd.model.ItemBKD;
import propensi.sibkd.sibkd.model.Dosen;
import propensi.sibkd.sibkd.model.Semester;
import propensi.sibkd.sibkd.model.Bidang;
import propensi.sibkd.sibkd.model.File;
import propensi.sibkd.sibkd.repository.ItemDb;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ItemBKDServiceImpl implements ItemBKDService{

    @Autowired
    ItemDb itemDb;

    @Override
    public void addItemBKD(ItemBKD ItemBKD){
        itemDb.save(ItemBKD);
    }

    @Override
    public void updateItemBKD(ItemBKD ItemBKD){
        itemDb.save(ItemBKD);
    }

    @Override
    public List<ItemBKD> getListByDosenAndSemesterAndBidang(Dosen dosen, Semester semester, Bidang bidang){
        return itemDb.findAllByDosenAndSemesterAndBidang(dosen, semester, bidang);
    }

    @Override
    public List<ItemBKD> getListByDosenAndSemester(Dosen dosen, Semester semester){
        return itemDb.findAllByDosenAndSemester(dosen, semester);
    }

    @Override
    public ItemBKD getItemById(Long idItem){
        Optional<ItemBKD> item = itemDb.findItemBKDByIdItem(idItem);
        if(item.isPresent()) return item.get();
        else return null;
    }

    @Override
    public void addFileToItem(Long idItem, int kategori, File file) {
        ItemBKD item = itemDb.findItemBKDByIdItem(idItem).get();
        //kategori 1 = penugasan, 2 = kinerja
        if (kategori == 1) {
            item.getListFileBuktiPenugasan().add(file);
        } else if (kategori == 2) {
            item.getListFileBukti().add(file);
        }
    }

    @Override
    public void deleteFileFromItem(Long idItem, int kategori, File file) {
        ItemBKD item = itemDb.findItemBKDByIdItem(idItem).get();
        //kategori 1 = penugasan, 2 = kinerja
        if (kategori == 1) {
            item.getListFileBuktiPenugasan().remove(file);
            
        } else if (kategori == 2) {
            item.getListFileBukti().remove(file);
        }
    }

    @Override
    public void deleteItemBKD(ItemBKD itemBKD) {
        itemDb.delete(itemBKD);
    }

}
