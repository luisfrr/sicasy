package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
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
@Builder
@Slf4j
public class Poliza implements Cloneable, Serializable {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poliza_id")
    private Long idPoliza;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "aseguradora_id", nullable = false)
    private Aseguradora aseguradora;

    @Column(name = "numero_poliza", nullable = false)
    private String numeroPoliza;

    @Setter
    @Getter
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date fechaInicioVigencia;

    @Setter
    @Getter
    @Column(name = "fecha_fin")
    @Temporal(TemporalType.DATE)
    private Date fechaFinVigencia;

    @Column(name = "beneficiario_preferente")
    private String beneficiarioPreferente;

    @Column(name = "tipo_cobertura")
    private String tipoCobertura;

    @Setter
    @Getter
    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Setter
    @Getter
    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Setter
    @Getter
    @OneToMany(mappedBy = "poliza")
    private Set<Inciso> incisoSet;

    @Getter
    @Setter
    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Getter
    @Setter
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Getter
    @Setter
    @Column(name = "fecha_modificacion", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", insertable = false)
    private String modificadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_borrado", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por", insertable = false)
    private String borradoPor;

    //region Transients

    @Getter
    @Setter
    @Transient
    private Double costoTotal;

    @Getter
    @Setter
    @Transient
    private Double saldoTotal;

    @Getter
    @Setter
    @Transient
    private Integer totalIncisos;

    @Getter
    @Setter
    @Transient
    private Integer idAseguradora;

    @Getter
    @Setter
    @Transient
    private List<Integer> idAseguradoraList;

    @Getter
    @Setter
    @Transient
    private List<String> numeroPolizaList;

    @Getter
    @Setter
    @Transient
    private boolean fethIncisoSet;

    @Getter
    @Setter
    @Transient
    private String fechaInicioStr;

    @Getter
    @Setter
    @Transient
    private String fechaFinStr;

    //endregion Transients

    //region Getters & Setters

    public String getNumeroPoliza() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(numeroPoliza);
    }

    public void setNumeroPoliza(String numeroPoliza) {
        this.numeroPoliza = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(numeroPoliza);
    }

    public String getBeneficiarioPreferente() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(beneficiarioPreferente);
    }

    public void setBeneficiarioPreferente(String beneficiarioPreferente) {
        this.beneficiarioPreferente = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(beneficiarioPreferente);
    }

    public String getTipoCobertura() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(tipoCobertura);
    }

    public void setTipoCobertura(String tipoCobertura) {
        this.tipoCobertura = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(tipoCobertura);
    }

    public String getCreadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(creadoPor);
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(creadoPor);
    }

    public String getModificadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(modificadoPor);
    }

    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(modificadoPor);
    }

    public String getBorradoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(borradoPor);
    }

    public void setBorradoPor(String borradoPor) {
        this.borradoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(borradoPor);
    }

    //endregion Getters & Setters

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
