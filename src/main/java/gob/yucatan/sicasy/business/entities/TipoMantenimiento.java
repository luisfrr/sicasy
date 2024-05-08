package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_mantenimiento")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TipoMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_mantenimiento_id")
    private Integer idTipoMantenimiento;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "estatus_registro", nullable = false)
    private Integer estatus;

}
