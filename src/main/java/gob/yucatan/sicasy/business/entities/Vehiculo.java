package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "vehiculo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Vehiculo implements Cloneable, Serializable {

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

    @Column(name = "tipo_vehiculo")
    private String tipoVehiculo;

    @Column(name = "no_motor", nullable = false)
    private String noMotor;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "descripcion_vehiculo")
    private String descripcionVehiculo;

    @Column(name = "monto_factura", nullable = false)
    private Double montoFactura;

    @Column(name = "no_factura", nullable = false)
    private String noFactura;

    @Column(name = "renta_mensual", nullable = false)
    private Double rentaMensual;

    @Column(name = "proveedor")
    private String proveedor;

    @ManyToOne
    @JoinColumn(name = "licitacion_id")
    private Licitacion licitacion;

    @ManyToOne
    @JoinColumn(name = "anexo_id")
    private Anexo anexo;

    @ManyToOne
    @JoinColumn(name = "dependencia_id", nullable = false)
    private Dependencia dependencia;

    @ManyToOne
    @JoinColumn(name = "asignada_dependencia_id")
    private Dependencia dependenciaAsignada;

    @ManyToOne
    @JoinColumn(name = "estatus_vehiculo_id", nullable = false)
    private EstatusVehiculo estatusVehiculo;

    @ManyToOne
    @JoinColumn(name = "condicion_vehiculo_id")
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
    public Vehiculo clone() {
        try {
            Vehiculo clone = (Vehiculo) super.clone();
            clone.licitacion = this.licitacion.clone();
            clone.anexo = this.anexo.clone();
            clone.dependencia = this.dependencia.clone();
            clone.dependenciaAsignada = this.dependenciaAsignada.clone();
            clone.estatusVehiculo = this.estatusVehiculo.clone();
            clone.condicionVehiculo = this.condicionVehiculo.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
