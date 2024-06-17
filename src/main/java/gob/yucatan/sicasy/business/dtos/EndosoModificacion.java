package gob.yucatan.sicasy.business.dtos;

import gob.yucatan.sicasy.business.entities.Inciso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EndosoModificacion {
    private Inciso inciso;
    private double costoMovimiento;
    private int tipoMovimiento;
    private String motivo;

    private String folioFactura;
    private String nombreArchivoFactura;
    private String rutaArchivoFactura;

}
