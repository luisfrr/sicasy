package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "mantenimiento_foto")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MantenimientoFoto {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mantenimiento_foto_id")
    private Long idMantenimientoFoto;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "mantenimiento_id")
    private Mantenimiento mantenimiento;

    @Setter
    @Getter
    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Setter
    @Getter
    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    @Setter
    @Getter
    @Column(name = "borrado", nullable = false)
    private Integer borrado;

    @Setter
    @Getter
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_borrado", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por", insertable = false)
    private String borradoPor;

    //region Getters & Setters

    public String getCreadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(creadoPor);
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = HtmlEntityConverter.convertHtmlEntitiesToSymbols(creadoPor);
    }

    public String getBorradoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(borradoPor);
    }

    public void setBorradoPor(String borradoPor) {
        this.borradoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(borradoPor);
    }

    //endregion Getters & Setters

}
