package gob.yucatan.sicasy.business.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import gob.yucatan.sicasy.utils.strings.JsonStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "movimiento_poliza")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovimientoPoliza implements Serializable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_poliza_id")
    private Long idMovimientoPoliza;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "poliza_id")
    private Poliza poliza;

    @Column(name = "movimiento")
    private String movimiento;

    @Column(name = "folio_factura")
    private String folioFactura;

    @Setter
    @Getter
    @Column(name = "nombre_archivo_factura")
    private String nombreArchivoFactura;

    @Setter
    @Getter
    @Column(name = "ruta_archivo_factura")
    private String rutaArchivoFactura;

    @Setter
    @Getter
    @Column(name = "usa_saldo_pendiente")
    private Integer usaSaldoPendiente;

    @Setter
    @Getter
    @Column(name = "subtotal")
    private Double subtotal;

    @Setter
    @Getter
    @Column(name = "saldo_pendiente")
    private Double saldoPendiente;

    @Setter
    @Getter
    @Column(name = "total")
    private Double total;

    @Setter
    @Getter
    @Column(name = "incisos_pagados")
    private String incisosPagados;

    @Setter
    @Getter
    @Column(name = "incisos_pendientes")
    private String incisosPendientes;

    @Setter
    @Getter
    @Column(name = "incisos_endoso")
    private String incisosEndoso;

    @Setter
    @Getter
    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Setter
    @Getter
    @Column(name = "fecha_creacion", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", updatable = false)
    private String creadoPor;

    //region Getters & Setters

    public String getMovimiento() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(movimiento);
    }

    public void setMovimiento(String movimiento) {
        this.movimiento = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(movimiento);
    }

    public String getFolioFactura() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(folioFactura);
    }

    public void setFolioFactura(String folioFactura) {
        this.folioFactura = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(folioFactura);
    }

    public String getCreadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(creadoPor);
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(creadoPor);
    }

    //endregion Getters & Setters

    public List<Inciso> getIncisosPagadosList() {
        List<Inciso> incisos = new ArrayList<>();
        if(this.incisosPagados != null && !this.incisosPagados.isEmpty()) {
            TypeReference<List<Inciso>> typeRef = new TypeReference<>() {};
            incisos = JsonStringConverter.convertToList(this.incisosPagados, typeRef);
        }
        return incisos;
    }

    public List<Inciso> getIncisosPendientesList() {
        List<Inciso> incisos = new ArrayList<>();
        if(this.incisosPendientes != null && !this.incisosPendientes.isEmpty()) {
            TypeReference<List<Inciso>> typeRef = new TypeReference<>() {};
            incisos = JsonStringConverter.convertToList(this.incisosPendientes, typeRef);
        }
        return incisos;
    }

    public List<Inciso> getIncisosEndososList() {
        List<Inciso> incisos = new ArrayList<>();
        if(this.incisosEndoso != null && !this.incisosEndoso.isEmpty()) {
            TypeReference<List<Inciso>> typeRef = new TypeReference<>() {};
            incisos = JsonStringConverter.convertToList(this.incisosEndoso, typeRef);
        }
        return incisos;
    }

}
