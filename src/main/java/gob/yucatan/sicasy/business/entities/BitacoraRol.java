package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import gob.yucatan.sicasy.utils.strings.JsonStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bitacora_rol", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BitacoraRol {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacora_rol_id")
    private Long idBitacoraRol;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "accion", nullable = false)
    private String accion;

    @Setter
    @Getter
    @Column(name = "cambios", nullable = false)
    private String cambios;

    @Setter
    @Getter
    @Column(name = "fecha_modificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", nullable = false)
    private String modificadoPor;

    //region Getters & Setters

    public String getAccion() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(accion);
    }

    public void setAccion(String accion) {
        this.accion = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(accion);
    }

    public String getModificadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(modificadoPor);
    }

    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(modificadoPor);
    }

    //endregion Getters & Setters

    public List<BitacoraCambios> getBitacoraCambios() {
        return JsonStringConverter.getBitacoraCambios(cambios);
    }

}
