package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "deducible")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Deducible implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deducible_id", nullable = false)
    private Long idDeducible;

    @Column(name = "tipo_deducible", nullable = false)
    private String tipoDeducible;

    @Column(name = "vehiculo_renta_mensual", nullable = false)
    private Double vehiculoRentaMensual;

    @Column(name = "vehiculo_valor_factura", nullable = false)
    private Double vehiculoValorFactura;

    @Column(name = "vehiculo_valor_actual", nullable = false)
    private Double vehiculoValorActual;

    @Column(name = "no_poliza")
    private String noPoliza;

    @Column(name = "no_inciso")
    private String noInciso;

    @Column(name = "poliza_total_asegurado")
    private Double polizaTotalAsegurado;

    @Column(name = "tiene_poliza_vigente", nullable = false)
    private Integer tienePolizaVigente;

    @Column(name = "costo_total_deducible", nullable = false)
    private Double costoTotalDeducible;

    @Column(name = "folio_factura_deducible", nullable = false)
    private String folioFacturaDeducible;

    @Column(name = "nombre_archivo_factura", nullable = false)
    private String nombreArchivoFactura;

    @Column(name = "ruta_archivo_factura", nullable = false)
    private String rutaArchivoFactura;

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

    @Override
    public Deducible clone() {
        try {
            return (Deducible) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
