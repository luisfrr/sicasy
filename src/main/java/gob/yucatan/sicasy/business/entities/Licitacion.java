package gob.yucatan.sicasy.business.entities;


import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.date.DateFormatUtil;
import gob.yucatan.sicasy.utils.strings.ReplaceSymbolsUtil;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "licitacion")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Licitacion implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "licitacion_id")
    private Integer idLicitacion;

    @Column(name = "numero_licitacion", nullable = false)
    @Getter(AccessLevel.NONE)
    private String numeroLicitacion;

    @Column(name = "nombre", nullable = false)
    @Getter(AccessLevel.NONE)
    private String nombre;

    @Column(name = "descripcion")
    @Getter(AccessLevel.NONE)
    private String descripcion;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_final")
    private Date fechaFinal;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Column(name = "fecha_creacion")
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

    public String fechaInicioString(){
        return DateFormatUtil.convertToFormat(fechaInicio, "dd-MM-yyyy");
    }

    public String fechaFinalString(){
        return DateFormatUtil.convertToFormat(fechaFinal, "dd-MM-yyyy");
    }

    public String getNumeroLicitacion() {
        return ReplaceSymbolsUtil.replaceSymbolsCode(this.numeroLicitacion);
    }

    public String getNombre() {
        return ReplaceSymbolsUtil.replaceSymbolsCode(this.nombre);
    }

    public String getDescripcion() {
        return ReplaceSymbolsUtil.replaceSymbolsCode(this.descripcion);
    }

    @Override
    public Licitacion clone() {
        try {
            return (Licitacion) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
