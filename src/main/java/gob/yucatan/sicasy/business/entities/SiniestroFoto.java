package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "siniestro_foto")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SiniestroFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "siniestro_foto_id")
    private Long idSiniestroFoto;

    @ManyToOne
    @JoinColumn(name = "siniestro_id")
    private Siniestro siniestro;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    @Column(name = "borrado", nullable = false)
    private Integer borrado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Column(name = "fecha_borrado", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por", insertable = false)
    private String borradoPor;

}
