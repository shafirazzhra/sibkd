package propensi.sibkd.sibkd.service;

import propensi.sibkd.sibkd.model.KegiatanBKD;

import java.util.List;

public interface KegiatanBKDService {
    void addKegiatan(KegiatanBKD kegiatan);
    void updateKegiatan(KegiatanBKD kegiatan);
    void deleteKegiatan(KegiatanBKD kegiatan);
    boolean isNamaExist(String namaInput);
    boolean isTemplateExist(String template);
    KegiatanBKD getKegiatanById(Long idKegiatan);
    KegiatanBKD getKegiatanByNama(String namaKegiatan);
    boolean isListItemEmpty(Long idKegiatan);
    List<KegiatanBKD> getListAllKegiatan();
    List<KegiatanBKD> getListKegiatanByBidang(Long idBidang);
}
