package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "rol", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rol {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long idRol;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Getter
    @Setter
    @OneToMany(mappedBy = "rol")
    private Set<UsuarioRol> usuarioRolSet;

    @Getter
    @Setter
    @Column(name = "estatus", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatus;

    @Getter
    @Setter
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Getter
    @Setter
    @Column(name = "fecha_modificacion", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", insertable = false)
    private String modificadoPor;

    @Getter
    @Setter
    @Column(name = "fecha_borrado", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por", insertable = false)
    private String borradoPor;

    @Getter
    @Setter
    @Transient
    private boolean leftJoinUsuarioRolSet;

    //region Getters & Setters

    public String getCodigo() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(codigo);
    }

    public void setCodigo(String codigo) {
        this.codigo = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(codigo);
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
        this.borradoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(borradoPor);
    }

    //endregion Getters & Setters


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(idRol, rol.idRol);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idRol);
    }
}
