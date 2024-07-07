package gob.yucatan.sicasy.business.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
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
@Getter
@Setter
@Builder
public class BitacoraDeducible implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacora_deducible_id")
    private Long idBitacoraDeducible;

    @ManyToOne
    @JoinColumn(name = "deducible_id", nullable = false)
    private Deducible deducible;

    @Column(name = "accion", nullable = false)
    private String accion;

    @Column(name = "cambios", nullable = false)
    private String cambios;

    @Column(name = "fecha_modificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por", nullable = false)
    private String modificadoPor;

    public List<BitacoraCambios> getBitacoraCambios() {
        List<BitacoraCambios> cambiosList = new ArrayList<>();
        if(cambios != null) {
            TypeReference<List<BitacoraCambios>> typeRef = new TypeReference<>() {};
            cambiosList = JsonStringConverter.convertToList(cambios, typeRef);
        }
        return cambiosList;
    }

}
