package gob.yucatan.sicasy.business.dtos;

import gob.yucatan.sicasy.business.entities.Inciso;
import jakarta.annotation.Nonnull;
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

    private List<Inciso> incisosPorPagar;
    private List<Inciso> incisosSaldosPendientes;
    private Double importe;
    private Double saldo;
    private Double total;

}
