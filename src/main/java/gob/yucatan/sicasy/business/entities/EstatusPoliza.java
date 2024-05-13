package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "estatus_poliza")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EstatusPoliza implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estatus_poliza_id")
    private Integer idEstatusPoliza;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Override
    public EstatusPoliza clone() {
        try {
            return (EstatusPoliza) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
