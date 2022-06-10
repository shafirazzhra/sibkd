package propensi.sibkd.sibkd.service;

import propensi.sibkd.sibkd.model.JabatanStruktural;
import java.util.List;

public interface JabatanStrukturalService {
    JabatanStruktural getJabstrukByNamaJabstruk(String nama);
    JabatanStruktural getJabstrukByIdJabstruk(Long id);
    List<JabatanStruktural> getListJabatanStruktural();
}
