package gob.yucatan.sicasy.business.entities;


import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "aseguradora")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Aseguradora implements Cloneable, Serializable {

    @Setter
    @Getter
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

    @Setter
    @Getter
    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatus;

    @Setter
    @Getter
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por")
    private String modificadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_borrado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por")
    private String borradoPor;

    //region Getters & Setters

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

    public String getRepresentante() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(representante);
    }

    public void setRepresentante(String representante) {
        this.representante = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(representante);
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
        this.borradoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(borradoPor);
    }

    //endregion Getters & Setters

    @Override
    public Aseguradora clone() {
        try {
            return (Aseguradora) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aseguradora that = (Aseguradora) o;
        return Objects.equals(idAseguradora, that.idAseguradora);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idAseguradora);
    }

}
