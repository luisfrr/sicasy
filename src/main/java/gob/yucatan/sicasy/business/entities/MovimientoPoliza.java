package gob.yucatan.sicasy.business.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
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
@Getter
@Setter
@Builder
public class MovimientoPoliza implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_poliza_id")
    private Long idMovimientoPoliza;

    @ManyToOne
    @JoinColumn(name = "poliza_id")
    private Poliza poliza;

    @Column(name = "movimiento")
    private String movimiento;

    @Column(name = "folio_factura")
    private String folioFactura;

    @Column(name = "nombre_archivo_factura")
    private String nombreArchivoFactura;

    @Column(name = "ruta_archivo_factura")
    private String rutaArchivoFactura;

    @Column(name = "usa_saldo_pendiente")
    private Integer usaSaldoPendiente;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "saldo_pendiente")
    private Double saldoPendiente;

    @Column(name = "total")
    private Double total;

    @Column(name = "incisos_pagados")
    private String incisosPagados;

    @Column(name = "incisos_pendientes")
    private String incisosPendientes;

    @Column(name = "incisos_endoso")
    private String incisosEndoso;

    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Column(name = "fecha_creacion", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", updatable = false)
    private String creadoPor;


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
