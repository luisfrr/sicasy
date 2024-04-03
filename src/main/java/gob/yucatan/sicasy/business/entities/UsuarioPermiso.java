package gob.yucatan.sicasy.business.entities;


import gob.yucatan.sicasy.business.enums.EstatusPermiso;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_permiso", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UsuarioPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_permiso_id")
    private Long idUsuarioPermiso;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "permiso_id", nullable = false)
    private Permiso permiso;

    @Column(name = "estatus", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusPermiso estatusPermiso;

}
