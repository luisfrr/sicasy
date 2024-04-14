package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "poliza")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poliza")
    private Integer idAseguradora;

    @ManyToOne
    @JoinColumn(name = "aseguradora_id", nullable = false)
    private Aseguradora aseguradora;

    @Column(name = "numero_poliza", nullable = false)
    private String numeroPoliza;

    @Column(name = "inciso", nullable = false)
    private String inciso;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private Date fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private Date fechaFinVigencia;

    @Column(name = "beneficiario", nullable = false)
    private String beneficiario;

    @Column(name = "monto_asegurado", nullable = false)
    private Double montoAsegurado;

    @Column(name = "tipo_cobertura")
    private String tipoCobertura;

    @Column(name = "estatus", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatus;

    @Column(name = "costo_poliza")
    private Double costoPoliza;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "baja_pendiente")
    private Integer bajaPendiente;

    @Column(name = "ruta_archivo_poliza")
    private String rutaArchivoPoliza;

}
