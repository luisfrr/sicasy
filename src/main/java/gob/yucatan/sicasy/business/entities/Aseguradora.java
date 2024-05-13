package gob.yucatan.sicasy.business.entities;


import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "aseguradora")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Aseguradora implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aseguradora")
    private Integer idAseguradora;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "representante", nullable = false)
    private String representante;

    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatus;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por")
    private String modificadoPor;

    @Column(name = "fecha_borrado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por")
    private String borradoPor;


    @Override
    public Aseguradora clone() {
        try {
            return (Aseguradora) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


}
