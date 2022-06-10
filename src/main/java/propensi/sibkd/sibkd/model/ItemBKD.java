package propensi.sibkd.sibkd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "item_bkd")
public class ItemBKD implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItem;

    @NotNull
    @Column(name="deskripsi", nullable = false)
    private String deskripsi; //String panjang isian template dari KegiatanBKD 

    @Column(name="nama_bukti_penugasan", nullable = false)
    private String namaBuktiPenugasan; 

    @OneToMany
    @JoinColumn(name="file_bukti_penugasan")
    private List<File> listFileBuktiPenugasan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_dosen", referencedColumnName = "idDosen", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dosen dosen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_bidang", referencedColumnName = "idBidang", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bidang bidang;

    @NotNull
    @Column(name="sks", nullable = false)
    private int sks;

    @NotNull
    @Column(name="masa_penugasan", nullable = false)
    private int masaPenugasan; //dalam semester

    @Column(name="nama_bukti", nullable = false)
    private String namaBukti;

    @OneToMany
    @JoinColumn(name="file_bukti")
    private List<File> listFileBukti;

    @Column(name="catatan_asesor1")
    private String catatanAsesor1;

    @Column(name="catatan_asesor2")
    private String catatanAsesor2;

    @Column(name="rekomendasi_asesor1")
    private String rekomendasiAsesor1;

    @Column(name="rekomendasi_asesor2")
    private String rekomendasiAsesor2;

    @Column(name="komentar_kaprodi")
    private String komentarKaprodi;
    
    @Column(name="sks_terpenuhi")
    private int sksTerpenuhi;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "id_kegiatan", referencedColumnName = "idKegiatan", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private KegiatanBKD kegiatanBKD;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "id_semester", referencedColumnName = "idSemester", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Semester semester;
}
