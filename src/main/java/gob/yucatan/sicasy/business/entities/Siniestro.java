package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "siniestro")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Siniestro implements Cloneable, Serializable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "siniestro_id", nullable = false)
    private Long idSiniestro;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "inciso_id")
    private Inciso inciso;

    @Column(name = "no_siniestro_saf", nullable = false)
    private String noSiniestroSAF;

    @Column(name = "no_siniestro_aseguradora", nullable = false)
    private String noSiniestroAseguradora;

    @Column(name = "ajustador_saf", nullable = false)
    private String ajustadorSAF;

    @Column(name = "ajustador_aseguradora", nullable = false)
    private String ajustadorAseguradora;

    @Column(name = "estado")
    private String estado;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "localidad")
    private String localidad;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "estatus_siniestro_id", nullable = false)
    private EstatusSiniestro estatusSiniestro;

    @Column(name = "causa", nullable = false)
    private String causa;

    @Column(name = "responsable", nullable = false)
    private String responsable;

    @Column(name = "reporta")
    private String reporta;

    @Column(name = "conductor", nullable = false)
    private String conductor;

    @Setter
    @Getter
    @Column(name = "fecha_siniestro", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSiniestro;

    @Setter
    @Getter
    @Column(name = "fecha_reporte", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaReporte;

    @Column(name = "declaracion_siniestro", nullable = false)
    private String declaracionSiniestro;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "no_licencia", nullable = false)
    private String noLicencia;

    @Column(name = "tipo_licencia")
    private String tipoLicencia;

    @Setter
    @Getter
    @Column(name = "vencimiento_licencia")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vencimientoLicencia;

    @Setter
    @Getter
    @Column(name = "danio_via_publica", nullable = false)
    private Integer danioViaPublica;

    @Setter
    @Getter
    @Column(name = "corralon", nullable = false)
    private Integer corralon;

    @Setter
    @Getter
    @Column(name = "multa_vehiculo", nullable = false)
    private Integer multaVehiculo;

    @Setter
    @Getter
    @Column(name = "costo_multa")
    private Double costoMulta;

    @Setter
    @Getter
    @OneToOne
    @JoinColumn(name="deducible_id")
    private Deducible deducible;

    @Setter
    @Getter
    @Column(name = "perdida_total", nullable = false)
    private Integer perdidaTotal;

    @Setter
    @Getter
    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Setter
    @Getter
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
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

    //region Transients

    @Setter
    @Getter
    @Transient
    private Date fechaInicioFilter;

    @Setter
    @Getter
    @Transient
    private Date fechaFinFilter;

    @Setter
    @Getter
    @Transient
    private Integer pagoDeducible;

    @Setter
    @Getter
    @Transient
    private boolean checkDanioViaPublica;

    @Setter
    @Getter
    @Transient
    private boolean checkCorralon;

    @Setter
    @Getter
    @Transient
    private boolean checkMulta;

    @Setter
    @Getter
    @Transient
    private boolean checkPerdidaTotal;

    //endregion Transients

    //region Getters & Setters

    public String getNoSiniestroSAF() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(noSiniestroSAF);
    }

    public void setNoSiniestroSAF(String noSiniestroSAF) {
        this.noSiniestroSAF = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(noSiniestroSAF);
    }

    public String getNoSiniestroAseguradora() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(noSiniestroAseguradora);
    }

    public void setNoSiniestroAseguradora(String noSiniestroAseguradora) {
        this.noSiniestroAseguradora = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(noSiniestroAseguradora);
    }

    public String getAjustadorSAF() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(ajustadorSAF);
    }

    public void setAjustadorSAF(String ajustadorSAF) {
        this.ajustadorSAF = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(ajustadorSAF);
    }

    public String getAjustadorAseguradora() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(ajustadorAseguradora);
    }

    public void setAjustadorAseguradora(String ajustadorAseguradora) {
        this.ajustadorAseguradora = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(ajustadorAseguradora);
    }

    public String getEstado() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(estado);
    }

    public void setEstado(String estado) {
        this.estado = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(estado);
    }

    public String getMunicipio() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(municipio);
    }

    public void setMunicipio(String municipio) {
        this.municipio = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(municipio);
    }

    public String getLocalidad() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(localidad);
    }

    public void setLocalidad(String localidad) {
        this.localidad = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(localidad);
    }

    public String getCausa() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(causa);
    }

    public void setCausa(String causa) {
        this.causa = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(causa);
    }

    public String getResponsable() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(responsable);
    }

    public void setResponsable(String responsable) {
        this.responsable = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(responsable);
    }

    public String getReporta() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(reporta);
    }

    public void setReporta(String reporta) {
        this.reporta = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(reporta);
    }

    public String getConductor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(conductor);
    }

    public void setConductor(String conductor) {
        this.conductor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(conductor);
    }

    public String getDeclaracionSiniestro() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(declaracionSiniestro);
    }

    public void setDeclaracionSiniestro(String declaracionSiniestro) {
        this.declaracionSiniestro = HtmlEntityConverter.convertHtmlEntitiesToSymbols(declaracionSiniestro);
    }

    public String getObservaciones() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(observaciones);
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(observaciones);
    }

    public String getNoLicencia() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(noLicencia);
    }

    public void setNoLicencia(String noLicencia) {
        this.noLicencia = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(noLicencia);
    }

    public String getTipoLicencia() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(tipoLicencia);
    }

    public void setTipoLicencia(String tipoLicencia) {
        this.tipoLicencia = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(tipoLicencia);
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

    @Override
    public Siniestro clone() {
        try {
            Siniestro clone = (Siniestro) super.clone();
            clone.vehiculo = this.vehiculo.clone();
            clone.inciso = this.inciso != null ? this.inciso.clone() : null;
            clone.deducible = this.deducible != null ? this.deducible.clone() : null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public boolean requierePagoDeducible() {
        return this.responsable != null &&
                Objects.equals(this.responsable, "ASEGURADO");
    }
}
