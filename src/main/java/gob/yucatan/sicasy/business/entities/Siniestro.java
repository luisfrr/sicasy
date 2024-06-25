package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "siniestro")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Siniestro implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "siniestro_id", nullable = false)
    private Long idSiniestro;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "poliza_id")
    private Poliza poliza;

    @Column(name = "no_siniestro_saf", nullable = false)
    private String noSiniestroSAF;

    @Column(name = "no_siniestro_aseguradora", nullable = false)
    private String noSiniestroAseguradora;

    @Column(name = "ajustador_saf", nullable = false)
    private String ajustadorSAF;

    @Column(name = "ajustador_aseguradora", nullable = false)
    private String ajustadorAseguradora;

    @Column(name = "estado")
    private String estado;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "localidad")
    private String localidad;

    @Column(name = "estatus", nullable = false)
    private String estatus;

    @Column(name = "causa", nullable = false)
    private String causa;

    @Column(name = "responsable", nullable = false)
    private String responsable;

    @Column(name = "reporta")
    private String reporta;

    @Column(name = "conductor", nullable = false)
    private String conductor;

    @Column(name = "fecha_siniestro", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSiniestro;

    @Column(name = "fecha_reporte", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaReporte;

    @Column(name = "declaracion_siniestro", nullable = false)
    private String declaracionSiniestro;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "no_licencia", nullable = false)
    private String noLicencia;

    @Column(name = "tipo_licencia")
    private String tipoLicencia;

    @Column(name = "vencimiento_licencia")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vencimientoLicencia;

    @Column(name = "danio_via_publica", nullable = false)
    private Integer danioViaPublica;

    @Column(name = "corralon", nullable = false)
    private Integer corralon;

    @Column(name = "corralon", nullable = false)
    private Integer multaVehiculo;

    @Column(name = "costo_multa")
    private Double costoMulta;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="deducible_id")
    private Deducible deducible;

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
    public Siniestro clone() {
        try {
            Siniestro clone = (Siniestro) super.clone();
            clone.vehiculo = this.vehiculo.clone();
            clone.poliza = this.poliza.clone();
            clone.deducible = this.deducible.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
