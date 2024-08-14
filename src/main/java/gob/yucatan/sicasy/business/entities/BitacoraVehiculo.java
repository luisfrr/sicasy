package gob.yucatan.sicasy.business.entities;


import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "bitacora_vehiculo")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BitacoraVehiculo {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacora_vehiculo_id")
    private Long idBitacoraVehiculo;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "accion", nullable = false)
    private String accion;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bitacoraVehiculo")
    private Set<BitacoraVehiculoCambio> cambioSet;

    @Getter
    @Setter
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

}
