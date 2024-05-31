package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "estatus_inciso")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EstatusInciso implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estatus_inciso_id")
    private Integer idEstatusPoliza;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Override
    public EstatusInciso clone() {
        try {
            return (EstatusInciso) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
