package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.utils.date.DateFormatUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
public class Anexo implements Cloneable {

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

    @Transient
    private String numLicitacionString;

    public String fechaInicioString(){
        return DateFormatUtil.convertToFormat(fechaInicio,"dd-MM-yyyy");
    }

    public String fechaFinalString(){
        return DateFormatUtil.convertToFormat(fechaFinal,"dd-MM-yyyy");
    }

    public String fechaFirmaString(){
        return DateFormatUtil.convertToFormat(fechaFirma, "dd-MM-yyyy");
    }

    public Boolean getIsExpirationDateFechaFinal(){

        if (fechaFinal != null){
            Instant instant = fechaFinal.toInstant();

            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate today = LocalDate.now(ZoneId.systemDefault());

            // return true si la fecha esta despues de la fecha de hoy menos 30 dias
            return date.isAfter(today.minusDays(30));
        }

        return false;
    }


    @Override
    public Anexo clone() {
        try {
            Anexo clone = (Anexo) super.clone();
            clone.licitacion = this.licitacion.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
