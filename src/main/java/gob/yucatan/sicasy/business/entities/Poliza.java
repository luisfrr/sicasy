package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "poliza")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Poliza implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poliza_id")
    private Long idPoliza;

    @ManyToOne
    @JoinColumn(name = "aseguradora_id", nullable = false)
    private Aseguradora aseguradora;

    @Column(name = "numero_poliza", nullable = false)
    private String numeroPoliza;

    @Column(name = "inciso")
    private String inciso;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Column(name = "fecha_inicio_vigencia")
    private Date fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private Date fechaFinVigencia;

    @Column(name = "beneficiario_preferente")
    private String beneficiarioPreferente;

    @Column(name = "tipo_cobertura", nullable = false)
    private String tipoCobertura;

    @Column(name = "costo_poliza")
    private Double costoPoliza;

    @Column(name = "saldo_poliza")
    private Double saldoPoliza;

    @Column(name = "tipo_pago")
    private String tipoPago;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "baja_pendiente")
    private Integer bajaPendiente;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @ManyToOne
    @JoinColumn(name = "estatus_poliza_id")
    private EstatusPoliza estatusPoliza;

    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por")
    private String modificadoPor;

    @Column(name = "fecha_borrado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por")
    private String borradoPor;

    @Override
    public Poliza clone() {
        try {
            Poliza clone = (Poliza) super.clone();
            clone.aseguradora = this.aseguradora != null ? this.aseguradora.clone() : null;
            clone.vehiculo = this.vehiculo != null ? this.vehiculo.clone() : null;
            clone.estatusPoliza = this.estatusPoliza != null ? this.estatusPoliza.clone() : null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
