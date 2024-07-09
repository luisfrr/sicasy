package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "estatus_siniestro")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EstatusSiniestro implements Serializable, Cloneable {

    @Id
    @Column(name = "estatus_siniestro_id", nullable = false)
    private Integer idEstatusSiniestro;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Override
    public EstatusSiniestro clone() {
        try {
            return (EstatusSiniestro) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
