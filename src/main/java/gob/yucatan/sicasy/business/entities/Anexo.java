package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "anexo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Slf4j
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anexo_id")
    private Long idAnexo;

    @ManyToOne
    @JoinColumn(name = "licitacion_id", nullable = false)
    private Licitacion licitacion;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_final")
    private Date fechaFinal;

    @Column(name = "fecha_firma")
    private Date fechaFirma;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Column(name = "estatus_registro", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatusRegistro;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "creado_por")
    private String creadoPor;

    @Column(name = "fecha_modificacion")
    private Date fechaModificacion;

    @Column(name = "modificado_por")
    private String modificadoPor;

    @Column(name = "fecha_borrado")
    private Date fechaBorrado;

    @Column(name = "borrado_por")
    private String borradoPor;


    public String fechaInicioString(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(fechaInicio);
    }

    public String fechaFinalString(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(fechaFinal);
    }

    public String fechaFirmaString(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(fechaFirma);
    }

    public String numLicitacionString(){
        return licitacion.getNumeroLicitacion();
    }

    public Boolean getIsExpirationDateFechaFinal(){
        Instant instant = fechaFinal.toInstant();

        // Replace JVM's timezone, ZoneId.systemDefault() with the applicable timezone
        // e.g. ZoneId.of("Etc/UTC")
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        // return true si la fecha esta despues de la fecha de hoy menos 30 dias
        if (date.isAfter(today.minusDays(30))) {
            System.out.println("fecha final proxima rebasa los 10 dias de tolerancia");
            return true;
        }

        return false;
    }


}
