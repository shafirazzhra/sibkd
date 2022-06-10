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
@Table(name= "kegiatan_bkd")
public class KegiatanBKD implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKegiatan;

 
    
    @NotNull
    @Column(name="nama_kegiatan", nullable = false)
    private String namaKegiatan;
    
    @NotNull
    @Column(name="template", nullable = false)
    private String template;

    @OneToMany(mappedBy = "kegiatanBKD", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ItemBKD> listItem;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_bidang", referencedColumnName = "idBidang", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bidang bidang;
}
