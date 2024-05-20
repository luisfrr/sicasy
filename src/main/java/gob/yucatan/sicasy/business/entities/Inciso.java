package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "inciso")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Inciso implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inciso_id")
    private Long idInciso;

    @ManyToOne
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Column(name = "inciso")
    private String inciso;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "folio_factura", nullable = false)
    private String folioFactura;

    @Column(name = "fecha_inicio_vigencia")
    private Date fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private Date fechaFinVigencia;

    @Column(name = "tipo_cobertura")
    private String tipoCobertura;

    @ManyToOne
    @JoinColumn(name = "estatus_inciso_id", nullable = false)
    private EstatusInciso estatusInciso;

    @Column(name = "costo")
    private Double costo;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "frecuencia_pago")
    private String frecuenciaPago;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "baja_pendiente_siniestro")
    private Integer bajaPendienteSiniestro;

    @Column(name = "tiene_nota_credito")
    private Integer tieneNotaCredito;

    @Column(name = "inciso_pagado")
    private Integer incisoPagado;

    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Column(name = "fecha_modificacion", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", insertable = false)
    private String modificadoPor;

    @Column(name = "fecha_borrado", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por", insertable = false)
    private String borradoPor;


    @Transient
    private String vehiculoNoSerie;

    @Transient
    private String polizaNoPoliza;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inciso inciso1 = (Inciso) o;
        return Objects.equals(poliza, inciso1.poliza) && Objects.equals(inciso, inciso1.inciso) && Objects.equals(vehiculo, inciso1.vehiculo) && estatusRegistro == inciso1.estatusRegistro;
    }

    @Override
    public int hashCode() {
        return Objects.hash(poliza, inciso, vehiculo, estatusRegistro);
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
