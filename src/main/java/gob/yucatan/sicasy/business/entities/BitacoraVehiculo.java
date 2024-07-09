package gob.yucatan.sicasy.business.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "bitacora_vehiculo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BitacoraVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacora_vehiculo_id")
    private Long idBitacoraVehiculo;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "accion", nullable = false)
    private String accion;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bitacoraVehiculo")
    private Set<BitacoraVehiculoCambio> cambioSet;

    @Column(name = "fecha_modificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", nullable = false)
    private String modificadoPor;

}
