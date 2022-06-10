package propensi.sibkd.sibkd.service;

import propensi.sibkd.sibkd.model.Bidang;

import java.util.List;
import java.util.Optional;

public interface BidangService {
    Bidang getBidangByNama(String nama);
    Bidang getBidangById(Long id);
    List<Bidang> getListBidang();
}
