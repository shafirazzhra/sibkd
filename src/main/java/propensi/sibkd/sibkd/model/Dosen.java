package propensi.sibkd.sibkd.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "dosen")
public class Dosen implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDosen;

    @NotNull
    @Column(name = "email_dosen", nullable = false, unique = true)
    private String emailDosen;

    @NotNull
    @Column(name = "nama_dosen", nullable = false)
    private String namaDosen;

    @Column(name = "nidn", nullable = true)
    private String nidn;

    @Column(name = "url_nidn")
    private String urlNidn;

    @Column(name = "no_serdos", nullable = true)
    private String noSerdos;

    @Column(name = "url_serdos")
    private String urlSerdos;

    @Column(name = "perguruang_tinggi")
    private String perguruanTinggi;

    @Column(name = "fakultas")
    private String fakultas;

    @Column(name = "prodi")
    private String prodi;
    
    @Column(name = "url_jastruk")
    private String urlJastruk;

    @Column(name = "jabatan_fungsional")
    private String jabatanFungsional;

    @Column(name = "url_jafung")
    private String urlJafung;

    @Column(name = "skripsi")
    private String skripsi;

    @Column(name = "tesis")
    private String tesis;

    // perguruan tinggi S1
    @Column(name = "pt_s1")
    private String ptS1;

    // perguruan tinggi S2
    @Column(name = "pt_s2")
    private String ptS2;

    @Column(name = "url_ijazah1")
    private String urlijazah1;

    @Column(name = "url_ijazah2")
    private String urlijazah2;

    @Column(name = "url_foto")
    private String urlFoto;

    @Column(name = "status")
    private String status;

    @ManyToMany
    @JoinTable(
            name = "asesor_dosen",
            joinColumns = @JoinColumn(name = "idDosen"),
            inverseJoinColumns = @JoinColumn(name = "idAsesor")
    )
    private List<Asesor> listAsesor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_jabatan_struktural", referencedColumnName = "idJabatanStruktural")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private JabatanStruktural jabstruk;

    @OneToMany(mappedBy = "dosen", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ItemBKD> listItem;
}
