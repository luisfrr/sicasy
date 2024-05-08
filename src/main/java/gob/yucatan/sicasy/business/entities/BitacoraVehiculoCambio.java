package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "bitacora_vehiculo_cambio")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BitacoraVehiculoCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacora_vehiculo_cambio_id")
    private Long idBitacoraVehiculoCambio;

    @ManyToOne
    @JoinColumn(name = "bitacora_vehiculo_id", nullable = false)
    private BitacoraVehiculo bitacoraVehiculo;

    @Column(name = "campo", nullable = false)
    private String campo;

    @Column(name = "tipo_dato", nullable = false)
    private String tipoDato;

    @Column(name = "valor_anterior", nullable = false)
    private String valorAnterior;

    @Column(name = "valor_nuevo", nullable = false)
    private String valorNuevo;

    @Column(name = "fecha_modificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitacoraVehiculoCambio that = (BitacoraVehiculoCambio) o;
        return Objects.equals(bitacoraVehiculo, that.bitacoraVehiculo) && Objects.equals(campo, that.campo) && Objects.equals(tipoDato, that.tipoDato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitacoraVehiculo, campo, tipoDato);
    }
}
