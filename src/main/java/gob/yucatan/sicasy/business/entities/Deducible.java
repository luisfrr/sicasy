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
    private String tipoDeducicle;

    @Column(name = "vehiculo_no_serie", nullable = false)
    private String vehiculoNoSerie;

    @Column(name = "vehiculo_placa", nullable = false)
    private String vehiculoPlaca;

    @Column(name = "vehiculo_marca", nullable = false)
    private String vehiculoMarca;

    @Column(name = "vehiculo_modelo", nullable = false)
    private String vehiculoModelo;

    @Column(name = "vehiculo_anio", nullable = false)
    private Integer vehiculoAnio;

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

    @Column(name = "inciso_fecha_inicio_vigencia", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date incisoFechaInicioVigencia;

    @Column(name = "inciso_fecha_fin_vigencia", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date incisoFechaFinVigencia;

    @Column(name = "tiene_poliza_vigente", nullable = false)
    private Integer tienePolizaVigente;

    @Column(name = "valor_actual", nullable = false)
    private Double valorActual;

    @Column(name = "costo_total_deducible", nullable = false)
    private Double costoTotalDeducible;

    @Column(name = "folio_factura_deducible", nullable = false)
    private String folioFacturaDeducible;

    @Column(name = "nombre_archivo_factura", nullable = false)
    private String nombreArchivoFactura;

    @Column(name = "ruta_archivo_factura", nullable = false)
    private String rutaArchivoFactura;

    @OneToOne(optional=false, mappedBy="deducible")
    public Siniestro siniestro;

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
