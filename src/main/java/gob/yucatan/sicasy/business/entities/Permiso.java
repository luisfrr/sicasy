package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "permiso", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permiso_id")
    private Long idPermiso;

    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "parent_permiso_id")
    private Permiso permisoParent;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "permisoParent")
    private Set<Permiso> subPermisos;

    @Column(name = "tipo_permiso", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private TipoPermiso tipoPermiso;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "estatus", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatus;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Column(name = "fecha_modificacion", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", insertable = false)
    private String modificadoPor;

    @Column(name = "fecha_borrado", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por", insertable = false)
    private String borradoPor;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return Objects.equals(idPermiso, permiso.idPermiso) && Objects.equals(codigo, permiso.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPermiso, codigo);
    }

}
