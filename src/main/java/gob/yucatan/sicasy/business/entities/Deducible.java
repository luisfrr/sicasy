package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "deducible")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deducible implements Cloneable, Serializable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deducible_id", nullable = false)
    private Long idDeducible;

    @Column(name = "tipo_deducible", nullable = false)
    private String tipoDeducicle;

    @Column(name = "vehiculo_no_serie", nullable = false)
    private String vehiculoNoSerie;

    @Column(name = "vehiculo_placa", nullable = false)
    private String vehiculoPlaca;

    @Column(name = "vehiculo_marca", nullable = false)
    private String vehiculoMarca;

    @Column(name = "vehiculo_modelo", nullable = false)
    private String vehiculoModelo;

    @Setter
    @Getter
    @Column(name = "vehiculo_anio", nullable = false)
    private Integer vehiculoAnio;

    @Setter
    @Getter
    @Column(name = "vehiculo_valor_factura", nullable = false)
    private Double vehiculoValorFactura;

    @Column(name = "poliza_numero", nullable = false)
    private String polizaNumero;

    @Column(name = "poliza_aseguradora", nullable = false)
    private String polizaAseguradora;

    @Column(name = "inciso_numero", nullable = false)
    private String incisoNumero;

    @Column(name = "inciso_tipo_cobertura")
    private String incisoTipoCobertura;

    @Setter
    @Getter
    @Column(name = "inciso_fecha_inicio_vigencia", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date incisoFechaInicioVigencia;

    @Setter
    @Getter
    @Column(name = "inciso_fecha_fin_vigencia", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date incisoFechaFinVigencia;

    @Setter
    @Getter
    @Column(name = "tiene_poliza_vigente", nullable = false)
    private Integer tienePolizaVigente;

    @Setter
    @Getter
    @Column(name = "valor_actual", nullable = false)
    private Double valorActual;

    @Setter
    @Getter
    @Column(name = "costo_total_deducible", nullable = false)
    private Double costoTotalDeducible;

    @Column(name = "folio_factura_deducible", nullable = false)
    private String folioFacturaDeducible;

    @Setter
    @Getter
    @Column(name = "nombre_archivo_factura", nullable = false)
    private String nombreArchivoFactura;

    @Setter
    @Getter
    @Column(name = "ruta_archivo_factura", nullable = false)
    private String rutaArchivoFactura;

    @Setter
    @Getter
    @OneToOne(optional=false, mappedBy="deducible")
    public Siniestro siniestro;

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

    @Override
    public Deducible clone() {
        try {
            return (Deducible) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    //region Getters & Setters

    public String getTipoDeducicle() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(tipoDeducicle);
    }

    public void setTipoDeducicle(String tipoDeducicle) {
        this.tipoDeducicle = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(tipoDeducicle);
    }

    public String getVehiculoNoSerie() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(vehiculoNoSerie);
    }

    public void setVehiculoNoSerie(String vehiculoNoSerie) {
        this.vehiculoNoSerie = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(vehiculoNoSerie);
    }

    public String getVehiculoPlaca() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(vehiculoPlaca);
    }

    public void setVehiculoPlaca(String vehiculoPlaca) {
        this.vehiculoPlaca = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(vehiculoPlaca);
    }

    public String getVehiculoMarca() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(vehiculoMarca);
    }

    public void setVehiculoMarca(String vehiculoMarca) {
        this.vehiculoMarca = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(vehiculoMarca);
    }

    public String getVehiculoModelo() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(vehiculoModelo);
    }

    public void setVehiculoModelo(String vehiculoModelo) {
        this.vehiculoModelo = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(vehiculoModelo);
    }

    public String getPolizaNumero() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(polizaNumero);
    }

    public void setPolizaNumero(String polizaNumero) {
        this.polizaNumero = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(polizaNumero);
    }

    public String getPolizaAseguradora() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(polizaAseguradora);
    }

    public void setPolizaAseguradora(String polizaAseguradora) {
        this.polizaAseguradora = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(polizaAseguradora);
    }

    public String getIncisoNumero() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(incisoNumero);
    }

    public void setIncisoNumero(String incisoNumero) {
        this.incisoNumero = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(incisoNumero);
    }

    public String getIncisoTipoCobertura() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(incisoTipoCobertura);
    }

    public void setIncisoTipoCobertura(String incisoTipoCobertura) {
        this.incisoTipoCobertura = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(incisoTipoCobertura);
    }

    public String getFolioFacturaDeducible() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(folioFacturaDeducible);
    }

    public void setFolioFacturaDeducible(String folioFacturaDeducible) {
        this.folioFacturaDeducible = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(folioFacturaDeducible);
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

}
