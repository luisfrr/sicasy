package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estatus_vehiculo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EstatusVehiculo implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estatus_vehiculo_id", nullable = false)
    private String idEstatusVehiculo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Override
    public EstatusVehiculo clone() {
        try {
            return (EstatusVehiculo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
