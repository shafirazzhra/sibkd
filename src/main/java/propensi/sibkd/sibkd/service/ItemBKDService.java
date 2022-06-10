package propensi.sibkd.sibkd.service;

import propensi.sibkd.sibkd.model.ItemBKD;
import propensi.sibkd.sibkd.model.Dosen;
import propensi.sibkd.sibkd.model.Semester;
import propensi.sibkd.sibkd.model.Bidang;
import propensi.sibkd.sibkd.model.File;

import java.util.List;

public interface ItemBKDService {
    void addItemBKD(ItemBKD itemBKD);
    void updateItemBKD(ItemBKD itemBKD);
    List<ItemBKD> getListByDosenAndSemesterAndBidang(Dosen dosen, Semester semester, Bidang bidang);
    List<ItemBKD> getListByDosenAndSemester(Dosen dosen, Semester semester);
    ItemBKD getItemById(Long idItem);

    void addFileToItem(Long idItem, int kategori, File file);
    void deleteFileFromItem(Long idItem, int kategori, File file);
    void deleteItemBKD(ItemBKD itemBKD);
}