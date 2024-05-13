package gob.yucatan.sicasy.business.dtos;

import lombok.*;

import java.util.Date;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GrupoPoliza {

    private String numeroPoliza;
    private Integer idAseguradora;
    private String aseguradora;
    private Date fechaInicio;
    private Date fechaFin;
    private Double totalCostoPoliza;
    private Double totalSaldoPoliza;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrupoPoliza that = (GrupoPoliza) o;
        return Objects.equals(numeroPoliza, that.numeroPoliza) && Objects.equals(aseguradora, that.aseguradora);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroPoliza, aseguradora);
    }
}
