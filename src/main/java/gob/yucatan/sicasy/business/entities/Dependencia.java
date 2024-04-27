package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "dependencia")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Dependencia implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dependencia_id", nullable = false)
    private Integer idDependencia;

    @Column(name = "clave_presupuestal", nullable = false)
    private String clavePresupuestal;

    @Column(name = "abreviatura", nullable = false)
    private String abreviatura;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "director")
    private String director;

    @Column(name = "puesto")
    private String puesto;

    @Column(name = "no_oficio")
    private String noOficio;

    @Override
    public Dependencia clone() {
        try {
            return (Dependencia) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
