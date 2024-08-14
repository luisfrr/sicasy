package gob.yucatan.sicasy.business.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import gob.yucatan.sicasy.utils.strings.JsonStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bitacora_usuario", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BitacoraUsuario {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacora_usuario_id")
    private Long idBitacoraUsuario;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

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
