package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.date.DateValidator;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "inciso")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inciso implements Cloneable, Serializable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inciso_id")
    private Long idInciso;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Column(name = "numero_inciso", nullable = false)
    private String numeroInciso;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "folio_factura", nullable = false)
    private String folioFactura;

    @Setter
    @Getter
    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private Date fechaInicioVigencia;

    @Setter
    @Getter
    @Column(name = "fecha_fin_vigencia", nullable = false)
    private Date fechaFinVigencia;

    @Column(name = "tipo_cobertura")
    private String tipoCobertura;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "estatus_inciso_id", nullable = false)
    private EstatusInciso estatusInciso;

    @Setter
    @Getter
    @Column(name = "costo_poliza")
    private Double costo;

    @Setter
    @Getter
    @Column(name = "saldo_poliza")
    private Double saldo;

    @Column(name = "frecuencia_pago")
    private String frecuenciaPago;

    @Column(name = "observaciones")
    private String observaciones;

    @Setter
    @Getter
    @Column(name = "baja_pendiente_siniestro")
    private Integer bajaPendienteSiniestro;

    @Setter
    @Getter
    @Column(name = "tiene_nota_credito")
    private Integer tieneNotaCredito;

    @Setter
    @Getter
    @Column(name = "inciso_pagado")
    private Integer incisoPagado;

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
    private String vehiculoNoSerie;

    @Setter
    @Getter
    @Transient
    private Integer polizaIdAseguradora;

    @Setter
    @Getter
    @Transient
    private String polizaNoPoliza;

    @Setter
    @Getter
    @Transient
    private List<Long> idPolizaList;

    @Setter
    @Getter
    @Transient
    private List<String> incisoList;

    @Setter
    @Getter
    @Transient
    private List<String> vehiculoNoSerieList;

    @Setter
    @Getter
    @Transient
    private List<Integer> idEstatusIncisoList;

    @Setter
    @Getter
    @Transient
    private boolean saldoDiferenteCero;

    //endregion Transients

    //region Getters & Setters

    public String getNumeroInciso() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(numeroInciso);
    }

    public void setNumeroInciso(String numeroInciso) {
        this.numeroInciso = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(numeroInciso);
    }

    public String getFolioFactura() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(folioFactura);
    }

    public void setFolioFactura(String folioFactura) {
        this.folioFactura = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(folioFactura);
    }

    public String getTipoCobertura() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(tipoCobertura);
    }

    public void setTipoCobertura(String tipoCobertura) {
        this.tipoCobertura = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(tipoCobertura);
    }

    public String getFrecuenciaPago() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(frecuenciaPago);
    }

    public void setFrecuenciaPago(String frecuenciaPago) {
        this.frecuenciaPago = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(frecuenciaPago);
    }

    public String getObservaciones() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(observaciones);
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(observaciones);
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

    public boolean esIncisoVigente() {
        Date hoy = new Date();
        return DateValidator.isDateBetween(this.fechaInicioVigencia, this.fechaFinVigencia, hoy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inciso inciso1 = (Inciso) o;
        return Objects.equals(poliza, inciso1.poliza) && Objects.equals(numeroInciso, inciso1.numeroInciso) && Objects.equals(vehiculo, inciso1.vehiculo) && estatusRegistro == inciso1.estatusRegistro;
    }

    @Override
    public int hashCode() {
        return Objects.hash(poliza, numeroInciso, vehiculo, estatusRegistro);
    }

    @Override
    public Inciso clone() {
        try {
            Inciso clone = (Inciso) super.clone();
            clone.poliza = this.poliza.clone();
            clone.vehiculo = this.vehiculo.clone();
            clone.estatusInciso = this.estatusInciso.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
