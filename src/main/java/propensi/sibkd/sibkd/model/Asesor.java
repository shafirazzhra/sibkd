package propensi.sibkd.sibkd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "asesor")
public class Asesor implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsesor;

    @NotNull
    @Column(name="email", nullable = false)
    private String emailAsesor;

    @NotNull
    @Column(name="nama", nullable = false)
    private String namaAsesor;

    @ManyToMany(mappedBy = "listAsesor")
    private List<Dosen> listDosen;

    public Asesor(Long id, String namaAsesor) {
        this.idAsesor = id;
        this.namaAsesor = namaAsesor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asesor asesor = (Asesor) o;
        return idAsesor.equals(asesor.idAsesor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAsesor);
    }
}
