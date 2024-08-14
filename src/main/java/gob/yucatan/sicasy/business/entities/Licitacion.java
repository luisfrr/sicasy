package gob.yucatan.sicasy.business.entities;


import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.date.DateFormatUtil;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "licitacion")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Licitacion implements Cloneable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "licitacion_id")
    private Integer idLicitacion;

    @Column(name = "numero_licitacion", nullable = false)
    private String numeroLicitacion;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Setter
    @Getter
    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Setter
    @Getter
    @Column(name = "fecha_final")
    private Date fechaFinal;

    @Setter
    @Getter
    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Setter
    @Getter
    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Setter
    @Getter
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_modificacion", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", insertable = false)
    private String modificadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_borrado", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por", insertable = false)
    private String borradoPor;


    //region Getters & Setters

    public String getNumeroLicitacion() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(numeroLicitacion);
    }

    public void setNumeroLicitacion(String numeroLicitacion) {
        this.numeroLicitacion = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(numeroLicitacion);
    }

    public String getNombre() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(nombre);
    }

    public void setNombre(String nombre) {
        this.nombre = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(nombre);
    }

    public String getDescripcion() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(descripcion);
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(descripcion);
    }

    public String getCreadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(creadoPor);
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(creadoPor);
    }

    public String getModificadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(modificadoPor);
    }

    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(modificadoPor);
    }

    public String getBorradoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(borradoPor);
    }

    public void setBorradoPor(String borradoPor) {
        this.borradoPor = HtmlEntityConverter.convertHtmlEntitiesToSymbols(borradoPor);
    }

    //endregion Getters & Setters

    public String fechaInicioString(){
        return DateFormatUtil.convertToFormat(fechaInicio, "dd-MM-yyyy");
    }

    public String fechaFinalString(){
        return DateFormatUtil.convertToFormat(fechaFinal, "dd-MM-yyyy");
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
