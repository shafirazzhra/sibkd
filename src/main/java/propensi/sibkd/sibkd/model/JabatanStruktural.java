package propensi.sibkd.sibkd.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "jabatan_struktural")
public class JabatanStruktural implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idJabatanStruktural;

    @NotNull
    @Column(name = "nama_jabatanStruktural", nullable = false)
    private String namaJabatanStruktural;

    // sks minimal pendidikan + penelitian
    @Column(name = "minPdPl")
    private Long minPdPl;

    // sks minimal pengabdian + penunjang
    @Column(name = "minPgPk")
    private Long minPgPk;

    // sks minimal pendidikan
    @Column(name = "minPd")
    private Long minPd;

    // sks minimal kewajiban khusus
    @Column(name = "minKK")
    private Long minKK;

    // sks maksimal pendidikan + penelitian + pengabdian + penunjang
    @Column(name = "maxTotal")
    private Long maxTotal;

    @OneToMany(mappedBy = "jabstruk", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Dosen> listDosen;
}
