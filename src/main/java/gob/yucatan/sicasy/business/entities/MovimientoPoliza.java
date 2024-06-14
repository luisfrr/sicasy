package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

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
    @Column(name = "id_movimiento_poliza")
    private Long idMovimientoPoliza;

    @ManyToOne
    @JoinColumn(name = "poliza_id")
    private Poliza poliza;

    @Column(name = "folio_factura")
    private String folioFactura;

    @Column(name = "ruta_archivo_factura")
    private String rutaArchivoFactura;

    @Column(name = "nombre_archivo_factura")
    private String nombreArchivoFactura;

    @Column(name = "movimiento")
    private String movimiento;

    @Column(name = "usa_saldo_pendiente")
    private Boolean usaSaldoPendiente;

    @Column(name = "incisos_pagados")
    private String incisosPagados;

    @Column(name = "incisos_pendientes")
    private String incisosPendientes;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "saldoPendiente")
    private Double saldoPendiente;

    @Column(name = "total_pagado")
    private Double total;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @Column(name = "created_by")
    private String createdBy;

}
