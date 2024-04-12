package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusPermiso;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @Transient
    private boolean estatusPermisoNotEqual;

    @Transient
    private List<RolPermiso> subRolPermisoList;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String checkEstatus;

    public String getCheckEstatus() {
        String SIN_ASIGNAR = "0";
        String HABILITADO = "1";
        String DESHABILITADO = "2";

        if(estatusPermiso == null)
            return SIN_ASIGNAR;
        else {
            if(estatusPermiso.equals(EstatusPermiso.HABILITADO))
                return HABILITADO;
            else if (estatusPermiso.equals(EstatusPermiso.DESHABILITADO))
                return DESHABILITADO;
            else
                return SIN_ASIGNAR;
        }
    }

    public void setCheckEstatus(String checkEstatus) {
        this.checkEstatus = checkEstatus;
        String HABILITADO = "1";
        String DESHABILITADO = "2";

        if(this.checkEstatus.equals(HABILITADO))
            this.estatusPermiso = EstatusPermiso.HABILITADO;
        else if(this.checkEstatus.equals(DESHABILITADO))
            this.estatusPermiso = EstatusPermiso.DESHABILITADO;
        else
            this.estatusPermiso = EstatusPermiso.SIN_ASIGNAR;

    }
}
