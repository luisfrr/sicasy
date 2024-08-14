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
import java.util.Set;

@Entity
@Table(name = "vehiculo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Vehiculo implements Cloneable, Serializable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehiculo_id", nullable = false)
    private Long idVehiculo;

    @Column(name = "no_serie", nullable = false)
    private String noSerie;

    @Column(name = "placa", nullable = false)
    private String placa;

    @Column(name = "marca", nullable = false)
    private String marca;

    @Column(name = "modelo", nullable = false)
    private String modelo;

    @Setter
    @Getter
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "no_motor", nullable = false)
    private String noMotor;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "descripcion_vehiculo")
    private String descripcionVehiculo;

    @Setter
    @Getter
    @Column(name = "monto_factura", nullable = false)
    private Double montoFactura;

    @Column(name = "no_factura", nullable = false)
    private String noFactura;

    @Setter
    @Getter
    @Column(name = "renta_mensual", nullable = false)
    private Double rentaMensual;

    @Column(name = "proveedor")
    private String proveedor;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "licitacion_id")
    private Licitacion licitacion;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "anexo_id")
    private Anexo anexo;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "dependencia_id", nullable = false)
    private Dependencia dependencia;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "asignada_dependencia_id")
    private Dependencia dependenciaAsignada;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "estatus_vehiculo_id", nullable = false)
    private EstatusVehiculo estatusVehiculo;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "condicion_vehiculo_id", nullable = false)
    private CondicionVehiculo condicionVehiculo;

    @Column(name = "resguardante")
    private String resguardante;

    @Column(name = "area_resguardante")
    private String areaResguardante;

    @Column(name = "autoriza_director_admin")
    private String autorizaDirectorAdmin;

    @Column(name = "autoriza_director_general")
    private String autorizaDirectorGeneral;

    @Column(name = "observaciones")
    private String observaciones;

    @Setter
    @Getter
    @OneToMany(mappedBy = "vehiculo", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Inciso> incisoSet;

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


    //region Transients

    @Setter
    @Getter
    @Transient
    private Integer condicionId;

    @Setter
    @Getter
    @Transient
    private String numLicitacion;

    @Setter
    @Getter
    @Transient
    private String anexoValue;

    @Setter
    @Getter
    @Transient
    private List<Integer> idEstatusVehiculoList;

    @Setter
    @Getter
    @Transient
    private List<String> noSerieList;

    @Setter
    @Getter
    @Transient
    private boolean fetchIncisoSet;

    //endregion Transients

    //region Getters & Setters

    public String getNoSerie() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(noSerie);
    }

    public void setNoSerie(String noSerie) {
        this.noSerie = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(noSerie);
    }

    public String getPlaca() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(placa);
    }

    public void setPlaca(String placa) {
        this.placa = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(placa);
    }

    public String getMarca() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(marca);
    }

    public void setMarca(String marca) {
        this.marca = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(marca);
    }

    public String getModelo() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(modelo);
    }

    public void setModelo(String modelo) {
        this.modelo = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(modelo);
    }

    public String getNoMotor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(noMotor);
    }

    public void setNoMotor(String noMotor) {
        this.noMotor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(noMotor);
    }

    public String getColor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(color);
    }

    public void setColor(String color) {
        this.color = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(color);
    }

    public String getDescripcionVehiculo() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(descripcionVehiculo);
    }

    public void setDescripcionVehiculo(String descripcionVehiculo) {
        this.descripcionVehiculo = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(descripcionVehiculo);
    }

    public String getNoFactura() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(noFactura);
    }

    public void setNoFactura(String noFactura) {
        this.noFactura = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(noFactura);
    }

    public String getProveedor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(proveedor);
    }

    public void setProveedor(String proveedor) {
        this.proveedor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(proveedor);
    }

    public String getResguardante() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(resguardante);
    }

    public void setResguardante(String resguardante) {
        this.resguardante = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(resguardante);
    }

    public String getAreaResguardante() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(areaResguardante);
    }

    public void setAreaResguardante(String areaResguardante) {
        this.areaResguardante = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(areaResguardante);
    }

    public String getAutorizaDirectorAdmin() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(autorizaDirectorAdmin);
    }

    public void setAutorizaDirectorAdmin(String autorizaDirectorAdmin) {
        this.autorizaDirectorAdmin = HtmlEntityConverter.convertHtmlEntitiesToSymbols(autorizaDirectorAdmin);
    }

    public String getAutorizaDirectorGeneral() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(autorizaDirectorGeneral);
    }

    public void setAutorizaDirectorGeneral(String autorizaDirectorGeneral) {
        this.autorizaDirectorGeneral = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(autorizaDirectorGeneral);
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


    @Override
    public Vehiculo clone() {
        try {
            Vehiculo clone = (Vehiculo) super.clone();
            clone.licitacion = this.licitacion != null ? this.licitacion.clone() : null;
            clone.anexo = this.anexo != null ? this.anexo.clone() : null;
            clone.dependencia = this.dependencia != null ? this.dependencia.clone() : null;
            clone.dependenciaAsignada = this.dependenciaAsignada != null ? this.dependenciaAsignada.clone() : null;
            clone.estatusVehiculo = this.estatusVehiculo != null ? this.estatusVehiculo.clone() : null;
            clone.condicionVehiculo = this.condicionVehiculo != null ? this.condicionVehiculo.clone() : null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehiculo vehiculo = (Vehiculo) o;
        return Objects.equals(idVehiculo, vehiculo.idVehiculo) && Objects.equals(noSerie, vehiculo.noSerie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVehiculo, noSerie);
    }

    public Inciso getIncisoVigente() {
        if(this.incisoSet != null && !this.incisoSet.isEmpty()) {
            Date today = new Date();
            return this.incisoSet.stream()
                    .filter(i -> DateValidator.isDateBetween(i.getFechaInicioVigencia(), i.getFechaFinVigencia(), today) &&
                            i.getEstatusRegistro() == EstatusRegistro.ACTIVO)
                    .findFirst().orElse(null);
        }
        return null;
    }
}
