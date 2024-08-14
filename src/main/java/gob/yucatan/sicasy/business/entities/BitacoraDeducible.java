package gob.yucatan.sicasy.business.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import gob.yucatan.sicasy.utils.strings.JsonStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bitacora_deducible")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BitacoraDeducible implements Serializable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacora_deducible_id")
    private Long idBitacoraDeducible;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "deducible_id", nullable = false)
    private Deducible deducible;

    @Column(name = "accion", nullable = false)
    private String accion;

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
