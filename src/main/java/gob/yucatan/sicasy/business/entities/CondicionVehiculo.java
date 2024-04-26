package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "condicion_vehiculo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CondicionVehiculo implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condicion_vehiculo_id")
    private Integer idCondicionVehiculo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Override
    public CondicionVehiculo clone() {
        try {
            return (CondicionVehiculo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
