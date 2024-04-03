package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusPermiso;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol_permiso", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_permiso_id")
    private Long idRolPermiso;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "permiso_id", nullable = false)
    private Permiso permiso;

    @Column(name = "estatus", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusPermiso estatusPermiso;

}
