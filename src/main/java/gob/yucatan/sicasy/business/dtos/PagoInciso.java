package gob.yucatan.sicasy.business.dtos;

import gob.yucatan.sicasy.business.entities.Inciso;
import gob.yucatan.sicasy.business.entities.Poliza;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagoInciso implements Serializable {
    private Poliza poliza;
    private String folioFactura;
    private List<Inciso> incisosPorPagar;
    private List<Inciso> incisosSaldosPendientes;
    private Double subtotal;
    private Double saldoPendiente;
    private Double total;
    private boolean usarSaldoPendiente;
}
