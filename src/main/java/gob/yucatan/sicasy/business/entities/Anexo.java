package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.date.DateFormatUtil;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "anexo")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class Anexo implements Cloneable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anexo_id")
    private Long idAnexo;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "licitacion_id", nullable = false)
    private Licitacion licitacion;

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
    @Column(name = "fecha_firma")
    private Date fechaFirma;

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

    @Column(name = "creado_por")
    private String creadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_modificacion")
    private Date fechaModificacion;

    @Column(name = "modificado_por")
    private String modificadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_borrado")
    private Date fechaBorrado;

    @Column(name = "borrado_por")
    private String borradoPor;


    //region Transients

    @Setter
    @Getter
    @Transient
    private String numLicitacionString;

    //endregion Transients

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

    public String fechaInicioString(){
        return DateFormatUtil.convertToFormat(fechaInicio,"dd-MM-yyyy");
    }

    public String fechaFinalString(){
        return DateFormatUtil.convertToFormat(fechaFinal,"dd-MM-yyyy");
    }

    public String fechaFirmaString(){
        return DateFormatUtil.convertToFormat(fechaFirma, "dd-MM-yyyy");
    }

    public Boolean getIsExpirationDateFechaFinal() {
        if (fechaFinal != null){
            Instant instant = fechaFinal.toInstant();

            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate today = LocalDate.now(ZoneId.systemDefault());

            // return true si la fecha esta despues de la fecha de hoy menos 30 dias
            return date.isAfter(today.minusDays(30));
        }
        return false;
    }

    @Override
    public Anexo clone() {
        try {
            Anexo clone = (Anexo) super.clone();
            clone.licitacion = this.licitacion.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
