package propensi.sibkd.sibkd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reactor.util.annotation.Nullable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"dosen", "asesor"})
@Table(name= "pengguna")
public class Pengguna implements Serializable {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    private String idPengguna;

    @NotNull
    @Column(name="email", nullable = false, unique = true)
    private String emailPengguna;

    @NotNull
    @Column(name="nama", nullable = false)
    private String namaPengguna;

    
    @Column(name="alamat")
    private String alamat;
    
    @Nullable 
    @Column(name="no_telp")
    private String noTelp;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dosen", referencedColumnName = "idDosen")
    private Dosen dosen;
   
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "asesor", referencedColumnName = "idAsesor")
    private Asesor asesor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Role role;

 
}
