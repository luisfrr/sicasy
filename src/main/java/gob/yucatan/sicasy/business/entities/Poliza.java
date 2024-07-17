package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.*;


@Entity
@Table(name = "poliza")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Slf4j
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

    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date fechaInicioVigencia;

    @Column(name = "fecha_fin")
    @Temporal(TemporalType.DATE)
    private Date fechaFinVigencia;

    @Column(name = "beneficiario_preferente")
    private String beneficiarioPreferente;

    @Column(name = "tipo_cobertura")
    private String tipoCobertura;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @OneToMany(mappedBy = "poliza")
    private Set<Inciso> incisoSet;

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
    private Double costoTotal;

    @Transient
    private Double saldoTotal;

    @Transient
    private Integer totalIncisos;

    @Transient
    private Integer idAseguradora;

    @Transient
    private List<Integer> idAseguradoraList;

    @Transient
    private List<String> numeroPolizaList;

    @Transient
    private boolean fethIncisoSet;

    @Transient
    private String fechaInicioStr;

    @Transient
    private String fechaFinStr;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poliza poliza = (Poliza) o;
        return Objects.equals(aseguradora, poliza.aseguradora) && Objects.equals(numeroPoliza, poliza.numeroPoliza) && Objects.equals(estatusRegistro, poliza.estatusRegistro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aseguradora, numeroPoliza, estatusRegistro);
    }

    @Override
    public Poliza clone() {
        try {
            Poliza clone = (Poliza) super.clone();
            clone.aseguradora = this.aseguradora != null ? this.aseguradora.clone() : null;

            // Verificar si incisoSet está inicializada
            if (Hibernate.isInitialized(this.incisoSet)) {
                clone.incisoSet = new HashSet<>(this.incisoSet);
            } else {
                clone.incisoSet = null; // O manejar de otra manera si es necesario
            }


            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


}
